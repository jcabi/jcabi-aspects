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
 * Execute annotated method in multiple threads.
 *
 * <p>This annotation should be applied only to methods that return void, in
 * other cases the behavior might be unexpected (because {@code NULL} will
 * always be returned).
 *
 * @since 0.10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Parallel {

    /**
     * Number of threads to use for parallel execution.
     *
     * @return The number of threads
     */
    int threads() default 1;

}
