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
 * Schedules the method to run with fixed delay, automatically.
 *
 * <p>For example, you want a method to do something every minute:
 *
 * <pre> &#64;ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.MINUTES)
 * public class Bucket implements Runnable, Closeable {
 *   &#64;Override
 *   void run() {
 *     // do some routine job
 *   }
 *   &#64;Override
 *   void close() {
 *     // close operations
 *   }
 * }</pre>
 *
 * <p>Execution will be started as soon as you make an instance of the class,
 * and will be stopped when you call {@code close()}:
 *
 * <pre>Bucket bucket = new Bucket();
 * // some time later
 * bucket.close();</pre>
 *
 * <p>In order to be executed the class should implement either
 * {@link Runnable} or {@link java.util.concurrent.Callable}. In order to
 * be closed and stopped, your the class should implement
 * {@link java.io.Closeable} and its {@code close()}
 * method should be explicitly called
 * at the moment you want it to stop.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.16
 * @see <a href="http://www.jcabi.com/jcabi-aspects">http://www.jcabi.com/jcabi-aspects/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScheduleWithFixedDelay {

    /**
     * Delay, in time units.
     */
    int delay() default 1;

    /**
     * Time units of delay.
     */
    TimeUnit unit() default TimeUnit.MINUTES;

}
