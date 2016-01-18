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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test case for {@link JSR-303} annotations and their implementations.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class JSR303Test {
    /**
     * The test message.
     */
    private static final String OVERRIDEN_MSG = "this is a message";

    /**
     * Expected exception rule.
     */
    @Rule
    public final transient ExpectedException thrown = ExpectedException.none();

    /**
     * NotNull can throw when invalid method parameters.
     * @throws Exception If something goes wrong
     */
    @Test(expected = ConstraintViolationException.class)
    public void throwsWhenMethodParametersAreInvalid() throws Exception {
        new JSR303Test.Foo().foo(null);
    }

    /**
     * NotNull can throw when regex doesn't match.
     * @throws Exception If something goes wrong
     */
    @Test(expected = ConstraintViolationException.class)
    public void throwsWhenRegularExpressionDoesntMatch() throws Exception {
        new JSR303Test.Foo().foo("some text");
    }

    /**
     * NotNull can pass for valid parameters.
     * @throws Exception If something goes wrong
     */
    @Test
    public void passesWhenMethodParametersAreValid() throws Exception {
        new JSR303Test.Foo().foo("123");
    }

    /**
     * NotNull can validate method output.
     * @throws Exception If something goes wrong
     */
    @Test(expected = ConstraintViolationException.class)
    public void validatesOutputForNonNull() throws Exception {
        new JSR303Test.Foo().nullValue();
    }

    /**
     * NotNull can ignore methods that return VOID.
     * @throws Exception If something goes wrong
     */
    @Test
    public void ignoresVoidResponses() throws Exception {
        new JSR303Test.Foo().voidAlways();
    }

    /**
     * Validates constructor parameters for directly invoked constructors.
     */
    @Test(expected = ConstraintViolationException.class)
    public void validatesConstructorParameters() {
        new JSR303Test.ConstructorValidation(null, null);
    }

    /**
     * Validates constructor parameters for other invoked constructors.
     */
    @Test(expected = ConstraintViolationException.class)
    public void validatesChainedConstructorParameters() {
        new JSR303Test.ConstructorValidation(null);
    }

    /**
     * NotNull can override the message.
     */
    @Test
    public void overridesMessage() {
        this.thrown.expect(ConstraintViolationException.class);
        this.thrown.expectMessage(JSR303Test.OVERRIDEN_MSG);
        new JSR303Test.Bar().test(null);
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
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    private @interface NoMeaning {
    }

    /**
     * Dummy class, for tests above.
     */
    @Loggable(Loggable.INFO)
    private static final class Foo {
        /**
         * Do nothing.
         * @param text Some text
         * @return Some data
         */
        @NotNull
        public int foo(
            @NotNull @Pattern(regexp = "\\d+")
            @JSR303Test.NoMeaning final String text) {
            return -1;
        }
        /**
         * Always return null.
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
     *
     * @author Carlos Miranda (miranda.cma@gmail.com)
     * @version $Id$
     */
    @Loggable(Loggable.INFO)
    private static final class ConstructorValidation {
        /**
         * Public ctor.
         * @param first First param
         * @param second Second param
         * @checkstyle UnusedFormalParameter (3 lines)
         */
        @SuppressWarnings("PMD.UnusedFormalParameter")
        public ConstructorValidation(@NotNull final String first,
            @NotNull final String second) {
            //Nothing to do.
        }
        /**
         * Public ctor.
         * @param param The param.
         */
        public ConstructorValidation(@NotNull final String param) {
            this(param, "foo");
        }
    }

    /**
     * Dummy interface for testing messages overriding.
     */
    private interface Fum {
        /**
         * Test method.
         * @param value Value
         */
        void test(@NotNull(message = JSR303Test.OVERRIDEN_MSG) String value);
    }

    /**
     * Dummy class for testing messages overriding.
     */
    @Loggable(Loggable.INFO)
    private static class Bar implements JSR303Test.Fum {
        @Override
        public void test(@NotNull final String value) {
            //Nothing to do.
        }
    }

}
