/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Cacheable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Page with content.
 */
public final class StaticPage {

    /**
     * Number of calls made.
     */
    private static int cnt;
    /**
     * Download some text.
     * @param text Some text
     */
    @Cacheable
    public static void download() {
        ++StaticPage.cnt;
    }
    /**
     * Get counter.
     * @return The number
     */
    public static int counted() {
        return StaticPage.cnt;
    }

}
