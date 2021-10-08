/*
 * Copyright (c) 2012-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.util.logging.Level;

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
        final Level lvl;
        if (level == Loggable.TRACE) {
            lvl = Level.FINEST;
        } else if (level == Loggable.DEBUG) {
            lvl = Level.FINE;
        } else if (level == Loggable.INFO) {
            lvl = Level.INFO;
        } else if (level == Loggable.WARN) {
            lvl = Level.WARNING;
        } else {
            lvl = Level.OFF;
        }
        Logger.log(lvl, log, message, params);
    }

    /**
     * Log level is enabled?
     *
     * @param level Level of logging
     * @param log Destination log
     * @return TRUE if enabled
     */
    public static boolean enabled(final int level, final Object log) {
        final Level lvl;
        if (level == Loggable.TRACE) {
            lvl = Level.FINEST;
        } else if (level == Loggable.DEBUG) {
            lvl = Level.FINE;
        } else if (level == Loggable.INFO) {
            lvl = Level.INFO;
        } else if (level == Loggable.WARN) {
            lvl = Level.WARNING;
        } else {
            lvl = Level.OFF;
        }
        return Logger.isEnabled(lvl, log);
    }
}

