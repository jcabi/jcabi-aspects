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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Logs method calls.
 *
 * <p>It is an AspectJ aspect and you are not supposed to use it directly. It
 * is instantiated by AspectJ runtime framework when your code is annotated
 * with {@link Loggable} annotation.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.2
 * @checkstyle IllegalThrows (500 lines)
 */
@Aspect
@SuppressWarnings({
    "PMD.AvoidCatchingThrowable",
    "PMD.TooManyMethods",
    "PMD.CyclomaticComplexity"
})
public final class MethodLogger {

    /**
     * Currently running methods.
     */
    private final transient Set<MethodLogger.Marker> running =
        new ConcurrentSkipListSet<MethodLogger.Marker>();

    /**
     * Service that monitors all running methods.
     */
    private final transient ScheduledExecutorService monitor;

    /**
     * Public ctor.
     */
    @SuppressWarnings("PMD.DoNotUseThreads")
    public MethodLogger() {
        this.monitor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreads(
                "loggable",
                "watching of @Loggable annotated methods"
            )
        );
        this.monitor.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        for (MethodLogger.Marker marker
                            : MethodLogger.this.running) {
                            marker.monitor();
                        }
                    }
                }
            ),
            1, 1, TimeUnit.SECONDS
        );
    }

    /**
     * Log methods in a class.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     */
    @Around(
        // @checkstyle StringLiteralsConcatenation (7 lines)
        "(execution(public * (@com.jcabi.aspects.Loggable *).*(..))"
        + " || initialization((@com.jcabi.aspects.Loggable *).new(..)))"
        + " && !execution(String *.toString())"
        + " && !execution(int *.hashCode())"
        + " && !execution(boolean *.canEqual(Object))"
        + " && !execution(boolean *.equals(Object))"
        + " && !cflow(call(com.jcabi.aspects.aj.MethodLogger.new()))"
    )
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
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     */
    @Around(
        // @checkstyle StringLiteralsConcatenation (2 lines)
        "(execution(* *(..)) || initialization(*.new(..)))"
        + " && @annotation(com.jcabi.aspects.Loggable)"
    )
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
     * @checkstyle ExecutableStatementCount (50 lines)
     */
    private Object wrap(final ProceedingJoinPoint point, final Method method,
        final Loggable annotation) throws Throwable {
        if (Thread.interrupted()) {
            throw new IllegalStateException("thread interrupted");
        }
        final long start = System.nanoTime();
        final MethodLogger.Marker marker =
            new MethodLogger.Marker(point, annotation);
        this.running.add(marker);
        try {
            final Class<?> type = method.getDeclaringClass();
            int level = annotation.value();
            final int limit = annotation.limit();
            if (annotation.prepend()) {
                MethodLogger.log(
                    level,
                    type,
                    new StringBuilder(
                        Mnemos.toText(
                            point,
                            annotation.trim(),
                            annotation.skipArgs()
                        )
                    ).append(": entered").toString()
                );
            }
            final Object result = point.proceed();
            final long nano = System.nanoTime() - start;
            final boolean over = nano > annotation.unit().toNanos(limit);
            if (MethodLogger.enabled(level, type) || over) {
                final StringBuilder msg = new StringBuilder();
                msg.append(
                    Mnemos.toText(
                        point,
                        annotation.trim(),
                        annotation.skipArgs()
                    )
                ).append(':');
                if (!method.getReturnType().equals(Void.TYPE)) {
                    msg.append(' ').append(
                        Mnemos.toText(
                            result,
                            annotation.trim(),
                            annotation.skipResult()
                        )
                    );
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
            if (!MethodLogger.contains(annotation.ignore(), ex)
                && !ex.getClass().isAnnotationPresent(Loggable.Quiet.class)) {
                final StackTraceElement trace = ex.getStackTrace()[0];
                MethodLogger.log(
                    Loggable.ERROR,
                    method.getDeclaringClass(),
                    Logger.format(
                        "%s: thrown %s out of %s#%s[%d] in %[nano]s",
                        Mnemos.toText(
                            point,
                            annotation.trim(),
                            annotation.skipArgs()
                        ),
                        Mnemos.toText(ex),
                        trace.getClassName(),
                        trace.getMethodName(),
                        trace.getLineNumber(),
                        System.nanoTime() - start
                    )
                );
            }
            throw ex;
        } finally {
            this.running.remove(marker);
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

    /**
     * Marker of a running method.
     */
    private static final class Marker
        implements Comparable<MethodLogger.Marker> {
        /**
         * When the method was started, in milliseconds.
         */
        private final transient long started = System.currentTimeMillis();
        /**
         * Which monitoring cycle was logged recently.
         */
        private final transient AtomicInteger logged = new AtomicInteger();
        /**
         * The thread it's running in.
         */
        @SuppressWarnings("PMD.DoNotUseThreads")
        private final transient Thread thread = Thread.currentThread();
        /**
         * Joint point.
         */
        private final transient ProceedingJoinPoint point;
        /**
         * Annotation.
         */
        private final transient Loggable annotation;
        /**
         * Public ctor.
         * @param pnt Joint point
         * @param annt Annotation
         */
        protected Marker(final ProceedingJoinPoint pnt, final Loggable annt) {
            this.point = pnt;
            this.annotation = annt;
        }
        /**
         * Monitor it's status and log the problem, if any.
         */
        public void monitor() {
            final TimeUnit unit = this.annotation.unit();
            final long threshold = this.annotation.limit();
            final long age = unit.convert(
                System.currentTimeMillis() - this.started, TimeUnit.MILLISECONDS
            );
            final int cycle = (int) ((age - threshold) / threshold);
            if (cycle > this.logged.get()) {
                final Method method = MethodSignature.class.cast(
                    this.point.getSignature()
                ).getMethod();
                Logger.warn(
                    method.getDeclaringClass(),
                    "%s: takes more than %[ms]s, %[ms]s already, thread=%s/%s",
                    Mnemos.toText(this.point, true, this.annotation.skipArgs()),
                    TimeUnit.MILLISECONDS.convert(threshold, unit),
                    TimeUnit.MILLISECONDS.convert(age, unit),
                    this.thread.getName(),
                    this.thread.getState()
                );
                Logger.debug(
                    method.getDeclaringClass(),
                    "%s: thread %s/%s stacktrace: %s",
                    Mnemos.toText(this.point, true, this.annotation.skipArgs()),
                    this.thread.getName(),
                    this.thread.getState(),
                    MethodLogger.textualize(this.thread.getStackTrace())
                );
                this.logged.set(cycle);
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.point.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            return obj == this || MethodLogger.Marker.class.cast(obj)
                .point.equals(this.point);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Marker marker) {
            return Long.compare(this.started, marker.started);
        }
    }

    /**
     * Checks whether array of types contains given type.
     * @param array Array of them
     * @param exp The exception to find
     * @return TRUE if it's there
     */
    private static boolean contains(final Class<? extends Throwable>[] array,
        final Throwable exp) {
        boolean contains = false;
        for (Class<? extends Throwable> type : array) {
            if (MethodLogger.instanceOf(exp.getClass(), type)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * The type is an instance of another type?
     * @param child The child type
     * @param parent Parent type
     * @return TRUE if child is really a child of a parent
     */
    private static boolean instanceOf(final Class<?> child,
        final Class<?> parent) {
        boolean instance = child.equals(parent)
            || (child.getSuperclass() != null
            && MethodLogger.instanceOf(child.getSuperclass(), parent));
        if (!instance) {
            for (Class<?> iface : child.getInterfaces()) {
                instance = MethodLogger.instanceOf(iface, parent);
                if (instance) {
                    break;
                }
            }
        }
        return instance;
    }

    /**
     * Textualize a stacktrace.
     * @param trace Array of stacktrace elements
     * @return The text
     */
    private static String textualize(final StackTraceElement[] trace) {
        final StringBuilder text = new StringBuilder();
        for (int pos = 0; pos < trace.length; ++pos) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(
                String.format(
                    "%s#%s[%d]",
                    trace[pos].getClassName(),
                    trace[pos].getMethodName(),
                    trace[pos].getLineNumber()
                )
            );
        }
        return text.toString();
    }

}
