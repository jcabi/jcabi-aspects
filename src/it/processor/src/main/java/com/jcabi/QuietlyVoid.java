/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Quietly;

/**
 * Quietly that should pass compilation checks.
 */
public final class QuietlyVoid {

    /**
     * Returns void. Should be allowed by annotation processor.
     */
    @Quietly
    public void foo() {
    }
}
