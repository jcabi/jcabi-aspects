/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Parallel;
import com.jcabi.log.VerboseThreads;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Execute method in multiple threads.
 *
 * @see Parallel
 * @since 0.10
 * @checkstyle NonStaticMethodCheck (100 lines)
 */
@Aspect
@Immutable
public final class Parallelizer {

    /**
     * Execute method in multiple threads.
     *
     * <p>This aspect should be used only on void returning methods.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Parallelizer.ParallelException If something goes wrong inside
     * @checkstyle IllegalThrowsCheck (4 lines)
     */
    @Around("execution(@com.jcabi.aspects.Parallel * * (..))")
    public Object wrap(final ProceedingJoinPoint point)
        throws Parallelizer.ParallelException {
        final int total = ((MethodSignature) point.getSignature())
            .getMethod().getAnnotation(Parallel.class).threads();
        final Collection<Callable<Throwable>> callables =
            new ArrayList<>(total);
        final CountDownLatch start = new CountDownLatch(1);
        for (int thread = 0; thread < total; ++thread) {
            callables.add(Parallelizer.callable(point, start));
        }
        final ExecutorService executor = Executors
            .newFixedThreadPool(total, new VerboseThreads());
        final Collection<Future<Throwable>> futures =
            new ArrayList<>(total);
        for (final Callable<Throwable> callable : callables) {
            futures.add(executor.submit(callable));
        }
        start.countDown();
        final Collection<Throwable> failures = new LinkedList<>();
        for (final Future<Throwable> future : futures) {
            Parallelizer.process(failures, future);
        }
        executor.shutdown();
        if (!failures.isEmpty()) {
            throw Parallelizer.exceptions(failures);
        }
        return null;
    }

    /**
     * Process futures.
     * @param failures Collection of failures.
     * @param future Future tu process.
     */
    private static void process(final Collection<Throwable> failures,
        final Future<Throwable> future) {
        final Throwable exception;
        try {
            exception = future.get();
            if (exception != null) {
                failures.add(exception);
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            failures.add(ex);
        } catch (final ExecutionException ex) {
            failures.add(ex);
        }
    }

    /**
     * Create parallel exception.
     * @param failures List of exceptions from threads.
     * @return Aggregated exceptions.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Parallelizer.ParallelException exceptions(
        final Iterable<Throwable> failures) {
        Parallelizer.ParallelException current = null;
        for (final Throwable failure : failures) {
            current = new Parallelizer.ParallelException(failure, current);
        }
        return current;
    }

    /**
     * Create callable that executes join point.
     * @param point Join point to use.
     * @param start Latch to use.
     * @return Created callable.
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private static Callable<Throwable> callable(final ProceedingJoinPoint point,
        final CountDownLatch start) {
        return () -> {
            Throwable result = null;
            try {
                start.await();
                point.proceed();
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Throwable ex) {
                result = ex;
            }
            return result;
        };
    }

    /**
     * Exception that encapsulates all exceptions thrown from threads.
     * @since 0.0.0
     */
    private static final class ParallelException extends Exception {

        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x8743ef363febc422L;

        /**
         * Next parallel exception.
         */
        private final transient Parallelizer.ParallelException next;

        /**
         * Constructor.
         * @param cause Cause of the current exception.
         * @param nxt Following exception.
         */
        protected ParallelException(final Throwable cause,
            final Parallelizer.ParallelException nxt) {
            super(cause);
            this.next = nxt;
        }

        /**
         * Get next parallel exception.
         * @return Next exception.
         */
        public Parallelizer.ParallelException getNext() {
            return this.next;
        }
    }

}
