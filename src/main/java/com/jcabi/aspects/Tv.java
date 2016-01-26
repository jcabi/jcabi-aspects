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
 * @author Simon Njenga (simtuje@gmail.com)
 * @version $Id$
 * @since 0.22.1
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 */
public final class Tv {

    /**
     * Two.
     */
    public static final int TWO = 2;

    /**
     * Three.
     */
    public static final int THREE = 3;

    /**
     * Four.
     */
    public static final int FOUR = 4;

    /**
     * Five.
     */
    public static final int FIVE = 5;

    /**
     * Six.
     */
    public static final int SIX = 6;

    /**
     * Seven.
     */
    public static final int SEVEN = 7;

    /**
     * Eight.
     */
    public static final int EIGHT = 8;

    /**
     * Nine.
     */
    public static final int NINE = 9;

    /**
     * Ten.
     */
    public static final int TEN = 10;

    /**
     * Fifteen.
     */
    public static final int FIFTEEN = 15;

    /**
     * Twenty.
     */
    public static final int TWENTY = 20;

    /**
     * Thirty.
     */
    public static final int THIRTY = 30;

    /**
     * Forty.
     */
    public static final int FORTY = 40;

    /**
     * Fifty.
     */
    public static final int FIFTY = 50;

    /**
     * Sixty.
     */
    public static final int SIXTY = 60;

    /**
     * Seventy.
     */
    public static final int SEVENTY = 70;

    /**
     * Eighty.
     */
    public static final int EIGHTY = 80;

    /**
     * Ninety.
     */
    public static final int NINETY = 90;

    /**
     * Hundren.
     */
    public static final int HUNDRED = 100;

    /**
     * Thousand.
     */
    public static final int THOUSAND = 1000;

    /**
     * Million.
     */
    public static final int MILLION = 1000000;

    /**
     * Billion.
     */
    public static final int BILLION = 1000000000;

    /**
     * Private constructor.
     */
    private Tv() {
        throw new IllegalStateException("Utility class - cannot instantiate!");
    }
}
