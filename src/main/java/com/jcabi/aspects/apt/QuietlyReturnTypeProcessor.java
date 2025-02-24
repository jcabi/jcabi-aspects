/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.aspects.apt;

import java.util.Set;
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
import javax.tools.Diagnostic;

/**
 * Annotation processor that checks whether methods annotated with
 * {@link com.jcabi.aspects.Quietly} have void return types.
 *
 * @since 0.16
 */
@SupportedAnnotationTypes("com.jcabi.aspects.Quietly")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class QuietlyReturnTypeProcessor extends AbstractProcessor {

    @Override
    public boolean process(
        final Set<? extends TypeElement> annotations,
        final RoundEnvironment env
    ) {
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
    private void checkMethods(
        final RoundEnvironment env,
        final TypeElement type
    ) {
        for (final Element element : env.getElementsAnnotatedWith(type)) {
            if (element.getKind() == ElementKind.METHOD) {
                final ExecutableElement method = (ExecutableElement) element;
                if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
                    this.processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "Method '%s.%s' annotated with @Quietly does not return void",
                            method.getEnclosingElement().getSimpleName(),
                            method.getSimpleName()
                        )
                    );
                }
            }
        }
    }

}
