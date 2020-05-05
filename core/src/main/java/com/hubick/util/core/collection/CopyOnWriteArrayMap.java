/*
 * Copyright 2015-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.collection;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

import org.eclipse.jdt.annotation.*;


/**
 * A thread-safe {@link Map} implementation in which all mutative operations are implemented by making a fresh copy of
 * the underlying array.
 * 
 * @param <K> The type of keys in this map.
 * @param <V> The type of values in this map.
 */
@NonNullByDefault
public class CopyOnWriteArrayMap<K,V> extends AbstractMap<K,V> implements Serializable {
  /**
   * The {@link CopyOnWriteArrayList} of entries backing this Map implementation.
   */
  protected final CopyOnWriteArrayList<Map.Entry<K,V>> entries = new CopyOnWriteArrayList<>();
  /**
   * A read-only {@link EntrySet} to be returned from {@link #entrySet()}.
   */
  protected final EntrySet entrySet = new EntrySet();
  /**
   * A {@link Comparator} if you wish the {@linkplain #entrySet() entries} to be sorted.
   */
  protected final @Nullable Comparator<Map.Entry<K,V>> comparator;

  /**
   * Construct a new <code>CopyOnWriteArrayMap</code>.
   * 
   * @param m Another map to initialize the contents of this map with.
   * @param comparator A {@link Comparator} if you wish the {@linkplain #entrySet() entries} to be sorted.
   */
  public CopyOnWriteArrayMap(final @Nullable Map<K,V> m, final @Nullable Comparator<Map.Entry<K,V>> comparator) {
    this.comparator = comparator;
    if (m != null) entries.addAll(m.entrySet().stream().map((e) -> new AbstractMap.SimpleImmutableEntry<K,V>(e.getKey(), e.getValue())).collect(Collectors.toList()));
    if (comparator != null) entries.sort(comparator);
    return;
  }

  /**
   * Construct a new <code>CopyOnWriteArrayMap</code>.
   * 
   * @param m Another map to initialize the contents of this map with.
   */
  public CopyOnWriteArrayMap(final @Nullable Map<K,V> m) {
    this(m, null);
    return;
  }

  /**
   * Construct a new <code>CopyOnWriteArrayMap</code>.
   * 
   * @param comparator A {@link Comparator} if you wish the {@linkplain #entrySet() entries} to be sorted.
   */
  public CopyOnWriteArrayMap(final @Nullable Comparator<Map.Entry<K,V>> comparator) {
    this(null, comparator);
    return;
  }

  /**
   * Construct a new <code>CopyOnWriteArrayMap</code>.
   */
  public CopyOnWriteArrayMap() {
    this(null, null);
    return;
  }

  @Override
  public Set<Map.Entry<K,V>> entrySet() {
    return entrySet;
  }

  @Override
  public @Nullable V put(final K key, final V value) {
    final Optional<Map.Entry<K,V>> oldEntry;
    synchronized (entries) {
      oldEntry = entries.stream().filter((e) -> (key != null) ? key.equals(e.getKey()) : e.getKey() == null).findAny();
      if (oldEntry.isPresent()) entries.remove(oldEntry.get());
      entries.add(new AbstractMap.SimpleImmutableEntry<>(key, value));
      if (comparator != null) entries.sort(comparator);
    }
    return oldEntry.isPresent() ? oldEntry.get().getValue() : null;
  }

  @Override
  public void putAll(final Map<? extends K,? extends V> m) {
    synchronized (entries) {
      entries.removeAll(entries.stream().filter((e) -> m.containsKey(e.getKey())).collect(Collectors.toList()));
      entries.addAll(m.entrySet().stream().map((e) -> new AbstractMap.SimpleImmutableEntry<K,V>(e.getKey(), e.getValue())).collect(Collectors.toList()));
      if (comparator != null) entries.sort(comparator);
    }
    return;
  }

  @Override
  public @Nullable V remove(final @Nullable Object key) {
    final Optional<Map.Entry<K,V>> oldEntry;
    synchronized (entries) {
      oldEntry = entries.stream().filter((e) -> (key != null) ? key.equals(e.getKey()) : e.getKey() == null).findAny();
      if (oldEntry.isPresent()) entries.remove(oldEntry.get());
    }
    return oldEntry.isPresent() ? oldEntry.get().getValue() : null;
  }

  /**
   * A wrapper around {@link CopyOnWriteArrayMap#entries} to prevent {@link #add(Object)} and
   * {@link #addAll(Collection)} from being invoked (potentially leading to multiple entries having duplicate keys),
   * while still supporting {@link #remove(Object)}, etc (which is why {@link Collections#unmodifiableSet(Set)} isn't
   * sufficient).
   */
  protected final class EntrySet extends AbstractSet<Map.Entry<K,V>> implements Serializable {

    @Override
    public final Iterator<Map.Entry<K,V>> iterator() {
      return entries.iterator(); // Map.entrySet() says it's result *does* support remove, but here we're returning a iterator which does *not* support remove!
    }

    @Override
    public final Spliterator<Map.Entry<K,V>> spliterator() {
      return entries.spliterator();
    }

    @Override
    public final int size() {
      return entries.size();
    }

    @Override
    public final boolean isEmpty() {
      return entries.isEmpty();
    }

    @Override
    public final boolean contains(final @Nullable Object o) {
      return entries.contains(o);
    }

    @Override
    public final boolean containsAll(final Collection<?> c) {
      return entries.containsAll(c);
    }

    @Override
    public final boolean remove(final @Nullable Object o) {
      return entries.remove(o);
    }

    @Override
    public final boolean removeAll(final Collection<?> c) {
      return entries.removeAll(c);
    }

    @Override
    public final boolean removeIf(final Predicate<? super Map.Entry<K,V>> filter) {
      return entries.removeIf(filter);
    }

    @Override
    public final boolean retainAll(final Collection<?> c) {
      return entries.retainAll(c);
    }

    @Override
    public final void clear() {
      entries.clear();
      return;
    }

    @Override
    public final Object[] toArray() {
      return entries.toArray();
    }

    @Override
    public final <T> T[] toArray(final T[] a) {
      return entries.toArray(a);
    }

    @Override
    public final String toString() {
      return entries.toString();
    }

  } // EntrySet

}
