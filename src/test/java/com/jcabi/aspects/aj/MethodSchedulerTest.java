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
package com.jcabi.aspects.aj;

import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.aspects.Tv;
import java.io.Closeable;
import java.io.IOException;
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
public final class MethodSchedulerTest {
    /**
     * MethodScheduler should wait for the task to finish.
     * @throws Exception When there is a problem.
     */
    @Test
    public void shortRunningTaskShouldBeAllowedToFinish() throws Exception {
        final ShortRun target = new ShortRun();
        TimeUnit.SECONDS.sleep((long) Tv.FIVE);
        target.close();
        MatcherAssert.assertThat(target.finished, Matchers.equalTo(true));
    }

    /**
     * MethodScheduler should interrupt long running task.
     * @throws Exception When there is a problem.
     */
    @Test
    public void interruptLongRunningTask() throws Exception {
        final LongRun target = new LongRun();
        target.close();
        MatcherAssert.assertThat(target.finished, Matchers.equalTo(false));
    }

    /**
     * Short running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.NANOSECONDS)
    private static class ShortRun implements Runnable, Closeable {

        /**
         * Have we finished?
         */
        private transient boolean finished;

        @Override
        public void run() {
            try {
                TimeUnit.SECONDS.sleep(1);
                this.finished = true;
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    /**
     * Long running task.
     * @since 0.7.22
     */
    @ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.NANOSECONDS,
        await = Tv.TEN, awaitUnit = TimeUnit.SECONDS)
    private static class LongRun implements Runnable, Closeable {
        /**
         * Have we finished?
         */
        private transient boolean finished;

        @Override
        public void run() {
            try {
                // @checkstyle MagicNumber (1 line)
                TimeUnit.SECONDS.sleep(30);
                this.finished = true;
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
