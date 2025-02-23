/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
 * @since 1.0
 */
final class ImprovedJoinPoint implements JoinPoint {

    /**
     * The original joinpoint.
     */
    private final JoinPoint joinpoint;

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
        final Method method = ((MethodSignature) this.joinpoint.getSignature()).getMethod();
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
