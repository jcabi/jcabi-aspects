/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import javax.validation.Configuration;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

/**
 * Fake validation provider for JSR-303.
 *
 * <p>This class can help when it's necessary to disable the entire JSR-303 validation
 * mechanism, but it's impossible to take certain classes from the classpath, which
 * are using JSR-303 and demand the presence of a validator.
 *
 * <p>A text resource <tt>META-INF/services/javax.validation.spi.ValidationProvider</tt>
 * must be created, with a single line inside:
 * <tt>com.jcabi.aspects.FakeValidationProvider</tt>. Once this file is found
 * in the classpath, JSR-303 engine will use this fake validator provider and no constraints
 * will be reported in runtime.
 *
 * @since 0.25.0
 */
public final class FakeValidationProvider implements
    ValidationProvider<FakeConfiguration> {

    @Override
    public FakeConfiguration createSpecializedConfiguration(final BootstrapState state) {
        return new FakeConfiguration();
    }

    @Override
    public Configuration<?> createGenericConfiguration(final BootstrapState state) {
        return new FakeConfiguration();
    }

    @Override
    public ValidatorFactory buildValidatorFactory(final ConfigurationState state) {
        return new FakeValidatorFactory();
    }
}
