/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeValidator implements Validator {

    @Override
    public <T> Set<ConstraintViolation<T>> validate(final T type,
        final Class<?>... classes) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(final T type,
        final String str, final Class<?>... classes) {
        return new HashSet<>();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(final Class<T> clazz,
        final String str, final Object obj, final Class<?>... classes) {
        return new HashSet<>();
    }

    @Override
    public BeanDescriptor getConstraintsForClass(final Class<?> clazz) {
        return null;
    }

    @Override
    public <T> T unwrap(final Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (final InstantiationException | IllegalAccessException
            | InvocationTargetException | NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public ExecutableValidator forExecutables() {
        return new FakeExecutableValidator();
    }
}
