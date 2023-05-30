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
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ImmutabilityChecker}.
 *
 * @since 0.7.8
 */
final class ImmutabilityCheckerTest {
    @Test
    void checksRecursiveClasses() {
        MatcherAssert.assertThat(
            new ImmutabilityCheckerTest.Recursive(null).getNext(), Matchers.nullValue()
        );
    }

    @Test
    void failsOnNonFinalFields() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new ImmutabilityCheckerTest.NonFinal("test")
        );
    }

    /**
     * Class with a field that is not final.
     *
     * @since 0.7.8
     */
    @Immutable
    @SuppressWarnings("PMD.ImmutableField")
    private static final class NonFinal {

        /**
         * Some private field.
         */
        private transient String field;

        /**
         * Constructor.
         * @param fld Field to store.
         */
        private NonFinal(final String fld) {
            this.field = fld;
        }
    }

    /**
     * Class that references itself.
     * @since 0.7.8
     */
    @Immutable
    private static final class Recursive {
        /**
         * Next object.
         */
        private final transient ImmutabilityCheckerTest.Recursive next;

        /**
         * Constructor.
         * @param nxt Next object.
         */
        private Recursive(final ImmutabilityCheckerTest.Recursive nxt) {
            this.next = nxt;
        }

        /**
         * Get the next object.
         * @return Object stored.
         */
        public ImmutabilityCheckerTest.Recursive getNext() {
            return this.next;
        }
    }
}
