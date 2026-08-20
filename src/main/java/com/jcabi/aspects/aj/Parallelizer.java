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
import java.util.Iterator;
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
     * @throws ParallelException If something goes wrong inside
     */
    @Around("execution(@com.jcabi.aspects.Parallel * * (..))")
    public Object wrap(final ProceedingJoinPoint point)
        throws ParallelException {
        final int total = ((MethodSignature) point.getSignature())
            .getMethod().getAnnotation(Parallel.class).threads();
        final Collection<Callable<Throwable>> callables =
            new ArrayList<>(total);
        final CountDownLatch start = new CountDownLatch(1);
        for (int thread = 0; thread < total; ++thread) {
            callables.add(Parallelizer.callable(point, start));
        }
        final Collection<Throwable> failures = new ArrayList<>(0);
        try (
            ExecutorService executor = Executors.newFixedThreadPool(
                total, new VerboseThreads()
            )
        ) {
            final Collection<Future<Throwable>> futures =
                new ArrayList<>(total);
            for (final Callable<Throwable> callable : callables) {
                futures.add(executor.submit(callable));
            }
            start.countDown();
            for (final Future<Throwable> future : futures) {
                Parallelizer.process(failures, future);
            }
            executor.shutdown();
        }
        if (!failures.isEmpty()) {
            throw Parallelizer.exceptions(failures);
        }
        return null;
    }

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

    private static ParallelException exceptions(
        final Collection<Throwable> failures) {
        final Iterator<Throwable> iter = failures.iterator();
        final ParallelException exception =
            new ParallelException(iter.next());
        while (iter.hasNext()) {
            exception.addSuppressed(iter.next());
        }
        return exception;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
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
}
