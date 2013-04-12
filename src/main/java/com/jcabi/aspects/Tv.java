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
package com.jcabi.aspects;

/**
 * Convenient holder of numeric constants for annotations
 * (TV means "time values").
 *
 * <p>For example, you can use it like this:
 *
 * <pre> &#64;Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.SECONDS)
 * String load(String resource) throws IOException {
 * }</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.17
 * @see <a href="http://www.jcabi.com/jcabi-aspects">http://www.jcabi.com/jcabi-aspects/</a>
 * @checkstyle InterfaceIsType (500 lines)
 */
@SuppressWarnings("PMD.AvoidConstantsInterface")
public interface Tv {

    /**
     * Three.
     */
    int THREE = 3;

    /**
     * Four.
     */
    int FOUR = 4;

    /**
     * Five.
     */
    int FIVE = 5;

    /**
     * Ten.
     */
    int TEN = 10;

    /**
     * Fifteen.
     */
    int FIFTEEN = 15;

    /**
     * Twenty.
     */
    int TWENTY = 20;

    /**
     * Thirty.
     */
    int THIRTY = 30;

    /**
     * Forty.
     */
    int FORTY = 40;

    /**
     * Fifty.
     */
    int FIFTY = 50;

}
