/*
 * Copyright (c) 2012-2022, jcabi.com
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
