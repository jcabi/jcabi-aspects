/*
 * Copyright (c) 2012-2017, jcabi.com
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
public final class ScheduleWithFixedDelayTest {

    /**
     * ScheduleWithFixedDelay can run routine operations.
     * @throws Exception If something goes wrong
     */
    @Test
    public void runsRoutineOperations() throws Exception {
        final AtomicLong counter = new AtomicLong();
        final ScheduleWithFixedDelayTest.Sample sample =
            new ScheduleWithFixedDelayTest.Sample(counter);
        TimeUnit.SECONDS.sleep(1L);
        MatcherAssert.assertThat(counter.get(), Matchers.greaterThan(0L));
        sample.close();
        TimeUnit.MILLISECONDS.sleep(Tv.TEN);
        MatcherAssert.assertThat(counter.get(), Matchers.lessThan(0L));
    }

    /**
     * ScheduleWithFixedDelay can stop a scheduled run before the first
     * execution occurs.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void canStopBeforeFirstScheduledRun() throws Exception {
        final AtomicLong counter = new AtomicLong();
        final ScheduleWithFixedDelayTest.LongDelaySample sample =
            new ScheduleWithFixedDelayTest.LongDelaySample(counter);
        sample.close();
        TimeUnit.MILLISECONDS.sleep(Tv.HUNDRED);
        MatcherAssert.assertThat(counter.get(), Matchers.is(0L));
    }

    /**
     * Sample annotated class.
     * @since 0.0.0
     */
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MILLISECONDS)
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
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MINUTES)
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
