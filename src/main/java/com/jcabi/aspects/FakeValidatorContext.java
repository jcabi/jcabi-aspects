/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeValidatorContext implements ValidatorContext {

    @Override
    public ValidatorContext messageInterpolator(final MessageInterpolator inter) {
        return this;
    }

    @Override
    public ValidatorContext traversableResolver(final TraversableResolver resolver) {
        return this;
    }

    @Override
    public ValidatorContext constraintValidatorFactory(final
        ConstraintValidatorFactory factory) {
        return this;
    }

    @Override
    public ValidatorContext parameterNameProvider(final
        ParameterNameProvider provider) {
        return this;
    }

    @Override
    public ValidatorContext clockProvider(final ClockProvider provider) {
        return this;
    }

    @Override
    public ValidatorContext addValueExtractor(final ValueExtractor<?> extractor) {
        return this;
    }

    @Override
    public Validator getValidator() {
        return new FakeValidator();
    }
}
