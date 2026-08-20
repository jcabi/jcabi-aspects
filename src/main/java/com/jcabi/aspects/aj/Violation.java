/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

/**
 * Immutability violation.
 * @since 0.0.0
 */
final class Violation extends Exception {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Public ctor.
     * @param msg Message
     */
    Violation(final String msg) {
        super(msg);
    }

    /**
     * Public ctor.
     * @param msg Message
     * @param cause Cause of it
     * @checkstyle ConstructorsOrderCheck (3 lines)
     */
    Violation(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
