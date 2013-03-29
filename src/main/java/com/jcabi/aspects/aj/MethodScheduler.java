/**
 * Copyright (c) 2012-2013, JCabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * Schedules methods.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.16
 */
@Aspect
@SuppressWarnings("PMD.DoNotUseThreads")
public final class MethodScheduler {

    /**
     * Objects and their running services.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<Object, MethodScheduler.Service> services =
        new ConcurrentHashMap<Object, MethodScheduler.Service>();

    /**
     * Instantiate a new routine task.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @checkstyle LineLength (2 lines)
     */
    @After("initialization((@com.jcabi.aspects.ScheduleWithFixedDelay *).new(..))")
    public void instantiate(final JoinPoint point) {
        final Object object = point.getTarget();
        if (this.services.containsKey(object)) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s was already scheduled for execution",
                    object
                )
            );
        }
        Runnable runnable;
        if (object instanceof Runnable) {
            runnable = new VerboseRunnable(Runnable.class.cast(object), true);
        } else if (object instanceof Callable) {
            runnable = new VerboseRunnable(Callable.class.cast(object), true);
        } else {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s doesn't implement Runnable or Callable",
                    object
                )
            );
        }
        this.services.put(
            object,
            new MethodScheduler.Service(
                runnable,
                object,
                object.getClass().getAnnotation(ScheduleWithFixedDelay.class)
            )
        );
    }

    /**
     * Stop/close a routine task.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @throws IOException If can't close
     */
    @After("execution(* (@com.jcabi.aspects.ScheduleWithFixedDelay *).close())")
    public void close(final JoinPoint point) throws IOException {
        final Object object = point.getTarget();
        this.services.get(object).close();
        this.services.remove(object);
    }

    /**
     * Running service.
     */
    private static final class Service implements Closeable {
        /**
         * Running scheduled service.
         */
        private final transient ScheduledExecutorService executor;
        /**
         * The object.
         */
        private final transient Object object;
        /**
         * Execution counter.
         */
        private final transient AtomicLong counter = new AtomicLong();
        /**
         * When started.
         */
        private final transient long start = System.currentTimeMillis();
        /**
         * Public ctor.
         * @param runnable The runnable to schedule
         * @param obj Object
         * @param annt Annotation
         */
        public Service(final Runnable runnable, final Object obj,
            final ScheduleWithFixedDelay annt) {
            this.object = obj;
            this.executor = Executors.newSingleThreadScheduledExecutor(
                new VerboseThreads(this.object)
            );
            this.executor.scheduleWithFixedDelay(
                new Runnable() {
                    @Override
                    public void run() {
                        runnable.run();
                        MethodScheduler.Service.this.counter.incrementAndGet();
                    }
                },
                annt.delay(),
                annt.delay(),
                annt.unit()
            );
            Logger.info(
                this.object,
                "scheduled for execution every %d %s",
                annt.delay(),
                annt.unit()
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            this.executor.shutdownNow();
            Logger.info(
                this.object,
                "execution stopped after %[ms]s and %d ticks",
                System.currentTimeMillis() - this.start,
                this.counter.get()
            );
        }
    }

}
