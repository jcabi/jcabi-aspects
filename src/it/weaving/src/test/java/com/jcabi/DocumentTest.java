/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi;

import org.junit.Test;

/**
 * Test case for {@link Document}, which is actually testing how
 * {@link Loggable} annotation works.
 */
public final class DocumentTest {

    @Test
    public void instantiates() throws Exception {
        final Document doc = new Document("test");
        doc.name();
    }

    @Test(expected = IllegalStateException.class)
    public void throwsAndLogs() throws Exception {
        new Document("foo").exception();
    }

    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void throwsOnNullParameter() throws Exception {
        new Document(null);
    }

}
