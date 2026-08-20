/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Schedules methods.
 * @since 0.7.16
 */
@Aspect
public final class MethodScheduler {

    /**
     * Objects and their running services.
     */
    private final transient
        ConcurrentMap<Object, Service> services;

    /**
     * Ctor.
     */
    public MethodScheduler() {
        this.services =
            new ConcurrentHashMap<>(0);
    }

    /**
     * Instantiate a new routine task.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     */
    @After("initialization((@com.jcabi.aspects.ScheduleWithFixedDelay *).new(..))")
    public void instantiate(final JoinPoint point) {
        final Object object = point.getTarget();
        if (this.services.containsKey(object)) {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s was already scheduled for execution",
                    object
                )
            );
        }
        final Runnable runnable;
        if (object instanceof Runnable) {
            runnable = new VerboseRunnable((Runnable) object, true);
        } else if (object instanceof Callable) {
            runnable = new VerboseRunnable((Callable<?>) object, true);
        } else {
            throw new IllegalStateException(
                Logger.format(
                    "%[type]s doesn't implement Runnable or Callable",
                    object
                )
            );
        }
        this.services.put(
            object,
            new Service(
                runnable,
                object,
                object.getClass().getAnnotation(ScheduleWithFixedDelay.class)
            )
        );
    }

    /**
     * Stop/close a routine task.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     */
    @Before("execution(* (@com.jcabi.aspects.ScheduleWithFixedDelay *).close())")
    public void close(final JoinPoint point) {
        final Object object = point.getTarget();
        this.services.get(object).close();
        this.services.remove(object);
    }
}
