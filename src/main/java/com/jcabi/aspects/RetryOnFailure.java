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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1.10
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryOnFailure {

    /**
     * How many times to retry.
     * @checkstyle MagicNumber (2 lines)
     */
    int attempts() default 3;

    /**
     * Delay between attempts, in time units.
     * @checkstyle MagicNumber (2 lines)
     */
    long delay() default 50;

    /**
     * Time units.
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;

    /**
     * When to retry (in case of what exception types).
     */
    Class<? extends Throwable>[] types() default { Throwable.class };

    /**
     * Exception types to ignore.
     */
    Class<? extends Throwable>[] ignore() default { };

    /**
     * Shall it be fully verbose (show full exception trace) or just
     * exception message?
     */
    boolean verbose() default true;

    /**
     * Shall the time between retries by randomized.
     */
    boolean randomize() default true;

}
