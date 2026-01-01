/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import javax.validation.constraints.NotNull;

/**
 * Foo.
 */
public final class Foo {

    /**
     * Ctor.
     */
    public Foo(@NotNull String txt) {
        System.out.println(txt);
    }

}
