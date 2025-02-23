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
 * Marks a method for asynchronous execution.
 *
 * <p>Add this annotation to the method you want to execute asynchronously.
 * Methods of return type {@code void} and {@link java.util.concurrent.Future}
 * are supported. In the latter case, an actual asynchronous Future will be
 * returned, but the target method should return a temporary {@code Future}
 * that passes the value through as the return type needs to be the same.
 *
 * <p>Usage with other return types may cause unexpected behavior (because
 * {@code NULL} will always be returned).
 *
 * <p>Keep in mind that there is a limited number of threads working with
 * methods annotated with <code>@Async</code>. Thus, if one of your
 * methods keep a thread busy for a long time, others will wait. Try to
 * make all methods fast, when you annotate them with <code>@Async</code>.
 *
 * @see <a href="http://aspects.jcabi.com">http://aspects.jcabi.com/</a>
 * @since 0.16
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {
}
