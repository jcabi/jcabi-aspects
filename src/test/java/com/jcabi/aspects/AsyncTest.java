/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Async} annotation and its implementation.
 * @since 0.0.0
 */
final class AsyncTest {

    /**
     * Thread name matcher.
     */
    private static final Matcher<String> THREAD_NAME = Matchers.allOf(
        Matchers.not(Thread.currentThread().getName()),
        Matchers.startsWith("jcabi-async")
    );

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    void executesAsynchronously() throws Exception {
        final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        final Runnable runnable = new Runnable() {
            @Async
            @Override
            public void run() {
                queue.offer(Thread.currentThread().getName());
            }
        };
        runnable.run();
        // @checkstyle MultipleStringLiterals (5 lines)
        MatcherAssert.assertThat(
            queue.poll(30, TimeUnit.SECONDS),
            AsyncTest.THREAD_NAME
        );
    }

    @Test
    void returnsFutureValue() throws Exception {
        MatcherAssert.assertThat(
            new AsyncTest.Foo().asyncMethodWithReturnValue()
                .get(5, TimeUnit.MINUTES),
            AsyncTest.THREAD_NAME
        );
    }

    @Test
    void throwsWhenMethodDoesNotReturnVoidOrFuture() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new AsyncTest.Foo().asyncMethodThatReturnsInt()
        );
    }

    /**
     * Dummy class for test purposes.
     * @since 0.0.0
     */
    private static final class Foo {

        /**
         * Async method that returns a Future containing the thread name.
         * @return The future.
         */
        @Async
        public Future<String> asyncMethodWithReturnValue() {
            // @checkstyle AnonInnerLength (23 lines)
            return new Future<String>() {

                @Override
                public boolean cancel(final boolean interruptible) {
                    return false;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public boolean isDone() {
                    return true;
                }

                @Override
                public String get() {
                    return Thread.currentThread().getName();
                }

                @Override
                public String get(final long timeout, final TimeUnit unit) {
                    return Thread.currentThread().getName();
                }
            };
        }

        /**
         * Async method that does not return void or Future. Should throw
         * exception.
         * @return An int value
         */
        @Async
        public int asyncMethodThatReturnsInt() {
            return 0;
        }
    }

}
