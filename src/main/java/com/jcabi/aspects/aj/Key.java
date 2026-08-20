/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Key of a callable target.
 * @since 0.8
 */
final class Key {

    /**
     * When instantiated.
     */
    private final transient long start;

    /**
     * How many times the key was already accessed.
     */
    private final transient AtomicInteger accessed;

    /**
     * Method.
     */
    private final transient Method method;

    /**
     * Object callable (or class, if static method).
     */
    private final transient Object target;

    /**
     * Arguments.
     */
    private final transient Object[] arguments;

    /**
     * Log level.
     */
    private final int level;

    /**
     * Public ctor.
     * @param point Joint point
     */
    Key(final JoinPoint point) {
        this(point, ((MethodSignature) point.getSignature()).getMethod());
    }

    /**
     * Ctor.
     * @param point Joint point
     * @param mtd The method being called
     */
    private Key(final JoinPoint point, final Method mtd) {
        this(
            mtd,
            Key.targetize(point),
            point.getArgs(),
            System.currentTimeMillis(),
            Key.severity(mtd)
        );
    }

    /**
     * Ctor.
     * @param mtd The method being called
     * @param tgt The object being called
     * @param args The arguments of the call
     * @param begin When it was instantiated
     * @param lvl Log level
     */
    private Key(final Method mtd, final Object tgt, final Object[] args,
        final long begin, final int lvl) {
        this.method = mtd;
        this.target = tgt;
        this.arguments = args.clone();
        this.start = begin;
        this.level = lvl;
        this.accessed = new AtomicInteger();
    }

    @Override
    public String toString() {
        return Mnemos.toText(this.method, this.arguments, true, false);
    }

    @Override
    public int hashCode() {
        return this.method.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean equals;
        if (this == obj) {
            equals = true;
        } else if (obj instanceof Key) {
            final Key key = (Key) obj;
            equals = key.method.equals(this.method)
                && this.target.equals(key.target)
                && Arrays.deepEquals(key.arguments, this.arguments);
        } else {
            equals = false;
        }
        return equals;
    }

    /**
     * Send a result through, with necessary logging.
     * @param result The result to send through
     * @return The same result/object
     */
    Object through(final Object result) {
        final int hit = this.accessed.getAndIncrement();
        final Class<?> type = this.method.getDeclaringClass();
        if (hit > 0 && LogHelper.enabled(this.level, type)) {
            LogHelper.log(
                this.level,
                type,
                "%s: %s from cache (hit #%d, %[ms]s old)",
                this,
                Mnemos.toText(result, true, false),
                hit,
                System.currentTimeMillis() - this.start
            );
        }
        return result;
    }

    /**
     * Is it related to the same target?
     * @param point Proceeding point
     * @return True if the target is the same
     */
    boolean sameTarget(final JoinPoint point) {
        return Key.targetize(point).equals(this.target);
    }

    int getLevel() {
        return this.level;
    }

    private static Object targetize(final JoinPoint point) {
        final Object tgt;
        final Method method = ((MethodSignature) point.getSignature()).getMethod();
        if (Modifier.isStatic(method.getModifiers())) {
            tgt = method.getDeclaringClass();
        } else {
            tgt = point.getTarget();
        }
        return tgt;
    }

    private static int severity(final Method method) {
        final int lvl;
        if (method.isAnnotationPresent(Loggable.class)) {
            lvl = method.getAnnotation(Loggable.class).value();
        } else {
            lvl = Loggable.DEBUG;
        }
        return lvl;
    }
}
