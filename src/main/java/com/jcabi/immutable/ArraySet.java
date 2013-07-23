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
package com.jcabi.immutable;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import javax.validation.constraints.NotNull;

/**
 * Set on top of array.
 *
 * @param <T> Value key type
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
@Immutable
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "unchecked", "PMD.TooManyMethods" })
public final class ArraySet<T> implements Set<T> {

    /**
     * All vals.
     */
    private final transient T[] values;

    /**
     * Public ctor.
     */
    public ArraySet() {
        this.values = (T[]) new Object[0];
    }

    /**
     * Public ctor.
     * @param set Original set
     */
    public ArraySet(@NotNull final Collection<T> set) {
        if (set instanceof ArraySet) {
            this.values = ((ArraySet<T>) set).values;
        } else {
            final Set<T> hset = new HashSet<T>(set);
            this.values = hset.toArray((T[]) new Object[hset.size()]);
        }
    }

    /**
     * Make a new one with an extra entry.
     * @param value The value
     * @return New set
     */
    public ArraySet<T> with(@NotNull final T value) {
        final Collection<T> list = new HashSet<T>(this.size() + 1);
        list.addAll(this);
        list.remove(value);
        list.add(value);
        return new ArraySet<T>(list);
    }

    /**
     * Make a new one with some extra entries.
     * @param vals Values to add
     * @return New set
     */
    public ArraySet<T> with(@NotNull final Collection<T> vals) {
        final Collection<T> list = new HashSet<T>(this.size());
        list.addAll(this);
        list.removeAll(vals);
        list.addAll(vals);
        return new ArraySet<T>(list);
    }

    /**
     * Make a new one without an extra entry.
     * @param value The value
     * @return New set
     */
    public ArraySet<T> without(@NotNull final T value) {
        final Collection<T> list = new LinkedList<T>();
        list.addAll(this);
        list.remove(value);
        return new ArraySet<T>(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        final boolean equals;
        if (object instanceof ArraySet) {
            equals = Arrays.deepEquals(
                this.values, ArraySet.class.cast(object).values
            );
        } else {
            equals = false;
        }
        return equals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (T item : this.values) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(item);
        }
        return text.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.values.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.values.length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object key) {
        return Arrays.asList(this.values).contains(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(this.values).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        final Object[] array = new Object[this.values.length];
        System.arraycopy(this.values, 0, array, 0, this.values.length);
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(final T[] array) {
        T[] dest;
        if (array.length == this.values.length) {
            dest = array;
        } else {
            dest = (T[]) new Object[this.values.length];
        }
        System.arraycopy(this.values, 0, dest, 0, this.values.length);
        return dest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final T element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> col) {
        return Arrays.asList(this.values).containsAll(col);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends T> col) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> col) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> col) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}
