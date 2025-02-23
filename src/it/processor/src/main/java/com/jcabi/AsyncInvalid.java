/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Async;

/**
 * Async that should fail to compile.
 */
public final class AsyncInvalid {

    /**
     * Does not return void or future.
     * Should be flagged by annotation processor.
     * @return non void or future.
     */
    @Async
    public int foo() {
        return 1;
    }
}
