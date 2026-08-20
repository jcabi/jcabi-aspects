/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Random;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Repeat execution in case of exception.
 * @see RetryOnFailure
 * @since 0.1.10
 */
@Aspect
@Immutable
public final class Repeater {

    /**
     * Pseudo random number generator.
     */
    private static final Random RAND = new SecureRandom();

    /**
     * Catch exception and re-call the method.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (11 lines)
     * @checkstyle NonStaticMethodCheck (100 lines)
     */
    @Around("execution(* * (..)) && @annotation(com.jcabi.aspects.RetryOnFailure)")
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature())
            .getMethod();
        final RetryOnFailure rof = method.getAnnotation(RetryOnFailure.class);
        int attempt = 0;
        final long begin = System.nanoTime();
        final ImprovedJoinPoint joinpoint = new ImprovedJoinPoint(point);
        while (true) {
            final long start = System.nanoTime();
            try {
                return point.proceed();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw ex;
                // @checkstyle IllegalCatch (1 line)
            } catch (final Throwable ex) {
                if (Repeater.fatal(rof, ex)) {
                    throw ex;
                }
                ++attempt;
                Repeater.report(
                    joinpoint.targetize(), method, rof, attempt,
                    System.nanoTime() - start, System.nanoTime() - begin, ex
                );
                if (attempt >= rof.attempts()) {
                    throw ex;
                }
                if (rof.delay() > 0L) {
                    this.delay(rof, attempt);
                }
            }
        }
    }

    private static boolean fatal(final RetryOnFailure rof,
        final Throwable exp) {
        return Repeater.matches(exp.getClass(), rof.ignore())
            || !Repeater.matches(exp.getClass(), rof.types());
    }

    private static void report(final Object target, final Method method,
        final RetryOnFailure rof, final int attempt, final long spent,
        final long total, final Throwable exp) {
        if (Logger.isWarnEnabled(target)) {
            if (rof.verbose()) {
                Logger.warn(
                    target,
                    "#%s(): attempt #%d of %d failed in %[nano]s (%[nano]s waiting already) with %[exception]s",
                    method.getName(),
                    attempt, rof.attempts(), spent, total, exp
                );
            } else {
                Logger.warn(
                    target,
                    "#%s(): attempt #%d/%d failed with %[type]s in %[nano]s (%[nano]s in total): %s",
                    method.getName(),
                    attempt, rof.attempts(), exp, spent, total,
                    Repeater.message(exp)
                );
            }
        }
    }

    private void delay(final RetryOnFailure rof, final int attempt) throws
        InterruptedException {
        final long delay;
        if (rof.randomize()) {
            delay = (long) Repeater.RAND.nextInt(2 << attempt) * rof.delay();
        } else {
            delay = rof.delay() * (long) attempt;
        }
        rof.unit().sleep(delay);
    }

    private static String message(final Throwable exp) {
        final StringBuilder text = new StringBuilder(
            String.valueOf(exp.getMessage())
        );
        if (exp.getCause() != null) {
            text.append("; ").append(Repeater.message(exp.getCause()));
        }
        String msg = text.toString();
        if (msg.length() > 100) {
            msg = String.format("%s...", msg.substring(0, 100));
        }
        return msg;
    }

    @SafeVarargs
    private static boolean matches(
        final Class<? extends Throwable> thrown,
        final Class<? extends Throwable>... types
    ) {
        boolean matches = false;
        for (final Class<? extends Throwable> type : types) {
            if (type.isAssignableFrom(thrown)) {
                matches = true;
                break;
            }
        }
        return matches;
    }
}
