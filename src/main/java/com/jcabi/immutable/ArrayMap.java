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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;

/**
 * Map on top of array.
 *
 * @param <K> Map key type
 * @param <V> Value key type
 * @author Yegor Bugayenko (yegor@woquo.com)
 * @version $Id$
 */
@Immutable
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "rawtypes", "unchecked", "PMD.TooManyMethods" })
public final class ArrayMap<K, V> implements ConcurrentMap<K, V> {

    /**
     * Comparator.
     */
    private static final class Cmp<K, V> implements
        Comparator<ArrayMap.ImmutableEntry<K, V>> {
        @Override
        public int compare(final ImmutableEntry<K, V> left,
            final ImmutableEntry<K, V> right) {
            int compare;
            if (left.getKey() instanceof Comparable) {
                compare = Comparable.class.cast(left.getKey())
                    .compareTo(right.getKey());
            } else {
                compare = left.getKey().toString()
                    .compareTo(right.getKey().toString());
            }
            return compare;
        }
    }

    /**
     * All entries.
     */
    private final transient ImmutableEntry<K, V>[] entries;

    /**
     * Public ctor.
     */
    public ArrayMap() {
        this.entries = new ArrayMap.ImmutableEntry[0];
    }

    /**
     * Public ctor.
     * @param map The original map
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public ArrayMap(@NotNull final Map<K, V> map) {
        final SortedSet<ArrayMap.ImmutableEntry<K, V>> entrs =
            new TreeSet<ArrayMap.ImmutableEntry<K, V>>(
                new ArrayMap.Cmp<K, V>()
            );
        for (Map.Entry<K, V> entry : map.entrySet()) {
            entrs.add(new ArrayMap.ImmutableEntry<K, V>(entry));
        }
        this.entries = entrs.toArray(new ArrayMap.ImmutableEntry[entrs.size()]);
    }

    /**
     * Make a new one with an extra entry.
     * @param key The key
     * @param value The value
     * @return New map
     */
    public ArrayMap<K, V> with(@NotNull final K key, @NotNull final V value) {
        final ConcurrentMap<K, V> map =
            new ConcurrentHashMap<K, V>(this.entries.length);
        map.putAll(this);
        map.put(key, value);
        return new ArrayMap<K, V>(map);
    }

    /**
     * Make a new one without this key.
     * @param key The key
     * @return New map
     */
    public ArrayMap<K, V> without(@NotNull final K key) {
        final ConcurrentMap<K, V> map =
            new ConcurrentHashMap<K, V>(this.entries.length);
        map.putAll(this);
        map.remove(key);
        return new ArrayMap<K, V>(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        final boolean equals;
        if (object instanceof ArrayMap) {
            equals = Arrays.deepEquals(
                this.entries, ArrayMap.class.cast(object).entries
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
        for (ImmutableEntry<K, V> item : this.entries) {
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
        return this.entries.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.entries.length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        boolean contains = false;
        for (Map.Entry<K, V> entry : this.entries) {
            if (entry.getKey().equals(key)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(final Object value) {
        boolean contains = false;
        for (Map.Entry<K, V> entry : this.entries) {
            if (entry.getValue().equals(value)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(final Object key) {
        V value = null;
        for (Map.Entry<K, V> entry : this.entries) {
            if (entry.getKey().equals(key)) {
                value = entry.getValue();
                break;
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V putIfAbsent(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(final K key, final V old, final V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V replace(final K key, final V value) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Set<K> keySet() {
        final Set<K> keys = new LinkedHashSet<K>(this.entries.length);
        for (Map.Entry<K, V> entry : this.entries) {
            keys.add(entry.getKey());
        }
        return Collections.unmodifiableSet(keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Collection<V> values() {
        final Collection<V> values = new ArrayList<V>(this.entries.length);
        for (Map.Entry<K, V> entry : this.entries) {
            values.add(entry.getValue());
        }
        return Collections.unmodifiableCollection(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Set<Map.Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(
            new LinkedHashSet<Map.Entry<K, V>>(Arrays.asList(this.entries))
        );
    }

    /**
     * Immutable map entry.
     */
    @Immutable
    private static final class ImmutableEntry<K, V> extends
        AbstractMap.SimpleImmutableEntry<K, V> {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 1L;
        /**
         * Public ctor.
         * @param entry Entry to encapsulate
         */
        private ImmutableEntry(final Map.Entry<K, V> entry) {
            this(entry.getKey(), entry.getValue());
        }
        /**
         * Public ctor.
         * @param key The key
         * @param value The value
         */
        private ImmutableEntry(final K key, final V value) {
            super(key, value);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("%s=%s", this.getKey(), this.getValue());
        }
    }

}
