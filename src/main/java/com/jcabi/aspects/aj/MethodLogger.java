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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
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
 * @since 0.7.2
 * @checkstyle IllegalThrows (500 lines)
 */
@Aspect
@SuppressWarnings
    (
        {
            "PMD.AvoidCatchingThrowable",
            "PMD.TooManyMethods",
            "PMD.CyclomaticComplexity"
        }
    )
public final class MethodLogger {

    /**
     * Currently running methods.
     */
    private final transient Set<MethodLogger.Marker> running;

    /**
     * Public ctor.
     */
    @SuppressWarnings(
        {
            "PMD.DoNotUseThreads",
            "PMD.ConstructorOnlyInitializesOrCallOtherConstructors"
        }
    )
    public MethodLogger() {
        this.running = new ConcurrentSkipListSet<>();
        final ScheduledExecutorService monitor =
            Executors.newSingleThreadScheduledExecutor(
                new NamedThreads(
                    "loggable",
                    "watching of @Loggable annotated methods"
                )
            );
        monitor.scheduleWithFixedDelay(
            new FutureTask<Void>(
                new VerboseRunnable(
                    () -> {
                        for (final MethodLogger.Marker marker
                            : this.running) {
                            marker.monitor();
                        }
                    }
                ), null
            ) {
                @Override
                protected void done() {
                    Logger.debug(this, "Logging monitor thread interrupted");
                    monitor.shutdown();
                }
            },
            1L, 1L, TimeUnit.SECONDS
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
    @Around
        (
            // @checkstyle StringLiteralsConcatenation (7 lines)
            "execution(public * (@com.jcabi.aspects.Loggable *).*(..))"
            + " && !execution(String *.toString())"
            + " && !execution(int *.hashCode())"
            + " && !execution(boolean *.canEqual(Object))"
            + " && !execution(boolean *.equals(Object))"
            + " && !cflow(call(com.jcabi.aspects.aj.MethodLogger.new()))"
        )
    public Object wrapClass(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            ((MethodSignature) point.getSignature()).getMethod();
        final Object output;
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
    @Around
        (
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "(execution(* *(..)) || initialization(*.new(..)))"
            + " && @annotation(com.jcabi.aspects.Loggable)"
        )
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public Object wrapMethod(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            ((MethodSignature) point.getSignature()).getMethod();
        return this.wrap(point, method, method.getAnnotation(Loggable.class));
    }

    /**
     * Catch exception and re-call the method.
     * @param point Joint point
     * @param method The method
     * @param annotation The annotation
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     * @checkstyle CyclomaticComplexityCheck (100 lines)
     */
    @SuppressWarnings({
        "PMD.AvoidThreadGroup",
        "PMD.GuardLogStatement"
    })
    private Object wrap(final ProceedingJoinPoint point, final Method method,
        final Loggable annotation) throws Throwable {
        if (Thread.interrupted()) {
            throw new IllegalStateException(
                String.format(
                    "thread '%s' in group '%s' interrupted",
                    Thread.currentThread().getName(),
                    Thread.currentThread().getThreadGroup().getName()
                )
            );
        }
        final long start = System.nanoTime();
        final MethodLogger.Marker marker =
            new MethodLogger.Marker(point, annotation);
        this.running.add(marker);
        int level = annotation.value();
        try {
            final Object logger = MethodLogger.logger(method, annotation.name());
            if (annotation.prepend()) {
                LogHelper.log(
                    level,
                    logger,
                    new StringBuilder(
                        Mnemos.toText(
                            point,
                            annotation.trim(),
                            annotation.skipArgs(),
                            annotation.logThis()
                        )
                    ).append(": entered").toString()
                );
            }
            final Object result = point.proceed();
            final long nano = System.nanoTime() - start;
            if (LogHelper.enabled(level, logger)
                || MethodLogger.over(annotation, nano)) {
                if (MethodLogger.over(annotation, nano)) {
                    level = Loggable.WARN;
                }
                LogHelper.log(
                    level, logger,
                    MethodLogger.message(point, method, annotation, result, nano)
                );
            }
            return result;
        // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            if (!MethodLogger.contains(annotation.ignore(), ex)
                && !ex.getClass().isAnnotationPresent(Loggable.Quiet.class)) {
                final StackTraceElement[] traces = ex.getStackTrace();
                final String origin;
                if (traces.length > 0) {
                    final StackTraceElement trace = traces[0];
                    origin = MethodLogger.oneText(trace);
                } else {
                    origin = "somewhere";
                }
                if (LogHelper.enabled(level, method.getDeclaringClass())) {
                    LogHelper.log(
                        level,
                        method.getDeclaringClass(),
                        Logger.format(
                            "%s: thrown %s out of %s in %[nano]s",
                            Mnemos.toText(
                                point,
                                annotation.trim(),
                                annotation.skipArgs(),
                                annotation.logThis()
                            ),
                            Mnemos.toText(ex),
                            origin,
                            System.nanoTime() - start
                        )
                    );
                }
            }
            throw ex;
        } finally {
            this.running.remove(marker);
        }
    }

    /**
     * Has time for method execution passed.
     * @param annotation Loggable annotation.
     * @param nano Execution time.
     * @return Is over time limit.
     */
    private static boolean over(final Loggable annotation, final long nano) {
        return nano > annotation.unit().toNanos(
            (long) annotation.limit()
        );
    }

    /**
     * Prepared message for log.
     * @param point JointPoint to use.
     * @param method Method for which to log.
     * @param annotation Loggable annotation.
     * @param result Method result.
     * @param nano Method execution time.
     * @return Log message.
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    private static String message(final ProceedingJoinPoint point, final Method method,
        final Loggable annotation, final Object result, final long nano) {
        final StringBuilder msg = new StringBuilder(0);
        msg.append(
            Mnemos.toText(
                point,
                annotation.trim(),
                annotation.skipArgs(),
                annotation.logThis()
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
        msg.append(
            Logger.format(
                String.format(
                    " in %%[nano].%ds", annotation.precision()
                ),
                nano
            )
        );
        if (MethodLogger.over(annotation, nano)) {
            msg.append(" (too slow!)");
        }
        return msg.toString();
    }

    /**
     * Get the destination logger for this method.
     * @param method The method
     * @param name The Loggable annotation
     * @return The logger that will be used
     */
    private static Object logger(final Method method, final CharSequence name) {
        final Object source;
        if (name.length() == 0) {
            source = method.getDeclaringClass();
        } else {
            source = name;
        }
        return source;
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
        for (final Class<? extends Throwable> type : array) {
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
            || child.getSuperclass() != null
            && MethodLogger.instanceOf(child.getSuperclass(), parent);
        if (!instance) {
            for (final Class<?> iface : child.getInterfaces()) {
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
    private static String allText(final StackTraceElement... trace) {
        final StringBuilder text = new StringBuilder(0);
        for (final StackTraceElement element : trace) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(MethodLogger.oneText(element));
        }
        return text.toString();
    }

    /**
     * Textualize a stacktrace.
     * @param trace One stacktrace element
     * @return The text
     */
    private static String oneText(final StackTraceElement trace) {
        return String.format(
            "%s#%s[%d]",
            trace.getClassName(),
            trace.getMethodName(),
            trace.getLineNumber()
        );
    }

    /**
     * Marker of a running method.
     * @since 0.0.0
     */
    private static final class Marker
        implements Comparable<MethodLogger.Marker> {

        /**
         * When the method was started, in milliseconds.
         */
        private final transient long started;

        /**
         * Which monitoring cycle was logged recently.
         */
        private final transient AtomicInteger logged;

        /**
         * The thread it's running in.
         */
        @SuppressWarnings("PMD.DoNotUseThreads")
        private final transient Thread thread;

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
            this.started = System.currentTimeMillis();
            this.logged = new AtomicInteger();
            this.point = pnt;
            this.annotation = annt;
            this.thread = Thread.currentThread();
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
                final Method method = ((MethodSignature) this.point.getSignature()).getMethod();
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
                    MethodLogger.allText(this.thread.getStackTrace())
                );
                this.logged.set(cycle);
            }
        }

        @Override
        public int hashCode() {
            return this.point.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this || ((MethodLogger.Marker) obj)
                .point.equals(this.point);
        }

        @Override
        public int compareTo(final MethodLogger.Marker marker) {
            int cmp = 0;
            if (this.started < marker.started) {
                cmp = 1;
            } else if (this.started > marker.started) {
                cmp = -1;
            }
            return cmp;
        }
    }

}
