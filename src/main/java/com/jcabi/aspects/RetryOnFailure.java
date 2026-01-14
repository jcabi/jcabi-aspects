/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
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
 * Retry the method in case of exception.
 *
 * <p>For example, this {@code load()} method will retry to load the URL
 * content if it fails at the first attempts:
 *
 * <pre> &#64;RetryOnFailure(attempts = 2)
 * String load(URL url) throws IOException {
 *   return url.getContent().toString();
 * }</pre>
 *
 * @since 0.1.10
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryOnFailure {

    /**
     * How many times to retry.
     * @return Number of attempts
     */
    int attempts() default 3;

    /**
     * Delay between attempts, in time units.
     * @return Delay
     */
    long delay() default 50;

    /**
     * Time unit.
     * @return Time unit.
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * When to retry (in case of what exception types).
     * @return Array of types.
     */
    Class<? extends Throwable>[] types() default {Throwable.class};

    /**
     * Exception types to ignore.
     * @return Array of types
     */
    Class<? extends Throwable>[] ignore() default {};

    /**
     * Shall it be fully verbose (show full exception trace) or just
     * exception message?
     * @return Verbosity flag
     */
    boolean verbose() default true;

    /**
     * Shall the time between retries by randomized.
     * @return Random retry time flag
     */
    boolean randomize() default true;

}
