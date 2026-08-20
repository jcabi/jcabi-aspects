/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Cacheable;
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Mutable caching/calling tunnel, it is thread-safe.
 * @since 0.8
 */
final class Tunnel {

    /**
     * Proceeding join point.
     */
    private final transient ProceedingJoinPoint point;

    /**
     * Key related to this tunnel.
     */
    private final transient Key key;

    /**
     * Whether asynchronous update.
     */
    private final transient boolean asynchupdate;

    /**
     * Was it already executed?
     */
    private transient boolean executed;

    /**
     * When will it expire (moment in time).
     */
    private transient long lifetime;

    /**
     * Cached value.
     */
    private transient Object cached;

    /**
     * Public ctor.
     * @param pnt ProceedingJoinPoint
     * @param akey Key
     * @param aupdate Boolean
     */
    Tunnel(final ProceedingJoinPoint pnt,
        final Key akey, final boolean aupdate) {
        this.point = pnt;
        this.key = akey;
        this.asynchupdate = aupdate;
    }

    @Override
    public String toString() {
        return Mnemos.toText(this.cached, true, false);
    }

    /**
     * Public ctor.
     * @return Tunnel
     */
    Tunnel copy() {
        return new Tunnel(
            this.point, this.key, this.asynchupdate
        );
    }

    /**
     * Get a result through the tunnel.
     * @return The result
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     */
    @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
    synchronized Object through() throws Throwable {
        if (!this.executed) {
            final long start = System.currentTimeMillis();
            this.cached = this.point.proceed();
            final Method method = ((MethodSignature) this.point.getSignature())
                .getMethod();
            final Cacheable annot = method.getAnnotation(Cacheable.class);
            final String suffix;
            if (annot.forever()) {
                this.lifetime = Long.MAX_VALUE;
                suffix = "valid forever";
            } else if (annot.lifetime() == 0) {
                this.lifetime = 0L;
                suffix = "invalid immediately";
            } else {
                final long msec = annot.unit().toMillis(
                    (long) annot.lifetime()
                );
                this.lifetime = start + msec;
                suffix = Logger.format("valid for %[ms]s", msec);
            }
            final Class<?> type = method.getDeclaringClass();
            if (LogHelper.enabled(this.key.getLevel(), type)) {
                LogHelper.log(
                    this.key.getLevel(),
                    type,
                    "%s: %s cached in %[ms]s, %s",
                    Mnemos.toText(
                        method, this.point.getArgs(), true, false
                    ),
                    Mnemos.toText(this.cached, true, false),
                    System.currentTimeMillis() - start,
                    suffix
                );
            }
            this.executed = true;
        }
        return this.key.through(this.cached);
    }

    /**
     * Is it expired already?
     * @return TRUE if expired
     */
    boolean expired() {
        return this.executed && this.lifetime < System.currentTimeMillis();
    }

    /**
     * Whether asynchronous update.
     * @return TRUE if asynchronous update
     */
    boolean asyncUpdate() {
        return this.asynchupdate;
    }
}
