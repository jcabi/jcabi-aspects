/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

/**
 * Exception that encapsulates all exceptions thrown from threads.
 * @since 0.0.0
 */
final class ParallelException extends Exception {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x8743EF363FEBC422L;

    /**
     * Constructor.
     * @param cause Cause of the current exception
     */
    ParallelException(final Throwable cause) {
        super(cause);
    }
}
