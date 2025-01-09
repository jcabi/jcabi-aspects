/*
 * Copyright (c) 2012-2025, jcabi.com
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
        MatcherAssert.assertThat(target.finished, Matchers.equalTo(true));
    }

    @Test
    void interruptLongRunningTask() throws Exception {
        final MethodSchedulerTest.LongRun target = new MethodSchedulerTest.LongRun();
        target.close();
        MatcherAssert.assertThat(target.finished, Matchers.equalTo(false));
    }

    /**
     * Short running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(unit = TimeUnit.NANOSECONDS)
    private static class ShortRun implements Runnable, Closeable {

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
     * Long running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(unit = TimeUnit.NANOSECONDS,
        await = 10, awaitUnit = TimeUnit.SECONDS)
    private static class LongRun implements Runnable, Closeable {
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
