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
import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Utility class with text functions for making mnemos.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.AvoidUsingShortType" })
final class Mnemos {

    /**
     * Comma between elements.
     */
    private static final String COMMA = ", ";

    /**
     * Dots that skip.
     */
    private static final String DOTS = "...";

    /**
     * Private ctor, it's a utility class.
     */
    private Mnemos() {
        // intentionally empty
    }

    /**
     * Make a string out of point.
     * @param point The point
     * @param trim Shall we trim long texts?
     * @param skip Shall we skip details and output just dots?
     * @param logthis Shall we add toString result to log?
     * @return Text representation of it
     * @since 0.8.1
     * @checkstyle ParameterNumber (3 lines)
     */
    public static String toText(final ProceedingJoinPoint point,
        final boolean trim, final boolean skip, final boolean logthis) {
        final String additional;
        if (logthis && (point.getThis() != null)) {
            additional = point.getThis().toString();
        } else {
            additional = "";
        }
        return Mnemos.toText(
            MethodSignature.class.cast(point.getSignature()).getMethod(),
            point.getArgs(), additional,
            trim, skip
        );
    }
    /**
     * Make a string out of point.
     * @param point The point
     * @param trim Shall we trim long texts?
     * @param skip Shall we skip details and output just dots?
     * @return Text representation of it
     * @since 0.7.19
     */
    public static String toText(final ProceedingJoinPoint point,
        final boolean trim, final boolean skip) {
        return Mnemos.toText(
            MethodSignature.class.cast(point.getSignature()).getMethod(),
            point.getArgs(),
            trim, skip
        );
    }

    /**
     * Make a string out of point.
     * @param point The point
     * @param trim Shall we trim long texts?
     * @return Text representation of it
     * @deprecated Use toText(Method,Object,boolean,boolean) instead
     */
    @Deprecated
    public static String toText(final ProceedingJoinPoint point,
        final boolean trim) {
        return Mnemos.toText(point, trim, false);
    }

    /**
     * Make a string out of point.
     * @param point The point
     * @param trim Shall we trim long texts?
     * @return Text representation of it
     * @deprecated Use toText() instead
     */
    @Deprecated
    public static String toString(final ProceedingJoinPoint point,
        final boolean trim) {
        return Mnemos.toText(point, trim, false);
    }

    /**
     * Make a string out of method.
     * @param method The method
     * @param args Actual arguments of the method
     * @param additional Additional text to add before log line
     * @param trim Shall we trim long texts?
     * @param skip Shall we skip details and output just dots?
     * @return Text representation of it
     * @since 0.8.1
     * @checkstyle ParameterNumber (4 lines)
     */
    public static String toText(final Method method, final Object[] args,
        final String additional, final boolean trim, final boolean skip) {
        final StringBuilder log = new StringBuilder();
        if (additional != null) {
            log.append(additional);
        }
        log.append('#').append(method.getName()).append('(');
        if (skip) {
            log.append(Mnemos.DOTS);
        } else {
            for (int pos = 0; pos < args.length; ++pos) {
                if (pos > 0) {
                    log.append(Mnemos.COMMA);
                }
                log.append(Mnemos.toText(args[pos], trim, false));
            }
        }
        log.append(')');
        return log.toString();
    }

    /**
     * Make a string out of method.
     * @param method The method
     * @param args Actual arguments of the method
     * @param trim Shall we trim long texts?
     * @param skip Shall we skip details and output just dots?
     * @return Text representation of it
     * @since 0.7.19
     * @checkstyle ParameterNumber (4 lines)
     */
    public static String toText(final Method method, final Object[] args,
        final boolean trim, final boolean skip) {
        return Mnemos.toText(method, args, "", trim, skip);
    }

    /**
     * Make a string out of method.
     * @param method The method
     * @param args Actual arguments of the method
     * @param trim Shall we trim long texts?
     * @return Text representation of it
     * @deprecated Use toText(Method,Object,boolean,boolean) instead
     */
    @Deprecated
    public static String toText(final Method method, final Object[] args,
        final boolean trim) {
        return Mnemos.toText(method, args, trim, false);
    }

