/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.RetryOnFailure;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Repeater}.
 *
 * @since 0.1.10
 */
final class RepeaterTest {

    @Test
    void retriesAfterFailure() {
        final AtomicInteger calls = new AtomicInteger(0);
        MatcherAssert.assertThat(
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure(verbose = false)
                public Boolean call() {
                    if (calls.get() < 3 - 1) {
                        calls.incrementAndGet();
                        throw new IllegalStateException();
                    }
                    return true;
                }
            } .call(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(calls.get(), Matchers.equalTo(3 - 1));
    }

    @Test
    void stopsRetryingAfterNumberOfAttempts() {
        final AtomicInteger calls = new AtomicInteger(0);
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new Callable<Boolean>() {
                @Override
                @RetryOnFailure(verbose = false)
                public Boolean call() {
                    if (calls.get() < 3) {
                        calls.incrementAndGet();
                        throw new IllegalStateException();
                    }
                    return true;
                }
            } .call()
        );
    }

    @Test
    void onlyRetryExceptionsWhichAreSpecified() {
        final AtomicInteger calls = new AtomicInteger(0);
        MatcherAssert.assertThat(
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure(
                    types = ArrayIndexOutOfBoundsException.class,
                    verbose = false
                )
                public Boolean call() {
                    if (calls.get() < 3 - 1) {
                        calls.incrementAndGet();
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    return true;
                }
            } .call(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(calls.get(), Matchers.equalTo(3 - 1));
    }

    @Test
    void throwExceptionsWhichAreNotSpecifiedAsRetry() {
        final AtomicInteger calls = new AtomicInteger(0);
        try {
            Assertions.assertThrows(
                ArrayIndexOutOfBoundsException.class,
                () -> new Callable<Boolean>() {
                    @Override
                    @RetryOnFailure(types = IllegalArgumentException.class, verbose = false)
                    public Boolean call() {
                        if (calls.get() < 3 - 1) {
                            calls.incrementAndGet();
                            throw new ArrayIndexOutOfBoundsException();
                        }
                        return true;
                    }
                } .call()
            );
        } finally {
            MatcherAssert.assertThat(calls.get(), Matchers.equalTo(1));
        }
    }

    @Test
    void retryExceptionsWhichAreSubTypesOfTheExceptionsSpecified() {
        final AtomicInteger calls = new AtomicInteger(0);
        MatcherAssert.assertThat(
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure(verbose = false, types = IndexOutOfBoundsException.class)
                public Boolean call() {
                    if (calls.get() < 3 - 1) {
                        calls.incrementAndGet();
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    return true;
                }
            } .call(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(calls.get(), Matchers.equalTo(3 - 1));
    }
}
