/**
 * Copyright (c) 2012-2014, jcabi.com
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Loggable} annotation and its implementation.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings({ "PMD.TestClassWithoutTestCases", "PMD.TooManyMethods" })
public final class LoggableTest {

    /**
     * Loggable can log simple calls.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsSimpleCall() throws Exception {
        new LoggableTest.Foo().revert("hello");
    }

    /**
     * Loggable can ignore toString() methods.
     * @throws Exception If something goes wrong
     */
    @Test
    public void ignoresToStringMethods() throws Exception {
        new LoggableTest.Foo().self();
    }

    /**
     * Loggable can log static methods.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsStaticMethods() throws Exception {
        LoggableTest.Foo.text();
    }

    /**
     * Loggable can ignore inherited methods.
     * @throws Exception If something goes wrong
     */
    @Test
    public void doesntLogInheritedMethods() throws Exception {
        new LoggableTest.Foo().parentText();
    }

    /**
     * Loggable can ignore some exceptions.
     * @throws Exception If something goes wrong
     */
    @Test(expected = IllegalStateException.class)
    public void ignoresSomeExceptions() throws Exception {
        new LoggableTest.Foo().doThrow();
    }

    /**
     * Loggable can log duration with a specific time unit.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsDurationWithSpecifiedTimeUnit() throws Exception {
        final StringWriter writer = new StringWriter();
        org.apache.log4j.Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        LoggableTest.Foo.logsDurationInSeconds();
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.anyOf(
                Matchers.containsString("in 2s"),
                Matchers.containsString("in 3s"),
                Matchers.containsString("in 4s")
            )
        );
    }

    /**
     * Parent class, without logging.
     */
    private static class Parent {
        /**
         * Get some text.
         * @return The text
         */
        public String parentText() {
            return "some parent text";
        }
    }

    /**
     * Dummy class, for tests above.
     */
    @Loggable(
        value = Loggable.DEBUG,
        prepend = true,
        limit = 1, unit = TimeUnit.MILLISECONDS
    )
    private static final class Foo extends LoggableTest.Parent {
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
         * @throws Exception If terminated
         */
        @Timeable(limit = 1, unit = TimeUnit.HOURS)
        public static String text() throws Exception {
            TimeUnit.SECONDS.sleep(2L);
            return LoggableTest.Foo.hiddenText();
        }
        /**
         * Revert string.
         * @param text Some text
         * @return Reverted text
         */
        @Timeable
        @Loggable(value = Loggable.INFO, trim = false)
        public String revert(final String text) {
            return new StringBuffer(text).reverse().toString();
        }
        /**
         * Method with different time unit specificaiton.
         * @return Some text
         * @throws Exception If terminated
         */
        @Loggable(precision = TimeUnit.SECONDS)
        public static String logsDurationInSeconds() throws Exception {
            TimeUnit.SECONDS.sleep(Tv.THREE);
            return LoggableTest.Foo.hiddenText();
        }
        /**
         * Private static method.
         * @return Some text
         */
        private static String hiddenText() {
            return "some static text";
        }
        /**
         * Always throw.
         */
        @Loggable(ignore = { IOException.class, RuntimeException.class })
        private void doThrow() {
            throw new IllegalStateException();
        }
    }

}
