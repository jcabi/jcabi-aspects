/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.annotation.ElementType;
import javax.validation.Path;
import javax.validation.TraversableResolver;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeTraversableResolver implements TraversableResolver {

    @Override
    public boolean isReachable(final Object obj, final Path.Node node,
        final Class<?> clazz, final Path path, final ElementType type) {
        return false;
    }

    @Override
    public boolean isCascadable(final Object obj, final Path.Node node,
        final Class<?> clazz, final Path path, final ElementType type) {
        return false;
    }
}
