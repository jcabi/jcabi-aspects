/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Validates method calls.
 *
 * <p>We do this manual processing of {@code javax.validation.constraints.*}
 * annotations only because
 * JSR-303 in its current version doesn't support method level validation
 * (see its Appendix C). At the moment we don't support anything expect these
 * two annotations. We think that it's better to wait for JSR-303.
 *
 * <p>The class is thread-safe.
 *
 * @see <a href="http://beanvalidation.org/1.0/spec/#appendix-methodlevelvalidation">Appendix C</a>
 * @see <a href="http://aspects.jcabi.com/jsr-303.html">How it works</a>
 * @since 0.1.10
 */
@Aspect
public final class MethodValidator {

    /**
     * JSR-303 Validator.
     */
    private final transient Validator validator = Validation
        .buildDefaultValidatorFactory()
        .getValidator();

    /**
     * Validate arguments of a method.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Join point
     */
    @Before("execution(* *(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeMethod(final JoinPoint point) {
        if (this.validator != null) {
            this.validateMethod(
                point.getThis(),
                ((MethodSignature) point.getSignature()).getMethod(),
                point.getArgs()
            );
        }
    }

    /**
     * Validate arguments of constructor.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Join point
     */
    @Before(
        // @checkstyle StringLiteralsConcatenation (3 lines)
        "preinitialization(*.new(.., @(javax.validation.*"
        + " || javax.validation.constraints.*) (*), ..))"
    )
    @SuppressWarnings("unchecked")
    public void beforeCtor(final JoinPoint point) {
        if (this.validator != null) {
            this.validateConstructor(
                (Constructor<Object>) ((ConstructorSignature) point
                    .getSignature()).getConstructor(),
                point.getArgs()
            );
        }
    }

    /**
     * Validate method response.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Join point
     * @param result Result of the method
     * @since 0.7.11
     */
    @AfterReturning
        (
            pointcut = "execution(@(javax.validation.* || javax.validation.constraints.*) * *(..))",
            returning = "result"
        )
    public void after(final JoinPoint point, final Object result) {
        this.checkForViolations(
            this.validator
                .forExecutables().validateReturnValue(
                    point.getThis(),
                    new ImprovedJoinPoint(point).currentMethod(),
                    result
                )
        );
    }

    private void validateMethod(final Object object, final Method method,
        final Object... args) {
        this.checkForViolations(
            this.validator
                .forExecutables()
                .validateParameters(object, method, args)
        );
    }

    private void validateConstructor(final Constructor<Object> ctr,
        final Object... args) {
        this.checkForViolations(
            this.validator
                .forExecutables()
                .validateConstructorParameters(ctr, args)
        );
    }

    private void checkForViolations(
        final Set<ConstraintViolation<Object>> violations) {
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                MethodValidator.pack(violations),
                violations
            );
        }
    }

    private static String pack(
        final Iterable<ConstraintViolation<Object>> errs) {
        return StreamSupport.stream(errs.spliterator(), false)
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining("; "));
    }
}
