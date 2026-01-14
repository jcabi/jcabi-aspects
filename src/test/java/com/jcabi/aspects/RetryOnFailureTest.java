/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link RetryOnFailure} annotation and its implementation.
 * @since 0.0.0
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class RetryOnFailureTest {

    @Test
    void executesMethodManyTimes() {
        final AtomicInteger count = new AtomicInteger();
        new Runnable() {
            @Override
            @RetryOnFailure(verbose = false, unit = TimeUnit.SECONDS, delay = 1)
            public void run() {
                if (count.incrementAndGet() < 2) {
                    throw new IllegalArgumentException(
                        "this exception should be caught and swallowed"
                    );
                }
            }
        } .run();
        MatcherAssert.assertThat(count.get(), Matchers.greaterThan(0));
    }

    @Test
    void retriesOnError() {
        final AtomicInteger count = new AtomicInteger();
        new Runnable() {
            @Override
            @RetryOnFailure(verbose = false, unit = TimeUnit.SECONDS, delay = 1)
            public void run() {
                if (count.incrementAndGet() < 2) {
                    throw new AssertionError("Should be caught and ignored.");
                }
            }
        } .run();
        MatcherAssert.assertThat(count.get(), Matchers.greaterThan(0));
    }

}
