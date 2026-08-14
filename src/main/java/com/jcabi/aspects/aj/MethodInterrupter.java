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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
     * Guard of the calls.
     */
    private final transient Lock lock;

    /**
     * Public ctor.
     */
    @SuppressWarnings(
        {
            "PMD.ConstructorOnlyInitializesOrCallOtherConstructors",
            "FutureReturnValueIgnored"
        }
    )
    public MethodInterrupter() {
        // @checkstyle ConstructorsCodeFreeCheck (15 lines)
        this.calls = new ConcurrentSkipListSet<>();
        this.lock = new ReentrantLock();
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
        this.lock.lock();
        try {
            this.calls.removeIf(
                call -> call.expired() && call.interrupted()
            );
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * A call being watched.
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
        Call(final ProceedingJoinPoint pnt) {
            this(
                pnt,
                Thread.currentThread(),
                System.currentTimeMillis(),
                MethodInterrupter.Call.limit(pnt)
            );
        }

        /**
         * Ctor.
         * @param pnt Joint point
         * @param thrd The thread that called
         * @param begin When it started
         * @param span How long the call may take, in milliseconds
         */
        private Call(final ProceedingJoinPoint pnt, final Thread thrd,
            final long begin, final long span) {
            this.point = pnt;
            this.thread = thrd;
            this.start = begin;
            this.deadline = begin + span;
        }

        @Override
        public int hashCode() {
            return this.point.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || ((MethodInterrupter.Call) obj)
                .point.equals(this.point);
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
        boolean expired() {
            return this.deadline < System.currentTimeMillis();
        }

        /**
         * This thread is stopped already (interrupt if not)?
         * @return TRUE if it's already dead
         */
        boolean interrupted() {
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

        /**
         * How long the call is allowed to take.
         * @param pnt Joint point
         * @return Milliseconds
         */
        private static long limit(final ProceedingJoinPoint pnt) {
            final Timeable annt = ((MethodSignature) pnt.getSignature())
                .getMethod().getAnnotation(Timeable.class);
            return annt.unit().toMillis((long) annt.limit());
        }
    }
}
