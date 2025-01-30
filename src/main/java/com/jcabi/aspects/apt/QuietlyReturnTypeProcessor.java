/*
 * Copyright (c) 2012-2025, jcabi.com
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
