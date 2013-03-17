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
package com.jcabi.aspects.aj;

import com.jcabi.log.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.metadata.ConstraintDescriptor;
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
 * @link <a href="http://beanvalidation.org/1.0/spec/#appendix-methodlevelvalidation">Appendix C</a>
 * @link <a href="http://www.jcabi.com/jcabi-aspects/jsr-303.html">How it works</a>
 */
@Aspect
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.TooManyMethods" })
public final class MethodValidator {

    /**
     * JSR-303 Validator.
     */
    private final transient Validator validator = MethodValidator.build();

    /**
     * Validate arguments of a method.
     * @param point Join point
     * @checkstyle LineLength (3 lines)
     */
    @Before("execution(* *(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeMethod(final JoinPoint point) {
        if (this.validator != null) {
            this.validate(
                point,
                MethodSignature.class.cast(point.getSignature())
                    .getMethod()
                    .getParameterAnnotations()
            );
        }
    }

    /**
     * Validate arguments of constructor.
     * @param point Join point
     * @checkstyle LineLength (3 lines)
     */
    @Before("initialization(*.new(.., @(javax.validation.* || javax.validation.constraints.*) (*), ..))")
    public void beforeCtor(final JoinPoint point) {
        if (this.validator != null) {
            this.validate(
                point,
                ConstructorSignature.class.cast(point.getSignature())
                    .getConstructor()
                    .getParameterAnnotations()
            );
        }
    }

    /**
     * Validate method response.
     * @param point Join point
     * @param result Result of the method
     * @checkstyle LineLength (4 lines)
     * @since 0.7.11
     */
    @AfterReturning(
        pointcut = "execution(@(javax.validation.* || javax.validation.constraints.*) * *(..))",
        returning = "result"
    )
    public void after(final JoinPoint point, final Object result) {
        final Method method = MethodSignature.class.cast(
            point.getSignature()
        ).getMethod();
        if (method.isAnnotationPresent(NotNull.class) && result == null
            && !method.getReturnType().equals(Void.TYPE)) {
            throw new ConstraintViolationException(
                new HashSet<ConstraintViolation<?>>(
                    Arrays.<ConstraintViolation<?>>asList(
                        MethodValidator.violation(
                            result,
                            method.getAnnotation(NotNull.class).message()
                        )
                    )
                )
            );
        }
        if (method.isAnnotationPresent(Valid.class)) {
            final Set<ConstraintViolation<Object>> violations =
                this.validate(result);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
    }

    /**
     * Validate method at the given point.
     * @param point Join point
     * @param params Parameters (their annotations)
     */
    private void validate(final JoinPoint point, final Annotation[][] params) {
        final Set<ConstraintViolation<?>> violations =
            new HashSet<ConstraintViolation<?>>();
        for (int pos = 0; pos < params.length; ++pos) {
            violations.addAll(
                this.validate(pos, point.getArgs()[pos], params[pos])
            );
        }
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                MethodValidator.pack(violations),
                violations
            );
        }
    }

    /**
     * Validate one method argument against its annotations.
     * @param pos Position of the argument in method signature
     * @param arg The argument
     * @param annotations Array of annotations
     * @return A set of violations
     * @todo #61 It's a temporary design, which enables only NotNull,
     *  Valid, and Pattern annotations. In the future we should use
     *  JSR-303 Validator, when they implement validation of values (see
     *  their appendix C).
     */
    private Set<ConstraintViolation<?>> validate(final int pos,
        final Object arg, final Annotation[] annotations) {
        final Set<ConstraintViolation<?>> violations =
            new HashSet<ConstraintViolation<?>>();
        for (Annotation antn : annotations) {
            if (antn.annotationType().equals(NotNull.class)) {
                if (arg == null) {
                    violations.add(
                        MethodValidator.violation(
                            String.format("param #%d", pos),
                            NotNull.class.cast(antn).message()
                        )
                    );
                }
            } else if (antn.annotationType().equals(Valid.class)) {
                violations.addAll(this.validate(arg));
            } else if (antn.annotationType().equals(Pattern.class)) {
                if (arg != null && !arg.toString()
                    .matches(Pattern.class.cast(antn).regexp())) {
                    violations.add(
                        MethodValidator.violation(
                            String.format("param #%d '%s'", pos, arg),
                            Pattern.class.cast(antn).message()
                        )
                    );
                }
            } else {
                throw new IllegalStateException(
                    Logger.format(
                        "%[type]s annotation is not supported at the moment",
                        antn
                    )
                );
            }
        }
        return violations;
    }

    /**
     * Create one simple violation.
     * @param arg The argument passed
     * @param msg Error message to show
     * @return The violation
     */
    private static ConstraintViolation<?> violation(final Object arg,
        final String msg) {
        // @checkstyle AnonInnerLength (50 lines)
        return new ConstraintViolation<String>() {
            @Override
            public String toString() {
                return String.format("%s %s", arg, msg);
            }
            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor() {
                return null;
            }
            @Override
            public Object getInvalidValue() {
                return arg;
            }
            @Override
            public Object getLeafBean() {
                return null;
            }
            @Override
            public String getMessage() {
                return msg;
            }
            @Override
            public String getMessageTemplate() {
                return msg;
            }
            @Override
            public Path getPropertyPath() {
                return null;
            }
            @Override
            public String getRootBean() {
                return "";
            }
            @Override
            public Class<String> getRootBeanClass() {
                return String.class;
            }
            @Override
            public Object[] getExecutableParameters() {
                return new Object[] {};
            }
            @Override
            public Object getExecutableReturnValue() {
                return null;
            }
            @Override
            public <U> U unwrap(final Class<U> type) {
                return null;
            }
        };
    }

    /**
     * Pack violations into string.
     * @param errs All violations
     * @return The full text
     */
    private static String pack(final Collection<ConstraintViolation<?>> errs) {
        final StringBuilder text = new StringBuilder();
        for (ConstraintViolation<?> violation : errs) {
            if (text.length() > 0) {
                text.append("; ");
            }
            text.append(violation.getMessage());
        }
        return text.toString();
    }

    /**
     * Build validator.
     * @return Validator to use in the singleton
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private static Validator build() {
        Validator val = null;
        try {
            val = Validation.buildDefaultValidatorFactory().getValidator();
            Logger.info(
                MethodValidator.class,
                // @checkstyle LineLength (1 line)
                "JSR-303 validator %[type]s instantiated by jcabi-aspects ${project.version}/${buildNumber}",
                val
            );
        } catch (javax.validation.ValidationException ex) {
            Logger.error(
                MethodValidator.class,
                "JSR-303 validator failed to initialize: %s",
                ex.getMessage()
            );
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            Logger.error(
                MethodValidator.class,
                "JSR-303 validator thrown during initialization: %[exception]s",
                ex
            );
        }
        return val;
    }

    /**
     * Check validity of an object, when it is annotated with {@link Valid}.
     * @param object The object to validate
     * @return Found violations
     * @param <T> Type of violations
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private <T> Set<ConstraintViolation<T>> validate(final T object) {
        Set<ConstraintViolation<T>> violations;
        try {
            violations = this.validator.validate(object);
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            Logger.error(
                this,
                // @checkstyle LineLength (1 line)
                "JSR-303 validator thrown %[type]s while validating %[type]s: %s",
                ex,
                object,
                ex.getMessage()
            );
            violations = new HashSet<ConstraintViolation<T>>();
        }
        return violations;
    }

}
