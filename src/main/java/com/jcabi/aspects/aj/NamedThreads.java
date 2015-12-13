/**
 * Copyright (c) 2012-2015, jcabi.com
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
package com.jcabi.aspects.aj;

import com.jcabi.aspects.version.Version;
import com.jcabi.log.Logger;
import java.util.concurrent.ThreadFactory;

/**
 * Factory of named threads, used in {@link MethodLogger},
 * {@link MethodCacher}, {@link MethodInterrupter}, etc.
 *
 * <p>This custom class is used instead of a default ThreadFactory in order
 * to name scheduled threads correctly on construction.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.17
 */
@SuppressWarnings("PMD.DoNotUseThreads")
final class NamedThreads implements ThreadFactory {

    /**
     * Name of the thread.
     */
    private final transient String name;

    /**
     * Purpose of these threads.
     */
    private final transient String purpose;

    /**
     * Thread group to use.
     */
    private final transient ThreadGroup group;

    /**
     * Public ctor.
     * @param suffix Suffix of thread names
     * @param desc Description of purpose
     */
    public NamedThreads(final String suffix, final String desc) {
        this.name = String.format("jcabi-%s", suffix);
        this.purpose = desc;
        this.group = new ThreadGroup("jcabi");
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(this.group, runnable);
        thread.setName(this.name);
        thread.setDaemon(true);
        Logger.info(
            this,
            // @checkstyle LineLength (1 line)
            "jcabi-aspects %s/%s started new daemon thread %s for %s",
            Version.CURRENT.projectVersion(),
            Version.CURRENT.buildNumber(),
            this.name,
            this.purpose
        );
        return thread;
    }

}
