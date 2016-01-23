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
package com.jcabi.aspects.aj;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1.10
 * @see <a href="http://beanvalidation.org/1.0/spec/#appendix-methodlevelvalidation">Appendix C</a>
 * @see <a href="http://aspects.jcabi.com/jsr-303.html">How it works</a>
 */
@Aspect
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.TooManyMethods" })
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
     * @checkstyle LineLength (3 lines)
     */
    @Before("execution(* *(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeMethod(final JoinPoint point) {
        if (this.validator != null) {
            this.validateMethod(
                point.getThis(),
                MethodSignature.class.cast(point.getSignature()).getMethod(),
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
     * @checkstyle LineLength (3 lines)
     */
    @Before("preinitialization(*.new(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeCtor(final JoinPoint point) {
        if (this.validator != null) {
            @SuppressWarnings("unchecked")
            final Constructor<Object> constructor = (Constructor<Object>)
                ConstructorSignature.class.cast(point.getSignature())
                    .getConstructor();
            this.validateConstructor(
                constructor,
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
            // @checkstyle LineLength (1 line)
            pointcut = "execution(@(javax.validation.* || javax.validation.constraints.*) * *(..))",
            returning = "result"
        )
    public void after(final JoinPoint point, final Object result) {
        this.checkForViolations(
            this.validator
                .forExecutables()
                .validateReturnValue(
                    point.getThis(),
                    new ImprovedJoinPoint(point).currentMethod(),
                    result
                )
        );
    }

    /**
     * Validates method parameters.
     * @param object Object at pointcut
     * @param method Method at pointcut
     * @param args Parameters of the method
     */
    private void validateMethod(final Object object, final Method method,
        final Object... args) {
        this.checkForViolations(this.validator
            .forExecutables()
            .validateParameters(
                object,
                method,
                args
            )
        );
    }

    /**
     * Validates constructor parameters.
     * @param ctr Constructor at pointcut
     * @param args Parameters of the method
     */
    private void validateConstructor(final Constructor<Object> ctr,
        final Object... args) {
        this.checkForViolations(
            this.validator
                .forExecutables()
                .validateConstructorParameters(
                    ctr,
                    args
                )
        );
    }

    /**
     * Checks if violations set is empty and throws a
     * {@link ConstraintViolationException} if it isn't.
     * @param violations JSR303 violations.
     */
    private void checkForViolations(
        final Set<ConstraintViolation<Object>> violations) {
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                MethodValidator.pack(violations),
                violations
            );
        }
    }

    /**
     * Pack violations into string.
     * @param errs All violations
     * @return The full text
     */
    private static String pack(
        final Collection<ConstraintViolation<Object>> errs) {
        final StringBuilder text = new StringBuilder(0);
        for (final ConstraintViolation<?> violation : errs) {
            if (text.length() > 0) {
                text.append("; ");
            }
            text.append(violation.getMessage());
        }
        return text.toString();
    }
}
