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
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import org.junit.Test;

/**
 * Test case for {@link MethodLogger}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MethodLoggerTest {

    /**
     * MethodLogger can log simple calls.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsSimpleCall() throws Exception {
        new MethodLoggerTest.Foo().revert("hello");
    }

    /**
     * MethodLogger can ignore toString() methods.
     * @throws Exception If something goes wrong
     */
    @Test
    public void ignoresToStringMethods() throws Exception {
        new MethodLoggerTest.Foo().self();
    }

    /**
     * MethodLogger can log static methods.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsStaticMethods() throws Exception {
        MethodLoggerTest.Foo.text();
    }

    /**
     * Dummy class, for tests above.
     */
    @Loggable(Loggable.DEBUG)
    private static final class Foo {
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "some text";
        }
        /**
         * Get self instance.
         * @return Self
         */
        @Loggable(Loggable.INFO)
        public Foo self() {
            return this;
        }
        /**
         * Static method.
         * @return Some text
         */
        public static String text() {
            return MethodLoggerTest.Foo.hiddenText();
        }
        /**
         * Revert string.
         * @param text Some text
         * @return Reverted text
         */
        @Loggable(value = Loggable.INFO, trim = false)
        public String revert(final String text) {
            return new StringBuffer(text).reverse().toString();
        }
        /**
         * Private static method.
         * @return Some text
         */
        private static String hiddenText() {
            return "some static text";
        }
    }

}
