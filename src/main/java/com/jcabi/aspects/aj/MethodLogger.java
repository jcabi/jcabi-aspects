/**
 * Copyright (c) 2012, jcabi.com
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
public final class MethodLogger {

    /**
     * Catch exception and re-call the method.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     * @checkstyle LineLength (3 lines)
     */
    @Around("(execution(* *(..)) || call(*.new(..))) && @annotation(com.jcabi.aspects.Loggable)")
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final long start = System.nanoTime();
        final Method method = MethodSignature.class.cast(
            point.getSignature()
        ).getMethod();
        try {
            final Object result = point.proceed();
            this.log(
                method.getAnnotation(Loggable.class).value(),
                method.getDeclaringClass(),
                this.compose(
                    method,
                    point,
                    Logger.format(
                        "returned %s in %[nano]s",
                        MethodLogger.text(result),
                        System.nanoTime() - start
                    )
                )
            );
            return result;
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            this.log(
                Loggable.ERROR,
                method.getDeclaringClass(),
                this.compose(
                    method,
                    point,
                    Logger.format(
                        "thrown %[type]s (%s) in %[nano]s",
                        ex,
                        MethodLogger.text(ex.getMessage()),
                        System.nanoTime() - start
                    )
                )
            );
            throw ex;
        }
    }

    /**
     * Compose a log message.
     * @param method Method just called
     * @param point Join point
     * @param result Result of execution
     * @return Composed log message
     */
    private String compose(final Method method, final ProceedingJoinPoint point,
        final String result) {
        final StringBuilder log = new StringBuilder();
        log.append('#').append(method.getName()).append('(');
        final Object[] args = point.getArgs();
        for (int pos = 0; pos < args.length; ++pos) {
            if (pos > 0) {
                log.append(", ");
            }
            log.append(MethodLogger.text(args[pos]));
        }
        log.append("): ").append(result);
        return log.toString();
    }

    /**
     * Log one line.
     * @param level Level of logging
     * @param log Destination log
     * @param message Message to log
     */
    private void log(final int level, final Class<?> log,
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
     * Argument to text.
     * @param arg The argument
     * @return Text representation of it
     */
    private static String text(final Object arg) {
        String text;
        if (arg == null) {
            text = "NULL";
        } else {
            text = Logger.format("'%s'", arg);
        }
        return text;
    }

}
