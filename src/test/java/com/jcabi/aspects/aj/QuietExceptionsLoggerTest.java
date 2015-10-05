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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@see QuietExceptionsLogger}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.1.10
 * @checkstyle IllegalThrows (500 lines)
 */
@SuppressWarnings
    (
        { "PMD.AvoidCatchingThrowable", "PMD.AvoidThrowingRawExceptionTypes" }
    )
public final class QuietExceptionsLoggerTest {
    /**
     * Call method that doesn't throw exception.
     * @throws Throwable In case of error.
     */
    @Test
    public void withoutException() throws Throwable {
        final ProceedingJoinPoint point = Mockito
            .mock(ProceedingJoinPoint.class);
        final MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod())
            .thenReturn(this.getClass().getMethods()[0]);
        Mockito.when(signature.getReturnType())
            .thenReturn(Void.TYPE);
        new QuietExceptionsLogger().wrap(point);
        Mockito.verify(point).proceed();
    }

    /**
     * Call method that throws exception.
     * @throws Throwable In case of error.
     */
    @Test
    public void exception() throws Throwable {
        final ProceedingJoinPoint point = Mockito
            .mock(ProceedingJoinPoint.class);
        Mockito.when(point.proceed()).thenThrow(new Exception());
        Mockito.when(point.getTarget()).thenReturn(new Object());
        final MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod())
            .thenReturn(this.getClass().getMethods()[0]);
        Mockito.when(signature.getReturnType())
            .thenReturn(Void.TYPE);
        new QuietExceptionsLogger().wrap(point);
        Mockito.verify(point).proceed();
    }

    /**
     * Throws exception when used with method that does not return void.
     * @throws Throwable in case of error
     */
    @Test(expected = IllegalStateException.class)
    public void throwsWhenUsedWithNonVoidReturnValue() throws Throwable {
        final ProceedingJoinPoint point = Mockito
            .mock(ProceedingJoinPoint.class);
        final MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod())
            .thenReturn(this.getClass().getMethods()[0]);
        Mockito.when(signature.getReturnType()).thenReturn(Object.class);
        new QuietExceptionsLogger().wrap(point);
    }
}
