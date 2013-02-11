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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Equips objects with three methods.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.8
 */
@Aspect
public final class Equipper {

    /**
     * Map of equipped classes.
     */
    private final transient ConcurrentMap<Class<?>, Class<?>> equipped =
        new ConcurrentHashMap<Class<?>, Class<?>>();

    /**
     * Equip the object and return it.
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrows (5 lines)
     * @checkstyle LineLength (3 lines)
     */
    @Around("call((@com.jcabi.aspects.Equipped *).new(..))")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        return this.proxy(point.proceed());
    }

    /**
     * Build a proxy for an object.
     * @param object The object
     * @return The proxy
     * @throws Exception If something goes wrong
     */
    private Object proxy(final Object object) throws Exception {
        final Class<?> origin = object.getClass();
        synchronized (this.equipped) {
            if (this.equipped.containsKey(origin)) {
                this.equipped.put(origin, this.equip(origin));
            }
        }
        return this.equipped.get(origin)
            .getConstructor(Object.class)
            .newInstance(object);
    }

    /**
     * Equip given class and return an equipped one.
     * @param origin Original class
     * @return Equipped one
     */
    private Class<?> equip(final Class<?> origin) {
        final ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        final String name = String.format(
            "%s__%d",
            origin.getName(),
            origin.getName().hashCode()
        );
        final ClassReader reader = new ClassReader(bytes);
        final ClassWriter writer = new ClassWriter(reader, 0);
        final ClassVisitor visitor = new ClassVisitor();
        reader.accept(visitor, 0);
        return new ClassLoader() {
            public Class<?> load(final String name, final byte[] bytes) {
                return this.defineClass(name, bytes, 0, bytes.length);
            }
        }.load(name, writer.toByteArray());
    }

    /**
     * Invocation handler.
     */
    private static final class Handler implements InvocationHandler {
        /**
         * Public ctor.
         * @param object Original object
         */
        public Handler(final Object object) {
            this.origin = object;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Object invoke(final Object proxy, final Method method,
            final Object[] args) throws Exception {
            return method.invoke(this.origin, args);
        }
    };

}
