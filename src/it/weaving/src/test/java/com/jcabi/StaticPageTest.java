/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link StaticPage}, which is actually testing how
 * {@link Cacheable} annotation works with static methods.
 */
final class StaticPageTest {

    @Test
    public void cachesResults() throws Exception {
        StaticPage.download();
        StaticPage.download();
        MatcherAssert.assertThat(StaticPage.counted(), Matchers.equalTo(1));
    }

}
