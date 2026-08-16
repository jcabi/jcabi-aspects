/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Immutable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Immutable} annotation.
 * {@link RetryOnFailure} annotation works.
 */
public final class ImmutableAnnotationTest {

    @Test(expected = IllegalStateException.class)
    public void validatesClassImmutability() throws Exception {
        new Mutable();
    }

    @Test
    public void validatesClassWithTrueImmutability() throws Exception {
        new TrulyImmutable();
    }

    /**
     * Supposedly immutable class.
     */
    @Immutable
    private static final class Mutable {
        private transient String data = "";
    }

    /**
     * Truly immutable class.
     */
    @Immutable
    private static final class TrulyImmutable {
        private final transient String data = "";
    }

}
