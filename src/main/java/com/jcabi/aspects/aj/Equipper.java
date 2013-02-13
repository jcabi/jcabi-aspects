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

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Equips objects with three methods.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.8
 * @todo #132 We assume that every class is available in current classloader
 *  as a resource (a binary file). In some cases this may be not true. The
 *  implemenation should be refactored in the nearest future. We should start
 *  building class skeleton according to its Reflection API information.
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
        final ConstructorSignature signature =
            ConstructorSignature.class.cast(point.getSignature());
        final Class<?> proxy = this.proxy(signature.getDeclaringType());
        Object output;
        if (proxy.getConstructors().length == 0) {
            output = proxy.newInstance();
        } else {
            output = proxy.getConstructor(signature.getParameterTypes())
                .newInstance(point.getArgs());
        }
        return output;
    }

    /**
     * Build a proxy for a class.
     * @param origin Origin class
     * @return The proxy
     * @throws Exception If something goes wrong
     */
    private Class<?> proxy(final Class<?> origin) throws Exception {
        synchronized (this.equipped) {
            if (!this.equipped.containsKey(origin)) {
                this.equipped.put(origin, this.equip(origin));
            }
        }
        return this.equipped.get(origin);
    }

    /**
     * Equip given class and return an equipped one.
     * @param origin Original class
     * @return Equipped one
     * @throws IOException If can't load class file
     */
    private Class<?> equip(final Class<?> origin) throws IOException {
        final ClassLoader loader =
            Thread.currentThread().getContextClassLoader();
        final String suffix = String.format(
            "__equipped__%d",
            Math.abs(origin.getName().hashCode())
        );
        final String mnemo = Type.getInternalName(origin);
        final String rename = new StringBuilder(mnemo)
            .append(suffix).toString();
        final ClassReader reader = new ClassReader(
            loader.getResourceAsStream(String.format("%s.class", mnemo))
        );
        final ClassWriter writer = new ClassWriter(reader, 0);
        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4, writer) {
//            @Override
//            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                System.out.println("name: " + name + ", signature: " + signature + ", super: " + superName + ", interface" + interfaces.length);
//                super.visit(version, access, name, signature, mnemo, interfaces);
//                System.out.println("rename: " + rename + ", mnemo: " + mnemo);
//            }
        };
        visitor = new RemappingClassAdapter(
            visitor,
            new Remapper() {
//                @Override
//                public String map(final String type) {
//                    String mapped;
//                    if (type.equals(Type.getInternalName(origin))) {
//                        mapped = rename;
//                    } else {
//                        mapped = type;
//                    }
//                    return mapped;
//                }
            }
        );
        reader.accept(visitor, 0);
        return new ClassLoader() {
            public Class<?> load(final String name, final byte[] bytes) {
                return this.defineClass(name, bytes, 0, bytes.length);
            }
        }.load(
            origin.getName(),
//            new StringBuilder(origin.getName()).append(suffix).toString(),
            writer.toByteArray()
        );
    }

}
