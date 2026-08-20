/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Timeable;
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * A call being watched.
 * @since 0.7.16
 */
final class Call implements
    Comparable<Call> {

    /**
     * The thread called.
     */
    private final transient Thread thread;

    /**
     * When started.
     */
    private final transient long start;

    /**
     * When will expire.
     */
    private final transient long deadline;

    /**
     * Join point.
     */
    private final transient ProceedingJoinPoint point;

    /**
     * Public ctor.
     * @param pnt Joint point
     */
    Call(final ProceedingJoinPoint pnt) {
        this(
            pnt,
            Thread.currentThread(),
            System.currentTimeMillis(),
            Call.limit(pnt)
        );
    }

    /**
     * Ctor.
     * @param pnt Joint point
     * @param thrd The thread that called
     * @param begin When it started
     * @param span How long the call may take, in milliseconds
     */
    private Call(final ProceedingJoinPoint pnt, final Thread thrd,
        final long begin, final long span) {
        this.point = pnt;
        this.thread = thrd;
        this.start = begin;
        this.deadline = begin + span;
    }

    @Override
    public int hashCode() {
        return this.point.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || ((Call) obj)
            .point.equals(this.point);
    }

    @Override
    public int compareTo(final Call obj) {
        final int compare;
        if (this.deadline > obj.deadline) {
            compare = 1;
        } else if (this.deadline < obj.deadline) {
            compare = -1;
        } else {
            compare = 0;
        }
        return compare;
    }

    /**
     * Is it expired already?
     * @return TRUE if expired
     */
    boolean expired() {
        return this.deadline < System.currentTimeMillis();
    }

    /**
     * This thread is stopped already (interrupt if not)?
     * @return TRUE if it's already dead
     */
    boolean interrupted() {
        final boolean dead;
        if (this.thread.isAlive()) {
            this.thread.interrupt();
            final Method method = ((MethodSignature) this.point.getSignature())
                .getMethod();
            if (Logger.isWarnEnabled(method.getDeclaringClass())) {
                Logger.warn(
                    method.getDeclaringClass(),
                    "%s: interrupted on %[ms]s timeout (over %[ms]s)",
                    Mnemos.toText(this.point, true, false),
                    System.currentTimeMillis() - this.start,
                    this.deadline - this.start
                );
            }
            dead = false;
        } else {
            dead = true;
        }
        return dead;
    }

    private static long limit(final ProceedingJoinPoint pnt) {
        final Timeable annt = ((MethodSignature) pnt.getSignature())
            .getMethod().getAnnotation(Timeable.class);
        return annt.unit().toMillis((long) annt.limit());
    }
}
