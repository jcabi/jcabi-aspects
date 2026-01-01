/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Timeable} annotation.
 * @since 0.0.0
 */
final class TimeableTest {

    @Test
    void interruptsLongRunningMethod() {
        Assertions.assertThrows(
            InterruptedException.class,
            this::slow
        );
    }

    /**
     * Long running method.
     * @throws Exception If terminated
     */
    @Timeable(limit = 1, unit = TimeUnit.MILLISECONDS)
    void slow() throws Exception {
        TimeUnit.MINUTES.sleep(1L);
    }

}
