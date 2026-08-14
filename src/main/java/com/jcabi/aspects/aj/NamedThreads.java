/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
 * @since 0.7.17
 */
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
    @SuppressWarnings("PMD.AvoidThreadGroup")
    NamedThreads(final String suffix, final String desc) {
        this(
            String.format("jcabi-%s", suffix), desc, new ThreadGroup("jcabi")
        );
    }

    /**
     * Ctor.
     * @param label Name of the threads
     * @param desc Description of purpose
     * @param grp Thread group to use
     */
    private NamedThreads(final String label, final String desc,
        final ThreadGroup grp) {
        this.name = label;
        this.purpose = desc;
        this.group = grp;
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(this.group, runnable);
        thread.setName(this.name);
        thread.setDaemon(true);
        Logger.info(
            this,
            "jcabi-aspects %s/%s started new daemon thread %s for %s",
            Version.CURRENT.projectVersion(),
            Version.CURRENT.buildNumber(),
            this.name,
            this.purpose
        );
        return thread;
    }
}
