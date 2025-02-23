/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import com.jcabi.aspects.Loggable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;

/**
 * Document.
 */
public final class Document {

    /**
     * Name of it.
     */
    private final String name;

    /**
     * Public ctor.
     * @param txt Name of it
     */
    public Document(@NotNull String txt) {
        this.name = txt;
    }

    /**
     * Get name of it.
     * @return Name of it
     * @throws Exception If something is wrong
     */
    @Loggable
    public String name() throws Exception {
        TimeUnit.SECONDS.sleep(1);
        return this.name;
    }

    /**
     * Always throws an exception.
     */
    @Loggable
    public void exception() {
        throw new IllegalStateException();
    }

}
