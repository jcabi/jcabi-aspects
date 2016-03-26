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
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Cacheable;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link MethodCacher}.
 * @author Nesterov Nikolay (nikolaynesterov@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class MethodCacherTest {
    /**
     * MethodCacher can support garbage collecting.
     * @throws Throwable If something goes wrong
     * @checkstyle IllegalThrowsCheck (3 lines)
     */
    @Test
    public void supportsGarbageCollecting() throws Throwable {
        final ProceedingJoinPoint point = Mockito.mock(
                ProceedingJoinPoint.class
        );
        Mockito.when(point.proceed()).thenReturn(new Object());
        final MethodCacher.Key key = Mockito.mock(
                MethodCacher.Key.class
        );
        final MethodCacher.Tunnel tunnel = new MethodCacher.Tunnel(
                point, key, false
        );
        final MethodSignature methodSignature = Mockito.mock(
                MethodSignature.class
        );
        final Method method = Buzz.class.getMethod("get");
        Mockito.when(methodSignature.getMethod()).thenReturn(method);
        Mockito.when(point.getSignature()).thenReturn(methodSignature);
        tunnel.through();
        MatcherAssert.assertThat(tunnel.expired(), Matchers.equalTo(false));
        tunnel.cached().clear();
        MatcherAssert.assertThat(tunnel.expired(), Matchers.equalTo(true));
    }

    /**
     * Test class for tests above.
     */
    private static class Buzz {
        /**
         * Return some object.
         * @return Some object.
         */
        @Cacheable(forever = true)
        public Object get() {
            return new Object();
        }
    }
}
