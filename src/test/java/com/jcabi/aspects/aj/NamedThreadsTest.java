/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.version.Version;
import java.io.StringWriter;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NamedThreads}.
 *
 * @since 0.22
 */
final class NamedThreadsTest {

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    void testVersion() {
        final Logger root = LogManager.getRootLogger();
        final Level level = root.getLevel();
        root.setLevel(Level.INFO);
        final StringWriter writer = new StringWriter();
        final Appender appender =
            new WriterAppender(new SimpleLayout(), writer);
        root.addAppender(appender);
        try {
            // @checkstyle MethodBodyCommentsCheck (5 lines)
            new NamedThreads("test", "desc").newThread(
                () -> {
                    // do nothing
                }
            );
            final String message = writer.toString();
            MatcherAssert.assertThat(
                "should contains project version",
                message,
                Matchers.containsString(
                    Version.CURRENT.projectVersion()
                )
            );
            MatcherAssert.assertThat(
                "should contains build number",
                message,
                Matchers.containsString(
                    Version.CURRENT.buildNumber()
                )
            );
        } finally {
            root.removeAppender(appender);
            root.setLevel(level);
        }
    }

}
