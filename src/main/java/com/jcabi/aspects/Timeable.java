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
 * Makes a method time constrained.
 *
 * <p>For example, this {@code load()} method should not take more than
 * a second, and should be interrupted if it takes more:
 *
 * <pre> &#64;Timeable(limit = 1, unit = TimeUnit.SECONDS)
 * String load(String resource) {
 *   // something that runs potentially long
 * }</pre>
 *
 * <p>Important to note that in Java 1.5+ it is impossible to force thread
 * termination, for many reasons. Thus, we can't
 * just call {@code Thread.stop()},
 * when a thread is over a specified time limit. The best thing we can do is to
 * call {@link Thread#interrupt()} and hope that the thread itself
 * is checking its
 * {@link Thread#isInterrupted()} status. If you want to design your long
 * running methods in a way that {@link Timeable} can terminate them, embed
 * a checker into your most intessively used place, for example:
 *
 * <pre> &#64;Timeable(limit = 1, unit = TimeUnit.SECONDS)
 * String load(String resource) {
 *   while (true) {
 *     if (Thread.currentThread.isInterrupted()) {
 *       throw new IllegalStateException("time out");
 *     }
 *     // execution as usual
 *   }
 * }</pre>
 *
 * @since 0.7.16
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 * @see <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html">Why Are Thread.stop, Thread.suspend, Thread.resume and Runtime.runFinalizersOnExit Deprecated?</a>
 * @see <a href="http://www.yegor256.com/2014/06/20/limit-method-execution-time.html">Limit Java Method Execution Time, by Yegor Bugayenko</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timeable {

    /**
     * The default maximum amount (of seconds).
     */
    int DEFAULT_LIMIT = 15;

    /**
     * Maximum amount allowed for this method.
     * @return The limit
     */
    int limit() default Timeable.DEFAULT_LIMIT;

    /**
     * Time unit for the limit.
     *
     * <p>The minimum unit you can use is a second. We simply can't monitor with
     * a frequency higher than a second.
     * @return The time unit
     */
    TimeUnit unit() default TimeUnit.SECONDS;

}
