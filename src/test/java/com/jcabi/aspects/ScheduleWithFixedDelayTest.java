/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ScheduleWithFixedDelay} annotation
 * and its implementation.
 * @since 0.0.0
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class ScheduleWithFixedDelayTest {

    @Test
    void runsRoutineOperations() throws Exception {
        final AtomicLong counter = new AtomicLong();
        final ScheduleWithFixedDelayTest.Sample sample =
            new ScheduleWithFixedDelayTest.Sample(counter);
        TimeUnit.SECONDS.sleep(1L);
        MatcherAssert.assertThat(counter.get(), Matchers.greaterThan(0L));
        sample.close();
        TimeUnit.MILLISECONDS.sleep(10);
        MatcherAssert.assertThat(counter.get(), Matchers.lessThan(0L));
    }

    @Test
    void canStopBeforeFirstScheduledRun() throws Exception {
        final AtomicLong counter = new AtomicLong();
        final ScheduleWithFixedDelayTest.LongDelaySample sample =
            new ScheduleWithFixedDelayTest.LongDelaySample(counter);
        sample.close();
        TimeUnit.MILLISECONDS.sleep(100);
        MatcherAssert.assertThat(counter.get(), Matchers.is(0L));
    }

    /**
     * Sample annotated class.
     * @since 0.0.0
     */
    @ScheduleWithFixedDelay(unit = TimeUnit.MILLISECONDS)
    private static final class Sample implements Runnable, Closeable {

        /**
         * Encapsulated counter.
         */
        private final transient AtomicLong counter;

        /**
         * Public ctor.
         * @param cnt Counter to encapsulate
         */
        Sample(final AtomicLong cnt) {
            this.counter = cnt;
        }

        @Override
        public void run() {
            this.counter.addAndGet(1L);
        }

        @Override
        public void close() {
            this.counter.set(-1L);
        }
    }

    /**
     * Sample class with long delay.
     * @since 0.0.0
     */
    @ScheduleWithFixedDelay()
    private static final class LongDelaySample implements Runnable, Closeable {

        /**
         * Encapsulated counter.
         */
        private final transient AtomicLong counter;

        /**
         * Public ctor.
         * @param cnt Counter to encapsulate
         */
        LongDelaySample(final AtomicLong cnt) {
            this.counter = cnt;
        }

        @Override
        public void run() {
            this.counter.addAndGet(1L);
        }

        @Override
        public void close() {
            // Nothing to do.
        }
    }

}
