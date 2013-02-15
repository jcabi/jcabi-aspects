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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.ConstructorSignature;

/**
 * Checks for class immutability.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.8
 */
@Aspect
public final class ImmutabilityChecker {

    /**
     * Checked classes.
     */
    private final transient Set<Class<?>> checked = new HashSet<Class<?>>();

    /**
     * Catch instantiation and validate class.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     */
    @Before("initialization((@com.jcabi.aspects.Immutable *).new(..))")
    public void before(final JoinPoint point) throws Throwable {
        final Class<?> type = ConstructorSignature.class.cast(
            point.getSignature()
        ).getDeclaringType();
        System.out.println("type: " + type.getName());
        synchronized (this.checked) {
            if (!this.checked.contains(type)) {
                if (!ImmutabilityChecker.immutable(type)) {
                    throw new IllegalStateException(
                        String.format(
                            "class %s is not immutable as expected",
                            type.getName()
                        )
                    );
                }
                this.checked.add(type);
            }
        }
    }

    /**
     * This type is immutable?
     * @param type The type to check
     * @return TRUE if it is immutable
     */
    private static boolean immutable(final Class<?> type) {
        final Class<?> parent = type.getSuperclass();
        boolean immutable = parent == null
            || (ImmutabilityChecker.immutable(parent)
            && Modifier.isFinal(type.getModifiers())
            && !type.isPrimitive());
        if (immutable) {
            final Field[] fields = type.getDeclaredFields();
            for (int pos = 0; pos < fields.length; ++pos) {
                immutable = Modifier.isFinal(fields[pos].getModifiers())
                    && Modifier.isPrivate(fields[pos].getModifiers())
                    && ImmutabilityChecker.immutable(fields[pos].getType());
                if (!immutable) {
                    break;
                }
            }
        }
        return immutable;
    }

}
