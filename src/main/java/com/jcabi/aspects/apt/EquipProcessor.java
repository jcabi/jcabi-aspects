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
package com.jcabi.aspects.apt;

import com.jcabi.aspects.Equipped;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Annotation processor for {@link Equipped} classes.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.7.9
 * @link <a href="http://docs.oracle.com/javase/6/docs/api/javax/annotation/processing/Processor.html">Annotation Processing</a>
 */
@SupportedAnnotationTypes("com.jcabi.aspects.Equipped")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public final class EquipProcessor extends AbstractProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
        final RoundEnvironment env) {
        for (Element elm : env.getElementsAnnotatedWith(Equipped.class)) {
            if (!elm.getKind().equals(ElementKind.CLASS)) {
                throw new IllegalStateException(
                    String.format(
                        "illegal element submitted for processing: %s",
                        elm
                    )
                );
            }
            final TypeElement element = TypeElement.class.cast(elm);
            try {
                this.equip(element);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
            this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                String.format(
                    "added hashCode(), equals(), and toString() methods to %s",
                    element.getQualifiedName()
                ),
                elm
            );
        }
        return true;
    }

    /**
     * Equip given class.
     * @param element The type element to equip
     * @throws IOException If something goes wrong
     */
    private void equip(final TypeElement element) throws IOException {
        this.processingEnv.getMessager().printMessage(
            Diagnostic.Kind.NOTE,
            String.format(
                "@Equip annotation is not implemented yet: %s",
                element
            )
        );
    }

}