    /**
     * Make a string out of method.
     * @param method The method
     * @param args Actual arguments of the method
     * @param trim Shall we trim long texts?
     * @return Text representation of it
     * @deprecated Use toText() instead
     */
    @Deprecated
    public static String toString(final Method method, final Object[] args,
        final boolean trim) {
        return Mnemos.toText(method, args, trim, false);
    }

    /**
     * Make a string out of an exception.
     * @param exp The exception
     * @return Text representation of it
     */
    public static String toText(final Throwable exp) {
        final StringBuilder text = new StringBuilder();
        text.append(exp.getClass().getName());
        final String msg = exp.getMessage();
        if (msg != null) {
            text.append('(').append(msg).append(')');
        }
        return text.toString();
    }

    /**
     * Make a string out of an object.
     * @param arg The argument
     * @param trim Shall we trim long texts?
     * @param skip Shall we skip it with dots?
     * @return Text representation of it
     * @since 0.7.19
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public static String toText(final Object arg, final boolean trim,
        final boolean skip) {
        final StringBuilder text = new StringBuilder();
        if (arg == null) {
            text.append("NULL");
        } else if (skip) {
            text.append(Mnemos.DOTS);
        } else {
            try {
                final String mnemo = Mnemos.toText(arg);
                if (trim) {
                    text.append(Logger.format("%[text]s", mnemo));
                } else {
                    text.append(mnemo);
                }
            // @checkstyle IllegalCatch (1 line)
            } catch (final Throwable ex) {
                text.append(
                    String.format(
                        "[%s thrown %s]",
                        arg.getClass().getName(),
                        Mnemos.toText(ex)
                    )
                );
            }
        }
        return text.toString();
    }

    /**
     * Make a string out of an object.
     * @param arg The argument
     * @param trim Shall we trim long texts?
     * @return Text representation of it
     * @deprecated Use toText(Object,boolean,boolean) instead
     */
    @Deprecated
    public static String toText(final Object arg, final boolean trim) {
        return Mnemos.toText(arg, trim, false);
    }

    /**
     * Make a string out of an object.
     * @param arg The argument
     * @param trim Shall we trim long texts?
     * @return Text representation of it
     * @deprecated Use toText() instead
     */
    @Deprecated
    public static String toString(final Object arg, final boolean trim) {
        return Mnemos.toText(arg, trim, false);
    }

    /**
     * Make a string out of an object.
     * @param arg The argument
     * @return Text representation of it
     */
    private static String toText(final Object arg) {
        String text;
        if (arg.getClass().isArray()) {
            if (arg instanceof Object[]) {
                text = Mnemos.objectArrays((Object[]) arg);
            } else {
                text = Mnemos.primitiveArrays(arg);
            }
        } else {
            final String origin = arg.toString();
            if (arg instanceof String || origin.contains(" ")
                || origin.isEmpty()) {
                text = String.format("'%s'", origin);
            } else {
                text = origin;
            }
        }
        return text;
    }

    /**
     * Text representation of object arrays.
     * @param arg Array to change into String.
     * @return Array in String.
     */
    private static String objectArrays(final Object... arg) {
        final StringBuilder bldr = new StringBuilder();
        bldr.append('[');
        for (final Object item : arg) {
            if (bldr.length() > 1) {
                bldr.append(Mnemos.COMMA);
            }
            bldr.append(Mnemos.toText(item, false, false));
        }
        return bldr.append(']').toString();
    }

    /**
     * Text representation of primitive arrays.
     * @param arg Array to change into String.
     * @return Array in String.
     */
    private static String primitiveArrays(final Object arg) {
        final String text;
        if (arg instanceof char[]) {
            text = Arrays.toString((char[]) arg);
        } else if (arg instanceof byte[]) {
            text = Arrays.toString((byte[]) arg);
        } else if (arg instanceof short[]) {
            text = Arrays.toString((short[]) arg);
        } else if (arg instanceof int[]) {
            text = Arrays.toString((int[]) arg);
        } else if (arg instanceof long[]) {
            text = Arrays.toString((long[]) arg);
        } else if (arg instanceof float[]) {
            text = Arrays.toString((float[]) arg);
        } else if (arg instanceof double[]) {
            text = Arrays.toString((double[]) arg);
        } else if (arg instanceof boolean[]) {
            text = Arrays.toString((boolean[]) arg);
        } else {
            text = "[unknown]";
        }
        return text;
    }
}
