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
import com.jcabi.aspects.UnitedThrow;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Throw single exception out of method.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.13
 */
@Aspect
@Immutable
public final class SingleException {

    /**
     * Catch all exceptions and throw a single selected exception.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     */
    @Around
        (
            // @checkstyle StringLiteralsConcatenation (2 lines)
            "execution(* * (..))"
            + " && @annotation(com.jcabi.aspects.UnitedThrow)"
        )
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    // @checkstyle IllegalThrowsCheck (1 line)
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final Method method =
            MethodSignature.class.cast(point.getSignature()).getMethod();
        final UnitedThrow annot = method.getAnnotation(UnitedThrow.class);
        final Class<? extends Throwable> clz = this.clazz(method, annot);
        try {
            return point.proceed();
            // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Throwable throwable = ex;
            if (!clz.isAssignableFrom(ex.getClass())) {
                if (this.exists(clz)) {
                    throwable = clz.getConstructor(Throwable.class)
                        .newInstance(ex);
                } else {
                    throwable = clz.newInstance();
                }
            }
            throw throwable;
        }
    }

    /**
     * Check if there is a constructor with single Throwable argument.
     * @param clz Class to check.
     * @return Whether constructor exists.
     */
    private boolean exists(final Class<? extends Throwable> clz) {
        boolean found = false;
        for (final Constructor<?> ctr : clz.getConstructors()) {
            if ((ctr.getParameterTypes().length == 1)
                && (ctr.getParameterTypes()[0] == Throwable.class)) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Get required exception class.
     * @param method Method declaring exception.
     * @param annot UnitedThrow annotation.
     * @return Class of exception.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> clazz(final Method method,
        final UnitedThrow annot) {
        Class<? extends Throwable> clz = annot.value();
        if (clz == UnitedThrow.None.class) {
            if (method.getExceptionTypes().length == 0) {
                clz = IllegalStateException.class;
            } else {
                clz = (Class<? extends Throwable>) method
                    .getExceptionTypes()[0];
            }
        }
        return clz;
    }
}
