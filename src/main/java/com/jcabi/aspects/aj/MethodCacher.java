/**
 * Copyright (c) 2012-2015, jcabi.com
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

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Aspect
@SuppressWarnings("PMD.TooManyMethods")
public final class MethodCacher {

    /**
     * Calling tunnels.
     */
    private final transient
        ConcurrentMap<MethodCacher.Key, MethodCacher.Tunnel> tunnels;

    /**
     * Save the keys of caches which need update.
     */
    private final transient BlockingQueue<Key> updatekeys;

    /**
     * Public ctor.
     */
    public MethodCacher() {
        this.tunnels =
            new ConcurrentHashMap<MethodCacher.Key, MethodCacher.Tunnel>(0);
        this.updatekeys =
            new LinkedBlockingQueue<MethodCacher.Key>();
        new UpdateMethodCacher(this.tunnels, this.updatekeys).start();
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
        final MethodCacher.Key key = new MethodCacher.Key(point);
        MethodCacher.Tunnel tunnel;
        final Method method = MethodSignature.class
            .cast(point.getSignature())
            .getMethod();
        final Cacheable annot = method.getAnnotation(Cacheable.class);
        synchronized (this.tunnels) {
            for (final Class<?> before : annot.before()) {
                final boolean flag = Boolean.class.cast(
                    before.getMethod("flushBefore").invoke(method.getClass())
                );
                if (flag) {
                    this.preflush(point);
                }
            }
            tunnel = this.tunnels.get(key);
            if (this.isCreateTunnel(tunnel)) {
                tunnel = new MethodCacher.Tunnel(
                    point, key, annot.asyncUpdate()
                );
                this.tunnels.put(key, tunnel);
            }
            if (tunnel.expired() && tunnel.asyncUpdate()) {
                this.updatekeys.offer(key);
            }
            for (final Class<?> after : annot.after()) {
                final boolean flag = Boolean.class.cast(
                    after.getMethod("flushAfter").invoke(method.getClass())
                );
                if (flag) {
                    this.postflush(point);
                }
            }
        }
        return tunnel.through();
    }

    /**
     * Flush cache.
     * @param point Join point
     * @return Value of the method
     * @since 0.7.14
     * @deprecated Since 0.7.17, and preflush() should be used
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (3 lines)
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
     */
    @Before
        (
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
     */
    @After
        (
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "execution(* *(..))"
            + " && @annotation(com.jcabi.aspects.Cacheable.FlushAfter)"
        )
    public void postflush(final JoinPoint point) {
        this.flush(point, "after the call");
    }

    /**
     * Flush cache.
     * @param point Joint point
     * @param when When it happens
     * @since 0.7.18
     */
    private void flush(final JoinPoint point, final String when) {
        synchronized (this.tunnels) {
            for (final MethodCacher.Key key : this.tunnels.keySet()) {
                if (!key.sameTarget(point)) {
                    continue;
                }
                final MethodCacher.Tunnel removed = this.tunnels.remove(key);
                final Method method = MethodSignature.class
                    .cast(point.getSignature())
                    .getMethod();
                if (LogHelper.enabled(
                    key.getLevel(), method.getDeclaringClass()
                )) {
                    LogHelper.log(
                        key.getLevel(),
                        method.getDeclaringClass(),
                        "%s: %s:%s removed from cache %s",
                        Mnemos.toText(method, point.getArgs(), true, false),
                        key,
                        removed,
                        when
                    );
                }
            }
        }
    }

    /**
     * Whether create a new Tunnel.
     * @param tunnel MethodCacher.Tunnel
     * @return Boolean
     */
    private boolean isCreateTunnel(final MethodCacher.Tunnel tunnel) {
        return tunnel == null || (tunnel.expired() && !tunnel.asyncUpdate());
    }

    /**
     * Mutable caching/calling tunnel, it is thread-safe.
     */
    protected static final class Tunnel {
        /**
         * Proceeding join point.
         */
        private final transient ProceedingJoinPoint point;
        /**
         * Key related to this tunnel.
         */
        private final transient MethodCacher.Key key;
        /**
         * Whether asynchronous update.
         */
        private final transient boolean async;
        /**
         * Was it already executed?
         */
        private transient boolean executed;
        /**
         * When will it expire (moment in time).
         */
        private transient long lifetime;
        /**
         * Has non-null result?
         */
        private transient boolean hasresult;
        /**
         * Cached value.
         */
        @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
        private transient SoftReference<Object> cached;

        /**
         * Public ctor.
         * @param pnt ProceedingJoinPoint
         * @param akey MethodCacher.Key
         * @param asy Boolean
         */
        Tunnel(final ProceedingJoinPoint pnt,
            final MethodCacher.Key akey, final boolean asy) {
            this.point = pnt;
            this.key = akey;
            this.async = asy;
        }

        @Override
        public String toString() {
            return Mnemos.toText(this.cached.get(), true, false);
        }

        /**
         * Get a new instance.
         * @return MethodCacher.Tunnel
         */
        public Tunnel copy() {
            return new Tunnel(
                this.point, this.key, this.async
            );
        }
        /**
         * Get a result through the tunnel.
         * @return The result
         * @throws Throwable If something goes wrong inside
         * @checkstyle IllegalThrows (5 lines)
         */
        @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
        public synchronized Object through() throws Throwable {
            if (!this.executed) {
                final long start = System.currentTimeMillis();
                final Object result = this.point.proceed();
                this.hasresult = result != null;
                this.cached = new SoftReference<Object>(result);
                final Method method = MethodSignature.class
                    .cast(this.point.getSignature())
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
                        Mnemos.toText(this.cached.get(), true, false),
                        System.currentTimeMillis() - start,
                        suffix
                    );
                }
                this.executed = true;
            }
            return this.key.through(this.cached.get());
        }
        /**
         * Is it expired already?
         * @return TRUE if expired
         */
        public boolean expired() {
            final boolean expired = this.lifetime < System.currentTimeMillis();
            final boolean collected = this.executed
                    && this.hasresult
                    && this.cached.get() == null;
            return this.executed && (expired || collected);
        }

        /**
         * Whether asynchronous update.
         * @return TRUE if asynchronous update
         */
        public boolean asyncUpdate() {
            return this.async;
        }

        /**
         * Soft reference to cached object.
         * Visible only for testing. Do not use directly.
         * @return Soft reference to cached object.
         */
        @SuppressWarnings("PMD.DefaultPackage")
        SoftReference<Object> cached() {
            return this.cached;
        }
    }

    /**
     * Key of a callable target.
     * @checkstyle DesignForExtensionCheck (2 lines)
     */
    protected static class Key {
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
            this.start = System.currentTimeMillis();
            this.accessed = new AtomicInteger();
            this.method = MethodSignature.class
                .cast(point.getSignature()).getMethod();
            this.target = MethodCacher.Key.targetize(point);
            this.arguments = point.getArgs();
            if (this.method.isAnnotationPresent(Loggable.class)) {
                this.level = this.method.getAnnotation(Loggable.class).value();
            } else {
                this.level = Loggable.DEBUG;
            }
        }

        /**
         * Get log level.
         * @return Log level of current method.
         */
        public final int getLevel() {
            return this.level;
        }

        @Override
        public final String toString() {
            return Mnemos.toText(this.method, this.arguments, true, false);
        }
        @Override
        public final int hashCode() {
            return this.method.hashCode();
        }
        @Override
        public final boolean equals(final Object obj) {
            final boolean equals;
            if (this == obj) {
                equals = true;
            } else if (obj instanceof MethodCacher.Key) {
                final MethodCacher.Key key = MethodCacher.Key.class.cast(obj);
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
         * @checkstyle DesignForExtensionCheck (2 lines)
         */
        public Object through(final Object result) {
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
        public final boolean sameTarget(final JoinPoint point) {
            return MethodCacher.Key.targetize(point).equals(this.target);
        }
        /**
         * Calculate its target.
         * @param point Proceeding point
         * @return The target
         */
        private static Object targetize(final JoinPoint point) {
            final Object tgt;
            final Method method = MethodSignature.class
                .cast(point.getSignature()).getMethod();
            if (Modifier.isStatic(method.getModifiers())) {
                tgt = method.getDeclaringClass();
            } else {
                tgt = point.getTarget();
            }
            return tgt;
        }

    }
}
