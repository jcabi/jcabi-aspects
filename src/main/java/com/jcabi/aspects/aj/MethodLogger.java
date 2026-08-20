/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class MethodLogger {

    /**
     * Currently running methods.
     */
    private final transient Set<Marker> running;

    /**
     * Public ctor.
     */
    @SuppressWarnings({
        "PMD.CloseResource",
        "PMD.ConstructorOnlyInitializesOrCallOtherConstructors",
        "FutureReturnValueIgnored"
    })
    public MethodLogger() {
        // @checkstyle ConstructorsCodeFreeCheck (30 lines)
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
                        for (final Marker marker
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
    public Object wrapMethod(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            ((MethodSignature) point.getSignature()).getMethod();
        return this.wrap(point, method, method.getAnnotation(Loggable.class));
    }

    static String allText(final StackTraceElement... trace) {
        return Arrays.stream(trace)
            .map(MethodLogger::oneText)
            .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("PMD.AvoidThreadGroup")
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
        final Marker marker =
            new Marker(point, annotation);
        this.running.add(marker);
        int level = annotation.value();
        try {
            final Object logger = MethodLogger.logger(method, annotation.name());
            if (annotation.prepend()) {
                LogHelper.log(
                    level,
                    logger,
                    "%s: entered",
                    Mnemos.toText(
                        point,
                        annotation.trim(),
                        annotation.skipArgs(),
                        annotation.logThis()
                    )
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
            MethodLogger.report(point, method, annotation, level, ex, start);
            throw ex;
        } finally {
            this.running.remove(marker);
        }
    }

    private static void report(final ProceedingJoinPoint point,
        final Method method, final Loggable annotation, final int level,
        final Throwable exp, final long start) {
        if (!MethodLogger.contains(annotation.ignore(), exp)
            && !exp.getClass().isAnnotationPresent(Loggable.Quiet.class)) {
            final int exlevel;
            if (annotation.logException() >= 0) {
                exlevel = annotation.logException();
            } else {
                exlevel = level;
            }
            if (LogHelper.enabled(exlevel, method.getDeclaringClass())) {
                LogHelper.log(
                    exlevel,
                    method.getDeclaringClass(),
                    Logger.format(
                        "%s: thrown %s out of %s in %[nano]s",
                        Mnemos.toText(
                            point,
                            annotation.trim(),
                            annotation.skipArgs(),
                            annotation.logThis()
                        ),
                        Mnemos.toText(exp),
                        MethodLogger.origin(exp),
                        System.nanoTime() - start
                    )
                );
            }
        }
    }

    private static String origin(final Throwable exp) {
        final StackTraceElement[] traces = exp.getStackTrace();
        final String origin;
        if (traces.length > 0) {
            origin = MethodLogger.oneText(traces[0]);
        } else {
            origin = "somewhere";
        }
        return origin;
    }

    private static boolean over(final Loggable annotation, final long nano) {
        return nano > annotation.unit().toNanos(
            (long) annotation.limit()
        );
    }

    private static String message(final ProceedingJoinPoint point, final Method method,
        final Loggable annotation, final Object result, final long nano) {
        final StringBuilder msg = new StringBuilder(
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

    private static Object logger(final Method method, final CharSequence name) {
        final Object source;
        if (name.length() == 0) {
            source = method.getDeclaringClass();
        } else {
            source = name;
        }
        return source;
    }

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

    private static String oneText(final StackTraceElement trace) {
        return String.format(
            "%s#%s[%d]",
            trace.getClassName(),
            trace.getMethodName(),
            trace.getLineNumber()
        );
    }
}
