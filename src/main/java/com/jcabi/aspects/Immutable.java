/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an immutable class.
 *
 * <p>For example:
 *
 * <pre> &#64;Immutable
 * public class Foo {
 *   private String data;
 * }</pre>
 *
 * <p>As soon as you try to instantiate this class a runtime exception
 * will be thrown, because this class is mutable.
 *
 * @since 0.7.8
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 * @see <a href="http://www.yegor256.com/2014/06/09/objects-should-be-immutable.html">Objects Should Be Immutable, by Yegor Bugayenko</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Immutable {
    /**
     * Identifies that an array-type field should be considered immutable. Note
     * that for an array to be considered immutable, its component type must
     * also be immutable.
     * @since 0.17
     * @todo #33 Let's prevent modifications to arrays having this annotation,
     *  somehow. Perhaps we can create an aspect that will throw an exception
     *  should something try to write into the array. See
     *  https://bugs.eclipse.org/bugs/show_bug.cgi?id=157031
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Array {
    }
}
