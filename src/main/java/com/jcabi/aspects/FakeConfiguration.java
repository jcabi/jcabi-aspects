/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.io.InputStream;
import javax.validation.BootstrapConfiguration;
import javax.validation.ClockProvider;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorFactory;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeConfiguration implements Configuration<FakeConfiguration> {

    @Override
    public FakeConfiguration ignoreXmlConfiguration() {
        return this;
    }

    @Override
    public FakeConfiguration messageInterpolator(final MessageInterpolator interpolator) {
        return this;
    }

    @Override
    public FakeConfiguration traversableResolver(final TraversableResolver resolver) {
        return this;
    }

    @Override
    public FakeConfiguration constraintValidatorFactory(final
        ConstraintValidatorFactory factory) {
        return this;
    }

    @Override
    public FakeConfiguration parameterNameProvider(final ParameterNameProvider provider) {
        return this;
    }

    @Override
    public FakeConfiguration clockProvider(final ClockProvider provider) {
        return this;
    }

    @Override
    public FakeConfiguration addValueExtractor(final ValueExtractor<?> extractor) {
        return this;
    }

    @Override
    public FakeConfiguration addMapping(final InputStream stream) {
        return this;
    }

    @Override
    public FakeConfiguration addProperty(final String str, final String another) {
        return this;
    }

    @Override
    public MessageInterpolator getDefaultMessageInterpolator() {
        return null;
    }

    @Override
    public TraversableResolver getDefaultTraversableResolver() {
        return null;
    }

    @Override
    public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
        return null;
    }

    @Override
    public ParameterNameProvider getDefaultParameterNameProvider() {
        return null;
    }

    @Override
    public ClockProvider getDefaultClockProvider() {
        return null;
    }

    @Override
    public BootstrapConfiguration getBootstrapConfiguration() {
        return null;
    }

    @Override
    public ValidatorFactory buildValidatorFactory() {
        return new FakeValidatorFactory();
    }
}
