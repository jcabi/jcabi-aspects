/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Fake class.
 * @param <T> Type
 * @param <X> Another type
 * @since 0.25.0
 */
final class FakeConstraintValidator<T extends Annotation,
    X> implements ConstraintValidator<T, X> {

    @Override
    public boolean isValid(final Object obj,
        final ConstraintValidatorContext context) {
        return true;
    }
}
