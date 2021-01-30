/*
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
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link ImmutabilityChecker}.
 *
 */
public final class ImmutabilityCheckerTest {
    /**
     * ImmutabilityChecker should check recursive classes.
     */
    @Test
    public void checksRecursiveClasses() {
        MatcherAssert.assertThat(
            new Recursive(null).getNext(), Matchers.nullValue()
        );
    }

    /**
     * ImmutabilityChecker should fail on non final fields.
     */
    @Test(expected = IllegalStateException.class)
    public void failsOnNonFinalFields() {
        new NonFinal("test");
    }

    /**
     * Class with a field that is not final.
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
        public NonFinal(final String fld) {
            this.field = fld;
        }
    }

    /**
     * Class that references itself.
     */
    @Immutable
    private static final class Recursive {
        /**
         * Next object.
         */
        private final transient Recursive next;
        /**
         * Constructor.
         * @param nxt Next object.
         */
        public Recursive(final Recursive nxt) {
            this.next = nxt;
        }
        /**
         * Get the next object.
         * @return Object stored.
         */
        public Recursive getNext() {
            return this.next;
        }
    }
}
