/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
                this::interrupt
            ),
            1L, 1L, TimeUnit.SECONDS
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
            this.calls.removeIf(
                call -> call.expired() && call.interrupted()
            );
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
            final Method method = ((MethodSignature) pnt.getSignature())
                .getMethod();
            final Timeable annt = method.getAnnotation(Timeable.class);
            this.deadline = this.start + annt.unit().toMillis(
                (long) annt.limit()
            );
        }

        @Override
        public int compareTo(final MethodInterrupter.Call obj) {
            final int compare;
            if (this.deadline > obj.deadline) {
                compare = 1;
            } else if (this.deadline < obj.deadline) {
                compare = -1;
            } else {
                compare = 0;
            }
            return compare;
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
                final Method method = ((MethodSignature) this.point.getSignature())
                    .getMethod();
                if (Logger.isWarnEnabled(method.getDeclaringClass())) {
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
