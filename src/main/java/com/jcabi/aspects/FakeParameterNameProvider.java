/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javax.validation.ParameterNameProvider;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeParameterNameProvider implements ParameterNameProvider {

    @Override
    public List<String> getParameterNames(final Constructor<?> ctor) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getParameterNames(final Method method) {
        return Collections.emptyList();
    }
}
