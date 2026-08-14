/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Mnemos}.
 *
 * @since 0.0.0
 */
final class MnemosTest {

    /**
     * The representation of a empty array.
     */
    private static final transient String EMPTY_ARRAY = "[]";

    @Test
    void buildsTextFromObject() {
        this.assertText(
            new Object[] {1, "1"},
            new Object[] {1.43f, "1.43"},
            new Object[] {"\u20ac-plain", "'\u20ac-plain'"},
            new Object[] {"test ", "'test '"},
            new Object[] {null, "NULL"},
            new Object[] {new String[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new String[] {"abc", "x"}, "['abc', 'x']"},
            new Object[] {new Object[] {null, 5}, "[NULL, 5]"}
        );
    }

    @Test
    void handlesToxicObjectsGracefully() {
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

    @Test
    void buildsTextFromIntArray() {
        this.assertText(
            new Object[] {new int[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new int[] {1}, "[1]"},
            new Object[] {new int[] {1, 2, 3}, "[1, 2, 3]"}
        );
    }

    @Test
    void buildsTextFromLongArray() {
        this.assertText(
            new Object[] {new long[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new long[] {2L}, "[2]"},
            new Object[] {new long[] {2L, 3L, 4L}, "[2, 3, 4]"}
        );
    }

    @Test
    void buildsTextFromFloatArray() {
        this.assertText(
            new Object[] {new float[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new float[] {1.01f}, "[1.01]"},
            new Object[] {
                new float[] {1.01f, 2.02f, 3.03f},
                "[1.01, 2.02, 3.03]",
            }
        );
    }

    @Test
    void buildsTextFromDoubleArray() {
        this.assertText(
            new Object[] {new double[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new double[] {2.01}, "[2.01]"},
            new Object[] {
                new double[] {2.01, 2.02, 2.03},
                "[2.01, 2.02, 2.03]",
            }
        );
    }

    @Test
    void buildsTextFromCharArray() {
        this.assertText(
            new Object[] {new char[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new char[] {'a'}, "[a]"},
            new Object[] {new char[] {'a', 'b', 'c'}, "[a, b, c]"}
        );
    }

    @Test
    void buildsTextFromBooleanArray() {
        this.assertText(
            new Object[] {new boolean[0], MnemosTest.EMPTY_ARRAY},
            new Object[] {new boolean[] {true}, "[true]"},
            new Object[] {
                new boolean[] {true, false, false},
                "[true, false, false]",
            }
        );
    }

    /**
     * Assert that the text built from every object matches its pair.
     * @param pairs The object pairs to validate
     */
    private void assertText(final Object[]... pairs) {
        for (final Object[] pair : pairs) {
            MatcherAssert.assertThat(
                "text cannot be built from the object",
                Mnemos.toText(pair[0], false, false),
                Matchers.equalTo(pair[1].toString())
            );
        }
    }
}
