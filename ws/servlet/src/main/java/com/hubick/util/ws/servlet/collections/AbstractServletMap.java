/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.ws.servlet.collections;

import java.util.*;

import org.eclipse.jdt.annotation.*;

import javax.servlet.*;


/**
 * An {@link AbstractMap} tailored for Servlet API use.
 * 
 * @param <V> The type of value exposed by this Map.
 */
@NonNullByDefault
public abstract class AbstractServletMap<@NonNull V> extends AbstractMap<String,V> {

  /**
   * Get the Object used to synchronize value access.
   * 
   * @param readOnly The caller should provide <code>false</code> if they intend to
   * {@linkplain #setServletMapValue(String, Object) set} or {@linkplain #removeServletMapValue(String) remove} values.
   * @return The Object to lock on.
   */
  protected abstract Object getLock(boolean readOnly);

  /**
   * Get an Enumeration of the key names exposed by this map.
   * 
   * @return The key names.
   * @throws IllegalStateException If the object backing this Map is invalid.
   */
  protected abstract Enumeration<String> getServletMapKeyNames() throws IllegalStateException;

  /**
   * Get the named value.
   * 
   * @param name The name of the value to get.
   * @return The value for the given name.
   * @throws IllegalStateException If the object backing this Map is invalid.
   */
  protected abstract @Nullable V getServletMapValue(String name) throws IllegalStateException;

  /**
   * Set the named value.
   * 
   * @param name The name of the value being set.
   * @param o The value to set.
   * @throws UnsupportedOperationException If this Map is immutable.
   * @throws IllegalStateException If the object backing this Map is invalid.
   */
  protected abstract void setServletMapValue(String name, @Nullable V o) throws UnsupportedOperationException, IllegalStateException;

  /**
   * Remove the named value.
   * 
   * @param name The name of the value to remove.
   * @throws UnsupportedOperationException If this Map is immutable.
   * @throws IllegalStateException If the object backing this Map is invalid.
   */
  protected abstract void removeServletMapValue(String name) throws UnsupportedOperationException, IllegalStateException;

  @Override
  public Set<Map.Entry<String,V>> entrySet() {
    return new EntrySet();
  }

  @Override
  public boolean isEmpty() {
    synchronized (getLock(true)) {
      final Enumeration<String> keyNames = getServletMapKeyNames();
      return !keyNames.hasMoreElements();
    }
  }

  @Override
  public boolean containsKey(final @Nullable Object key) {
    if (key == null) return false;
    synchronized (getLock(true)) {
      return getServletMapValue((String)key) != null;
    }
  }

  @Override
  public @Nullable V get(final @Nullable Object key) {
    if (key == null) return null;
    synchronized (getLock(true)) {
      return getServletMapValue((String)key);
    }
  }

  @Override
  public @Nullable V put(final String key, final V value) {
    synchronized (getLock(false)) {
      final @Nullable V oldValue = getServletMapValue(key);
      setServletMapValue(key, value);
      return oldValue;
    }
  }

  /**
   * A {@link Set} of {@link java.util.Map.Entry} elements, backed by {@link ServletRequest}
   * {@linkplain ServletRequest#getAttributeNames() attributes}.
   */
  protected class EntrySet extends AbstractSet<Map.Entry<String,V>> {

    @Override
    public Iterator<Map.Entry<String,V>> iterator() {
      return new EntryIterator();
    }

    @Override
    public int size() {
      int size = 0;
      synchronized (getLock(true)) {
        final Enumeration<String> attributeNames = getServletMapKeyNames();
        for (size = 0; attributeNames.hasMoreElements(); size++) {
          attributeNames.nextElement();
        }
      }
      return size;
    }

    /**
     * An {@link Iterator} over {@link java.util.Map.Entry} elements, backed by {@link ServletRequest}
     * {@linkplain ServletRequest#getAttributeNames() attributes}.
     */
    protected class EntryIterator implements Iterator<Map.Entry<String,V>> {
      /**
       * The {@link ServletRequest} {@linkplain ServletRequest#getAttributeNames() attributes} which existed at the time
       * this object was instantiated.
       */
      protected final @NonNull String[] attributeNames;
      /**
       * The current index this Iterator points to in the {@link #attributeNames} array.
       */
      protected int current = -1;

      /**
       * Construct a new <code>EntryIterator</code>.
       */
      public EntryIterator() {
        synchronized (getLock(true)) {
          attributeNames = new @NonNull String[size()];
          int i = 0;
          final Enumeration<String> attributeNamesEnum = getServletMapKeyNames();
          while ((attributeNamesEnum.hasMoreElements()) && (i < attributeNames.length)) {
            attributeNames[i++] = attributeNamesEnum.nextElement();
          }
        }
        return;
      }

      @Override
      public boolean hasNext() {
        return current + 1 < attributeNames.length;
      }

      @Override
      public Map.Entry<String,V> next() {
        if (!hasNext()) throw new NoSuchElementException();
        return new Entry(attributeNames[++current]);
      }

      @Override
      public void remove() {
        if ((current < 0) || (current >= attributeNames.length)) throw new IllegalStateException();
        synchronized (getLock(false)) {
          removeServletMapValue(attributeNames[current]);
        }
        return;
      }

    } // EntrySet.EntryIterator

  } // EntrySet

  /**
   * {@link java.util.Map.Entry} implementation backed by a specified {@link ServletRequest}
   * {@linkplain ServletRequest#getAttribute(String) attribute}.
   */
  protected class Entry implements Map.Entry<String,V> {
    /**
     * They key of the {@linkplain ServletRequest#getAttribute(String) attribute} represented by this Entry.
     */
    protected final String key;

    /**
     * Construct a new <code>Entry</code> for the {@linkplain ServletRequest#getAttribute(String) attribute}
     * corresponding to the specified <code>key</code>.
     * 
     * @param key They key of the {@linkplain ServletRequest#getAttribute(String) attribute} represented by this Entry.
     */
    public Entry(final String key) {
      this.key = key;
      return;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public V getValue() {
      synchronized (getLock(true)) {
        return Objects.requireNonNull(getServletMapValue(key));
      }
    }

    @Override
    public @Nullable V setValue(final @Nullable V value) {
      synchronized (getLock(true)) {
        final @Nullable V oldValue = getServletMapValue(key);
        if ((value == null) && (oldValue == null)) return null;
        synchronized (getLock(false)) {
          if (value != null) {
            setServletMapValue(key, value);
          } else {
            removeServletMapValue(key);
          }
        }
        return oldValue;
      }
    }

  } // Entry

}
