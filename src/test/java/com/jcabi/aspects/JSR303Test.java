/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for JSR-303 annotations and their implementations.
 *
 * <a href="https://beanvalidation.org/1.0/spec/"></a>
 * @since 0.0.0
 * @checkstyle AbbreviationAsWordInNameCheck (5 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class JSR303Test {
    /**
     * The test message.
     */
    private static final String OVERRIDDEN_MSG = "this is a message";

    @Test
    void throwsWhenMethodParametersAreInvalid() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.Foo().foo(null)
        );
    }

    @Test
    void throwsWhenRegularExpressionDoesntMatch() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.Foo().foo("some text")
        );
    }

    @Test
    void passesWhenMethodParametersAreValid() {
        new JSR303Test.Foo().foo("123");
    }

    @Test
    void validatesOutputForNonNull() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.Foo().nullValue()
        );
    }

    @Test
    void ignoresVoidResponses() {
        new JSR303Test.Foo().voidAlways();
    }

    @Test
    @Disabled
    void validatesConstructorParameters() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.ConstructorValidation(null, null)
        );
    }

    @Test
    @Disabled
    void validatesChainedConstructorParameters() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.ConstructorValidation(null)
        );
    }

    @Test
    void overridesMessage() {
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                ConstraintViolationException.class,
                () -> new JSR303Test.Bar().test(null)
            ),
            Matchers.hasProperty(
                "message",
                Matchers.containsString(JSR303Test.OVERRIDDEN_MSG)
            )
        );
    }

    @Test
    void skipsConstraintRule() {
        new JSR303Test.Bar().test("value");
    }

    /**
     * Annotation.
     * @since 0.0.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    private @interface NoMeaning {
    }

    /**
     * Dummy interface for testing messages overriding.
     * @since 0.0.0
     */
    private interface Fum {
        /**
         * Test method.
         *
         * @param value Value
         */
        void test(@NotNull(message = JSR303Test.OVERRIDDEN_MSG) String value);
    }

    /**
     * Dummy class, for tests above.
     * @since 0.0.0
     */
    @Loggable()
    private static final class Foo {
        /**
         * Do nothing.
         *
         * @param text Some text
         * @return Some data
         */
        @NotNull
        public int foo(
            @NotNull @Pattern(regexp = "\\d+")
            @JSR303Test.NoMeaning final String text
        ) {
            return -1;
        }

        /**
         * Always return null.
         *
         * @return Some data
         */
        @NotNull
        @Valid
        public Integer nullValue() {
            return null;
        }

        /**
         * Ignores when void.
         */
        public void voidAlways() {
            // nothing to do
        }
    }

    /**
     * Dummy class for testing constructor validation.
     * @since 0.0.0
     */
    @Loggable()
    private static final class ConstructorValidation {
        /**
         * Public ctor.
         *
         * @param first First param
         * @param second Second param
         * @checkstyle UnusedFormalParameter (3 lines)
         */
        @SuppressWarnings("PMD.UnusedFormalParameter")
        private ConstructorValidation(
            @NotNull final String first,
            @NotNull final String second
        ) {
            //Nothing to do.
        }

        /**
         * Public ctor.
         *
         * @param param The param.
         */
        private ConstructorValidation(@NotNull final String param) {
            this(param, "foo");
        }
    }

    /**
     * Dummy class for testing messages overriding.
     *
     * @since 0.0.0
     */
    @Loggable()
    final private static class Bar implements JSR303Test.Fum {
        @Override
        public void test(@NotNull final String value) {
            //Nothing to do.
        }
    }

}
