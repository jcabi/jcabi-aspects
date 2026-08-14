/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MethodScheduler}.
 * @since 0.7.22
 */
@SuppressWarnings("PMD.CloseResource")
final class MethodSchedulerTest {

    @Test
    void shortRunningTaskShouldBeAllowedToFinish() throws Exception {
        final AtomicBoolean finished = new AtomicBoolean();
        final MethodSchedulerTest.ShortRun target =
            new MethodSchedulerTest.ShortRun(finished);
        TimeUnit.SECONDS.sleep(5L);
        target.close();
        MatcherAssert.assertThat(
            "short running task cannot be cut short",
            finished.get(),
            Matchers.equalTo(true)
        );
    }

    @Test
    void interruptLongRunningTask() {
        final AtomicBoolean finished = new AtomicBoolean();
        final MethodSchedulerTest.LongRun target =
            new MethodSchedulerTest.LongRun(finished);
        target.close();
        MatcherAssert.assertThat(
            "long running task cannot outlive its close",
            finished.get(),
            Matchers.equalTo(false)
        );
    }

    /**
     * Short running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(unit = TimeUnit.NANOSECONDS)
    private static final class ShortRun implements Runnable, Closeable {

        /**
         * Have we finished?
         */
        private final transient AtomicBoolean finished;

        /**
         * Ctor.
         * @param flag Flag to raise when finished
         */
        ShortRun(final AtomicBoolean flag) {
            this.finished = flag;
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(1L);
                this.finished.set(true);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() {
            // do nothing
        }
    }

    /**
     * Long running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(
        unit = TimeUnit.NANOSECONDS,
        await = 10, awaitUnit = TimeUnit.SECONDS
    )
    private static final class LongRun implements Runnable, Closeable {

        /**
         * Have we finished?
         */
        private final transient AtomicBoolean finished;

        /**
         * Ctor.
         * @param flag Flag to raise when finished
         */
        LongRun(final AtomicBoolean flag) {
            this.finished = flag;
        }

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(30L);
                this.finished.set(true);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() {
            // do nothing
        }
    }
}
