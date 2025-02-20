/*
 * Copyright (c) 2012-2025, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
 *
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
     * @checkstyle IllegalThrows (7 lines)
     * @checkstyle LineLength (4 lines)
     * @checkstyle NonStaticMethodCheck (100 lines)
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     */
    @Around("execution(* * (..)) && @annotation(com.jcabi.aspects.RetryOnFailure)")
    @SuppressWarnings({ "PMD.AvoidCatchingThrowable", "PMD.GuardLogStatement" })
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Method method = ((MethodSignature) point.getSignature()).getMethod();
        final RetryOnFailure rof = method.getAnnotation(RetryOnFailure.class);
        final ImprovedJoinPoint jpoint = new ImprovedJoinPoint(point);
        final Class<? extends Throwable>[] rtypes = rof.types();
        final long start = System.nanoTime();
        int attempt = 0;
        while (true) {
            final long attstart = System.nanoTime();
            try {
                return point.proceed();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw ex;
            } catch (final Throwable ex) {
                this.handleException(ex, rof, rtypes, jpoint, method, attempt, start, attstart);
                ++attempt;
                if (attempt >= rof.attempts()) {
                    throw ex;
                }
                if (rof.delay() > 0L) {
                    this.delay(rof, attempt);
                }
            }
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void handleException(
        final Throwable exc, final RetryOnFailure rof,
        final Class<? extends Throwable>[] retrytypes,
        final ImprovedJoinPoint joinpoint, final Method method,
        final int attempt, final long start, final long attemptstart
    ) throws Throwable {
        if (Repeater.matches(exc.getClass(), rof.ignore())) {
            throw exc;
        }
        if (!Repeater.matches(exc.getClass(), retrytypes)) {
            throw exc;
        }
        if (Logger.isWarnEnabled(joinpoint.targetize())) {
            this.logFailure(rof, joinpoint, method, attempt, exc, start, attemptstart);
        }
    }

    private void logFailure(final RetryOnFailure rof, final ImprovedJoinPoint joinpoint,
        final Method method, final int atmp, final Throwable exc,
        final long start, final long attemptstart) {
        final long elps = System.nanoTime() - start;
        final long atelps = System.nanoTime() - attemptstart;
        if (rof.verbose()) {
            Logger.warn(
                joinpoint.targetize(),
                "#%s(): attempt #%d of %d failed in %[nano]s (%[nano]s waiting already) with %[exception]s",
                method.getName(), atmp, rof.attempts(), atelps, elps, exc
            );
        } else {
            Logger.warn(
                joinpoint.targetize(),
                "#%s(): attempt #%d/%d failed with %[type]s in %[nano]s (%[nano]s in total): %s",
                method.getName(), atmp, rof.attempts(), exc, atelps, elps, Repeater.message(exc)
            );
        }
    }

    /**
     * Waits certain time before returning.
     * @param rof RetryOnFailure parameters.
     * @param attempt Attempt number.
     * @throws InterruptedException If wait has been interrupted.
     */
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

    /**
     * Get a message out of a potentially chained exception (recursively
     * calls itself in order to reproduce a chain of messages).
     * @param exp The exception
     * @return The message
     */
    private static String message(final Throwable exp) {
        final StringBuilder text = new StringBuilder(0);
        text.append(exp.getMessage());
        if (exp.getCause() != null) {
            text.append("; ").append(Repeater.message(exp.getCause()));
        }
        String msg = text.toString();
        if (msg.length() > 100) {
            msg = String.format("%s...", msg.substring(0, 100));
        }
        return msg;
    }

    /**
     * Checks if the exception thrown matches the list.
     * @param thrown The thrown exception class
     * @param types The exceptions to match
     * @return TRUE if matches
     */
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
