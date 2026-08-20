/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.util.Locale;
import javax.validation.MessageInterpolator;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeMessageInterpolator implements MessageInterpolator {

    @Override
    public String interpolate(final String str, final Context context) {
        return "empty";
    }

    @Override
    public String interpolate(final String str, final Context context,
        final Locale locale) {
        return "empty";
    }
}
