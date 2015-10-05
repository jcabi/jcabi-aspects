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
package com.jcabi.aspects;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

/**
 * Test case for {@link Loggable} annotation and its implementation.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidUsingShortType" })
public final class LoggableTest {
    /**
     * Foo toString result.
     */
    private static final transient String RESULT = "some text";

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
            RegexContainsMatcher.contains("in \\d.\\d{3}")
        );
    }

    /**
     * Loggable can log toString method.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsToStringResult() throws Exception {
        final StringWriter writer = new StringWriter();
        org.apache.log4j.Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        new LoggableTest.Foo().last("TEST");
        MatcherAssert.assertThat(
            writer.toString(),
            RegexContainsMatcher.contains(LoggableTest.RESULT)
        );
    }

    /**
     * Loggable can log byte array results.
     */
    @Test
    public void logsByteArray() {
        final StringWriter writer = new StringWriter();
        org.apache.log4j.Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        final byte[] result = new LoggableTest.Foo().logsByteArray();
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.not(
                Matchers.containsString(
                    ClassCastException.class.getSimpleName()
                )
            )
        );
        final Collection<String> bytes = new LinkedList<String>();
        for (final byte part : result) {
            bytes.add(Byte.toString(part));
        }
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.stringContainsInOrder(bytes)
        );
    }

    /**
     * Loggable can log short array results.
     */
    @Test
    public void logsShortArray() {
        final StringWriter writer = new StringWriter();
        org.apache.log4j.Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        final short[] result = new LoggableTest.Foo().logsShortArray();
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.not(
                Matchers.containsString(
                    ClassCastException.class.getSimpleName()
                )
            )
        );
        final Collection<String> shorts = new LinkedList<String>();
        for (final short part : result) {
            shorts.add(Short.toString(part));
        }
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.stringContainsInOrder(shorts)
        );
    }

    /**
     * Loggable can log methods that specify their own logger name.
     * @throws Exception If something goes wrong
     */
    @Test
    public void logsWithExplicitLoggerName() throws Exception {
        final StringWriter writer = new StringWriter();
        org.apache.log4j.Logger.getRootLogger().addAppender(
            new WriterAppender(new PatternLayout("%t %c: %m%n"), writer)
        );
        LoggableTest.Foo.explicitLoggerName();
        MatcherAssert.assertThat(
            // @checkstyle MultipleStringLiterals (2 lines)
            writer.toString(),
            Matchers.containsString("test-logger")
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
    @Loggable
        (
            value = Loggable.DEBUG,
            prepend = true,
            limit = 1, unit = TimeUnit.MILLISECONDS
        )
    private static final class Foo extends LoggableTest.Parent {
        @Override
        public String toString() {
            return LoggableTest.RESULT;
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
         * Method annotated with Loggable specifying explicit logger name.
         * @return A String
         * @throws Exception If terminated
         */
        @Loggable(value = Loggable.DEBUG, name = "test-logger", prepend = true)
        public static String explicitLoggerName() throws Exception {
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
         * Method with different time unit specification.
         * @return Some text
         * @throws Exception If terminated
         */
        @Loggable(precision = Tv.THREE)
        public static String logsDurationInSeconds() throws Exception {
            TimeUnit.SECONDS.sleep(2);
            return LoggableTest.Foo.hiddenText();
        }
        /**
         * Method returns byte array.
         * @return Byte array.
         */
        @Loggable
        public byte[] logsByteArray() {
            final byte[] bytes = new byte[Tv.TEN];
            new Random().nextBytes(bytes);
            return bytes;
        }
        /**
         * Method returns short array.
         * @return Byte array.
         */
        @Loggable
        public short[] logsShortArray() {
            final short[] shorts = new short[Tv.TEN];
            final Random random = new Random();
            for (int idx = 0; idx < shorts.length; ++idx) {
                shorts[idx] = (short) random.nextInt();
            }
            return shorts;
        }
        /**
         * Get last char.
         * @param text Text to get last char from.
         * @return Last char.
         */
        @Loggable(value = Loggable.INFO, logThis = true)
        public String last(final String text) {
            return text.substring(text.length() - 1);
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

    /**
     * Matcher that checks if a string contains the given pattern.
     */
    private static class RegexContainsMatcher extends TypeSafeMatcher<String> {
        /**
         * Regex to match against.
         */
        private final transient Pattern pattern;
        /**
         * Ctor.
         * @param regex The regex pattern
         */
        public RegexContainsMatcher(final String regex) {
            super();
            this.pattern = Pattern.compile(regex);
        }
        @Override
        public boolean matchesSafely(final String str) {
            return this.pattern.matcher(str).find();
        }
        @Override
        public void describeTo(final Description description) {
            description.appendText("matches regex=");
        }
        /**
         * Matcher for regex patterns.
         * @param regex The regex pattern
         * @return Matcher for containing regex pattern
         */
        public static RegexContainsMatcher contains(final String regex) {
            return new RegexContainsMatcher(regex);
        }
    }
}
