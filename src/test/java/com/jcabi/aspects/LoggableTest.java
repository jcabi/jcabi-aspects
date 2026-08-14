/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Loggable} annotation and its implementation.
 * @since 0.0.0
 */
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.UnitTestContainsTooManyAsserts"
})
final class LoggableTest {
    /**
     * Foo toString result.
     */
    private static final transient String RESULT = "some text";

    @Test
    void logsSimpleCall() {
        MatcherAssert.assertThat(
            "text cannot stay unreverted",
            new LoggableTest.Foo().revert("hello"),
            Matchers.equalTo("olleh")
        );
    }

    @Test
    void ignoresToStringMethods() {
        MatcherAssert.assertThat(
            "self cannot be anything but itself",
            new LoggableTest.Foo().self(),
            Matchers.hasToString(LoggableTest.RESULT)
        );
    }

    @Test
    void logsStaticMethods() throws Exception {
        MatcherAssert.assertThat(
            "static method cannot lose its text",
            LoggableTest.Foo.text(),
            Matchers.equalTo("some static text")
        );
    }

    @Test
    void doesntLogInheritedMethods() {
        MatcherAssert.assertThat(
            "inherited method cannot lose its text",
            new LoggableTest.Foo().parentText(),
            Matchers.equalTo("some parent text")
        );
    }

    @Test
    void ignoresSomeExceptions() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new LoggableTest.Foo().doThrow()
        );
    }

    @Test
    void logsDurationWithSpecifiedTimeUnit() throws Exception {
        final StringWriter writer = new StringWriter();
        Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        LoggableTest.Foo.logsDurationInSeconds();
        MatcherAssert.assertThat(
            writer.toString(),
            new LoggableTest.RegexContainsMatcher("in \\d.\\d{3}")
        );
    }

    @Test
    void logsToStringResult() {
        final StringWriter writer = new StringWriter();
        Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        new LoggableTest.Foo().last("TEST");
        MatcherAssert.assertThat(
            writer.toString(),
            new LoggableTest.RegexContainsMatcher(LoggableTest.RESULT)
        );
    }

    @Test
    void logsByteArray() {
        final StringWriter writer = new StringWriter();
        Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        final Collection<String> bytes = new LinkedList<>();
        for (final byte part : new LoggableTest.Foo().logsByteArray()) {
            bytes.add(Byte.toString(part));
        }
        MatcherAssert.assertThat(
            "byte array cannot be logged as anything else",
            writer.toString(),
            Matchers.allOf(
                Matchers.not(
                    Matchers.containsString(
                        ClassCastException.class.getSimpleName()
                    )
                ),
                Matchers.stringContainsInOrder(bytes)
            )
        );
    }

    @Test
    void logsShortArray() {
        final StringWriter writer = new StringWriter();
        Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        final Collection<String> shorts = new LinkedList<>();
        for (final short part : new LoggableTest.Foo().logsShortArray()) {
            shorts.add(Short.toString(part));
        }
        MatcherAssert.assertThat(
            "short array cannot be logged as anything else",
            writer.toString(),
            Matchers.allOf(
                Matchers.not(
                    Matchers.containsString(
                        ClassCastException.class.getSimpleName()
                    )
                ),
                Matchers.stringContainsInOrder(shorts)
            )
        );
    }

    @Test
    void logsExceptionAtConfiguredLevel() {
        final StringWriter writer = new StringWriter();
        Logger.getRootLogger().addAppender(
            new WriterAppender(new SimpleLayout(), writer)
        );
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new LoggableTest.Bar().throwAtConfiguredLevel()
        );
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.containsString("ERROR")
        );
    }

    @Test
    void logsWithExplicitLoggerName() throws Exception {
        final StringWriter writer = new StringWriter();
        Logger.getRootLogger().addAppender(
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
     * @since 0.0.0
     */
    @SuppressWarnings("PMD.JUnitTestClassShouldBeFinal")
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
     * @since 0.0.0
     */
    @Loggable
        (
            value = Loggable.DEBUG,
            prepend = true,
            unit = TimeUnit.MILLISECONDS
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
        @Loggable()
        public LoggableTest.Foo self() {
            return this;
        }

        /**
         * Revert string.
         * @param text Some text
         * @return Reverted text
         */
        @Timeable
        @Loggable(trim = false)
        public String revert(final String text) {
            return new StringBuffer(text).reverse().toString();
        }

        /**
         * Method returns byte array.
         * @return Byte array.
         */
        @Loggable
        public byte[] logsByteArray() {
            final byte[] bytes = new byte[10];
            new Random().nextBytes(bytes);
            return bytes;
        }

        /**
         * Method returns short array.
         * @return Byte array.
         */
        @Loggable
        public short[] logsShortArray() {
            final short[] shorts = new short[10];
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
        @Loggable(logThis = true)
        public String last(final String text) {
            return text.substring(text.length() - 1);
        }

        /**
         * Static method.
         * @return Some text
         * @throws Exception If terminated
         */
        @Timeable(limit = 1, unit = TimeUnit.HOURS)
        static String text() throws Exception {
            TimeUnit.SECONDS.sleep(2L);
            return LoggableTest.Foo.hiddenText();
        }

        /**
         * Method annotated with Loggable specifying explicit logger name.
         * @return A String
         */
        @Loggable(value = Loggable.DEBUG, name = "test-logger", prepend = true)
        static String explicitLoggerName() {
            return LoggableTest.Foo.hiddenText();
        }

        /**
         * Method with different time unit specification.
         * @return Some text
         * @throws Exception If terminated
         */
        @Loggable(precision = 3)
        static String logsDurationInSeconds() throws Exception {
            TimeUnit.SECONDS.sleep(2L);
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

    /**
     * Class with a method that uses logException to control exception log level.
     * @since 0.0.0
     */
    private static final class Bar {
        /**
         * Throws with exception logged at ERROR even though method is DEBUG.
         */
        @Loggable(value = Loggable.DEBUG, logException = Loggable.ERROR)
        public void throwAtConfiguredLevel() {
            throw new IllegalStateException("test");
        }
    }

    /**
     * Matcher that checks if a string contains the given pattern.
     * @since 0.0.0
     */
    private static final class RegexContainsMatcher extends TypeSafeMatcher<String> {

        /**
         * Regex to match against.
         */
        private final transient Pattern pattern;

        /**
         * Ctor.
         * @param regex The regex pattern
         */
        private RegexContainsMatcher(final String regex) {
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
    }
}
