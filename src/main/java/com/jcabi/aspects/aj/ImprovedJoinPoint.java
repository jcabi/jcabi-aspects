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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;

/**
 * This class implements the methods from JointPoint interface.
 *
 * @author Shelan Perera (shelanrc@gmail.com)
 * @version $Id$
 * @since 1.0
 */
final class ImprovedJoinPoint implements JoinPoint {

    /**
     *
     */
    private final transient JoinPoint joinpoint;

    /**
     * Constructor.
     *
     * @param jpt Origin instance
     */
    ImprovedJoinPoint(final JoinPoint jpt) {
        this.joinpoint = jpt;
    }

    @Override
    public String toString() {
        return this.joinpoint.toString();
    }
    @Override
    public String toShortString() {
        return this.joinpoint.toShortString();
    }
    @Override
    public String toLongString() {
        return this.joinpoint.toLongString();
    }
    @Override
    public Object getThis() {
        return this.joinpoint.getThis();
    }

    @Override
    public Object getTarget() {
        return this.joinpoint.getTarget();
    }

    @Override
    public Object[] getArgs() {
        return this.joinpoint.getArgs();
    }

    @Override
    public Signature getSignature() {
        return this.joinpoint.getSignature();
    }

    @Override
    public SourceLocation getSourceLocation() {
        return this.joinpoint.getSourceLocation();
    }

    @Override
    public String getKind() {
        return this.joinpoint.getKind();
    }

    @Override
    public JoinPoint.StaticPart getStaticPart() {
        return this.joinpoint.getStaticPart();
    }

    /**
     * Calculate log target.
     *
     * @return The target
     */
    public Object targetize() {
        final Object target;
        final Method method = MethodSignature.class
                .cast(this.joinpoint.getSignature()).getMethod();
        if (Modifier.isStatic(method.getModifiers())) {
            target = method.getDeclaringClass();
        } else {
            target = this.joinpoint.getTarget();
        }
        return target;
    }

    /**
     * Get current method.
     *
     * @return Current method in join point
     */
    public Method currentMethod() {
        return ((MethodSignature) this.joinpoint.getSignature()).getMethod();
    }
}
