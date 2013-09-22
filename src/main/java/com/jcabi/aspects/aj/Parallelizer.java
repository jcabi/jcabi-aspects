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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Parallel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Execute method in multiple threads.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 0.7.22
 * @see com.jcabi.aspects.Parallel
 */
@Aspect
@Immutable
public final class Parallelizer {

    /**
     * Execute method in multiple threads.
     *
     * <p>This aspect should be used only on void returning methods.
     *
     * <p>Try NOT to change the signature of this method, in order to keep
     * it backward compatible.
     *
     * @param point Joint point
     * @return The result of call
     * @throws Throwable If something goes wrong inside
     * @checkstyle IllegalThrowsCheck (4 lines)
     */
    @Around("execution(@com.jcabi.aspects.Parallel void * (..))")
    public Object wrap(final ProceedingJoinPoint point) throws Throwable {
        final int threadCount = ((MethodSignature) point.getSignature())
            .getMethod().getAnnotation(Parallel.class).threads();
        final Collection<Callable<Throwable>> callables =
            new ArrayList<Callable<Throwable>>();
        final CountDownLatch start = new CountDownLatch(1);
        for (int thread = 0; thread < threadCount; ++thread) {
            callables.add(this.callable(point, start));
        }
        final ExecutorService executor = Executors
            .newFixedThreadPool(threadCount);
        final List<Future<Throwable>> futures =
            new ArrayList<Future<Throwable>>();
        for (Callable<Throwable> callable : callables) {
            futures.add(executor.submit(callable));
        }
        start.countDown();
        for (Future<Throwable> future : futures) {
            final Throwable exception = future.get();
            if (exception != null) {
                executor.shutdownNow();
                throw exception;
            }
        }
        return null;
    }

    /**
     * Create callable that executes join point.
     * @param point Join point to use.
     * @param start Latch to use.
     * @return Created callable.
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private Callable<Throwable> callable(final ProceedingJoinPoint point,
        final CountDownLatch start) {
        return new Callable<Throwable>() {
            @Override
            public Throwable call() {
                try {
                    start.await();
                    point.proceed();
                    // @checkstyle IllegalCatchCheck (1 line)
                } catch (Throwable ex) {
                    return ex;
                }
                return null;
            }
        };
    }
}