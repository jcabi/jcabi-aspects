/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Parallel;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Parallel} annotation.
 */
final class ParallelTest {

    @Test
    public void runsMethodInParallelThreads() throws Exception {
        final AtomicInteger cnt = new AtomicInteger();
        new Runnable() {
            @Override
            @Parallel(threads = 5)
            public void run() {
                cnt.incrementAndGet();
            }
        } .run();
        MatcherAssert.assertThat(cnt.get(), Matchers.equalTo(5));
        new Callable<Integer>() {
            @Override
            @Parallel(threads = 5)
            public Integer call() throws Exception {
                return cnt.decrementAndGet();
            }
        } .call();
        MatcherAssert.assertThat(cnt.get(), Matchers.equalTo(0));
    }

}
