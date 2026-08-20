/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseThreads;
import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Running service.
 * @since 0.0.0
 */
final class Service implements Closeable {

    /**
     * Running scheduled service.
     */
    private final transient ScheduledExecutorService executor;

    /**
     * The object.
     */
    private final transient Object object;

    /**
     * Execution counter.
     */
    private final transient AtomicLong counter;

    /**
     * When started.
     */
    private final transient long start;

    /**
     * How long to wait for the task to finish.
     */
    private final transient long await;

    /**
     * Shutdown attempts count.
     */
    private final transient long attempts;

    /**
     * Should more information be logged?
     */
    private final transient boolean verbose;

    /**
     * Public ctor.
     * @param runnable The runnable to schedule
     * @param obj Object
     * @param annt Annotation
     */
    @SuppressWarnings("FutureReturnValueIgnored")
    Service(final Runnable runnable, final Object obj,
        final ScheduleWithFixedDelay annt) {
        // @checkstyle ConstructorsCodeFreeCheck (30 lines)
        this.start = System.currentTimeMillis();
        this.counter = new AtomicLong();
        this.object = obj;
        this.executor = Executors.newScheduledThreadPool(
            annt.threads(),
            new VerboseThreads(this.object)
        );
        this.verbose = annt.verbose();
        this.await = annt.awaitUnit().toMillis(
            (long) annt.await()
        );
        this.attempts = (long) annt.shutdownAttempts();
        for (int thread = 0; thread < annt.threads(); ++thread) {
            this.executor.scheduleWithFixedDelay(
                () -> {
                    runnable.run();
                    this.counter.incrementAndGet();
                },
                (long) annt.delay(), (long) annt.delay(),
                annt.unit()
            );
        }
        if (this.verbose) {
            Logger.info(
                this.object,
                "scheduled for execution with %d %s interval",
                annt.delay(),
                annt.unit()
            );
        }
    }

    @Override
    public void close() {
        this.executor.shutdown();
        final long begin = System.currentTimeMillis();
        try {
            while (true) {
                if (this.executor.awaitTermination(1L, TimeUnit.SECONDS)) {
                    break;
                }
                final long age = System.currentTimeMillis() - begin;
                if (age > this.await) {
                    break;
                }
                if (this.verbose) {
                    Logger.info(
                        this, "waiting %[ms]s for threads termination", age
                    );
                }
            }
            for (int attempt = 0; attempt < this.attempts; ++attempt) {
                this.executor.shutdownNow();
                this.executor.awaitTermination(1L, TimeUnit.SECONDS);
            }
            if (!this.executor.isTerminated()) {
                throw new IllegalStateException(
                    Logger.format(
                        "failed to shutdown %[type]s of %[type]s",
                        this.executor,
                        this.object
                    )
                );
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        if (this.verbose && Logger.isInfoEnabled(this.object)) {
            Logger.info(
                this.object,
                "execution stopped after %[ms]s and %d tick(s)",
                System.currentTimeMillis() - this.start,
                this.counter.get()
            );
        }
    }
}
