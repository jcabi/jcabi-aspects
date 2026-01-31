/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MethodScheduler}.
 *
 * @since 0.7.22
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class MethodSchedulerTest {
    @Test
    void shortRunningTaskShouldBeAllowedToFinish() throws Exception {
        final MethodSchedulerTest.ShortRun target = new MethodSchedulerTest.ShortRun();
        TimeUnit.SECONDS.sleep(5);
        target.close();
        MatcherAssert.assertThat("should be true", target.finished, Matchers.equalTo(true));
    }

    @Test
    void interruptLongRunningTask() throws Exception {
        final MethodSchedulerTest.LongRun target = new MethodSchedulerTest.LongRun();
        target.close();
        MatcherAssert.assertThat("should be false", target.finished, Matchers.equalTo(false));
    }

    /**
     * Short running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(unit = TimeUnit.NANOSECONDS)
    final private static class ShortRun implements Runnable, Closeable {

        /**
         * Have we finished?
         */
        private transient boolean finished;

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(1L);
                this.finished = true;
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
     * Long-running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(unit = TimeUnit.NANOSECONDS, await = 10, awaitUnit = TimeUnit.SECONDS)
    final private static class LongRun implements Runnable, Closeable {
        /**
         * Have we finished?
         */
        private transient boolean finished;

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(30L);
                this.finished = true;
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
