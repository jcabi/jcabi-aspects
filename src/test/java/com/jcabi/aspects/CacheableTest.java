/**
 * Copyright (c) 2012-2013, JCabi.com
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

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MethodCacher}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class CacheableTest {

    /**
     * MethodCacher can cache calls.
     * @throws Exception If something goes wrong
     */
    @Test
    public void cachesSimpleCall() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo();
        final String first = foo.get();
        MatcherAssert.assertThat(first, Matchers.equalTo(foo.get()));
        foo.flush();
        MatcherAssert.assertThat(
            foo.get(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    /**
     * MethodCacher can cache static calls.
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
     * MethodCacher can clean cache after timeout.
     * @throws Exception If something goes wrong
     */
    @Test
    public void cleansCacheWhenExpired() throws Exception {
        final CacheableTest.Foo foo = new CacheableTest.Foo();
        final String first = foo.get();
        // @checkstyle MagicNumber (1 line)
        TimeUnit.SECONDS.sleep(3);
        MatcherAssert.assertThat(
            foo.get(),
            Matchers.not(Matchers.equalTo(first))
        );
    }

    /**
     * Dummy class, for tests above.
     */
    private static final class Foo {
        /**
         * Random.
         */
        private static final Random RANDOM = new Random();
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.get().hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object obj) {
            return obj == this;
        }
        /**
         * Download some text.
         * @return Downloaded text
         */
        @Cacheable(lifetime = 1, unit = TimeUnit.SECONDS)
        public String get() {
            return Long.toString(CacheableTest.Foo.RANDOM.nextLong());
        }
        /**
         * Flush it.
         */
        @Cacheable.Flush
        public void flush() {
            // nothing to do
        }
        /**
         * Download some text.
         * @return Downloaded text
         */
        @Cacheable
        public static String staticGet() {
            return Long.toString(CacheableTest.Foo.RANDOM.nextLong());
        }
        /**
         * Flush it.
         */
        @Cacheable.Flush
        public static void staticFlush() {
            // nothing to do
        }
    }

}
