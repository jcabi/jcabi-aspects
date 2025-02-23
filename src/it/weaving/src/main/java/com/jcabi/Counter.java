/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.RetryOnFailure;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter.
 */
public final class Counter {

    /**
     * Count.
     */
    private final AtomicInteger count = new AtomicInteger();

    /**
     * Get count.
     * @return Count
     */
    public int get() {
        return this.count.get();
    }

    /**
     * Ping it.
     */
    @RetryOnFailure(attempts = 4, delay = 0, verbose = false)
    public void ping() {
        throw new IllegalStateException(
            String.format(
                "ping #%d",
                this.count.incrementAndGet()
            )
        );
    }

}
