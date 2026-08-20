/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Timeable;
import com.jcabi.log.VerboseRunnable;
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
    private final transient Set<Call> calls;

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
        final Call call = new Call(point);
        this.calls.add(call);
        final Object output;
        try {
            output = point.proceed();
        } finally {
            this.calls.remove(call);
        }
        return output;
    }

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
}
