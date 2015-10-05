/**
 * Copyright (c) 2012-2015, jcabi.com
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1.10
 * @see com.jcabi.aspects.RetryOnFailure
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
     * @checkstyle IllegalThrows (6 lines)
     * @checkstyle LineLength (4 lines)
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     */
    @Around("execution(* * (..)) && @annotation(com.jcabi.aspects.RetryOnFailure)")
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Method method = MethodSignature.class
            .cast(point.getSignature())
            .getMethod();
        final RetryOnFailure rof = method.getAnnotation(RetryOnFailure.class);
        int attempt = 0;
        final long begin = System.nanoTime();
        final Class<? extends Throwable>[] types = rof.types();
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
                if (Repeater.matches(ex.getClass(), rof.ignore())) {
                    throw ex;
                }
                if (!Repeater.matches(ex.getClass(), types)) {
                    throw ex;
                }
                ++attempt;
                if (rof.verbose()) {
                    Logger.warn(
                        joinpoint.targetize(),
                        // @checkstyle LineLength (1 line)
                        "#%s(): attempt #%d of %d failed in %[nano]s (%[nano]s waiting already) with %[exception]s",
                        method.getName(),
                        attempt, rof.attempts(), System.nanoTime() - start,
                        System.nanoTime() - begin, ex
                    );
                } else {
                    Logger.warn(
                           joinpoint.targetize(),
                        // @checkstyle LineLength (1 line)
                        "#%s(): attempt #%d/%d failed with %[type]s in %[nano]s (%[nano]s in total): %s",
                        method.getName(),
                        attempt, rof.attempts(), ex, System.nanoTime() - start,
                        System.nanoTime() - begin, Repeater.message(ex)
                    );
                }
                if (attempt >= rof.attempts()) {
                    throw ex;
                }
                if (rof.delay() > 0) {
                    this.delay(rof, attempt);
                }
            }
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
            delay = Repeater.RAND.nextInt(2 << attempt) * rof.delay();
        } else {
            delay = rof.delay() * attempt;
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
        final StringBuilder text = new StringBuilder();
        text.append(exp.getMessage());
        if (exp.getCause() != null) {
            text.append("; ").append(Repeater.message(exp.getCause()));
        }
        return text.toString();
    }

    /**
     * Checks if the exception thrown matches the list.
     * @param thrown The thrown exception class
     * @param types The exceptions to match
     * @return TRUE if matches
     */
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
