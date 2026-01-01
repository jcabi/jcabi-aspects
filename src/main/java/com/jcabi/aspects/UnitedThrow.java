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
 * Throw only allowed exceptions, encapsulate others.
 *
 * @since 0.13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressWarnings("PMD.DoNotExtendJavaLangThrowable")
public @interface UnitedThrow {
    /**
     * Default empty exception.
     *
     * @since 0.13
     */
    class None extends Throwable {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = -2085901092419654865L;
    }

    /**
     * Exception that should be allowed.
     *
     * @return The type of exception.
     */
    Class<? extends Throwable> value() default UnitedThrow.None.class;
}
