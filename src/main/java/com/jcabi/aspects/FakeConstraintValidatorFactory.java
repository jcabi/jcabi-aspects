/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeConstraintValidatorFactory
    implements ConstraintValidatorFactory {

    @Override
    @SuppressWarnings("PMD.SingletonClassReturningNewInstance")
    public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> clazz) {
        return clazz.cast(new FakeConstraintValidator<>());
    }

    @Override
    public void releaseInstance(final ConstraintValidator<?, ?> validator) {
        // intentionally empty
    }
}
