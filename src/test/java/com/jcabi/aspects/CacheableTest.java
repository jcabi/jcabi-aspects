/**
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
import org.junit.Test;

/**
 * Test case for {@link Cacheable} annotation and its implementation.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.DoNotUseThreads" })
public final class CacheableTest {

    /**
     * Random.
     * @checkstyle ConstantUsageCheck (3 lines)
     */
    private static final Random RANDOM = new SecureRandom();

    /**
     * Cacheable can cache calls.
     * @throws Exception If something goes wrong
     */
    @Test
    public void cachesSimpleCall() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.get().toString();
        MatcherAssert.assertThat(first, Matchers.equalTo(foo.get().toString()));
        foo.flush();
        MatcherAssert.assertThat(
            foo.get().toString(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    /**
     * Cacheable can Asynchronous update.
     * @throws Exception If something goes wrong
     */
    @Test
    public void asyncUpdateCacheSimpleCall() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.asyncGet().toString();
        MatcherAssert.assertThat(
            first,
            Matchers.equalTo(foo.asyncGet().toString())
        );
        TimeUnit.SECONDS.sleep(2);
        MatcherAssert.assertThat(
            first,
            Matchers.equalTo(foo.asyncGet().toString())
        );
        TimeUnit.SECONDS.sleep(2);
        MatcherAssert.assertThat(
            first,
            Matchers.not(Matchers.equalTo(foo.asyncGet().toString()))
        );
    }

    /**
     * Cacheable can cache static calls.
     * @throws Exception If something goes wrong
     */
    @Test
    public void cachesSimpleStaticCall() throws Exception {
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

    /**
     * Cacheable can clean cache after timeout.
     * @throws Exception If something goes wrong
     */
    @Test
    public void cleansCacheWhenExpired() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final String first = foo.get().toString();
        TimeUnit.SECONDS.sleep((long) Tv.FIVE);
        MatcherAssert.assertThat(
            foo.get().toString(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    /**
     * Cacheable can cache just once.
     * @throws Exception If something goes wrong
     */
    @Test
    public void cachesJustOnceInParallelThreads() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo(1L);
        final Thread never = new Thread(
            new Runnable() {
                @Override
                public void run() {
                    foo.never();
                }
            }
        );
        never.start();
        final Set<String> values = new ConcurrentSkipListSet<String>();
        final int threads = Runtime.getRuntime().availableProcessors() << 1;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final Callable<Boolean> task = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                start.await(1L, TimeUnit.SECONDS);
                values.add(foo.get().toString());
                done.countDown();
                return true;
            }
        };
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            for (int pos = 0; pos < threads; ++pos) {
                executor.submit(task);
            }
            start.countDown();
            done.await((long) Tv.THIRTY, TimeUnit.SECONDS);
            MatcherAssert.assertThat(values.size(), Matchers.equalTo(1));
            never.interrupt();
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Cacheable can flush with a static trigger.
     * @throws Exception If something goes wrong
     */
    @Test
    public void flushesWithStaticTrigger() throws Exception {
        final CacheableTest.Bar bar = new CacheableTest.Bar();
        MatcherAssert.assertThat(
            bar.get(),
            Matchers.not(Matchers.equalTo(bar.get()))
        );
    }

    /**
     * Dummy class, for tests above.
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
        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        @Loggable(Loggable.DEBUG)
        public CacheableTest.Foo get() {
            return new CacheableTest.Foo(CacheableTest.RANDOM.nextLong());
        }

        /**
         * Download some text.
         * @return Downloaded text
         */
        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS, asyncUpdate = true)
        @Loggable(Loggable.DEBUG)
        public CacheableTest.Foo asyncGet() {
            return new CacheableTest.Foo(CacheableTest.RANDOM.nextLong());
        }
        /**
         * Sleep forever, to abuse caching system.
         * @return The same object
         */
        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
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
        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
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
     */
    public static final class Bar {
        /**
         * Get some number.
         * @return The number
         */
        @Cacheable(before = Bar.class)
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
