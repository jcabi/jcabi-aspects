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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Makes a method loggable via {@link com.jcabi.log.Logger}.
 *
 * <p>For example, this {@code load()} method produces a log line
 * on every call:
 *
 * <pre> &#64;Loggable
 * String load(String resource) throws IOException {
 *   return "something";
 * }</pre>
 *
 * <p>You can configure the level of logging:
 *
 * <pre> &#64;Loggable(Loggable.DEBUG)
 * void save(String resource) throws IOException {
 *   // do something
 * }</pre>
 *
 * <p>Since version 0.7.6, you can specify a maximum execution time limit for
 * a method. If such a limit is reached a logging message will be issued with
 * a {@code WARN} priority. It is a very convenient mechanism for profiling
 * applications in production. Default value of a limit is 1 second.
 *
 * <pre> &#64;Loggable(limit = 2)
 * void save(String resource) throws IOException {
 *   // do something, potentially slow
 * }</pre>
 *
 * <p>Since version 0.7.14 you can change the time unit for the "limit"
 * parameter. Default unit of measurement is a second:
 *
 * <pre> &#64;Loggable(limit = 200, unit = TimeUnit.MILLISECONDS)
 * void save(String resource) throws IOException {
 *   // do something, potentially slow
 * }</pre>
 *
 * <p>Since version 0.7.17 you can ignore certain exception types, and they
 * won't be logged when thrown. It is very useful when exceptions are used
 * to control flow (which is not a good practice, but is still used in
 * some frameworks, for example in JAX-RS):
 *
 * <pre> &#64;Loggable(ignore = WebApplicationException.class)
 * String get() {
 *   if (not_logged_in()) {
 *     throw new WebApplicationException(forward_to_login_page());
 *   }
 * }</pre>
 *
 * <p>Since version 0.8 you can mark some exceptions as "always to be ignored",
 * using {@link Loggable.Quiet} annotation.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.2
 * @see com.jcabi.log.Logger
 * @see <a href="http://www.jcabi.com/jcabi-aspects">http://www.jcabi.com/jcabi-aspects/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
@SuppressWarnings("PMD.VariableNamingConventions")
public @interface Loggable {

    /**
     * TRACE level of logging.
     */
    int TRACE = 0;

    /**
     * DEBUG level of logging.
     */
    int DEBUG = 1;

    /**
     * INFO level of logging.
     */
    int INFO = 2;

    /**
     * WARN level of logging.
     */
    int WARN = 3;

    /**
     * ERROR level of logging.
     */
    int ERROR = 4;

    /**
     * Level of logging.
     */
    int value() default Loggable.INFO;

    /**
     * Maximum amount allowed for this method (a warning will be
     * issued if it takes longer).
     * @since 0.7.6
     */
    int limit() default 1;

    /**
     * Time unit for the limit.
     * @since 0.7.14
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Shall we trim long texts in order to make log lines more readable?
     * @since 0.7.13
     */
    boolean trim() default true;

    /**
     * Method entry moment should be reported as well (by default only
     * an exit moment is reported).
     * @since 0.7.16
     */
    boolean prepend() default false;

    /**
     * List of exception types, which should not be logged if thrown.
     *
     * <p>You can also mark some exception types as "always to be ignored",
     * using {@link Loggable.Quiet} annotation.
     *
     * @since 0.7.17
     */
    Class<? extends Throwable>[] ignore() default { };

    /**
     * Skip logging of result, replacing it with dots?
     * @since 0.7.19
     */
    boolean skipResult() default false;

    /**
     * Skip logging of arguments, replacing them all with dots?
     * @since 0.7.19
     */
    boolean skipArgs() default false;

    /**
     * Identifies an exception that is never logged by {@link Loggable} if/when
     * being thrown out of an annotated method.
     *
     * <p>Sometimes exceptions are used as flow control instruments (although
     * this may be considered as a bad practice in most casts). In such
     * situations we don't want to flood log console with error messages. One
     * of the options is to use {@link Loggable#ignore()} attribute to list
     * all exception types that should be ignored. However, this
     * {@link Loggable.Quiet} annotation is more convenient when we want to
     * ignore one specific exception type in all situations.
     *
     * @since 0.8
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Quiet {
    }

}
