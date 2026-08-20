/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseRunnable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Cache method results.
 *
 * <p>It is an AspectJ aspect and you are not supposed to use it directly. It
 * is instantiated by AspectJ runtime framework when your code is annotated
 * with {@link Cacheable} annotation.
 *
 * <p>The class is thread-safe.
 *
 * @since 0.8
 */
@Aspect
public final class MethodCacher {

    /**
     * Calling tunnels.
     */
    private final transient ConcurrentMap<Key, Tunnel> tunnels;

    /**
     * Save the keys which need update.
     */
    private final transient BlockingQueue<Key> updatekeys;

    /**
     * Service that cleans cache.
     */
    private final transient ScheduledExecutorService cleaner;

    /**
     * Service that update cache.
     */
    private final transient ScheduledExecutorService updater;

    /**
     * Guard of the tunnels.
     */
    private final transient Lock lock;

    /**
     * Public ctor.
     */
    @SuppressWarnings(
        {
            "PMD.ConstructorOnlyInitializesOrCallOtherConstructors",
            "FutureReturnValueIgnored"
        }
    )
    public MethodCacher() {
        // @checkstyle ConstructorsCodeFreeCheck (30 lines)
        this.tunnels = new ConcurrentHashMap<>(0);
        this.updatekeys = new LinkedBlockingQueue<>();
        this.lock = new ReentrantLock();
        this.cleaner = Executors.newSingleThreadScheduledExecutor(
            new NamedThreads(
                "cacheable-clean",
                "automated cleaning of expired @Cacheable values"
            )
        );
        this.cleaner.scheduleWithFixedDelay(
            new VerboseRunnable(
                this::clean
            ),
            1L, 1L, TimeUnit.SECONDS
        );
        this.updater = Executors.newSingleThreadScheduledExecutor(
            new NamedThreads(
                "cacheable-update",
                "async update of expired @Cacheable values"
            )
        );
        this.updater.schedule(
            new VerboseRunnable(
                this::update
            ),
            0L, TimeUnit.SECONDS
        );
    }

    /**
     * Call the method or fetch from cache.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (4 lines)
     */
    @Around("execution(* *(..)) && @annotation(com.jcabi.aspects.Cacheable)")
    public Object cache(final ProceedingJoinPoint point) throws Throwable {
        final Key key = new Key(point);
        Tunnel tunnel;
        final Method method = ((MethodSignature) point.getSignature())
            .getMethod();
        final Cacheable annot = method.getAnnotation(Cacheable.class);
        this.lock.lock();
        try {
            for (final Class<?> before : annot.before()) {
                if (MethodCacher.triggered(before, "flushBefore")) {
                    this.preflush(point);
                }
            }
            tunnel = this.tunnels.get(key);
            if (MethodCacher.isCreateTunnel(tunnel)) {
                tunnel = new Tunnel(
                    point, key, annot.asyncUpdate()
                );
                this.tunnels.put(key, tunnel);
            }
            if (tunnel.expired() && tunnel.asyncUpdate()) {
                this.updatekeys.offer(key);
            }
            for (final Class<?> after : annot.after()) {
                if (MethodCacher.triggered(after, "flushAfter")) {
                    this.postflush(point);
                }
            }
        } finally {
            this.lock.unlock();
        }
        return tunnel.through();
    }

    private static boolean isCreateTunnel(final Tunnel tunnel) {
        return tunnel == null || tunnel.expired() && !tunnel.asyncUpdate();
    }

    private static boolean triggered(final Class<?> trigger, final String name)
        throws NoSuchMethodException, IllegalAccessException,
        InvocationTargetException {
        return Boolean.TRUE.equals(trigger.getMethod(name).invoke(trigger));
    }

    /**
     * Flush cache.
     * @param point Join point
     * @return Value of the method
     * @throws Throwable If something goes wrong inside
     * @since 0.7.14
     * @deprecated Since 0.7.17, and preflush() should be used
     * @checkstyle IllegalThrows (4 lines)
     * @checkstyle MethodsOrderCheck (3 lines)
     */
    @Deprecated
    public Object flush(final ProceedingJoinPoint point) throws Throwable {
        this.preflush(point);
        return point.proceed();
    }

    /**
     * Flush cache.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @since 0.7.14
     * @checkstyle MethodsOrderCheck (3 lines)
     */
    @Before(
        // @checkstyle StringLiteralsConcatenation (3 lines)
        "execution(* *(..))"
        + " && (@annotation(com.jcabi.aspects.Cacheable.Flush)"
        + " || @annotation(com.jcabi.aspects.Cacheable.FlushBefore))"
    )
    public void preflush(final JoinPoint point) {
        this.flush(point, "before the call");
    }

    /**
     * Flush cache after method execution.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @since 0.7.18
     * @checkstyle MethodsOrderCheck (3 lines)
     */
    @After(
        // @checkstyle StringLiteralsConcatenation (2 lines)
        "execution(* *(..))"
        + " && @annotation(com.jcabi.aspects.Cacheable.FlushAfter)"
    )
    public void postflush(final JoinPoint point) {
        this.flush(point, "after the call");
    }

    private void flush(final JoinPoint point, final String when) {
        this.lock.lock();
        try {
            for (final Key key : this.tunnels.keySet()) {
                if (key.sameTarget(point)) {
                    MethodCacher.removal(
                        key, this.tunnels.remove(key), point, when
                    );
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    private static void removal(final Key key,
        final Tunnel tunnel, final JoinPoint point,
        final String when) {
        final Method method = ((MethodSignature) point.getSignature())
            .getMethod();
        if (LogHelper.enabled(key.getLevel(), method.getDeclaringClass())) {
            LogHelper.log(
                key.getLevel(),
                method.getDeclaringClass(),
                "%s: %s:%s removed from cache %s",
                Mnemos.toText(method, point.getArgs(), true, false),
                key,
                tunnel,
                when
            );
        }
    }

    private void clean() {
        this.lock.lock();
        try {
            for (final Map.Entry<Key, Tunnel> entry
                : this.tunnels.entrySet()) {
                final Key key = entry.getKey();
                if (entry.getValue().expired()
                    && !entry.getValue().asyncUpdate()) {
                    LogHelper.log(
                        key.getLevel(),
                        this,
                        "%s:%s expired in cache",
                        key,
                        this.tunnels.remove(key)
                    );
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void update() {
        while (true) {
            try {
                final Key key = this.updatekeys.take();
                final Tunnel tunnel = this.tunnels.get(key);
                if (tunnel != null && tunnel.expired()) {
                    final Tunnel after = tunnel.copy();
                    after.through();
                    this.tunnels.put(key, after);
                }
            } catch (final InterruptedException ex) {
                LogHelper.log(
                    Loggable.ERROR,
                    this,
                    "%s:%s",
                    ex.getMessage(),
                    ex
                );
            // @checkstyle IllegalCatch (1 line)
            } catch (final Throwable ex) {
                LogHelper.log(
                    Loggable.ERROR,
                    this,
                    "Exception message is %s, Exception is %s",
                    ex.getMessage(),
                    ex
                );
            }
        }
    }
}
