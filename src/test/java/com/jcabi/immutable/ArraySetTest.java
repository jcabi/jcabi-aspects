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
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link ArraySet}.
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
public final class ArraySetTest {

    /**
     * ArraySet can work as a sorted set.
     * @throws Exception If some problem inside
     */
    @Test
    public void worksAsANormalSortedSet() throws Exception {
        final Collection<Integer> list = new LinkedList<Integer>();
        list.add(Tv.TEN);
        list.add(Tv.FIVE);
        final Set<Integer> set = new ArraySet<Integer>(list);
        MatcherAssert.assertThat(set, Matchers.hasItem(Tv.TEN));
        MatcherAssert.assertThat(set, Matchers.hasSize(2));
    }

    /**
     * ArraySet can build set fluently.
     * @throws Exception If some problem inside
     */
    @Test
    public void buildsSetFluently() throws Exception {
        MatcherAssert.assertThat(
            new ArraySet<Integer>()
                .with(Tv.TEN)
                .with(Tv.FIVE)
                .with(Tv.FIVE)
                .with(Tv.THOUSAND)
                .without(Tv.TEN)
                .without(Tv.THREE)
                .without(Tv.THOUSAND),
            Matchers.allOf(
                Matchers.<Integer>iterableWithSize(1),
                Matchers.hasItem(Tv.FIVE)
            )
        );
    }

    /**
     * ArraySet can compare correctly with another set.
     * @throws Exception If some problem inside
     */
    @Test
    public void comparesWithAnotherArraySet() throws Exception {
        MatcherAssert.assertThat(
            new ArraySet<Integer>().with(Tv.TEN).with(2),
            Matchers.equalTo(new ArraySet<Integer>().with(2).with(Tv.TEN))
        );
    }

}
