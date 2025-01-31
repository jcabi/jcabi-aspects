/*
 * Copyright (c) 2012-2025, jcabi.com
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

import com.jcabi.aspects.version.Version;
import java.util.regex.Pattern;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Immutable} annotation and its implementation.
 *
 * @since 0.0.0
 * @checkstyle ConstantUsageCheck (500 lines)
 */
@SuppressWarnings
    (
    {
    "PMD.UnusedPrivateField",
    "PMD.UnusedLocalVariable",
    "PMD.FinalFieldCouldBeStatic"
    }
    )
final class ImmutableTest {

    @Test
    void catchedMutableTypes() {
        Assertions.assertThrows(
            IllegalStateException.class,
            ImmutableTest.Mutable::new
        );
    }

    @Test
    void catchedMutableTypesWithArrays() {
        Assertions.assertThrows(
            IllegalStateException.class,
            ImmutableTest.MutableWithArray::new
        );
    }

    @Test
    void passesImmutableObjects() {
        final Object obj = new ImmutableTest.TruelyImmutable(
            new ImmutableTest.TruelyImmutableWithNonPrivateFields()
        );
    }

    @Test
    void passesImmutableObjectsWithNonPrivateFields() {
        new ImmutableTest.TruelyImmutableWithNonPrivateFields();
    }

    @Test
    void catchesTypesMutableByClassInheritance() {
        Assertions.assertThrows(
            IllegalStateException.class,
            ImmutableTest.MutableByInheritance::new
        );
    }

    @Test
    void informsVersionOnError() {
        MatcherAssert.assertThat(
            "should inform version",
            Assertions.assertThrows(
                IllegalStateException.class,
                ImmutableTest.Mutable::new
            ),
            Matchers.hasProperty(
                "message",
                Matchers.allOf(
                    Matchers.containsString(Version.CURRENT.projectVersion()),
                    Matchers.containsString(Version.CURRENT.buildNumber())
                )
            )
        );
    }

    /**
     * Other vague interface.
     *
     * @since 0.0.0
     */
    @Immutable
    private interface ImmutableInterface {
        /**
         * This function seems to be harmless.
         *
         * @param input An input
         */
        void willBreakImmutability(int input);
    }

    /**
     * Supposedly immutable class.
     *
     * @since 0.0.0
     */
    @Immutable
    private static final class Mutable {
        /**
         * Mutable class member.
         */
        @SuppressWarnings("PMD.ImmutableField")
        private transient String data = "hello";
    }

    /**
     * Mutable class because of array.
     *
     * @since 0.0.0
     */
    @Immutable
    private static final class MutableWithArray {
        /**
         * Mutable class member.
         */
        private final transient int[] data = null;
    }

    /**
     * Truely immutable class.
     *
     * @since 0.0.0
     */
    @Immutable
    private static final class TruelyImmutable {
        /**
         * Something static final.
         */
        private static final Pattern PATTERN = Pattern.compile(".?");

        /**
         * Something just static.
         */
        private static Pattern ptrn = Pattern.compile("\\d+");

        /**
         * Immutable class member.
         */
        private final transient String data;

        /**
         * Another immutable class member.
         */
        private final transient int number;

        /**
         * Another immutable class member.
         */
        private final transient String text;

        /**
         * An immutable array member.
         */
        @Immutable.Array
        private final transient String[] texts;

        /**
         * Immutable iface.
         */
        private final transient ImmutableTest.ImmutableInterface iface;

        /**
         * Ctor.
         */
        private TruelyImmutable() {
            this("Hello, world!");
        }

        /**
         * Ctor.
         *
         * @param ipt Input
         */
        private TruelyImmutable(final ImmutableTest.TruelyImmutableWithNonPrivateFields ipt) {
            this(ipt.text);
        }

        /**
         * Ctor.
         *
         * @param ipt Input
         */
        @SuppressWarnings("PMD.NullAssignment")
        private TruelyImmutable(final String ipt) {
            this.text = ipt;
            this.texts = new String[]{"foo"};
            this.iface = null;
            this.data = null;
            this.number = 2;
        }
    }

    /**
     * Truely immutable class with non-private fields.
     *
     * @since 0.0.0
     * @checkstyle VisibilityModifier (25 lines)
     */
    @Immutable
    private static final class TruelyImmutableWithNonPrivateFields {
        /**
         * Something static final.
         */
        public static final Pattern PATTERN = Pattern.compile(".*");

        /**
         * Something just static.
         */
        public static final Pattern PTRN = Pattern.compile(".+");

        /**
         * Immutable class member.
         */
        public final String data = null;

        /**
         * Another immutable class member.
         */
        public final int number = 2;

        /**
         * Another immutable class member.
         * @checkstyle VisibilityModifierCheck (3 lines)
         */
        public final String text = "Hello!";
    }

    /**
     * Almost immutable class. It can be inherited, because it is non-final;
     * thus methods in the child class can return nonsensical values (e.g.
     * getters that do no return the original value of their corresponding
     * fields). Moreover, immutability cannot be forced to a subclass.
     * See <a href=
     * "http://marxsoftware.blogspot.se/2009/09/
     * is-java-immutable-class-always-final.html">
     * Is java immutable class always final?</a>
     *
     * @since 0.0.0
     */
    @Immutable
    private static class MutableByInheritance {
        /**
         * Immutable class member.
         */
        private final transient String data = null;

        /**
         * Could be overloaded by a child of the class and then return
         * nonsensical value.
         *
         * @return A value that could differ from what is expected if
         *  returned by an overriding method
         */
        public String getData() {
            return this.data;
        }
    }

}
