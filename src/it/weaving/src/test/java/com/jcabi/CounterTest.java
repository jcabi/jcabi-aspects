/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link Counter}, which is actually testing how
 * {@link RetryOnFailure} annotation works.
 */
public final class CounterTest {

    @Test
    public void retriesOnFailure() throws Exception {
        final Counter counter = new Counter();
        try {
            counter.ping();
            Assert.fail("exception expected");
        } catch (IllegalStateException ex) {
            MatcherAssert.assertThat(counter.get(), Matchers.equalTo(4));
        }
    }

}
