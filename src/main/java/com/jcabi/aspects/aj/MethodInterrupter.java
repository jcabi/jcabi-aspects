/*
 * Copyright (c) 2012-2017, jcabi.com
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

import com.jcabi.aspects.Timeable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Interrupts long-running methods.
 *
 * <p>It is an AspectJ aspect and you are not supposed to use it directly. It
 * is instantiated by AspectJ runtime framework when your code is annotated
 * with {@link Timeable} annotation.
 *
 * <p>The class is thread-safe.
 *
 * @since 0.7.16
 */
@Aspect
@SuppressWarnings("PMD.DoNotUseThreads")
public final class MethodInterrupter {

    /**
     * Calls being watched.
     */
    private final transient Set<MethodInterrupter.Call> calls;

    /**
     * Service that interrupts threads.
     */
    private final transient ScheduledExecutorService interrupter;

    /**
     * Public ctor.
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public MethodInterrupter() {
        this.calls = new ConcurrentSkipListSet<>();
        this.interrupter = Executors.newSingleThreadScheduledExecutor(
            new NamedThreads(
                "timeable",
                "interrupting of @Timeable annotated methods"
            )
        );
        this.interrupter.scheduleWithFixedDelay(
            new VerboseRunnable(
                MethodInterrupter.this::interrupt
            ),
            1, 1, TimeUnit.SECONDS
        );
    }

    /**
     * Run and interrupt a method, if stuck.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     */
    @Around("execution(* * (..)) && @annotation(com.jcabi.aspects.Timeable)")
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final MethodInterrupter.Call call = new MethodInterrupter.Call(point);
        this.calls.add(call);
        final Object output;
        try {
            output = point.proceed();
        } finally {
            this.calls.remove(call);
        }
        return output;
    }

    /**
     * Interrupt threads when needed.
     */
    private void interrupt() {
        synchronized (this.interrupter) {
            this.calls.removeIf(call -> call.expired() && call.interrupted());
        }
    }

    /**
     * A call being watched.
     *
     * @since 0.7.16
     */
    private static final class Call implements
        Comparable<MethodInterrupter.Call> {
        /**
         * The thread called.
         */
        private final transient Thread thread;

        /**
         * When started.
         */
        private final transient long start;

        /**
         * When will expire.
         */
        private final transient long deadline;

        /**
         * Join point.
         */
        private final transient ProceedingJoinPoint point;

        /**
         * Public ctor.
         * @param pnt Joint point
         */
        @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
        Call(final ProceedingJoinPoint pnt) {
            this.thread = Thread.currentThread();
            this.start = System.currentTimeMillis();
            this.point = pnt;
            final Method method = MethodSignature.class
                .cast(pnt.getSignature())
                .getMethod();
            final Timeable annt = method.getAnnotation(Timeable.class);
            this.deadline = this.start + annt.unit().toMillis(annt.limit());
        }

        @Override
        public int compareTo(final Call obj) {
            return Long.compare(this.deadline, obj.deadline);
        }

        /**
         * Is it expired already?
         * @return TRUE if expired
         */
        public boolean expired() {
            return this.deadline < System.currentTimeMillis();
        }

        /**
         * This thread is stopped already (interrupt if not)?
         * @return TRUE if it's already dead
         */
        public boolean interrupted() {
            final boolean dead;
            if (this.thread.isAlive()) {
                this.thread.interrupt();
                final Method method = MethodSignature.class
                    .cast(this.point.getSignature())
                    .getMethod();
                if (Logger.isWarnEnabled(this)) {
                    Logger.warn(
                        method.getDeclaringClass(),
                        "%s: interrupted on %[ms]s timeout (over %[ms]s)",
                        Mnemos.toText(this.point, true, false),
                        System.currentTimeMillis() - this.start,
                        this.deadline - this.start
                    );
                }
                dead = false;
            } else {
                dead = true;
            }
            return dead;
        }
    }

}
