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

/**
 * Log all exceptions thrown out of this method.
 *
 * <p>Add this annotation to the method you want to log. Any exception thrown
 * out of it will be logged through {@link com.jcabi.log.Logger}, for example:
 *
 * <pre> &#64;LogExceptions
 * String load(URL url) throws IOException{
 *   return url.getContent().toString();
 * }</pre>
 *
 * <p>This method will log its exception, if thrown.
 *
 * @since 0.1.10
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExceptions {
}
