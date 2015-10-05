/**
 * Copyright (c) 2012-2015, jcabi.com
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
import com.jcabi.aspects.Tv;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link Repeater}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.1.10
 */
public final class RepeaterTest {

    /**
     * Repeater should retry a method that fails.
     */
    @Test
    public void retriesAfterFailure() {
        final AtomicInteger calls = new AtomicInteger(0);
        MatcherAssert.assertThat(
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure(attempts = Tv.THREE, verbose = false)
                public Boolean call() {
                    if (calls.get() < Tv.THREE - 1) {
                        calls.incrementAndGet();
                        throw new IllegalStateException();
                    }
                    return true;
                }
            } .call(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(calls.get(), Matchers.equalTo(Tv.THREE - 1));
    }

    /**
     * Repeater should stop method retries if it exceeds a threshold.
     */
    @Test(expected = IllegalStateException.class)
    public void stopsRetryingAfterNumberOfAttempts() {
        final AtomicInteger calls = new AtomicInteger(0);
        new Callable<Boolean>() {
            @Override
            @RetryOnFailure(attempts = Tv.THREE, verbose = false)
            public Boolean call() {
                if (calls.get() < Tv.THREE) {
                    calls.incrementAndGet();
                    throw new IllegalStateException();
                }
                return true;
            }
        } .call();
    }

    /**
     * Repeater should retry if an exception specified to be
     * retried is thrown from the method.
     */
    @Test
    public void onlyRetryExceptionsWhichAreSpecified() {
        final AtomicInteger calls = new AtomicInteger(0);
        MatcherAssert.assertThat(
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure
                    (
                        attempts = Tv.THREE,
                        types = {ArrayIndexOutOfBoundsException.class },
                        verbose = false
                    )
                public Boolean call() {
                    if (calls.get() < Tv.THREE - 1) {
                        calls.incrementAndGet();
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    return true;
                }
            } .call(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(calls.get(), Matchers.equalTo(Tv.THREE - 1));
    }

    /**
     * Repeater should throw the exception if it is
     * not specified to be retried.
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void throwExceptionsWhichAreNotSpecifiedAsRetry() {
        final AtomicInteger calls = new AtomicInteger(0);
        try {
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure
                    (
                        attempts = Tv.THREE,
                        types = {IllegalArgumentException.class },
                        verbose = false
                    )
                public Boolean call() {
                    if (calls.get() < Tv.THREE - 1) {
                        calls.incrementAndGet();
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    return true;
                }
            } .call();
        } finally {
            MatcherAssert.assertThat(calls.get(), Matchers.equalTo(1));
        }
    }

    /**
     * Repeater should retry even if the exception is a
     * subtype of the class specified to be retried.
     */
    @Test
    public void retryExceptionsWhichAreSubTypesOfTheExceptionsSpecified() {
        final AtomicInteger calls = new AtomicInteger(0);
        MatcherAssert.assertThat(
            new Callable<Boolean>() {
                @Override
                @RetryOnFailure
                    (
                        attempts = Tv.THREE,
                        verbose = false,
                        types = {IndexOutOfBoundsException.class }
                    )
                public Boolean call() {
                    if (calls.get() < Tv.THREE - 1) {
                        calls.incrementAndGet();
                        throw new ArrayIndexOutOfBoundsException();
                    }
                    return true;
                }
            } .call(),
            Matchers.equalTo(true)
        );
        MatcherAssert.assertThat(calls.get(), Matchers.equalTo(Tv.THREE - 1));
    }
}
