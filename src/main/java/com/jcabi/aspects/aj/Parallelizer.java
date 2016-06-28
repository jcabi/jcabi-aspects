/**
 * Copyright (c) 2012-2016, jcabi.com
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
import com.jcabi.log.VerboseThreads;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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
 * @since 0.10
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
     * @throws ParallelException If something goes wrong inside
     * @checkstyle IllegalThrowsCheck (4 lines)
     */
    @Around("execution(@com.jcabi.aspects.Parallel * * (..))")
    public Object wrap(final ProceedingJoinPoint point)
        throws ParallelException {
        final int total = ((MethodSignature) point.getSignature())
            .getMethod().getAnnotation(Parallel.class).threads();
        final Collection<Callable<Throwable>> callables =
            new ArrayList<Callable<Throwable>>(total);
        final CountDownLatch start = new CountDownLatch(1);
        for (int thread = 0; thread < total; ++thread) {
            callables.add(this.callable(point, start));
        }
        final ExecutorService executor = Executors
            .newFixedThreadPool(total, new VerboseThreads());
        final List<Future<Throwable>> futures =
            new ArrayList<Future<Throwable>>(total);
        for (final Callable<Throwable> callable : callables) {
            futures.add(executor.submit(callable));
        }
        start.countDown();
        final Collection<Throwable> failures = new LinkedList<Throwable>();
        for (final Future<Throwable> future : futures) {
            this.process(failures, future);
        }
        executor.shutdown();
        if (!failures.isEmpty()) {
            throw this.exceptions(failures);
        }
        return null;
    }

    /**
     * Process futures.
     * @param failures Collection of failures.
     * @param future Future tu process.
     */
    private void process(final Collection<Throwable> failures,
        final Future<Throwable> future) {
        final Throwable exception;
        try {
            exception = future.get();
            if (exception != null) {
                failures.add(exception);
            }
        } catch (final InterruptedException ex) {
            failures.add(ex);
        } catch (final ExecutionException ex) {
            failures.add(ex);
        }
    }

    /**
     * Create parallel exception.
     * @param failures List of exceptions from threads.
     * @return Aggregated exceptions.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private ParallelException exceptions(
        final Collection<Throwable> failures) {
        ParallelException current = null;
        for (final Throwable failure : failures) {
            current = new ParallelException(failure, current);
        }
        return current;
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
                Throwable result = null;
                try {
                    start.await();
                    point.proceed();
                    // @checkstyle IllegalCatchCheck (1 line)
                } catch (final Throwable ex) {
                    result = ex;
                }
                return result;
            }
        };
    }

    /**
     * Exception that encapsulates all exceptions thrown from threads.
     */
    private static final class ParallelException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x8743ef363febc422L;
        /**
         * Next parallel exception.
         */
        private final transient ParallelException next;
        /**
         * Constructor.
         * @param cause Cause of the current exception.
         * @param nxt Following exception.
         */
        protected ParallelException(final Throwable cause,
            final ParallelException nxt) {
            super(cause);
            this.next = nxt;
        }
        /**
         * Get next parallel exception.
         * @return Next exception.
         */
        public ParallelException getNext() {
            return this.next;
        }
    }

}
