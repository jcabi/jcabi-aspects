/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Cacheable;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Page with content.
 */
@ToString
@EqualsAndHashCode(callSuper = false)
public final class Page {

    /**
     * Number of calls made.
     */
    private int cnt;
    /**
     * Download some text (use cache).
     * @param text Some text
     * @return Downloaded text
     */
    @Cacheable
    public String downloadWithCache(final String text) {
        ++this.cnt;
        return "done with cache";
    }
    /**
     * Download some text (don't cache).
     * @param text Some text
     * @return Downloaded text
     */
    @Cacheable(lifetime = 0)
    public String downloadWithoutCache(final String text) {
        ++this.cnt;
        return "done without cache";
    }
    /**
     * Get counter.
     * @return The number
     */
    public int counted() {
        return this.cnt;
    }

}
