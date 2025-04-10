/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.UnitedThrow;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Throw single exception out of method.
 *
 * @since 0.13
 * @checkstyle NonStaticMethodCheck (100 lines)
 */
@Aspect
@Immutable
public final class SingleException {

    /**
     * Catch all exceptions and throw a single selected exception.
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
            + " && @annotation(com.jcabi.aspects.UnitedThrow)"
        )
    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "PMD.PreserveStackTrace"})
    // @checkstyle IllegalThrowsCheck (1 line)
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            ((MethodSignature) point.getSignature()).getMethod();
        final UnitedThrow annot = method.getAnnotation(UnitedThrow.class);
        final Class<? extends Throwable> clz = SingleException.clazz(
            method,
            annot
        );
        try {
            return point.proceed();
            // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Throwable throwable = ex;
            if (!clz.isAssignableFrom(ex.getClass())) {
                if (SingleException.exists(clz)) {
                    throwable = clz.getConstructor(Throwable.class)
                        .newInstance(ex);
                } else {
                    throwable = clz.getConstructor().newInstance();
                }
            }
            throw throwable;
        }
    }

    /**
     * Check if there is a constructor with single Throwable argument.
     * @param clz Class to check.
     * @return Whether constructor exists.
     */
    private static boolean exists(final Class<? extends Throwable> clz) {
        boolean found = false;
        for (final Constructor<?> ctr : clz.getConstructors()) {
            if (ctr.getParameterTypes().length == 1
                && Objects.equals(ctr.getParameterTypes()[0], Throwable.class)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Get required exception class.
     * @param method Method declaring exception.
     * @param annot UnitedThrow annotation.
     * @return Class of exception.
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Throwable> clazz(final Method method,
        final UnitedThrow annot) {
        Class<? extends Throwable> clz = annot.value();
        if (Objects.equals(clz, UnitedThrow.None.class)) {
            if (method.getExceptionTypes().length == 0) {
                clz = IllegalStateException.class;
            } else {
                clz = (Class<? extends Throwable>) method
                    .getExceptionTypes()[0];
            }
        }
        return clz;
    }
}
