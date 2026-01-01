/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
