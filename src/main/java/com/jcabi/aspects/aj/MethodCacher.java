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

import com.jcabi.aspects.Cacheable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Cache method results.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Aspect
@SuppressWarnings("PMD.DoNotUseThreads")
public final class MethodCacher {

    /**
     * Calling tunnels.
     * @checkstyle LineLength (2 lines)
     */
    private final transient ConcurrentMap<MethodCacher.Key, MethodCacher.Tunnel> tunnels =
        new ConcurrentHashMap<MethodCacher.Key, MethodCacher.Tunnel>();

    /**
     * Service that cleans cache.
     */
    private final transient ScheduledExecutorService cleaner =
        Executors.newScheduledThreadPool(1, new VerboseThreads());

    /**
     * Public ctor.
     */
    public MethodCacher() {
        this.cleaner.schedule(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        MethodCacher.this.clean();
                    }
                }
            ),
            1, TimeUnit.MINUTES
        );
    }

    /**
     * Catch exception and re-call the method.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     * @checkstyle LineLength (3 lines)
     */
    @Around("(execution(* *(..)) || initialization(*.new(..))) && @annotation(com.jcabi.aspects.Cacheable)")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Key key = new MethodCacher.Key(point);
        Tunnel tunnel;
        synchronized (this.tunnels) {
            tunnel = this.tunnels.get(key);
            if (tunnel == null || tunnel.expired()) {
                tunnel = new MethodCacher.Tunnel(point, key);
                this.tunnels.put(key, tunnel);
            }
        }
        synchronized (tunnel) {
            return tunnel.through();
        }
    }

    /**
     * Clean cache.
     */
    private void clean() {
        synchronized (this.tunnels) {
            for (Key key : this.tunnels.keySet()) {
                if (this.tunnels.get(key).expired()) {
                    this.tunnels.remove(key);
                }
            }
        }
    }

    /**
     * Immutable caching/calling tunnel.
     */
    private static final class Tunnel {
        /**
         * Cached value.
         */
        private final transient Object cached;
        /**
         * Key related to this tunnel.
         */
        private final transient Key key;
        /**
         * When will it expire (moment in time).
         */
        private final transient long lifetime;
        /**
         * Public ctor.
         * @param point Joint point
         * @param akey The key related to it
         * @throws Throwable If something goes wrong inside
         * @checkstyle IllegalThrows (5 lines)
         */
        public Tunnel(final ProceedingJoinPoint point, final Key akey)
            throws Throwable {
            this.cached = point.proceed();
            this.key = akey;
            final Cacheable annot = MethodSignature.class
                .cast(point.getSignature())
                .getMethod()
                .getAnnotation(Cacheable.class);
            if (annot.forever()) {
                this.lifetime = Long.MAX_VALUE;
            } else {
                this.lifetime = System.currentTimeMillis()
                    + annot.unit().toMillis(annot.lifetime());
            }
        }
        /**
         * Get a result through the tunnel.
         * @return The result
         */
        public Object through() {
            return this.key.through(this.cached);
        }
        /**
         * Is it expired already?
         * @return TRUE if expired
         */
        public boolean expired() {
            return this.lifetime < System.currentTimeMillis();
        }
    }

    /**
     * Key of a callable target.
     */
    private static final class Key {
        /**
         * How many times the key was already accessed.
         */
        private final transient AtomicInteger accessed = new AtomicInteger();
        /**
         * Method.
         */
        private final transient Method method;
        /**
         * Object callable.
         */
        private final transient Object object;
        /**
         * Arguments.
         */
        private final transient Object[] arguments;
        /**
         * Public ctor.
         * @param point Joint point
         */
        public Key(final ProceedingJoinPoint point) {
            this.method = MethodSignature.class
                .cast(point.getSignature()).getMethod();
            this.object = point.getTarget();
            this.arguments = point.getArgs();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.method.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            boolean equals;
            if (this == obj) {
                equals = true;
            } else if (obj instanceof MethodCacher.Key) {
                final MethodCacher.Key key = MethodCacher.Key.class.cast(obj);
                equals = key.method.equals(this.method)
                    && (
                        (key.object == null && this.object == null)
                        || this.object.equals(key.object)
                    )
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
        public Object through(final Object result) {
            if (Logger.isDebugEnabled(this)) {
                Logger.debug(
                    this,
                    "%s: %s from cache (hit #%d)",
                    Mnemos.toString(this.method),
                    Mnemos.toString(result),
                    this.accessed.incrementAndGet()
                );
            }
            return result;
        }
    }

}
