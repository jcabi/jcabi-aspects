/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.aj;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.version.Version;
import com.jcabi.log.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * Checks for class immutability.
 *
 * <p>The class is thread-safe.
 *
 * @since 0.7.8
 */
@Aspect
public final class ImmutabilityChecker {

    /**
     * Already checked immutable classes.
     */
    private final transient Collection<Class<?>> immutable = new HashSet<>();

    /**
     * Guard of the checked classes.
     */
    private final transient Lock lock = new ReentrantLock();

    /**
     * Catch instantiation and validate class.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     */
    @After("initialization((@com.jcabi.aspects.Immutable *).new(..))")
    public void after(final JoinPoint point) {
        final Class<?> type = point.getTarget().getClass();
        try {
            this.check(type);
        } catch (final Violation ex) {
            throw new IllegalStateException(
                String.format(
                    "%s is not immutable, can't use it (jcabi-aspects %s/%s)",
                    type,
                    Version.CURRENT.projectVersion(),
                    Version.CURRENT.buildNumber()
                ),
                ex
            );
        }
    }

    private void check(final Class<?> type) throws Violation {
        this.lock.lock();
        try {
            if (!this.ignore(type)) {
                if (type.isInterface()
                    && !type.isAnnotationPresent(Immutable.class)) {
                    throw new Violation(
                        String.format(
                            "Interface '%s' is not annotated with @Immutable",
                            type.getName()
                        )
                    );
                }
                if (!type.isInterface()
                    && !Modifier.isFinal(type.getModifiers())) {
                    throw new Violation(
                        String.format(
                            "Class '%s' is not final",
                            type.getName()
                        )
                    );
                }
                try {
                    this.fields(type);
                } catch (final Violation ex) {
                    throw new Violation(
                        String.format("Class '%s' is mutable", type.getName()),
                        ex
                    );
                }
                this.immutable.add(type);
                Logger.debug(this, "#check(%s): immutability checked", type);
            }
        } finally {
            this.lock.unlock();
        }
    }

    private boolean ignore(final Class<?> type) {
        // @checkstyle BooleanExpressionComplexity (5 lines)
        return type.equals(Object.class)
            || type.equals(String.class)
            || type.isPrimitive()
            || type.getName().startsWith("org.aspectj.runtime.reflect.")
            || this.immutable.contains(type);
    }

    private void fields(final Class<?> type) throws Violation {
        final Field[] fields = type.getDeclaredFields();
        for (final Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isFinal(field.getModifiers())) {
                throw new Violation(
                    String.format(
                        "field '%s' is not final in %s",
                        field, type.getName()
                    )
                );
            }
            try {
                if (field.getType().isArray()) {
                    this.checkArray(field);
                }
            } catch (final Violation ex) {
                throw new Violation(
                    String.format(
                        "field '%s' is mutable",
                        field
                    ),
                    ex
                );
            }
        }
    }

    private void checkArray(final Field field) throws Violation {
        if (!field.isAnnotationPresent(Immutable.Array.class)) {
            throw new Violation(
                String.format(
                    "Field '%s' is an array and is not annotated with @Immutable.Array",
                    field.getName()
                )
            );
        }
        final Class<?> type = field.getType().getComponentType();
        try {
            this.check(type);
        } catch (final Violation ex) {
            throw new Violation(
                String.format(
                    "Field array component type '%s' is mutable",
                    type.getName()
                ),
                ex
            );
        }
    }
}
