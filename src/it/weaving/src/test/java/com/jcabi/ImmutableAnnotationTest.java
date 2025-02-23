/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
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
        new TruelyImmutable();
    }

    /**
     * Supposedly immutable class.
     */
    @Immutable
    private static final class Mutable {
        private transient String data = "";
    }

    /**
     * Truely immutable class.
     */
    @Immutable
    private static final class TruelyImmutable {
        private final transient String data = "";
    }

}
