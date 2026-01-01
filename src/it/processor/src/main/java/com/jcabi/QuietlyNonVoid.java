/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Quietly;

/**
 * Quietly that should fail to compile.
 */
public final class QuietlyNonVoid {

    /**
     * Does not return void. Should be flagged by annotation processor.
     * @return non void
     */
    @Quietly
    public int foo() {
        return 1;
    }
}
