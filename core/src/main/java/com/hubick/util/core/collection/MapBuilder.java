/*
 * Copyright 2017-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.collection;

import java.util.*;
import java.util.function.*;

import org.eclipse.jdt.annotation.*;


/**
 * A class for building a {@link Map}.
 * 
 * @param <K> The type of keys in this map builder.
 * @param <V> The type of values in this map builder.
 * @param <M> The type of map being built.
 * @param <MB> The concrete type of this builder.
 */
@NonNullByDefault
public class MapBuilder<K,V,@NonNull M extends Map<K,V>,@NonNull MB extends MapBuilder<K,V,M,@NonNull ?>> implements Supplier<M> {
  /**
   * The concrete {@link Class} of this builder.
   */
  protected final Class<MB> builderClass;
  /**
   * The {@link Map} we're building.
   */
  protected final M map;

  /**
   * Construct a new <code>MapBuilder</code>.
   * 
   * @param builderClass The concrete {@link Class} of this builder.
   * @param map The Map to build.
   */
  public MapBuilder(final Class<MB> builderClass, final M map) {
    this.builderClass = Objects.requireNonNull(builderClass);
    this.map = Objects.requireNonNull(map);
    return;
  }

  /**
   * {@linkplain Map#put(Object, Object) Put} supplied key/value pair into the {@linkplain #get() map}.
   * 
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the specified key.
   * @return this.
   */
  public MB put(final K key, final V value) {
    map.put(key, value);
    return Objects.requireNonNull(builderClass.cast(this));
  }

  /**
   * {@linkplain Map#put(Object, Object) Put} supplied key/value pair into the {@linkplain #get() map}, but only if the
   * value {@linkplain Optional#isPresent() is present}.
   * 
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the specified key.
   * @return this.
   */
  public MB putIfPresent(final K key, final Optional<? extends V> value) {
    value.ifPresent((v) -> map.put(key, v));
    return Objects.requireNonNull(builderClass.cast(this));
  }

  /**
   * {@linkplain Map#put(Object, Object) Put} supplied key/value pair into the {@linkplain #get() map}, but only if the
   * value is non null.
   * 
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the specified key.
   * @return this.
   */
  public MB putIfNonNull(final K key, final @Nullable V value) {
    if (value != null) map.put(key, value);
    return Objects.requireNonNull(builderClass.cast(this));
  }

  /**
   * {@linkplain Map#putAll(Map) Put} all of the supplied mappings into the {@linkplain #get() map}.
   * 
   * @param m The mappings to add to the map being built.
   * @return this.
   */
  public MB putAll(final Map<? extends K,? extends V> m) {
    map.putAll(m);
    return Objects.requireNonNull(builderClass.cast(this));
  }

  /**
   * {@linkplain Map#putAll(Map) Put} all of the supplied mappings into the {@linkplain #get() map}.
   * 
   * @param m The mappings to add to the map being built.
   * @return this.
   */
  public MB putAllIfPresent(final Optional<? extends Map<? extends K,? extends V>> m) {
    m.ifPresent((v) -> map.putAll(v));
    return Objects.requireNonNull(builderClass.cast(this));
  }

  /**
   * {@linkplain Map#putAll(Map) Put} all of the supplied mappings into the {@linkplain #get() map}.
   * 
   * @param m The mappings to add to the map being built.
   * @return this.
   */
  public MB putAllIfNonNull(final @Nullable Map<? extends K,? extends V> m) {
    if (m != null) map.putAll(m);
    return Objects.requireNonNull(builderClass.cast(this));
  }

  @Override
  public M get() {
    return map;
  }

  /**
   * A simplified {@link MapBuilder} for building generic {@link Map}'s and when you don't need to subclass the builder.
   *
   * @param <K> The type of keys in this map builder.
   * @param <V> The type of values in this map builder.
   */
  public static class Simple<K,V> extends MapBuilder<K,V,Map<K,V>,Simple<K,V>> {

    /**
     * Construct a new <code>Simple</code> {@link MapBuilder}.
     * 
     * @param map The Map to build.
     */
    @SuppressWarnings("unchecked")
    public Simple(final Map<K,V> map) {
      super((Class<Simple<K,V>>)(Object)MapBuilder.class, map);
      return;
    }

    /**
     * Construct a new <code>Simple</code> {@link MapBuilder}.
     * 
     * @param supplier A {@link Supplier} for the Map to build.
     */
    public Simple(final Supplier<Map<K,V>> supplier) {
      this(supplier.get());
      return;
    }

  } // Simple

}
