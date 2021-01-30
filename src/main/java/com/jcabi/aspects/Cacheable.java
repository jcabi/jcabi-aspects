/**
 * Copyright (c) 2012-2017, jcabi.com
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Makes a method response cacheable in memory for some time.
 *
 * <p>For example, this {@code load()} method loads some data from the network
 * and we want it to cache loaded data for 5 seconds (to avoid delays):
 *
 * <pre> &#64;Cacheable(lifetime = 5, unit = TimeUnit.SECONDS)
 * String load(String resource) throws IOException {
 *   return "something";
 * }</pre>
 *
 * <p>You can cache them forever, which means that once calculated and
 * cached value will never expire (may be a nice alternative to static
 * initializers):
 *
 * <pre> &#64;Cacheable(forever = true)
 * String load(String resource) throws IOException {
 *   return "something";
 * }</pre>
 *
 * <p>Since version 0.7.14 you can also annotate methods that should flush
 * cache of the object.
 *
 * <p>Since 0.7.18 you can control when exactly flushing happens, with
 * {@link Cacheable.FlushBefore} and {@link Cacheable.FlushAfter} annotations
 * ({@link Cacheable.Flush} is deprecated), for example:
 *
 * <pre>public class Page {
 *   &#64;Cacheable
 *   public String get() {
 *     // load data from external source, e.g. the network
 *     return data;
 *   }
 *   &#64;Cacheable.FlushBefore
 *   public void set(String data) {
 *     // save data to the network
 *   }
 * }</pre>
 *
 * @since 0.7.7
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

    /**
     * Lifetime of an object in cache, in time units.
     */
    int lifetime() default 1;

    /**
     * Time units of object lifetime.
     *
     * <p>The minimum unit you can use is a second. We simply can't cache for
     * less than a second, because cache is being cleaned every second.
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Keep in cache forever.
     */
    boolean forever() default false;

    /**
     * Returns the current store after the expiration, and
     * then asynchronously update the data.
     */
    boolean asyncUpdate() default false;

    /**
     * Before-flushing trigger(s).
     *
     * <p>Before calling the method, call static method {@code flushBefore()}
     * in this class and, according to its result, either flush or not.
     * For example:
     *
     * <pre> class Foo {
     *   &#64;Cacheable(before = Foo.class)
     *   int read() {
     *     // return some number
     *   }
     *   public static boolean flushBefore() {
     *   // if TRUE is returned, flushing will happen before
     *   // the call to #read()
     *   }
     * }</pre>
     *
     * @since 0.21
     */
    Class<?>[] before() default { };

    /**
     * After-flushing trigger(s).
     *
     * <p>After calling the method, call static method {@code flushAfter()}
     * in this class and, according to its result, either flush or not.
     * For example:
     *
     * <pre> class Foo {
     *   &#64;Cacheable(after = Foo.class)
     *   int read() {
     *     // return some number
     *   }
     *   public static boolean flushAfter() {
     *   // if TRUE is returned, flushing will happen after
     *   // the call to #read()
     *   }
     * }</pre>
     *
     * @since 0.21
     */
    Class<?>[] after() default { };

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object.
     * @since 0.7.14
     * @deprecated It is identical to {@link Cacheable.FlushBefore}
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Deprecated
    @interface Flush {
    }

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, before being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface FlushBefore {
    }

    /**
     * Identifies a method that should flush all cached entities of
     * this class/object, after being executed.
     * @since 0.7.18
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface FlushAfter {
    }

}
