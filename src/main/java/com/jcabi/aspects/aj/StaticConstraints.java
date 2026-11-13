/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.NotNull;

/**
 * Checks constraints of static methods, which Bean Validation providers
 * refuse to validate, because they need an object to call a method on.
 *
 * <p>The class supports {@link NotNull}, {@link javax.validation.constraints.Null},
 * {@link javax.validation.constraints.Pattern}, and custom constraints that
 * name their validators explicitly.</p>
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @since 0.26.0
 */
final class StaticConstraints {

    /**
     * Static method to check.
     */
    private final Method method;

    /**
     * Ctor.
     * @param mtd Static method
     */
    StaticConstraints(final Method mtd) {
        this.method = mtd;
    }

    /**
     * Messages of violated constraints of the arguments.
     * @param args Arguments of the method
     * @return Messages, empty if all arguments are valid
     */
    Collection<String> arguments(final Object... args) {
        final Collection<String> messages = new ArrayList<>(0);
        final Annotation[][] all = this.method.getParameterAnnotations();
        for (int idx = 0; idx < all.length; ++idx) {
            for (final Annotation annotation : all[idx]) {
                StaticConstraints.check(annotation, args[idx], messages);
            }
        }
        return messages;
    }

    /**
     * Messages of violated constraints of the result.
     * @param result Result of the method
     * @return Messages, empty if the result is valid
     */
    Collection<String> result(final Object result) {
        final Collection<String> messages = new ArrayList<>(0);
        for (final Annotation annotation : this.method.getAnnotations()) {
            StaticConstraints.check(annotation, result, messages);
        }
        return messages;
    }

    private static void check(final Annotation annotation, final Object value,
        final Collection<String> messages) {
        if (annotation.annotationType().isAnnotationPresent(Constraint.class)
            && !StaticConstraints.valid(annotation, value)) {
            messages.add(StaticConstraints.message(annotation));
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean valid(final Annotation annotation,
        final Object value) {
        final boolean result;
        if (annotation instanceof NotNull) {
            result = value != null;
        } else if (annotation instanceof javax.validation.constraints.Null) {
            result = value == null;
        } else if (annotation instanceof javax.validation.constraints.Pattern) {
            result = value == null || Pattern.compile(
                ((javax.validation.constraints.Pattern) annotation).regexp()
            ).matcher(value.toString()).matches();
        } else {
            final Class<?>[] types = annotation.annotationType()
                .getAnnotation(Constraint.class).validatedBy();
            if (types.length == 0) {
                throw new UnsupportedOperationException(
                    String.format(
                        "Constraint @%s is not supported on static methods",
                        annotation.annotationType().getName()
                    )
                );
            }
            try {
                final ConstraintValidator<Annotation, Object> validator =
                    (ConstraintValidator<Annotation, Object>)
                        types[0].getDeclaredConstructor().newInstance();
                validator.initialize(annotation);
                result = validator.isValid(value, null);
            } catch (final ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return result;
    }

    private static String message(final Annotation annotation) {
        String text;
        try {
            text = (String) annotation.annotationType()
                .getMethod("message").invoke(annotation);
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        if (text.startsWith("{")) {
            if (annotation instanceof NotNull) {
                text = "must not be null";
            } else if (annotation instanceof javax.validation.constraints.Null) {
                text = "must be null";
            } else if (annotation
                instanceof javax.validation.constraints.Pattern) {
                text = String.format(
                    "must match \"%s\"",
                    ((javax.validation.constraints.Pattern) annotation).regexp()
                );
            }
        }
        return text;
    }
}
