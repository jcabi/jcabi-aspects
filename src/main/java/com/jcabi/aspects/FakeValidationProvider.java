/*
 * Copyright (c) 2012-2025 Yegor Bugayenko
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
package com.jcabi.aspects;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.BootstrapConfiguration;
import javax.validation.ClockProvider;
import javax.validation.Configuration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Fake validation provider for JSR-303.
 *
 * This class can help when it's necessary to disable the entire JSR-303 validation
 * mechanism, but it's impossible to take certain classes from the classpath, which
 * are using JSR-303 and demand the presence of a validator.
 *
 * A text resource <tt>META-INF/services/javax.validation.spi.ValidationProvider</tt>
 * must be created, with a single line inside:
 * <tt>com.jcabi.aspects.FakeValidationProvider</tt>. Once this file is found
 * in the classpath, JSR-303 engine will use this fake validator provider and no constraints
 * will be reported in runtime.
 *
 * @since 0.25.0
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
public final class FakeValidationProvider implements
    ValidationProvider<FakeValidationProvider.FakeConfiguration> {

    @Override
    public FakeConfiguration createSpecializedConfiguration(final BootstrapState state) {
        return new FakeValidationProvider.FakeConfiguration();
    }

    @Override
    public Configuration<?> createGenericConfiguration(final BootstrapState state) {
        return new FakeValidationProvider.FakeConfiguration();
    }

    @Override
    public ValidatorFactory buildValidatorFactory(final ConfigurationState state) {
        return new FakeValidationProvider.FakeValidatorFactory();
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static class FakeValidatorFactory implements ValidatorFactory {
        @Override
        public Validator getValidator() {
            return new FakeValidationProvider.FakeValidator();
        }

        @Override
        public ValidatorContext usingContext() {
            return new FakeValidatorContext();
        }

        @Override
        public MessageInterpolator getMessageInterpolator() {
            return new FakeMessageInterpolator();
        }

        @Override
        public TraversableResolver getTraversableResolver() {
            return new FakeTraversableResolver();
        }

        @Override
        public ConstraintValidatorFactory getConstraintValidatorFactory() {
            return new FakeConstraintValidatorFactory();
        }

        @Override
        public ParameterNameProvider getParameterNameProvider() {
            return new FakeParameterNameProvider();
        }

        @Override
        public ClockProvider getClockProvider() {
            return new FakeClockProvider();
        }

        @Override
        public <T> T unwrap(final Class<T> clazz) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (final InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public void close() {
            // intentionally empty
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeClockProvider implements ClockProvider {
        @Override
        public Clock getClock() {
            return Clock.systemUTC();
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeParameterNameProvider implements ParameterNameProvider {
        @Override
        public List<String> getParameterNames(final Constructor<?> ctor) {
            return Collections.emptyList();
        }

        @Override
        public List<String> getParameterNames(final Method method) {
            return Collections.emptyList();
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeConstraintValidatorFactory
        implements ConstraintValidatorFactory {
        @Override
        @SuppressWarnings("PMD.SingletonClassReturningNewInstance")
        public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> clazz) {
            return clazz.cast(new FakeConstraintValidator<Annotation, String>());
        }

        @Override
        public void releaseInstance(final ConstraintValidator<?, ?> validator) {
            // intentionally empty
        }
    }

    /**
     * Fake class.
     *
     * @param <T> Type
     * @param <X> Another type
     * @since 0.25.0
     */
    static final class FakeConstraintValidator<T extends Annotation,
        X> implements ConstraintValidator<T, X> {
        @Override
        public boolean isValid(final Object obj,
            final ConstraintValidatorContext context) {
            return true;
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeMessageInterpolator implements MessageInterpolator {
        @Override
        public String interpolate(final String str, final Context context) {
            return "empty";
        }

        @Override
        public String interpolate(final String str, final Context context,
            final Locale locale) {
            return "empty";
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeTraversableResolver implements TraversableResolver {
        @Override
        public boolean isReachable(final Object obj, final Path.Node node,
            final Class<?> clazz, final Path path, final ElementType type) {
            return false;
        }

        @Override
        public boolean isCascadable(final Object obj, final Path.Node node,
            final Class<?> clazz, final Path path, final ElementType type) {
            return false;
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeValidatorContext implements ValidatorContext {
        @Override
        public ValidatorContext messageInterpolator(final MessageInterpolator inter) {
            return this;
        }

        @Override
        public ValidatorContext traversableResolver(final TraversableResolver resolver) {
            return this;
        }

        @Override
        public ValidatorContext constraintValidatorFactory(final
            ConstraintValidatorFactory factory) {
            return this;
        }

        @Override
        public ValidatorContext parameterNameProvider(final
            ParameterNameProvider provider) {
            return this;
        }

        @Override
        public ValidatorContext clockProvider(final ClockProvider provider) {
            return this;
        }

        @Override
        public ValidatorContext addValueExtractor(final ValueExtractor<?> extractor) {
            return this;
        }

        @Override
        public Validator getValidator() {
            return new FakeValidator();
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeValidator implements Validator {
        @Override
        public <T> Set<ConstraintViolation<T>> validate(final T type,
            final Class<?>... classes) {
            return new HashSet<>();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateProperty(final T type,
            final String str, final Class<?>... classes) {
            return new HashSet<>();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateValue(final Class<T> clazz,
            final String str, final Object obj, final Class<?>... classes) {
            return new HashSet<>();
        }

        @Override
        public BeanDescriptor getConstraintsForClass(final Class<?> clazz) {
            return null;
        }

        @Override
        public <T> T unwrap(final Class<T> clazz) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (final InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public ExecutableValidator forExecutables() {
            return new FakeExecutableValidator();
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeExecutableValidator implements ExecutableValidator {
        @Override
        public <T> Set<ConstraintViolation<T>> validateParameters(final T type,
            final Method method, final Object[] objects, final Class<?>... classes) {
            return new HashSet<>();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateReturnValue(final T type,
            final Method method, final Object obj, final Class<?>... classes) {
            return new HashSet<>();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateConstructorParameters(final
            Constructor<? extends T> constructor,
            final Object[] objects, final Class<?>... classes) {
            return new HashSet<>();
        }

        @Override
        public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(final
            Constructor<? extends T> constructor,
            final T type, final Class<?>... classes) {
            return new HashSet<>();
        }
    }

    /**
     * Fake class.
     *
     * @since 0.25.0
     */
    static final class FakeConfiguration implements Configuration<FakeConfiguration> {
        @Override
        public FakeConfiguration ignoreXmlConfiguration() {
            return this;
        }

        @Override
        public FakeConfiguration messageInterpolator(final MessageInterpolator interpolator) {
            return this;
        }

        @Override
        public FakeConfiguration traversableResolver(final TraversableResolver resolver) {
            return this;
        }

        @Override
        public FakeConfiguration constraintValidatorFactory(final
            ConstraintValidatorFactory factory) {
            return this;
        }

        @Override
        public FakeConfiguration parameterNameProvider(final ParameterNameProvider provider) {
            return this;
        }

        @Override
        public FakeConfiguration clockProvider(final ClockProvider provider) {
            return this;
        }

        @Override
        public FakeConfiguration addValueExtractor(final ValueExtractor<?> extractor) {
            return this;
        }

        @Override
        public FakeConfiguration addMapping(final InputStream stream) {
            return this;
        }

        @Override
        public FakeConfiguration addProperty(final String str, final String another) {
            return this;
        }

        @Override
        public MessageInterpolator getDefaultMessageInterpolator() {
            return null;
        }

        @Override
        public TraversableResolver getDefaultTraversableResolver() {
            return null;
        }

        @Override
        public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
            return null;
        }

        @Override
        public ParameterNameProvider getDefaultParameterNameProvider() {
            return null;
        }

        @Override
        public ClockProvider getDefaultClockProvider() {
            return null;
        }

        @Override
        public BootstrapConfiguration getBootstrapConfiguration() {
            return null;
        }

        @Override
        public ValidatorFactory buildValidatorFactory() {
            return new FakeValidatorFactory();
        }
    }
}
