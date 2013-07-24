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
package com.jcabi.immutable;

import com.jcabi.aspects.Tv;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ArrayMap}.
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
public final class ArrayMapTest {

    /**
     * ArrayMap can work as a map.
     * @throws Exception If some problem inside
     */
    @Test
    public void worksAsANormalMap() throws Exception {
        final ConcurrentMap<Integer, String> map =
            new ConcurrentHashMap<Integer, String>(0);
        final String value = "first value";
        map.put(1, value);
        map.put(2, "second");
        MatcherAssert.assertThat(
            new ArrayMap<Integer, String>(map),
            Matchers.hasEntry(1, value)
        );
    }

    /**
     * ArrayMap can make a map fluently.
     * @throws Exception If some problem inside
     */
    @Test
    public void buildsMapFluently() throws Exception {
        MatcherAssert.assertThat(
            new ArrayMap<Integer, String>()
                .with(Tv.FIVE, "four")
                .with(Tv.FIVE, Integer.toString(Tv.FIVE))
                .with(Tv.FORTY, "fourty")
                .without(Tv.FORTY)
                .with(Tv.TEN, "ten"),
            Matchers.allOf(
                Matchers.not(Matchers.hasKey(Tv.FORTY)),
                Matchers.hasValue(Integer.toString(Tv.FIVE)),
                Matchers.hasKey(Tv.FIVE),
                Matchers.hasEntry(Tv.FIVE, Integer.toString(Tv.FIVE))
            )
        );
    }

    /**
     * ArrayMap can be equal to another map.
     * @throws Exception If some problem inside
     */
    @Test
    public void comparesCorrectlyWithAnotherMap() throws Exception {
        MatcherAssert.assertThat(
            new ArrayMap<Integer, Integer>().with(1, 1).with(2, 2),
            Matchers.equalTo(
                new ArrayMap<Integer, Integer>().with(2, 2).with(1, 1)
            )
        );
        MatcherAssert.assertThat(
            new ArrayMap<Class<?>, Integer>()
                .with(String.class, 1)
                .with(Integer.class, 2),
            Matchers.equalTo(
                new ArrayMap<Class<?>, Integer>()
                    .with(Integer.class, 2)
                    .with(String.class, 1)
            )
        );
    }

    /**
     * ArrayMap can sort keys.
     * @throws Exception If some problem inside
     */
    @Test
    public void sortsKeys() throws Exception {
        MatcherAssert.assertThat(
            new ArrayMap<Integer, String>()
                .with(Tv.FIVE, "")
                .with(Tv.FOUR, "")
                .with(1, "")
                .with(Tv.TEN, "")
                .keySet(),
            Matchers.hasToString("[1, 4, 5, 10]")
        );
    }

}
