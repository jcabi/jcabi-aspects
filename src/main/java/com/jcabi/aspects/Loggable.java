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
 * @since 0.7.2
 * @see com.jcabi.log.Logger
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 * @see <a href="http://www.yegor256.com/2014/06/01/aop-aspectj-java-method-logging.html">Java Method Logging with AOP and Annotations, by Yegor Bugayenko</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@SuppressWarnings({
    "PMD.VariableNamingConventions", "PMD.RedundantFieldInitializer"
})
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
     *
     * @return The log level
     */
    int value() default Loggable.INFO;

    /**
     * Log level for exceptions. If not defined then the log
     * level of value is used.
     * @return The log level
     */
    int exceptionLevel() default -1;

    /**
     * Maximum amount allowed for this method (a warning will be
     * issued if it takes longer).
     * @since 0.7.6
     * @return The limit
     */
    int limit() default 1;

    /**
     * Time unit for the limit.
     * @since 0.7.14
     * @return The time unit
     */
    TimeUnit unit() default TimeUnit.MINUTES;

    /**
     * Shall we trim long texts in order to make log lines more readable?
     * @since 0.7.13
     * @return The flag
     */
    boolean trim() default true;

    /**
     * Method entry moment should be reported as well (by default only
     * an exit moment is reported).
     * @since 0.7.16
     * @return The flag
     */
    boolean prepend() default false;

    /**
     * List of exception types, which should not be logged if thrown.
     *
     * <p>You can also mark some exception types as "always to be ignored",
     * using {@link Loggable.Quiet} annotation.
     *
     * @since 0.7.17
     * @return Array of types
     */
    Class<? extends Throwable>[] ignore() default { };

    /**
     * Skip logging of result, replacing it with dots?
     * @since 0.7.19
     * @return The flag
     */
    boolean skipResult() default false;

    /**
     * Skip logging of arguments, replacing them all with dots?
     * @since 0.7.19
     * @return The flag
     */
    boolean skipArgs() default false;

    /**
     * Add toString() result to log line.
     * @since 0.8.1
     * @return The flag
     */
    boolean logThis() default false;

    /**
     * The precision (number of fractional digits) to be used when displaying
     * the measured execution time.
     * @since 0.18
     * @return The precision
     */
    int precision() default 2;

    /**
     * The name of the logger to be used. If not specified, defaults to the
     * class name of the annotated class or method.
     * @since 0.18
     * @return The logger's name
     */
    String name() default "";

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
    @interface Quiet {
    }

}
