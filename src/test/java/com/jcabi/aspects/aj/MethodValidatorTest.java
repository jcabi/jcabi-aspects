/**
 * Copyright (c) 2012-2013, JCabi.com
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

import com.jcabi.aspects.Loggable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.junit.Test;

/**
 * Test case for {@link MethodValidator}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MethodValidatorTest {

    /**
     * MethodValidator can throw when invalid method parameters.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void throwsWhenMethodParametersAreInvalid() throws Exception {
        new MethodValidatorTest.Foo().foo(null);
    }

    /**
     * MethodValidator can throw when regex doesn't match.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void throwsWhenRegularExpressionDoesntMatch() throws Exception {
        new MethodValidatorTest.Foo().foo("some text");
    }

    /**
     * MethodValidator can pass for valid parameters.
     * @throws Exception If something goes wrong
     */
    @Test
    public void passesWhenMethodParametersAreValid() throws Exception {
        new MethodValidatorTest.Foo().foo("123");
    }

    /**
     * MethodValidator can validate method output.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void validatesOutputForNonNull() throws Exception {
        new MethodValidatorTest.Foo().nullValue();
    }

    /**
     * MethodValidator can ignore methods that return VOID.
     * @throws Exception If something goes wrong
     */
    @Test
    public void ignoresVoidResponses() throws Exception {
        new MethodValidatorTest.Foo().voidAlways();
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
            @NotNull @Pattern(regexp = "\\d+") final String text) {
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
        @NotNull
        public void voidAlways() {
            // nothing to do
        }
    }

}
