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

import com.jcabi.log.Logger;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Utility class with text functions for making mnemos.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
final class Mnemos {

    /**
     * Private ctor, it's a utility class.
     */
    private Mnemos() {
        // intentionally empty
    }

    /**
     * Make a string out of point.
     * @param point The point
     * @return Text representation of it
     */
    public static String toString(final ProceedingJoinPoint point) {
        return Mnemos.toString(
            MethodSignature.class.cast(point.getSignature()).getMethod(),
            point.getArgs()
        );
    }

    /**
     * Make a string out of method.
     * @param method The method
     * @param args Actual arguments of the method
     * @return Text representation of it
     */
    public static String toString(final Method method, final Object[] args) {
        final StringBuilder log = new StringBuilder();
        log.append('#').append(method.getName()).append('(');
        for (int pos = 0; pos < args.length; ++pos) {
            if (pos > 0) {
                log.append(", ");
            }
            log.append(Mnemos.toString(args[pos]));
        }
        log.append(')');
        return log.toString();
    }

    /**
     * Make a string out of an object.
     * @param arg The argument
     * @return Text representation of it
     */
    public static String toString(final Object arg) {
        String text;
        if (arg == null) {
            text = "NULL";
        } else {
            text = Logger.format("'%[text]s'", arg);
        }
        return text;
    }

}
