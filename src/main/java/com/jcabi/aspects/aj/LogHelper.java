/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;

/**
 * Helper methods for logging.
 *
 * @since 0.17
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
final class LogHelper {

    /**
     * Helper constructor.
     */
    private LogHelper() {
        // do nothing
    }

    /**
     * Log one line.
     * @param level Level of logging
     * @param log Destination log
     * @param message Message to log
     * @param params Message parameters
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    public static void log(final int level, final Object log,
        final String message, final Object... params) {
        if (level == Loggable.TRACE) {
            Logger.trace(log, message, params);
        } else if (level == Loggable.DEBUG) {
            Logger.debug(log, message, params);
        } else if (level == Loggable.INFO) {
            Logger.info(log, message, params);
        } else if (level == Loggable.WARN) {
            Logger.warn(log, message, params);
        } else if (level == Loggable.ERROR) {
            Logger.error(log, message, params);
        }
    }

    /**
     * Log level is enabled?
     *
     * @param level Level of logging
     * @param log Destination log
     * @return TRUE if enabled
     */
    public static boolean enabled(final int level, final Object log) {
        final boolean enabled;
        if (level == Loggable.TRACE) {
            enabled = Logger.isTraceEnabled(log);
        } else if (level == Loggable.DEBUG) {
            enabled = Logger.isDebugEnabled(log);
        } else if (level == Loggable.INFO) {
            enabled = Logger.isInfoEnabled(log);
        } else if (level == Loggable.WARN) {
            enabled = Logger.isWarnEnabled(log);
        } else {
            enabled = true;
        }
        return enabled;
    }
}
