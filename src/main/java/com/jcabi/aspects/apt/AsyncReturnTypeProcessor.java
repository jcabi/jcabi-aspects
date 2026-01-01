/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.apt;

import java.util.Set;
import java.util.concurrent.Future;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Annotation processor that checks whether methods annotated with
 * {@link com.jcabi.aspects.Async} have void or
 * {@link Future} return types.
 *
 * @since 0.17
 */
@SupportedAnnotationTypes("com.jcabi.aspects.Async")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class AsyncReturnTypeProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
        final RoundEnvironment env) {
        for (final TypeElement type : annotations) {
            this.checkMethods(env, type);
        }
        return true;
    }

    /**
     * Check methods annotated with {@link com.jcabi.aspects.Quietly}.
     * @param env The environment.
     * @param type The annotation type.
     */
    private void checkMethods(final RoundEnvironment env,
        final TypeElement type) {
        for (final Element element: env.getElementsAnnotatedWith(type)) {
            if (element.getKind() == ElementKind.METHOD) {
                final ExecutableElement method = (ExecutableElement) element;
                final TypeMirror returned = method.getReturnType();
                if (!returned.getKind().equals(TypeKind.VOID)
                    && !this.assignableToFuture(returned)) {
                    this.processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "Method '%s.%s' annotated with @Async does not return void or Future",
                            method.getEnclosingElement().getSimpleName(),
                            method.getSimpleName()
                        )
                    );
                }
            }
        }
    }

    /**
     * Is the given type assignable from {@link Future}?
     * @param type The type to check.
     * @return If it's assignable from Future.
     */
    private boolean assignableToFuture(final TypeMirror type) {
        final Types types = this.processingEnv.getTypeUtils();
        return types.isAssignable(
            types.erasure(type),
            types.erasure(
                this.processingEnv.getElementUtils()
                    .getTypeElement(Future.class.getCanonicalName()).asType()
            )
        );
    }

}
