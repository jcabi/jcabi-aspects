/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Page}, which is actually testing how
 * {@link Cacheable} annotation works.
 */
final class PageTest {

    @Test
    public void cachesResults() throws Exception {
        final Page page = new Page();
        page.downloadWithCache("with");
        page.downloadWithCache("with");
        page.downloadWithoutCache("without");
        page.downloadWithoutCache("without");
        MatcherAssert.assertThat(page.counted(), Matchers.equalTo(3));
    }

    @Test
    public void equippedWithThreeMethods() throws Exception {
        final Page first = new Page();
        first.downloadWithoutCache("");
        final Page second = new Page();
        second.downloadWithoutCache("");
        MatcherAssert.assertThat(first, Matchers.equalTo(second));
        MatcherAssert.assertThat(
            first.hashCode(),
            Matchers.equalTo(second.hashCode())
        );
        MatcherAssert.assertThat(first, Matchers.hasToString("Page(cnt=1)"));
    }

}
