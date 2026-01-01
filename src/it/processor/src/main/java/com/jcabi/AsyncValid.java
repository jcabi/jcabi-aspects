/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import java.util.concurrent.Future;

import com.jcabi.aspects.Async;

/**
 * Async that should compile.
 */
public final class AsyncValid {

    /**
     * Returns void.
     * Should not be flagged by annotation processor.
     */
    @Async
    public void returnsVoid() {
        // nothing to do
    }

    /**
     * Returns void.
     * Should not be flagged by annotation processor.
     * @return Future type.
     */
    @Async
    public Future<Integer> returnsFuture() {
        return null;
    }
}
