/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link UnitedThrow}.
 * @since 0.0.0
 */
final class UnitedThrowTest {

    @Test
    void rethrowsDeclaredException() {
        Assertions.assertThrows(
            IOException.class,
            () -> new UnitedThrowTest.Thrower().file()
        );
    }

    @Test
    void throwsDeclaredException() {
        Assertions.assertThrows(
            IOException.class,
            () -> new UnitedThrowTest.Thrower().save()
        );
    }

    @Test
    void throwsConfiguredException() {
        Assertions.assertThrows(
            IOException.class,
            () -> new UnitedThrowTest.Thrower().multiple()
        );
    }

    @Test
    void throwsIllegalStateException() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new UnitedThrowTest.Thrower().def()
        );
    }

    @Test
    void throwsOriginalExceptionEncapsulatedInsideDeclared() {
        try {
            new UnitedThrowTest.Thrower().encapsulate();
        } catch (final IOException ex) {
            MatcherAssert.assertThat(
                "should throw IllegalStateException",
                ex.getCause(),
                Matchers.instanceOf(IllegalStateException.class)
            );
        }
    }

    /**
     * Class for testing UnitedThrow.
     * @since 0.0.0
     */
    private static final class Thrower {
        /**
         * Test method.
         */
        @UnitedThrow
        public void save() throws IOException {
            throw new IllegalStateException();
        }

        /**
         * Test method.
         * @throws IOException In case of exception.
         */
        @UnitedThrow
        public void file() throws IOException {
            throw new FileNotFoundException();
        }

        /**
         * Test method.
         * @checkstyle ThrowsCountCheck (3 lines)
         */
        @UnitedThrow(IOException.class)
        public void multiple() throws InterruptedException, IOException {
            throw new IllegalStateException();
        }

        /**
         * Test method.
         */
        @UnitedThrow
        public void def() {
            throw new IllegalArgumentException();
        }

        /**
         * Test method.
         */
        @UnitedThrow(IOException.class)
        public void encapsulate() throws IOException {
            throw new IllegalStateException();
        }
    }
}
