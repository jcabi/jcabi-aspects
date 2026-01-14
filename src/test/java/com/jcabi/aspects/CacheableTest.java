/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Cacheable} annotation and its implementation.
 * @since 0.0.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings
    (
        {
            "PMD.TooManyMethods",
            "PMD.DoNotUseThreads",
            "PMD.ProhibitPublicStaticMethods"
        }
    )
final class CacheableTest {

    /**
     * Random.
     * @checkstyle ConstantUsageCheck (3 lines)
     */
    private static final Random RANDOM = new SecureRandom();

    @Test
    void cachesSimpleCall() {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.get().toString();
        MatcherAssert.assertThat(first, Matchers.equalTo(foo.get().toString()));
        foo.flush();
        MatcherAssert.assertThat(
            foo.get().toString(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    @Test
    @Disabled
    void asyncUpdateCacheSimpleCall() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.asyncGet().toString();
        MatcherAssert.assertThat(
            first,
            Matchers.equalTo(foo.asyncGet().toString())
        );
        TimeUnit.SECONDS.sleep(2L);
        MatcherAssert.assertThat(
            first,
            Matchers.equalTo(foo.asyncGet().toString())
        );
        TimeUnit.SECONDS.sleep(2L);
        MatcherAssert.assertThat(
            first,
            Matchers.not(Matchers.equalTo(foo.asyncGet().toString()))
        );
    }

    @Test
    void cachesSimpleStaticCall() {
        final String first = CacheableTest.Foo.staticGet();
        MatcherAssert.assertThat(
            first,
            Matchers.equalTo(CacheableTest.Foo.staticGet())
        );
        CacheableTest.Foo.staticFlush();
        MatcherAssert.assertThat(
            CacheableTest.Foo.staticGet(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    @Test
    void cleansCacheWhenExpired() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.get().toString();
        TimeUnit.SECONDS.sleep(5);
        MatcherAssert.assertThat(
            foo.get().toString(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    @Test
    void cachesJustOnceInParallelThreads() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final Thread never = new Thread(foo::never);
        never.start();
        final Set<String> values = new ConcurrentSkipListSet<>();
        final int threads = Runtime.getRuntime().availableProcessors() << 1;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final Callable<?> task = () -> {
            start.await(1L, TimeUnit.SECONDS);
            values.add(foo.get().toString());
            done.countDown();
            return null;
        };
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            for (int pos = 0; pos < threads; ++pos) {
                executor.submit(task);
            }
            start.countDown();
            done.await(30, TimeUnit.SECONDS);
            MatcherAssert.assertThat(values.size(), Matchers.equalTo(1));
            never.interrupt();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void flushesWithStaticTrigger() {
        final CacheableTest.Bar bar = new CacheableTest.Bar();
        MatcherAssert.assertThat(
            bar.get(),
            Matchers.not(Matchers.equalTo(bar.get()))
        );
    }

    /**
     * Dummy class, for tests above.
     * @since 0.0.0
     */
    private static final class Foo {

        /**
         * Encapsulated long.
         */
        private final transient long number;

        /**
         * Public ctor.
         * @param num Number to encapsulate
         */
        Foo(final long num) {
            this.number = num;
        }

        @Override
        public int hashCode() {
            return this.get().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj == this;
        }

        @Override
        @Cacheable(forever = true)
        @Loggable(Loggable.DEBUG)
        public String toString() {
            return Long.toString(this.number);
        }

        /**
         * Download some text.
         * @return Downloaded text
         */
        @Cacheable(unit = TimeUnit.SECONDS)
        @Loggable(Loggable.DEBUG)
        public CacheableTest.Foo get() {
            return new CacheableTest.Foo(CacheableTest.RANDOM.nextLong());
        }

        /**
         * Download some text.
         * @return Downloaded text
         */
        @Cacheable(unit = TimeUnit.SECONDS, asyncUpdate = true)
        @Loggable(Loggable.DEBUG)
        public CacheableTest.Foo asyncGet() {
            return new CacheableTest.Foo(CacheableTest.RANDOM.nextLong());
        }

        /**
         * Sleep forever, to abuse caching system.
         * @return The same object
         */
        @Cacheable(unit = TimeUnit.SECONDS)
        @Loggable(Loggable.DEBUG)
        public CacheableTest.Foo never() {
            try {
                TimeUnit.HOURS.sleep(1L);
            } catch (final InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
            return this;
        }

        /**
         * Flush it.
         */
        @Cacheable.FlushBefore
        public void flush() {
            // nothing to do
        }

        /**
         * Download some text.
         * @return Downloaded text
         */
        @Cacheable(unit = TimeUnit.SECONDS)
        public static String staticGet() {
            return Long.toString(CacheableTest.RANDOM.nextLong());
        }

        /**
         * Flush it.
         */
        @Cacheable.FlushBefore
        public static void staticFlush() {
            // nothing to do
        }
    }

    /**
     * Dummy class, for tests above.
     * @since 0.0.0
     */
    public static final class Bar {
        /**
         * Get some number.
         * @return The number
         */
        @Cacheable(before = CacheableTest.Bar.class)
        public long get() {
            return CacheableTest.RANDOM.nextLong();
        }

        /**
         * Flush before?
         * @return TRUE if flush is required
         */
        public static boolean flushBefore() {
            return true;
        }
    }

}
