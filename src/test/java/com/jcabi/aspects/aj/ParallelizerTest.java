/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Parallel;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Parallelizer}.
 *
 * @since 0.10
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class ParallelizerTest {
    @Test
    void executesInParallel() {
        final AtomicInteger count = new AtomicInteger(10);
        new Runnable() {
            @Override
            @Parallel(threads = 10)
            public void run() {
                count.decrementAndGet();
            }
        } .run();
        MatcherAssert.assertThat("should equals to 0", count.get(), Matchers.equalTo(0));
    }

    @Test
    void throwsCaughtException() {
        Assertions.assertThrows(
            Exception.class,
            () -> new Runnable() {
                @Override
                @Parallel(threads = 10)
                public void run() {
                    throw new IllegalArgumentException();
                }
            }.run()
        );
    }
}
