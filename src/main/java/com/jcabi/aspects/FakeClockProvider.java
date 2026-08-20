/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.time.Clock;
import javax.validation.ClockProvider;

/**
 * Fake class.
 * @since 0.25.0
 */
final class FakeClockProvider implements ClockProvider {

    @Override
    public Clock getClock() {
        return Clock.systemUTC();
    }
}
