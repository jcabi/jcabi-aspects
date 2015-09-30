/**
 * Copyright (c) 2012-2015, jcabi.com
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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link NamedThreads}.
 *
 * @author Haris Peco (snpe60@gmail.com)
 * @version $Id$
 */
public final class NamedThreadsTest {
    /**
     * Version test.
     */
    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void testVersion() {
        final org.apache.log4j.Logger root = LogManager.getRootLogger();
        root.setLevel(org.apache.log4j.Level.INFO);
        final StringAppender app = new StringAppender();
        root.addAppender(app);
        new NamedThreads("test", "desc").newThread(
            new Runnable() {
                @Override
                // @checkstyle MethodBodyCommentsCheck (2 lines)
                public void run() {
                    // do nothing
                }
            });
        final String message = app.getBuffer();
        MatcherAssert.assertThat(
            message,
            Matchers.not(
                Matchers.containsString(
                    "${projectVersion}"
                )
            )
        );
        MatcherAssert.assertThat(
            message,
            Matchers.not(
                Matchers.containsString(
                    "${buildNumber}"
                )
            )
        );
    }

    /**
     * Simple string appender.
     * @author Haris Peco (snpe60@gmail.com)
     * @version $Id
     */
    @SuppressWarnings("PMD.AvoidStringBufferField")
    private class StringAppender extends AppenderSkeleton {
        /**
         * String buffer.
         */
        private final StringBuilder buffer = new StringBuilder();
        @Override
        public boolean requiresLayout() {
            return false;
        }
        @Override
        // @checkstyle MethodBodyCommentsCheck (2 lines)
        public void close() {
            // do nothing
        }

        /**
         * Returns string buffer.
         * @return String buffer
         */
        public String getBuffer() {
            return this.buffer.toString();
        }

        @Override
        protected void append(final LoggingEvent event) {
            this.buffer.append(event.getMessage());
        }
    }
}
