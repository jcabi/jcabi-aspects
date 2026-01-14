/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Loggable;

/**
 * Loggable class for threading tests.
 */
public final class LoggableThreading {

    /**
     * Loggable method.
     * @return Some value
     */
    @Loggable(Loggable.DEBUG)
    public int foo() {
        return 1;
    }
}
