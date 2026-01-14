/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Loggable;
import java.lang.Thread.State;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Threading test for {@link com.jcabi.Loggable}.
 */
public final class LoggableThreadingTest {

    @Test
    public void loggableThreadTerminates() throws Exception {
        new LoggableThreading().foo();
        Thread thread = null;
        final Set<Thread> keys = Thread.getAllStackTraces().keySet();
        final Thread[] threads = keys.toArray(new Thread[keys.size()]);
        for (int idx = 0; idx < threads.length; idx++) {
            if (threads[idx].getName().equals("jcabi-loggable")) {
                thread =  threads[idx];
                break;
            }
        }
        MatcherAssert.assertThat(thread, Matchers.notNullValue());
        thread.interrupt();
        TimeUnit.SECONDS.sleep(10);
        MatcherAssert.assertThat(
            thread.getState(), Matchers.is(State.TERMINATED)
        );
    }

}
