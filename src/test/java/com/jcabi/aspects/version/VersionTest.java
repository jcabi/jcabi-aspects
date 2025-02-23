/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.version;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Version}.
 * @since 0.23
 */
final class VersionTest {

    @Test
    void containsCorrectVersionNumber() {
        MatcherAssert.assertThat(
            Version.CURRENT.projectVersion(),
            Matchers.not(
                Matchers.equalTo("${projectVersion}")
            )
        );
    }

    @Test
    void containsCorrectBuildNumber() {
        MatcherAssert.assertThat(
            Version.CURRENT.buildNumber(),
            Matchers.not(
                Matchers.equalTo("${buildNumber}")
            )
        );
    }
}
