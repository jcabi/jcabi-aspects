/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Logs all exceptions thrown out of a method.
 *
 * @since 0.1.10
 * @see com.jcabi.aspects.LogExceptions
 * @checkstyle IllegalThrows (100 lines)
 * @checkstyle NonStaticMethodCheck (100 lines)
 */
@Aspect
@Immutable
public final class ExceptionsLogger {

    /**
     * Catch exception and log it.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     */
    @Around
        (
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "execution(* * (..))"
            + " && @annotation(com.jcabi.aspects.LogExceptions)"
        )
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        try {
            return point.proceed();
        // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Logger.warn(
                new ImprovedJoinPoint(point).targetize(),
                "%[exception]s",
                ex
            );
            throw ex;
        }
    }
}
