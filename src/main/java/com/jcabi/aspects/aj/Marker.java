/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Marker of a running method.
 * @since 0.0.0
 */
final class Marker
    implements Comparable<Marker> {

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
    Marker(final ProceedingJoinPoint pnt, final Loggable annt) {
        this(
            pnt, annt, System.currentTimeMillis(), Thread.currentThread()
        );
    }

    /**
     * Ctor.
     * @param pnt Joint point
     * @param annt Annotation
     * @param begin When the method was started, in milliseconds
     * @param thrd The thread it's running in
     */
    private Marker(final ProceedingJoinPoint pnt, final Loggable annt,
        final long begin, final Thread thrd) {
        this.point = pnt;
        this.annotation = annt;
        this.started = begin;
        this.thread = thrd;
        this.logged = new AtomicInteger();
    }

    @Override
    public int hashCode() {
        return this.point.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || ((Marker) obj)
            .point.equals(this.point);
    }

    @Override
    public int compareTo(final Marker marker) {
        int cmp = 0;
        if (this.started < marker.started) {
            cmp = 1;
        } else if (this.started > marker.started) {
            cmp = -1;
        }
        return cmp;
    }

    /**
     * Monitor it's status and log the problem, if any.
     */
    void monitor() {
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
}
