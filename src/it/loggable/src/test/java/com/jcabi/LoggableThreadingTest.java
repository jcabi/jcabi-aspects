/**
 * Copyright (c) 2012-2017, jcabi.com
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
package com.jcabi;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
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

    /**
     * Check if thread created by Loggable is terminated.
     * @throws Exception if something goes wrong
     */
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
        TimeUnit.SECONDS.sleep(Tv.TEN);
        MatcherAssert.assertThat(
            thread.getState(), Matchers.is(State.TERMINATED)
        );
    }

}
