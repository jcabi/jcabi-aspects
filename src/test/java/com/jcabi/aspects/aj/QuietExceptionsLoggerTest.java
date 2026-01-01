/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@see QuietExceptionsLogger}.
 *
 * @since 0.1.10
 * @checkstyle IllegalThrows (500 lines)
 */
@SuppressWarnings
    (
        { "PMD.AvoidCatchingThrowable", "PMD.AvoidThrowingRawExceptionTypes" }
    )
final class QuietExceptionsLoggerTest {
    @Test
    void withoutException() throws Throwable {
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

    @Test
    void exception() throws Throwable {
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

    @Test
    void throwsWhenUsedWithNonVoidReturnValue() {
        final ProceedingJoinPoint point = Mockito
            .mock(ProceedingJoinPoint.class);
        final MethodSignature signature = Mockito.mock(MethodSignature.class);
        Mockito.when(point.getSignature()).thenReturn(signature);
        Mockito.when(signature.getMethod())
            .thenReturn(this.getClass().getMethods()[0]);
        Mockito.when(signature.getReturnType()).thenReturn(Object.class);
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new QuietExceptionsLogger().wrap(point)
        );
    }
}
