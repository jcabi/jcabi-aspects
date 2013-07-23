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
import java.util.Collection;
import java.util.LinkedList;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Array}.
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
public final class ArrayTest {

    /**
     * Array can work as an array.
     * @throws Exception If some problem inside
     */
    @Test
    public void worksAsANormalArray() throws Exception {
        final Collection<Integer> list = new LinkedList<Integer>();
        list.add(Tv.TEN);
        list.add(Tv.FIVE);
        final Array<Integer> array = new Array<Integer>(list);
        MatcherAssert.assertThat(array, Matchers.hasItem(Tv.TEN));
        MatcherAssert.assertThat(array, Matchers.hasSize(2));
    }

    /**
     * ArraySet can build array fluently.
     * @throws Exception If some problem inside
     */
    @Test
    public void buildsArrayFluently() throws Exception {
        MatcherAssert.assertThat(
            new Array<Integer>()
                .with(Tv.FIVE)
                .with(Tv.TEN)
                .with(Tv.THOUSAND)
                .with(0, Tv.TEN)
                .with(Tv.THREE, Tv.THREE)
                .with(1, Tv.THOUSAND),
            Matchers.allOf(
                Matchers.<Integer>iterableWithSize(Tv.FOUR),
                Matchers.contains(Tv.TEN, Tv.THOUSAND, Tv.THOUSAND, Tv.THREE)
            )
        );
    }

}
