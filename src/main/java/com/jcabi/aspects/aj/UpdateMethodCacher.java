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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseRunnable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * For updating cache and cleaning expired cache.
 * @author Peng Chuanjiang (pengchuanjiang@souyidai.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class UpdateMethodCacher {

    /**
     * Calling tunnels.
     */
    private final transient
        ConcurrentMap<MethodCacher.Key, MethodCacher.Tunnel> tunnels;

    /**
     * Service that cleans cache.
     */
    private final transient ScheduledExecutorService cleaner;

    /**
     * Save the keys of caches which need update.
     */
    private final transient BlockingQueue<MethodCacher.Key> updatekeys;

    /**
     * Service that update cache.
     */
    private final transient ScheduledExecutorService updater;

    /**
     * Public ctor.
     * @param tls ConcurrentMap
     * @param ukeys BlockingQueue
     */
    public UpdateMethodCacher(
        final ConcurrentMap<MethodCacher.Key, MethodCacher.Tunnel> tls,
        final BlockingQueue<MethodCacher.Key> ukeys) {
        this.tunnels = tls;
        this.updatekeys = ukeys;
        this.cleaner = Executors.newSingleThreadScheduledExecutor(
            new NamedThreads(
                "cacheable-clean",
                "automated cleaning of expired @Cacheable values"
            )
        );
        this.updater = Executors.newSingleThreadScheduledExecutor(
            new NamedThreads(
                "cacheable-update",
                "async update of expired @Cacheable values"
            )
        );
    }

    /**
     * Staring two threads(cheaner and updater) are used for
     * updating cache and cleaning expired cache.
     */
    public void start() {
        this.cleaner.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        UpdateMethodCacher.this.clean();
                    }
                }
            ),
            1L, 1L, TimeUnit.SECONDS
        );
        this.updater.schedule(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        UpdateMethodCacher.this.update();
                    }
                }
            ),
            0L, TimeUnit.SECONDS
        );
    }

    /**
     * Clean cache.
     */
    private void clean() {
        synchronized (this.tunnels) {
            for (final MethodCacher.Key key : this.tunnels.keySet()) {
                if (this.tunnels.get(key).expired()
                    && !this.tunnels.get(key).asyncUpdate()) {
                    final MethodCacher.Tunnel tunnel = this.tunnels.remove(key);
                    LogHelper.log(
                        key.getLevel(),
                        this,
                        "%s:%s expired in cache",
                        key,
                        tunnel
                    );
                }
            }
        }
    }

    /**
     * Update cache.
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void update() {
        while (true) {
            try {
                final MethodCacher.Key key = this.updatekeys.take();
                final MethodCacher.Tunnel tunnel = this.tunnels.get(key);
                if (tunnel != null && tunnel.expired()) {
                    final MethodCacher.Tunnel newTunnel = tunnel.copy();
                    newTunnel.through();
                    this.tunnels.put(key, newTunnel);
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
