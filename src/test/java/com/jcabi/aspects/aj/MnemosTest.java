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
     * The representation of a empty array.
     */
    private static final transient String EMPTY_ARRAY = "[]";

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
            new Object[] {new String[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new String[] {"abc", "x"}, "['abc', 'x']"},
            new Object[] {new Object[] {null, 5}, "[NULL, 5]"},
        };
        this.validateText(pairs);
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
                }, true, false
            ),
            Matchers.equalTo(
                // @checkstyle LineLength (1 line)
                "[com.jcabi.aspects.aj.MnemosTest$1 thrown java.lang.IllegalArgumentException(boom)]"
            )
        );
    }

    /**
     * Mnemos can build a string from an int array.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromIntArray() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {new int[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new int[] {1}, "[1]"},
            new Object[] {new int[] {1, 2, 3}, "[1, 2, 3]"},
        };
        this.validateText(pairs);
    }

    /**
     * Mnemos can build a string from an long array.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromLongArray() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {new long[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new long[] {2L}, "[2]"},
            new Object[] {new long[] {2L, 3L, 4L}, "[2, 3, 4]"},
        };
        this.validateText(pairs);
    }

    /**
     * Mnemos can build a string from an float array.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromFloatArray() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {new float[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new float[] {1.01f}, "[1.01]"},
            new Object[] {
                new float[] {1.01f, 2.02f, 3.03f},
                "[1.01, 2.02, 3.03]",
            },
        };
        this.validateText(pairs);
    }

    /**
     * Mnemos can build a string from an double array.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromDoubleArray() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {new double[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new double[] {2.01}, "[2.01]"},
            new Object[] {
                new double[] {2.01, 2.02, 2.03},
                "[2.01, 2.02, 2.03]",
            },
        };
        this.validateText(pairs);
    }

    /**
     * Mnemos can build a string from an char array.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromCharArray() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {new char[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new char[] {'a'}, "[a]"},
            new Object[] {new char[] {'a', 'b', 'c'}, "[a, b, c]"},
        };
        this.validateText(pairs);
    }

    /**
     * Mnemos can build a string from an boolean array.
     * @throws Exception If something goes wrong
     */
    @Test
    public void buildsTextFromBooleanArray() throws Exception {
        final Object[][] pairs = new Object[][] {
            new Object[] {new boolean[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new boolean[] {true}, "[true]"},
            new Object[] {
                new boolean[] {true, false, false},
                "[true, false, false]",
            },
        };
        this.validateText(pairs);
    }

    /**
     * Method that validates the text built from an object.
     * @param pairs The object pairs to validate.
     */
    private void validateText(final Object[]... pairs) {
        for (final Object[] pair : pairs) {
            MatcherAssert.assertThat(
                Mnemos.toText(pair[0], false, false),
                Matchers.equalTo(pair[1].toString())
            );
        }
    }
}
