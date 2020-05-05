/*
 * Copyright 2013-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.stream;

import java.util.function.*;
import java.util.stream.*;

import org.eclipse.jdt.annotation.*;


/**
 * {@link Stream} Utilities.
 */
@NonNullByDefault
public abstract class StreamUtil {

  /**
   * A variant of {@link Stream#collect(Collector)} which also {@linkplain Stream#close() closes} the {@link Stream}.
   * 
   * @param <T> The type of stream elements.
   * @param <R> The type of the result.
   * @param <A> The type of the intermediate accumulation.
   * @param stream The stream to reduce and then close.
   * @param collector The collector describing the reduction.
   * @return The result of the reduction.
   */
  public static final <T,R,A> R collect(final Stream<T> stream, final Collector<? super T,A,R> collector) {
    try {
      return stream.collect(collector);
    } finally {
      stream.close();
    }
  }

  /**
   * {@linkplain Stream#limit(long) Limit} the given <code>stream</code> if <code>maxSize</code> isn't negative,
   * otherwise return it untouched.
   * 
   * @param <T> The type of stream elements.
   * @param stream The stream to be truncated.
   * @param maxSize The number of elements the stream should be limited to, or a negative number if it shouldn't be
   * limited.
   * @return The truncated stream.
   * @see Stream#limit(long)
   */
  public static final <T> Stream<T> limit(final Stream<T> stream, final long maxSize) {
    return (maxSize >= 0) ? stream.limit(maxSize) : stream;
  }

  /**
   * {@linkplain Stream#skip(long) Skip} leading elements from the given <code>stream</code> if <code>n</code> is
   * greater than zero, otherwise return it untouched.
   * 
   * @param <T> The type of stream elements.
   * @param stream The stream to skip elements on.
   * @param n The number of leading elements to skip, or a number less than one if none should be skipped.
   * @return The new stream.
   * @see Stream#skip(long)
   */
  public static final <T> Stream<T> skip(final Stream<T> stream, final long n) {
    return (n > 0) ? stream.skip(n) : stream;
  }

  /**
   * Additional {@link java.util.stream.Collectors Collectors}.
   */
  public abstract static class Collectors {

    /**
     * A {@code Collector} that performs multiplication on it's {@link Integer} input elements.
     * 
     * @param <T> The type of the input elements.
     * @param mapper A function extracting the value to be multiplied.
     * @return A {@code Collector} that multiplies it's input elements.
     */
    public static final <T> Collector<T,?,Integer> multiplyingInt(final ToIntFunction<? super T> mapper) {
      return java.util.stream.Collectors.collectingAndThen(Collector.of(() -> new int[1], (a, t) -> a[0] *= mapper.applyAsInt(t), (a, b) -> {
        a[0] *= b[0];
        return a;
      }), a -> a[0]);
    }

    /**
     * A {@code Collector} that performs multiplication on it's {@link Long} input elements.
     * 
     * @param <T> The type of the input elements.
     * @param mapper A function extracting the value to be multiplied.
     * @return A {@code Collector} that multiplies it's input elements.
     */
    public static final <T> Collector<T,?,Long> multiplyingLong(final ToLongFunction<? super T> mapper) {
      return java.util.stream.Collectors.collectingAndThen(Collector.of(() -> new long[1], (a, t) -> a[0] *= mapper.applyAsLong(t), (a, b) -> {
        a[0] *= b[0];
        return a;
      }), a -> a[0]);
    }

  } // Collectors

}
