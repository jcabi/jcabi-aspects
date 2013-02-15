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

import com.jcabi.aspects.Equipped;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Equipper}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class EquipperTest {

    /**
     * Equipper can wrap classes.
     * @throws Exception If something goes wrong
     */
    @Test
    public void wrapsSimpleClass() throws Exception {
        final EquipperTest.Foo first = new EquipperTest.Foo(1);
        MatcherAssert.assertThat(
            first.getClass().getName(),
            Matchers.not(Matchers.equalTo(EquipperTest.Foo.class.getName()))
        );
        MatcherAssert.assertThat(first, Matchers.notNullValue());
        final EquipperTest.Foo second = new EquipperTest.Foo(1);
        MatcherAssert.assertThat(first, Matchers.equalTo(second));
        MatcherAssert.assertThat(
            first.hashCode(),
            Matchers.equalTo(second.hashCode())
        );
        MatcherAssert.assertThat(first, Matchers.hasToString("data=1"));
    }

    /**
     * Dummy class, for tests above.
     */
    @Equipped
    public static class Foo {
        /**
         * Internal variable.
         */
        private final transient int data;
        /**
         * Public ctor.
         * @param input Input data
         */
        public Foo(final int input) {
            this.data = input;
        }
        /**
         * Test method.
         * @return The data encapsulated
         */
        public int get() {
            return this.data;
        }
    }

}
