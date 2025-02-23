/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Foo}.
 */
public final class FooTest {

    @Test
    public void simpleRun() throws Exception {
        new Foo(null);
        new Foo("Hello, world!");
    }

}
