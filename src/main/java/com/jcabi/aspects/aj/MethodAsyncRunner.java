/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Execute method asynchronously.
 *
 * <p>It is an AspectJ aspect and you are not supposed to use it directly. It
 * is instantiated by AspectJ runtime framework when your code is annotated
 * with {@link com.jcabi.aspects.Async} annotation.
 *
 * @since 0.16
 */
@Aspect
public final class MethodAsyncRunner {

    /**
     * Thread pool for asynchronous execution.
     */
    private final transient ExecutorService executor =
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new NamedThreads(
                "async",
                "Asynchronous method execution"
            )
        );

    /**
     * Execute method asynchronously.
     *
     * <p>This aspect should be used only on {@code void} or
     * {@link Future} returning methods.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     */
    @Around("execution(@com.jcabi.aspects.Async * * (..))")
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrap(final ProceedingJoinPoint point) {
        final Class<?> returned = ((MethodSignature) point.getSignature()).getMethod()
            .getReturnType();
        if (!Future.class.isAssignableFrom(returned)
            && !returned.equals(Void.TYPE)) {
            // @checkstyle LineLength (3 lines)
            throw new IllegalStateException(
                String.format(
                    "%s: Return type is %s, not void or Future, cannot use @Async",
                    Mnemos.toText(point, true, true),
                    returned.getCanonicalName()
                )
            );
        }
        final Future<?> result = this.executor.submit(
            // @checkstyle AnonInnerLength (23 lines)
            () -> {
                Object ret = null;
                try {
                    final Object res = point.proceed();
                    if (res instanceof Future) {
                        ret = ((Future<?>) res).get();
                    }
                // @checkstyle IllegalCatch (1 line)
                } catch (final Throwable ex) {
                    throw new IllegalStateException(
                        String.format(
                            "%s: Exception thrown",
                            Mnemos.toText(point, true, true)
                        ),
                        ex
                    );
                }
                return ret;
            }
        );
        Object res = null;
        if (Future.class.isAssignableFrom(returned)) {
            res = result;
        }
        return res;
    }

}
