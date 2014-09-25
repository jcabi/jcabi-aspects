/**
 * Copyright (c) 2012-2014, jcabi.com
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

/**
 * Helper methods for logging.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
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
        boolean enabled;
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

