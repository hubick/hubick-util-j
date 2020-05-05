/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.collection;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.eclipse.jdt.annotation.*;

import com.hubick.util.core.function.*;


/**
 * {@link Collection} Utilities.
 */
@NonNullByDefault
public abstract class CollUtil {
  /**
   * An accumulator which can be used to {@linkplain Stream#collect(Supplier, BiConsumer, BiConsumer) collect} into a
   * {@link Properties} instance
   * (<code>.collect(Properties::new, CollUtil.PROPERTIES_ACCUMULATOR, Properties::putAll)</code>).
   */
  public static final BiConsumer<Properties,? super Map.Entry<?,?>> PROPERTIES_ACCUMULATOR = (props, entry) -> props.setProperty(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));

  /**
   * Ensure the the supplied <code>iterable</code> is not <code>null</code>.
   * 
   * @param iterable The {@link Iterable} to ensure is not <code>null</code>.
   * @param <T> The type iterated over by the <code>iterable</code> argument.
   * @return The supplied <code>iterable</code>, if not <code>null</code>, else an {@linkplain Collections#emptyList()
   * empty list}.
   * @see Collections#emptyList()
   */
  public static final <T> Iterable<T> mkNonNull(final @Nullable Iterable<T> iterable) {
    return Optional.ofNullable(iterable).orElse(Collections.<T> emptyList());
  }

  /**
   * Return the {@linkplain Collection#size() size} of the supplied {@link Iterable}.
   * 
   * @param iterable The {@link Iterable} to calculate the {@linkplain Collection#size() size} of.
   * @return The {@linkplain Collection#size() size} of the iterable.
   */
  public static final long size(final @Nullable Iterable<?> iterable) {
    return Optional.ofNullable(iterable).<Long> map((i) -> (i instanceof Collection) ? ((Collection<?>)i).size() : StreamSupport.stream(i.spliterator(), false).count()).orElse(Long.valueOf(0));
  }

  /**
   * Get the {@linkplain Iterator#next() next} element from the supplied {@link Iterator} after
   * {@linkplain Optional#filter(Predicate) filtering} it's elements.
   * 
   * @param <T> The type of object being iterated.
   * @param iterator The {@link Iterator} to retrieve the {@linkplain Iterator#next() next} element from.
   * @param filter A {@link Predicate} to {@linkplain Optional#filter(Predicate) filter} iterated elements with.
   * @return An {@link Optional} containing the next element.
   */
  public static final <T> Optional<@NonNull T> next(final @Nullable Iterator<? extends T> iterator, final @Nullable Predicate<? super T> filter) {
    return Optional.ofNullable(iterator).flatMap((i) -> StreamSupport.stream(Spliterators.<T> spliteratorUnknownSize(i, Spliterator.ORDERED), false).filter(FuncUtil.mkNonNull(filter)).findFirst());
  }

  /**
   * Return the first item of the supplied <code>list</code>.
   * 
   * @param list The list to retrieve the first element of.
   * @param <T> The type of the <code>list</code> items.
   * @return The first item in <code>list</code>, or {@linkplain Optional#empty() empty} if there isn't one.
   */
  @SafeVarargs
  public static final <T> Optional<@NonNull T> first(final @Nullable T @Nullable... list) {
    return Optional.ofNullable(list).filter((l) -> l.length >= 1).map((l) -> l[0]);
  }

  /**
   * Return the first item of the supplied <code>iterable</code>.
   * 
   * @param <T> The type of the <code>iterable</code> items.
   * @param iterable The {@link Iterable} to retrieve the first element of.
   * @return The first item from the <code>iterable</code>, or {@linkplain Optional#empty() empty} if there isn't one.
   */
  public static final <T> Optional<@NonNull T> first(final @Nullable Iterable<T> iterable) {
    return Optional.ofNullable(iterable).flatMap((i) -> StreamSupport.stream(i.spliterator(), false).findFirst());
  }

  /**
   * Return the first item of the supplied <code>iterator</code>.
   * 
   * @param <T> The type of the <code>iterator</code> items.
   * @param iterator The {@link Iterator} to retrieve the first element of.
   * @return The first item from the <code>iterator</code>, or {@linkplain Optional#empty() empty} if there isn't one.
   */
  public static final <T> Optional<@NonNull T> first(final @Nullable Iterator<T> iterator) {
    return ((iterator != null) && (iterator.hasNext())) ? Optional.ofNullable(iterator.next()) : Optional.empty();
  }

  /**
   * Return the first non-<code>null</code> item of the supplied <code>iterable</code>.
   * 
   * @param <T> The type of the <code>iterable</code> items.
   * @param iterable The {@link Iterable} to retrieve the first element of.
   * @return The first non-<code>null</code> item from the <code>iterable</code>, or {@linkplain Optional#empty() empty}
   * if there isn't one.
   */
  public static final <T> Optional<@NonNull T> firstNN(final @Nullable Iterable<T> iterable) {
    return Optional.ofNullable(iterable).flatMap((i) -> StreamSupport.stream(i.spliterator(), false).filter(Objects::nonNull).findFirst());
  }

  /**
   * Return the last item of the supplied <code>list</code>.
   * 
   * @param list The list to retrieve the last element of.
   * @param <T> The type of the <code>list</code> items.
   * @return The last (optionally non-<code>null</code>) item in <code>list</code>, or {@linkplain Optional#empty()
   * empty} if there isn't one.
   */
  @SafeVarargs
  public static final <T> Optional<@NonNull T> last(final @Nullable T @Nullable... list) {
    return Optional.ofNullable(list).filter((l) -> l.length >= 1).map((l) -> l[l.length - 1]);
  }

  /**
   * Return the last item of the supplied <code>iterable</code>.
   * 
   * @param <T> The type of the <code>iterable</code> items.
   * @param iterable The {@link Iterable} to retrieve the last element of.
   * @return The last item from the <code>iterable</code>, or {@linkplain Optional#empty() empty} if there isn't one.
   */
  public static final <T> Optional<@NonNull T> last(final @Nullable Iterable<T> iterable) {
    return Optional.ofNullable(iterable).flatMap((i) -> StreamSupport.stream(i.spliterator(), false).reduce((p, c) -> c));
  }

  /**
   * Return the last non-<code>null</code> item of the supplied <code>iterable</code>.
   * 
   * @param <T> The type of the <code>iterable</code> items.
   * @param iterable The {@link Iterable} to retrieve the last element of.
   * @return The last non-<code>null</code> item from the <code>iterable</code>, or {@linkplain Optional#empty() empty}
   * if there isn't one.
   */
  public static final <T> Optional<@NonNull T> lastNN(final @Nullable Iterable<T> iterable) {
    return Optional.ofNullable(iterable).flatMap((i) -> StreamSupport.stream(i.spliterator(), false).filter(Objects::nonNull).reduce((p, c) -> c));
  }

  /**
   * {@linkplain Collection#addAll(Collection) Add} all the elements from the <code>iterable</code> {@link Iterable} to
   * the specified <code>collection</code>.
   * 
   * @param <T> The type of elements contained within the <code>collection</code>.
   * @param <E> The type of elements being added.
   * @param <C> The type of collection itself.
   * @param collection The {@link Collection} to {@linkplain Collection#addAll(Collection) add} elements to.
   * @param iterable The elements to {@linkplain Collection#addAll(Collection) add}, or <code>null</code>.
   * @return The supplied <code>collection</code>.
   * @throws IllegalArgumentException If some property of an element of <code>iterable</code> prevents it from being
   * added to the <code>collection</code>.
   * @throws UnsupportedOperationException If the {@link Collection#addAll(Collection)} operation is not supported by
   * the <code>collection</code>.
   * @throws ClassCastException If the class of an element of <code>iterable</code> prevents it from being added to the
   * <code>collection</code>.
   * @throws NullPointerException If <code>iterable</code> contains a <code>null</code> element and the
   * <code>collection</code> does not permit <code>null</code> elements.
   * @throws IllegalStateException If not all the elements can be added at this time due to insertion restrictions.
   * @see Collection#addAll(Collection)
   */
  public static final <T,E extends T,C extends Collection<T>> C addAll(final C collection, final @Nullable Iterable<E> iterable) throws IllegalArgumentException, UnsupportedOperationException, ClassCastException, NullPointerException, IllegalStateException {
    if (iterable == null) return collection;
    if (iterable instanceof Collection) {
      collection.addAll((Collection<E>)iterable);
    } else {
      iterable.forEach((e) -> collection.add(e));
    }
    return collection;
  }

  /**
   * Returns the value to which the specified <code>key</code> is mapped.
   * 
   * @param <V> The type of values stored by the <code>map</code>.
   * @param map The Map to retrieve the value from.
   * @param key the key whose associated value is to be returned.
   * @return The value to which the specified <code>key</code> is mapped, or {@linkplain Optional#empty() empty} if the
   * <code>map</code> contains no mapping for the <code>key</code>.
   * @see Map#get(Object)
   */
  public static final <V> Optional<V> get(final @Nullable Map<?,V> map, final Object key) {
    return Optional.ofNullable(map).map((m) -> m.get(key));
  }

  /**
   * Return the {@linkplain Map#keySet() keys} contained within the <code>map</code>.
   * 
   * @param <V> The type of key the Map contains.
   * @param map The Map from which to retrieve the keys.
   * @return A Set view of the Map keys, or an {@linkplain Collections#emptySet() empty set}, never <code>null</code>.
   * @see Map#keySet()
   */
  public static final <V> Set<V> keySet(final @Nullable Map<V,?> map) {
    return Optional.ofNullable(map).map(Map::keySet).map(Collections::unmodifiableSet).orElse(Collections.emptySet());
  }

  /**
   * Return the {@linkplain Map#values() values} contained within the <code>map</code>.
   * 
   * @param <V> The type of value the Map contains.
   * @param map The Map from which to retrieve the values.
   * @return A Collection view of the Map values, never <code>null</code>.
   * @see Map#values()
   */
  public static final <V> Collection<V> values(final @Nullable Map<?,V> map) {
    return Optional.ofNullable(map).map(Map::values).map(Collections::unmodifiableCollection).orElse(Collections.emptyList());
  }

  /**
   * This does the same as {@link Map#get(Object)}, except it will work on a {@link HashMap} even when the key's
   * {@linkplain Comparable#compareTo(Object) natural ordering is inconsistent with equals}.
   * 
   * @param entries The {@link java.util.Map.Entry Entry}'s to retrieve the result from.
   * @param key The {@linkplain java.util.Map.Entry#getKey() key} whose associated value is to be returned.
   * @param <K> The type of keys stored by the <code>map</code>.
   * @param <V> The type of values stored by the <code>map</code>.
   * @return The <code>map</code> entry to which the specified <code>key</code> is mapped, or
   * {@linkplain Optional#empty() empty} if there was no entry for the given <code>key</code>.
   */
  public static final <K,V> Optional<Map.Entry<K,V>> getEntry(final @Nullable Iterable<Map.Entry<K,V>> entries, final K key) {
    return Optional.ofNullable(entries).flatMap((e) -> StreamSupport.stream(e.spliterator(), false).filter(Objects::nonNull).filter((entry) -> (key != null) ? key.equals(entry.getKey()) : (entry.getKey() == null)).findAny());
  }

  /**
   * Return the {@link java.util.Map.Entry Entry} having the specified {@linkplain java.util.Map.Entry#getKey() key}.
   * 
   * @param entries The {@link java.util.Map.Entry Entry}'s to retrieve the result from.
   * @param key The {@linkplain java.util.Map.Entry#getKey() key} whose associated value is to be returned.
   * @param <K> The type of keys stored by the <code>map</code>.
   * @param <V> The type of values stored by the <code>map</code>.
   * @return The <code>map</code> entry to which the specified <code>key</code> is mapped, or
   * {@linkplain Optional#empty() empty} if there was no entry for the given <code>key</code>.
   */
  public static final <K,V> Optional<Map.Entry<K,V>> getEntry(final @Nullable Iterator<Map.Entry<K,V>> entries, final K key) {
    return Optional.ofNullable(entries).flatMap((e) -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(entries, Spliterator.IMMUTABLE | Spliterator.ORDERED), false).filter(Objects::nonNull).filter((entry) -> (key != null) ? key.equals(entry.getKey()) : (entry.getKey() == null)).findAny());
  }

  /**
   * Get the {@linkplain java.util.Map.Entry#getValue() value} of the supplied <code>entry</code>.
   * 
   * @param <K> The type of mapped keys.
   * @param <V> The type of mapped values.
   * @param entry The {@link java.util.Map.Entry Map.Entry} to get the {@linkplain java.util.Map.Entry#getValue() value}
   * from.
   * @return The {@linkplain java.util.Map.Entry#getValue() value} of the <code>entry</code>, or
   * {@linkplain Optional#empty() empty} if there wasn't one.
   */
  public static final <K,V> Optional<@NonNull V> getValue(final Map.@Nullable Entry<K,V> entry) {
    return Optional.ofNullable(entry).map(Map.Entry::getValue);
  }

  /**
   * Return a String representation of the supplied <code>list</code>.
   * 
   * @param list The list of elements to iterate through.
   * @param delimiter The CharSequence that the result should have placed between <code>list</code> items.
   * @param prefix The CharSequence the result should be prefixed with.
   * @param suffix The CharSequence the result should be suffixed with.
   * @return A {@link String} representation of the supplied <code>list</code>.
   */
  public static final String toString(final Iterable<?> list, final @Nullable CharSequence delimiter, final @Nullable CharSequence prefix, final @Nullable CharSequence suffix) {
    return StreamSupport.stream(list.spliterator(), false).map(String::valueOf).collect(Collectors.joining((delimiter != null) ? delimiter : "", (prefix != null) ? prefix : "", (suffix != null) ? suffix : ""));
  }

  /**
   * Return a String representation created by iterating through the supplied <code>list</code> of values.
   * 
   * @param list The list of elements to iterate through.
   * @return A {@link String} representation of the supplied <code>list</code> in the format
   * <code>"[item1,item2,etc]"</code>.
   * @see #toString(Iterable, CharSequence, CharSequence, CharSequence)
   */
  public static final String toString(final Iterable<?> list) {
    return toString(list, ",", "[", "]");
  }

  /**
   * Create a {@link Comparator} to compare objects based on their {@linkplain Object#hashCode() hashCode}.
   * 
   * @param <T> The type of objects being compared.
   * @return The {@linkplain Object#hashCode() hashCode} {@link Comparator}.
   */
  public static final <T> Comparator<T> mkHashCodeComparator() {
    class HashCodeComparator implements Comparator<T> {

      @Override
      public int compare(final T o1, final T o2) {
        if (o1 == null) return (o2 == null) ? 0 : 1;
        else if (o2 == null) return -1;
        else if (o1 == o2) return 0;
        final int hashCode1;
        synchronized (o1) {
          hashCode1 = o1.hashCode();
        }
        final int hashCode2;
        synchronized (o2) {
          hashCode2 = o2.hashCode();
        }
        return (hashCode1 < hashCode2 ? -1 : (hashCode1 == hashCode2 ? 0 : 1));
      }

    };
    return new HashCodeComparator();
  }

  /**
   * Get a {@linkplain Map} which acts as a wrapper around a supplied primary <code>childMap</code>, where the
   * implementation additionally examines a provided secondary <code>parentMap</code> for any requested keys not
   * {@link Map#containsKey(Object) contained} within the wrapped child.
   * 
   * @param <K> The type of keys in the chained maps.
   * @param <V> The type of values in the chained maps.
   * @param parentMap The Map to be examined for keys not found in the <code>childMap</code>.
   * @param childMap The Map to be examined first for any queried keys.
   * @return A new Map which chains together the given <code>parentMap</code> and <code>childMap</code> into a
   * hierarchy.
   */
  protected static final <K,V> Map<K,V> chain(final Map<K,V> parentMap, final Map<K,V> childMap) {
    class HierarchicalMap extends AbstractMap<K,V> implements Serializable {
      /**
       * @see #entrySet()
       */
      protected final Set<Map.Entry<K,V>> entrySet = new EntrySet();

      @Override
      public Set<Map.Entry<K,V>> entrySet() {
        return entrySet;
      }

      @Override
      public boolean isEmpty() {
        return childMap.isEmpty() && parentMap.isEmpty();
      }

      @Override
      public boolean containsKey(final @Nullable Object key) {
        return childMap.containsKey(key) || parentMap.containsKey(key);
      }

      @Override
      public @Nullable V get(final @Nullable Object key) {
        return childMap.containsKey(key) ? childMap.get(key) : parentMap.get(key);
      }

      @Override
      public Set<K> keySet() {
        final Set<K> keys = new HashSet<K>(childMap.size() + parentMap.size());
        keys.addAll(parentMap.keySet());
        keys.addAll(childMap.keySet());
        return keys;
      }

      @Override
      public @Nullable V put(final K key, final V value) {
        final @Nullable V v = get(key);
        childMap.put(key, value);
        return v;
      }

      @Override
      public void putAll(final Map<? extends K,? extends V> m) {
        childMap.putAll(m);
        return;
      }

      @Override
      public void clear() {
        childMap.clear();
        parentMap.clear();
        return;
      }

      class EntrySet extends AbstractSet<Map.Entry<K,V>> implements Serializable {

        @Override
        public Iterator<Map.Entry<K,V>> iterator() {
          if (isEmpty()) {
            final List<Map.Entry<K,V>> emptyList = Collections.emptyList();
            return emptyList.iterator();
          }
          return new Iterator<Map.Entry<K,V>>() {
            /**
             * The {@link Iterator} over the {@link HierarchicalMap#keySet() keys} backing this iterator instance.
             */
            protected final Iterator<K> keyIter = keySet().iterator();

            @Override
            public boolean hasNext() {
              return keyIter.hasNext();
            }

            @Override
            public Map.Entry<K,V> next() {
              final K key = keyIter.next();
              return childMap.containsKey(key) ? getEntry(childMap.entrySet(), key).get() : getEntry(parentMap.entrySet(), key).get();
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }

          };
        } // iterator()

        @Override
        public int size() {
          return keySet().size();
        }

        @Override
        public boolean isEmpty() {
          return HierarchicalMap.this.isEmpty();
        }

        @Override
        public void clear() {
          HierarchicalMap.this.clear();
          return;
        }

      }; // EntrySet

    };
    return new HierarchicalMap();
  }

  /**
   * Chain together the supplied {@link List} of {@link Map}'s hierarchically (similar to {@link java.util.Properties}),
   * so that when the value for a given key is {@linkplain Map#get(Object) retrieved} from the resulting Map, the
   * <em>last</em> Map in the supplied list will be checked first to see if it {@linkplain Map#containsKey(Object)
   * contains} that key, and if it does not, the <em>second last</em> Map in the list will then be checked, and so on
   * back to the first Map in the supplied list. If an {@linkplain Set#iterator() iterator} is retrieved over the result
   * Map, it will iterate over elements from <em>all</em> Maps in the hierarchy.
   * 
   * @param <K> The type of keys in the chained maps.
   * @param <V> The type of values in the chained maps.
   * @param maps The maps to be chained together.
   * @return A new Map that can be used to query the hierarchy formed by the supplied Maps.
   */
  public static final <K,V> Map<K,V> chain(final List<Map<K,V>> maps) {
    if (maps.isEmpty()) return Collections.emptyMap();
    Map<K,V> result = maps.get(0);
    if (maps.size() == 1) return result;
    for (int i = 1; i < maps.size(); i++) {
      result = chain(result, maps.get(i));
    }
    return result;
  }

  /**
   * Create a wrapper around the supplied {@link Properties} instance to make it appear as a
   * <code>{@link Map}&lt;String,String&gt;</code>.
   * 
   * @param properties The {@link Properties} instance to be exposed as a Map.
   * @return A Map wrapping the supplied <code>properties</code>.
   */
  public static final Map<String,String> toMap(final Properties properties) {
    class PropertiesMap extends AbstractMap<String,String> implements Serializable {
      /**
       * @see #entrySet()
       */
      protected final Set<Map.Entry<String,String>> entrySet = new EntryView();

      @Override
      public Set<Map.Entry<String,String>> entrySet() {
        return entrySet;
      }

      @Override
      public boolean containsKey(final @Nullable Object key) {
        return (key != null) && (properties.getProperty(key.toString()) != null); // Shortcut, since Properties don't allow null values.
      }

      @Override
      public @Nullable String get(final @Nullable Object key) {
        return (key != null) ? properties.getProperty(key.toString()) : null;
      }

      @Override
      public @Nullable String put(final String key, final String value) {
        return (String)properties.setProperty(key, value);
      }

      class EntryView extends AbstractSet<Map.Entry<String,String>> implements Serializable {

        @Override
        public Iterator<Map.Entry<String,String>> iterator() {
          return new Iterator<Map.Entry<String,String>>() {
            /**
             * An Iterator over the wrapped property names.
             */
            protected final Iterator<String> stringPropertyNameIterator = properties.stringPropertyNames().iterator();

            @Override
            public boolean hasNext() {
              return stringPropertyNameIterator.hasNext();
            }

            @Override
            public Map.Entry<String,String> next() {
              final String key = stringPropertyNameIterator.next();
              final String value = Objects.requireNonNull(properties.getProperty(key), "concurrent modification");
              return new AbstractMap.SimpleImmutableEntry<String,String>(key, value);
            }

          };
        } // iterator()

        @Override
        public int size() {
          return properties.stringPropertyNames().size();
        }

      } // EntryView

    }; // PropertiesMap
    return new PropertiesMap();
  } // toMap(Properties)

  /**
   * Get an {@link Iterator} wrapper that uses a {@link Function} to translate wrapped elements from one type to
   * another.
   * 
   * @param <ET> Source element type.
   * @param <ER> Result element type.
   * @param iterator The {@link Iterator} being wrapped.
   * @param elementReadingFunction The {@link Function} used to translate read elements from the source type to the
   * result type.
   * @return The result type {@link Iterator}.
   */
  public static final <ET,ER> Iterator<ER> mapIterator(final Iterator<ET> iterator, final Function<? super ET,? extends ER> elementReadingFunction) {
    class MappingIterator implements Iterator<ER>, Serializable {

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public ER next() {
        return elementReadingFunction.apply(iterator.next());
      }

      @Override
      public void remove() {
        iterator.remove();
        return;
      }

    };
    return new MappingIterator();
  } // mapIterator

  /**
   * Get a {@link Collection} wrapper that uses a {@link Function} to translate wrapped elements from one type to
   * another.
   * 
   * @param <ET> Source element type.
   * @param <ER> Result element type.
   * @param collection The {@link Collection} being wrapped.
   * @param elementReadingFunction The {@link Function} used to translate read elements from the source type to the
   * result type.
   * @param elementWritingFunction The {@link Function} used to translate written elements from the result type back to
   * the source type.
   * @return The result type {@link Collection}.
   */
  public static final <ET,ER> Collection<ER> mapCollection(final Collection<ET> collection, final Function<? super ET,? extends ER> elementReadingFunction, final @Nullable Function<? super ER,? extends ET> elementWritingFunction) {
    class MappingCollection extends AbstractCollection<ER> implements Serializable {

      @Override
      public int size() {
        return collection.size();
      }

      @Override
      public boolean isEmpty() {
        return collection.isEmpty();
      }

      @Override
      public Iterator<ER> iterator() {
        return mapIterator(collection.iterator(), elementReadingFunction);
      }

      @Override
      public boolean add(final ER e) {
        if (elementWritingFunction == null) throw new UnsupportedOperationException();
        return collection.add(elementWritingFunction.apply(e));
      }

      @Override
      public void clear() {
        collection.clear();
        return;
      }

    };
    return new MappingCollection();
  } // mapCollection

  /**
   * Get a {@link List} wrapper that uses a {@link Function} to translate wrapped elements from one type to another.
   * 
   * @param <ET> Source element type.
   * @param <ER> Result element type.
   * @param list The {@link List} being wrapped.
   * @param elementReadingFunction The {@link Function} used to translate read elements from the source type to the
   * result type.
   * @param elementWritingFunction The {@link Function} used to translate written elements from the result type back to
   * the source type.
   * @return The result type {@link List}.
   */
  public static final <ET,ER> List<ER> mapList(final List<ET> list, final Function<? super ET,? extends ER> elementReadingFunction, final @Nullable Function<? super ER,? extends ET> elementWritingFunction) {
    class MappingList extends AbstractList<ER> implements Serializable {

      @Override
      public int size() {
        return list.size();
      }

      @Override
      public boolean isEmpty() {
        return list.isEmpty();
      }

      @Override
      public void clear() {
        list.clear();
        return;
      }

      @Override
      public ER get(final int index) {
        return elementReadingFunction.apply(list.get(index));
      }

      @Override
      public boolean add(final ER e) {
        if (elementWritingFunction == null) throw new UnsupportedOperationException();
        return list.add(elementWritingFunction.apply(e));
      }

      @Override
      public void add(final int index, final ER element) {
        if (elementWritingFunction == null) throw new UnsupportedOperationException();
        list.add(index, elementWritingFunction.apply(element));
        return;
      }

      @Override
      public @Nullable ER set(final int index, final ER element) {
        if (elementWritingFunction == null) throw new UnsupportedOperationException();
        final @Nullable ET oldValue = list.set(index, elementWritingFunction.apply(element));
        return (oldValue != null) ? elementReadingFunction.apply(oldValue) : null;
      }

    };
    return new MappingList();
  } // mapList

  /**
   * Get a {@link Set} wrapper that uses a {@link Function} to translate wrapped elements from one type to another.
   * 
   * @param <ET> Source element type.
   * @param <ER> Result element type.
   * @param set The {@link Set} being wrapped.
   * @param elementReadingFunction The {@link Function} used to translate read elements from the source type to the
   * result type.
   * @param elementWritingFunction The {@link Function} used to translate written elements from the result type back to
   * the source type.
   * @return The result type {@link Set}.
   */
  public static final <ET,ER> Set<ER> mapSet(final Set<ET> set, final Function<? super ET,? extends ER> elementReadingFunction, final @Nullable Function<? super ER,? extends ET> elementWritingFunction) {
    class MappingSet extends AbstractSet<ER> implements Serializable {

      @Override
      public int size() {
        return set.size();
      }

      @Override
      public boolean isEmpty() {
        return set.isEmpty();
      }

      @Override
      public void clear() {
        set.clear();
        return;
      }

      @Override
      public boolean add(final ER e) {
        if (elementWritingFunction == null) throw new UnsupportedOperationException();
        return set.add(elementWritingFunction.apply(e));
      }

      @Override
      public Iterator<ER> iterator() {
        return mapIterator(set.iterator(), elementReadingFunction);
      }

    };
    return new MappingSet();
  } // mapSet

  /**
   * Get a {@link java.util.Map.Entry Map.Entry} wrapper that uses a {@link Function} to translate wrapped elements from
   * one type to another.
   * 
   * @param <KT> Source key type.
   * @param <KR> Result key type.
   * @param <VT> Source value type.
   * @param <VR> Result value type.
   * @param entry The {@link java.util.Map.Entry Map.Entry} being wrapped.
   * @param keyReadingFunction The {@link Function} used to translate read keys from the source type to the result type.
   * @param valueReadingFunction The {@link Function} used to translate read values from the source type to the result
   * type.
   * @param valueWritingFunction The {@link Function} used to translate written values from the result type back to the
   * source type.
   * @return The result type {@link java.util.Map.Entry Map.Entry}.
   */
  public static final <KT,KR,VT,VR> Map.Entry<KR,VR> mapEntry(final Map.Entry<KT,VT> entry, final Function<? super KT,? extends KR> keyReadingFunction, final Function<? super VT,? extends VR> valueReadingFunction, final @Nullable Function<? super VR,? extends VT> valueWritingFunction) {
    class MappingMapEntry implements Map.Entry<KR,VR>, Serializable {

      @Override
      public KR getKey() {
        return keyReadingFunction.apply(entry.getKey());
      }

      @Override
      public VR getValue() {
        return valueReadingFunction.apply(entry.getValue());
      }

      @Override
      public @Nullable VR setValue(final VR value) {
        if (valueWritingFunction == null) throw new UnsupportedOperationException();
        final @Nullable VT oldValue = entry.setValue(valueWritingFunction.apply(value));
        return (oldValue != null) ? valueReadingFunction.apply(oldValue) : null;
      }

      @Override
      public boolean equals(final @Nullable Object other) {
        if (!(other instanceof Map.Entry)) return false;
        final Map.Entry<?,?> otherEntry = (Map.Entry<?,?>)other;
        return Objects.equals(getKey(), otherEntry.getKey()) && Objects.equals(getValue(), otherEntry.getValue());
      }

      @Override
      public int hashCode() {
        final KR k = getKey();
        final VR v = getValue();
        return ((k != null) ? k.hashCode() : 0) ^ ((v != null) ? v.hashCode() : 0);
      }

      @Override
      public String toString() {
        return String.valueOf(getKey()) + '=' + String.valueOf(getValue());
      }

    };
    return new MappingMapEntry();
  } // mapEntry

  /**
   * Get a {@link Map} wrapper that uses a {@link Function} to translate wrapped elements from one type to another.
   * 
   * @param <KT> Source key type.
   * @param <KR> Result key type.
   * @param <VT> Source value type.
   * @param <VR> Result value type.
   * @param map The {@link Map} being wrapped.
   * @param keyReadingFunction The {@link Function} used to translate read keys from the source type to the result type.
   * @param keyWritingFunction The {@link Function} used to translate written keys from the result type back to the
   * source type.
   * @param valueReadingFunction The {@link Function} used to translate read values from the source type to the result
   * type.
   * @param valueWritingFunction The {@link Function} used to translate written values from the result type back to the
   * source type.
   * @return The result type {@link Map}.
   */
  public static final <KT,KR,VT,VR> Map<KR,VR> mapMap(final Map<KT,VT> map, final Function<? super KT,? extends KR> keyReadingFunction, final @Nullable Function<? super KR,? extends KT> keyWritingFunction, final Function<? super VT,? extends VR> valueReadingFunction, final @Nullable Function<? super VR,? extends VT> valueWritingFunction) {
    class MappingMap extends AbstractMap<KR,VR> implements Serializable {
      /**
       * The cached wrapper exposed by {@link #entrySet()}.
       */
      @SuppressWarnings("unchecked")
      protected final Set<Map.Entry<KR,VR>> entrySet = CollUtil.<Map.@NonNull Entry<KT,VT>,Map.@NonNull Entry<KR,VR>> mapSet(map.entrySet(), (Function<Map.Entry<KT,VT>,Map.Entry<KR,VR>> & Serializable)(e) -> mapEntry(e, keyReadingFunction, valueReadingFunction, valueWritingFunction), (((keyWritingFunction != null) && (valueWritingFunction != null)) ? (Function<Map.Entry<KR,VR>,Map.Entry<KT,VT>> & Serializable)(e) -> mapEntry(e, keyWritingFunction, valueWritingFunction, valueReadingFunction) : null));

      @Override
      public int size() {
        return map.size();
      }

      @Override
      public boolean isEmpty() {
        return map.isEmpty();
      }

      @Override
      public void clear() {
        map.clear();
        return;
      }

      @Override
      public Set<Map.Entry<KR,VR>> entrySet() {
        return entrySet;
      }

      @Override
      public @Nullable VR put(final KR key, final VR value) {
        if ((keyWritingFunction == null) || (valueWritingFunction == null)) throw new UnsupportedOperationException();
        final @Nullable VT oldValue = map.put(keyWritingFunction.apply(key), valueWritingFunction.apply(value));
        return (oldValue != null) ? valueReadingFunction.apply(oldValue) : null;
      }

    };
    return new MappingMap();
  } // mapMap

}
