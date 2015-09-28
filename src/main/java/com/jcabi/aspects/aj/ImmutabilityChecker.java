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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.version.Version;
import com.jcabi.log.Logger;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

/**
 * Checks for class immutability.
 *
 * <p>The class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.8
 */
@Aspect
public final class ImmutabilityChecker {

    /**
     * Already checked immutable classes.
     */
    private final transient Set<Class<?>> immutable = new HashSet<Class<?>>();

    /**
     * Catch instantiation and validate class.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     * @param point Joint point
     */
    @After("initialization((@com.jcabi.aspects.Immutable *).new(..))")
    public void after(final JoinPoint point) {
        final Class<?> type = point.getTarget().getClass();
        try {
            this.check(type);
        } catch (final ImmutabilityChecker.Violation ex) {
            throw new IllegalStateException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "%s is not immutable, can't use it (jcabi-aspects %s/%s)",
                    type,
                    Version.CURRENT.projectVersion(),
                    Version.CURRENT.buildNumber()
                ),
                ex
            );
        }
    }

    /**
     * This class is immutable?
     * @param type The class to check
     * @throws ImmutabilityChecker.Violation If it is mutable
     */
    private void check(final Class<?> type)
        throws ImmutabilityChecker.Violation {
        synchronized (this.immutable) {
            if (!this.ignore(type)) {
                if (type.isInterface()
                    && !type.isAnnotationPresent(Immutable.class)) {
                    throw new ImmutabilityChecker.Violation(
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
                } catch (final ImmutabilityChecker.Violation ex) {
                    throw new ImmutabilityChecker.Violation(
                        String.format("Class '%s' is mutable", type.getName()),
                        ex
                    );
                }
                this.immutable.add(type);
                Logger.debug(this, "#check(%s): immutability checked", type);
            }
        }
    }

    /**
     * This class should be ignored and never checked any more?
     * @param type The type to check
     * @return TRUE if this class shouldn't be checked
     */
    private boolean ignore(final Class<?> type) {
        // @checkstyle BooleanExpressionComplexity (5 lines)
        return type.equals(Object.class)
            || type.equals(String.class)
            || type.isPrimitive()
            || type.getName().startsWith("org.aspectj.runtime.reflect.")
            || this.immutable.contains(type);
    }

    /**
     * All its fields are safe?
     * @param type Type to check
     * @throws ImmutabilityChecker.Violation If it is mutable
     */
    private void fields(final Class<?> type)
        throws ImmutabilityChecker.Violation {
        final Field[] fields = type.getDeclaredFields();
        for (int pos = 0; pos < fields.length; ++pos) {
            final Field field = fields[pos];
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isFinal(field.getModifiers())) {
                throw new ImmutabilityChecker.Violation(
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
            } catch (final ImmutabilityChecker.Violation ex) {
                throw new ImmutabilityChecker.Violation(
                    String.format(
                        "field '%s' is mutable",
                        field
                    ),
                    ex
                );
            }
        }
    }

    /**
     * This array field immutable?
     * @param field The field to check
     * @throws Violation If it is mutable.
     */
    private void checkArray(final Field field)
        throws ImmutabilityChecker.Violation {
        if (!field.isAnnotationPresent(Immutable.Array.class)) {
            throw new ImmutabilityChecker.Violation(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "Field '%s' is an array and is not annotated with @Immutable.Array",
                    field.getName()
                )
            );
        }
        final Class<?> type = field.getType().getComponentType();
        try {
            this.check(type);
        } catch (final ImmutabilityChecker.Violation ex) {
            throw new ImmutabilityChecker.Violation(
                String.format(
                    "Field array component type '%s' is mutable",
                    type.getName()
                ),
                ex
            );
        }
    }

    /**
     * Immutability violation.
     */
    private static final class Violation extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 1L;
        /**
         * Public ctor.
         * @param msg Message
         */
        public Violation(final String msg) {
            super(msg);
        }
        /**
         * Public ctor.
         * @param msg Message
         * @param cause Cause of it
         */
        public Violation(final String msg, final Exception cause) {
            super(msg, cause);
        }
    }

}
