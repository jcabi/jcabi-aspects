/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Logs all exceptions thrown out of a method and swallow exception.
 *
 * @see com.jcabi.aspects.LogExceptions
 * @since 0.1.10
 * @checkstyle IllegalThrows (100 lines)
 * @checkstyle NonStaticMethodCheck (100 lines)
 */
@Aspect
@Immutable
public final class QuietExceptionsLogger {

    /**
     * Catch exception and log it, the exception will be swallowed.
     *
     * <p>This aspect should be used only on void returning methods.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     */
    @Around
        (
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "execution(* * (..))"
            + " && @annotation(com.jcabi.aspects.Quietly)"
        )
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrap(final ProceedingJoinPoint point) {
        if (!((MethodSignature) point.getSignature()).getReturnType()
            .equals(Void.TYPE)) {
            throw new IllegalStateException(
                String.format(
                    "%s: Return type is not void, cannot use @Quietly",
                    Mnemos.toText(point, true, true)
                )
            );
        }
        Object result = null;
        try {
            result = point.proceed();
        // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Logger.warn(
                new ImprovedJoinPoint(point).targetize(),
                "%[exception]s",
                ex
            );
        }
        return result;
    }
}
