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

import java.lang.reflect.Method;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.mockito.Mockito;

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
        this.call(new Object[] {null});
    }

    /**
     * MethodValidator can throw when regex doesn't match.
     * @throws Exception If something goes wrong
     */
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void throwsWhenRegularExpressionDoesntMatch() throws Exception {
        this.call(new Object[] {"some text"});
    }

    /**
     * MethodValidator can pass for valid parameters.
     * @throws Exception If something goes wrong
     */
    @Test
    public void passesWhenMethodParametersAreValid() throws Exception {
        this.call(new Object[] {"123"});
    }

    /**
     * Call it with the provided params.
     * @param args The args
     * @throws Exception If something goes wrong
     */
    private void call(final Object[] args) throws Exception {
        final MethodValidator validator = new MethodValidator();
        final Method method = MethodValidatorTest.Foo.class
            .getMethod("foo", String.class);
        final MethodSignature sig = Mockito.mock(MethodSignature.class);
        Mockito.doReturn(method).when(sig).getMethod();
        final JoinPoint point = Mockito.mock(JoinPoint.class);
        Mockito.doReturn(sig).when(point).getSignature();
        Mockito.doReturn(args).when(point).getArgs();
        validator.beforeMethod(point);
    }

    /**
     * Dummy class, for tests above.
     */
    private static final class Foo {
        /**
         * Do nothing.
         * @param text Some text
         */
        public void foo(@NotNull @Pattern(regexp = "\\d+") final String text) {
            // nothing to do
        }
    }

}
