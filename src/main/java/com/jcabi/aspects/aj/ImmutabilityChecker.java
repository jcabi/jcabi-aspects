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
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

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
     * Catch instantiation and validate class.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     */
    @After("initialization((@com.jcabi.aspects.Immutable *).new(..))")
    public void after(final JoinPoint point) throws Throwable {
        final Object object = point.getTarget();
        if (!ImmutabilityChecker.immutable(object)) {
            throw new IllegalStateException(
                String.format(
                    "object of class %s is not @Immutable as expected",
                    object.getClass().getName()
                )
            );
        }
    }

    /**
     * This object is immutable?
     * @param object The object to check
     * @return TRUE if it is immutable
     * @throws Exception If some error
     */
    private static boolean immutable(final Object object) throws Exception {
        boolean immutable;
        final Class<?> type = object.getClass();
        if (type.equals(Object.class) || type.equals(String.class)) {
            immutable = true;
        } else if (type.getName().startsWith("org.aspectj.runtime.reflect.")) {
            immutable = true;
        } else {
            immutable = !type.isPrimitive()
                && ImmutabilityChecker.fields(object, type);
        }
        return immutable;
    }

    /**
     * All its fields are safe?
     * @param object The object to check
     * @param type Its type
     * @return TRUE if it is immutable
     * @throws Exception If some error
     */
    private static boolean fields(final Object object, final Class<?> type)
        throws Exception {
        boolean immutable = true;
        final Field[] fields = type.getDeclaredFields();
        for (int pos = 0; pos < fields.length; ++pos) {
            fields[pos].setAccessible(true);
            immutable = Modifier.isFinal(fields[pos].getModifiers())
                && Modifier.isPrivate(fields[pos].getModifiers())
                && ImmutabilityChecker.immutable(fields[pos].get(object));
            if (!immutable) {
                break;
            }
        }
        return immutable;
    }

}
