/*
 * Copyright (c) 2012-2023, jcabi.com
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
