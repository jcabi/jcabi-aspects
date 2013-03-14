/**
 * Copyright (c) 2012-2013, JCabi.com
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
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Logs method calls.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.2
 */
@Aspect
@Immutable
@SuppressWarnings("PMD.AvoidCatchingThrowable")
public final class MethodLogger {

    /**
     * Log methods in a class.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     * @checkstyle LineLength (3 lines)
     */
    @Around("((execution(public * (@com.jcabi.aspects.Loggable *).*(..)) || initialization((@com.jcabi.aspects.Loggable *).new(..))) && !execution(String *.toString()) && !execution(int *.hashCode()) && !execution(boolean *.equals(Object)))")
    public Object wrapClass(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            MethodSignature.class.cast(point.getSignature()).getMethod();
        Object output;
        if (method.isAnnotationPresent(Loggable.class)) {
            output = point.proceed();
        } else {
            output = this.wrap(
                point,
                method,
                method.getDeclaringClass().getAnnotation(Loggable.class)
            );
        }
        return output;
    }

    /**
     * Log individual methods.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     * @checkstyle LineLength (3 lines)
     */
    @Around("(execution(* *(..)) || initialization(*.new(..))) && @annotation(com.jcabi.aspects.Loggable)")
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrapMethod(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            MethodSignature.class.cast(point.getSignature()).getMethod();
        return this.wrap(point, method, method.getAnnotation(Loggable.class));
    }

    /**
     * Catch exception and re-call the method.
     * @param point Joint point
     * @param method The method
     * @param annotation The annotation
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (3 lines)
     */
    private Object wrap(final ProceedingJoinPoint point, final Method method,
        final Loggable annotation) throws Throwable {
        final long start = System.nanoTime();
        try {
            final Object result = point.proceed();
            final Class<?> type = method.getDeclaringClass();
            final int limit = annotation.limit();
            int level = annotation.value();
            final long nano = System.nanoTime() - start;
            final boolean over = nano > annotation.unit().toNanos(limit);
            if (MethodLogger.enabled(level, type) || over) {
                final StringBuilder msg = new StringBuilder();
                msg.append(Mnemos.toString(point, annotation.trim()))
                    .append(':');
                if (!method.getReturnType().equals(Void.TYPE)) {
                    msg.append(" returned ")
                        .append(Mnemos.toString(result, annotation.trim()));
                }
                msg.append(Logger.format(" in %[nano]s", nano));
                if (over) {
                    level = Loggable.WARN;
                    msg.append(" (too slow!)");
                }
                MethodLogger.log(
                    level,
                    type,
                    msg.toString()
                );
            }
            return result;
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            MethodLogger.log(
                Loggable.ERROR,
                method.getDeclaringClass(),
                Logger.format(
                    "%s: thrown %[type]s (%s) in %[nano]s",
                    Mnemos.toString(point, annotation.trim()),
                    ex,
                    Mnemos.toString(ex.getMessage(), false),
                    System.nanoTime() - start
                )
            );
            throw ex;
        }
    }

    /**
     * Log one line.
     * @param level Level of logging
     * @param log Destination log
     * @param message Message to log
     */
    private static void log(final int level, final Class<?> log,
        final String message) {
        if (level == Loggable.TRACE) {
            Logger.trace(log, message);
        } else if (level == Loggable.DEBUG) {
            Logger.debug(log, message);
        } else if (level == Loggable.INFO) {
            Logger.info(log, message);
        } else if (level == Loggable.WARN) {
            Logger.warn(log, message);
        } else if (level == Loggable.ERROR) {
            Logger.error(log, message);
        }
    }

    /**
     * Log level is enabled?
     * @param level Level of logging
     * @param log Destination log
     * @return TRUE if enabled
     */
    private static boolean enabled(final int level, final Class<?> log) {
        boolean enabled;
        if (level == Loggable.TRACE) {
            enabled = Logger.isTraceEnabled(log);
        } else if (level == Loggable.DEBUG) {
            enabled = Logger.isDebugEnabled(log);
        } else if (level == Loggable.INFO) {
            enabled = Logger.isInfoEnabled(log);
        } else if (level == Loggable.WARN) {
            enabled = Logger.isWarnEnabled(log);
        } else {
            enabled = true;
        }
        return enabled;
    }

}
