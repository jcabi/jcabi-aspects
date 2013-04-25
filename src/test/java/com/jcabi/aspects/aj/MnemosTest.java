/**
 * Copyright (c) 2012-2013, JCabi.com
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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Mnemos}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MnemosTest {

    /**
     * Mnemos can build a string from an object.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromObject() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {1, "1"},
            new Object[] {1.43f, "1.43"},
            new Object[] {"\u20ac-plain", "'\u20ac-plain'"},
            new Object[] {"test ", "'test '"},
            new Object[] {null, "NULL"},
            new Object[] {new String[0], "[]"},
            new Object[] {new String[] {"abc", "x"}, "['abc', 'x']"},
            new Object[] {new Object[] {null, 5}, "[NULL, 5]"},
        };
        for (Object[] pair : pairs) {
            MatcherAssert.assertThat(
                Mnemos.toText(pair[0], false),
                Matchers.equalTo(pair[1].toString())
            );
        }
    }

    /**
     * Mnemos can handle toxic objects gracefully.
     * @throws Exception If something goes wrong
     */
    @Test
    public void handlesToxicObjectsGracefully() throws Exception {
        MatcherAssert.assertThat(
            Mnemos.toText(
                new Object() {
                    @Override
                    public String toString() {
                        throw new IllegalArgumentException("boom");
                    }
                }, true
            ),
            Matchers.equalTo(
                // @checkstyle LineLength (1 line)
                "[com.jcabi.aspects.aj.MnemosTest$1 thrown java.lang.IllegalArgumentException(boom)]"
            )
        );
    }

}
