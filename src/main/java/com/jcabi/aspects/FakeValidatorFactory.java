/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.reflect.InvocationTargetException;
import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

/**
 * Fake class.
 * @since 0.25.0
 */
class FakeValidatorFactory implements ValidatorFactory {

    @Override
    public Validator getValidator() {
        return new FakeValidator();
    }

    @Override
    public ValidatorContext usingContext() {
        return new FakeValidatorContext();
    }

    @Override
    public MessageInterpolator getMessageInterpolator() {
        return new FakeMessageInterpolator();
    }

    @Override
    public TraversableResolver getTraversableResolver() {
        return new FakeTraversableResolver();
    }

    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return new FakeConstraintValidatorFactory();
    }

    @Override
    public ParameterNameProvider getParameterNameProvider() {
        return new FakeParameterNameProvider();
    }

    @Override
    public ClockProvider getClockProvider() {
        return new FakeClockProvider();
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
    public void close() {
        // intentionally empty
    }
}
