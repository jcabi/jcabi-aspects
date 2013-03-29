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
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    private final transient ConcurrentMap<Object, ScheduledExecutorService> services =
        new ConcurrentHashMap<Object, ScheduledExecutorService>();

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
        final ScheduleWithFixedDelay annt = object.getClass()
            .getAnnotation(ScheduleWithFixedDelay.class);
        final ScheduledExecutorService service =
            Executors.newSingleThreadScheduledExecutor(new VerboseThreads());
        service.scheduleWithFixedDelay(
            runnable,
            annt.delay(),
            annt.delay(),
            annt.unit()
        );
        this.services.put(object, service);
        Logger.info(
            object,
            "#run() method scheduled for execution every %d %s",
            annt.delay(),
            annt.unit()
        );
    }

    /**
     * Stop/close a routine task.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     */
    @After("execution(* (@com.jcabi.aspects.ScheduleWithFixedDelay *).close())")
    public void close(final JoinPoint point) {
        final Object object = point.getTarget();
        final ScheduledExecutorService service = this.services.get(object);
        service.shutdownNow();
        this.services.remove(object);
        Logger.info(object, "scheduled execution terminated");
    }

}
