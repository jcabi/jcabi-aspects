/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.executable.ExecutableValidator;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeExecutableValidator implements ExecutableValidator {

    @Override
    public <T> Set<ConstraintViolation<T>> validateParameters(final T type,
        final Method method, final Object[] objects, final Class<?>... classes) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateReturnValue(final T type,
        final Method method, final Object obj, final Class<?>... classes) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorParameters(final
        Constructor<? extends T> constructor,
        final Object[] objects, final Class<?>... classes) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(final
        Constructor<? extends T> constructor,
        final T type, final Class<?>... classes) {
        return new HashSet<>();
    }
}
