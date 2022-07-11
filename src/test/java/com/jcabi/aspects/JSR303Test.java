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
public final class JSR303Test {
    /**
     * The test message.
     */
    private static final String OVERRIDEN_MSG = "this is a message";

    /**
     * NotNull can throw when invalid method parameters.
     */
    @Test
    public void throwsWhenMethodParametersAreInvalid() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.Foo().foo(null)
        );
    }

    /**
     * NotNull can throw when regex doesn't match.
     */
    @Test
    public void throwsWhenRegularExpressionDoesntMatch() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.Foo().foo("some text")
        );
    }

    /**
     * NotNull can pass for valid parameters.
     */
    @Test
    public void passesWhenMethodParametersAreValid() {
        new JSR303Test.Foo().foo("123");
    }

    /**
     * NotNull can validate method output.
     */
    @Test
    public void validatesOutputForNonNull() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.Foo().nullValue()
        );
    }

    /**
     * NotNull can ignore methods that return VOID.
     */
    @Test
    public void ignoresVoidResponses() {
        new JSR303Test.Foo().voidAlways();
    }

    /**
     * Validates constructor parameters for directly invoked constructors.
     */
    @Test
    @Disabled
    public void validatesConstructorParameters() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.ConstructorValidation(null, null)
        );
    }

    /**
     * Validates constructor parameters for other invoked constructors.
     */
    @Test
    @Disabled
    public void validatesChainedConstructorParameters() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new JSR303Test.ConstructorValidation(null)
        );
    }

    /**
     * NotNull can override the message.
     */
    @Test
    public void overridesMessage() {
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                ConstraintViolationException.class,
                () -> new JSR303Test.Bar().test(null)
            ),
            Matchers.hasProperty(
                "message",
                Matchers.containsString(JSR303Test.OVERRIDEN_MSG)
            )
        );
    }

    /**
     * Validator can skip a constraint rule.
     */
    @Test
    public void skipsConstraintRule() {
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
        void test(@NotNull(message = JSR303Test.OVERRIDEN_MSG) String value);
    }

    /**
     * Dummy class, for tests above.
     * @since 0.0.0
     */
    @Loggable(Loggable.INFO)
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
    @Loggable(Loggable.INFO)
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
    @Loggable(Loggable.INFO)
    private static class Bar implements JSR303Test.Fum {
        @Override
        public void test(@NotNull final String value) {
            //Nothing to do.
        }
    }

}
