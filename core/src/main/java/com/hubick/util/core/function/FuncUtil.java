/*
 * Copyright 2013-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.function;

import java.util.*;
import java.util.function.*;

import org.eclipse.jdt.annotation.*;


/**
 * {@link Function} Utilities.
 */
@NonNullByDefault
public abstract class FuncUtil {

  /**
   * Return a {@link Function} wrapping the supplied <code>function</code>, which only
   * {@linkplain Function#apply(Object) applies} it to an {@link Optional} argument when that argument is
   * {@linkplain Optional#isPresent() present}.
   * 
   * @param <T> The type of the input to the Function.
   * @param <R> The type of the result of the Function.
   * @param function The {@link Function} being wrapped.
   * @return A new {@link Function} which accepts and returns {@link Optional} objects, returning an
   * {@linkplain Optional#empty() empty} result for any argument which isn't {@linkplain Optional#isPresent() present},
   * instead of {@linkplain Function#apply(Object) applying} the wrapped <code>function</code>.
   */
  public static final <T,R> Function<Optional<T>,Optional<R>> applyOptional(final Function<? super T,? extends R> function) {
    return (t) -> (t.isPresent()) ? Optional.ofNullable(function.apply(t.get())) : Optional.empty();
  }

  /**
   * Return the supplied {@link Predicate}, or if <code>null</code>, one that always returns <code>true</code>. This
   * method is designed for use by code which {@linkplain java.util.stream.Stream#filter(Predicate) filters} on a
   * {@link Nullable} {@link Predicate} argument.
   * 
   * @param <T> The type of input to the predicate.
   * @param predicate The {@link Predicate} to test for <code>null</code>.
   * @return The supplied <code>predicate</code> if not <code>null</code>, otherwise one which always returns
   * <code>true</code>.
   * 
   * @see java.util.stream.Stream#filter(Predicate)
   */
  public static final <T> Predicate<T> mkNonNull(final @Nullable Predicate<? super T> predicate) {
    return (predicate != null) ? ((t) -> predicate.test(t)) : ((t) -> true);
  }

  /**
   * Similar to {@link Function#andThen(Function)}, but where a {@link Predicate} is performed <code>after</code> the
   * <code>function</code>.
   * 
   * @param <T> The type of the input to the Function.
   * @param <R> The type of the result of the Function.
   * @param function The {@link Function} to compose onto.
   * @param after The {@link Predicate} to perform on the result of the <code>function</code> invocation.
   * @return A new {@link Predicate} composed from the original <code>function</code> followed by the <code>after</code>
   * Predicate.
   * @see Function#andThen(Function)
   */
  public static final <T,R> Predicate<T> andThen(final Function<? super T,? extends R> function, final @Nullable Predicate<? super R> after) {
    final Predicate<? super R> afterNN = mkNonNull(after);
    return (t) -> afterNN.test(function.apply(t));
  }

  /**
   * Create a wrapper around a {@link Function} which catches exceptions and throws an alternate exception type instead.
   * 
   * @param <T> The type of the input to the wrapped function.
   * @param <R> The type of the result from the wrapped function.
   * @param <CE> The type of the exception to be caught.
   * @param <TE> The type of the alternate exception to be thrown.
   * @param onFunction The {@link Function} being wrapped.
   * @param catchClass The {@link Class} of the exception to be caught.
   * @param throwInstead When an exception is caught, this constructor method reference will be used to construct an
   * alternate {@link RuntimeException} to be thrown instead. Normally this function will return an alternate exception
   * which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised to the exception
   * argument passed in.
   * @return A new {@link Function} wrapping the supplied <code>onFunction</code> argument, but which translates the
   * specified exceptions.
   */
  public static final <T,R,@NonNull CE extends RuntimeException,@NonNull TE extends RuntimeException> Function<T,R> mapEx(final Function<? super T,? extends R> onFunction, final Class<CE> catchClass, final Function<CE,TE> throwInstead) {
    return (t) -> {
      try {
        return onFunction.apply(t);
      } catch (RuntimeException re) {
        if (catchClass.isInstance(re)) {
          throw throwInstead.apply(Objects.requireNonNull(catchClass.cast(re)));
        }
        throw re;
      }
    };
  }

  /**
   * Create a wrapper around a {@link Consumer} which catches exceptions and throws an alternate exception type instead.
   * 
   * @param <T> The type of the input to the wrapped consumer.
   * @param <CE> The type of the exception to be caught.
   * @param <TE> The type of the alternate exception to be thrown.
   * @param onConsumer The {@link Consumer} being wrapped.
   * @param catchClass The {@link Class} of the alternate exception to be thrown.
   * @param exceptionConsumer A {@link Consumer} to be provided the caught exception before the alternate is thrown.
   * @param throwInstead When an exception is caught, this constructor method reference will be used to construct an
   * alternate {@link RuntimeException} to be thrown instead. Normally this function will return an alternate exception
   * which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised to the exception
   * argument passed in. If <code>null</code> is supplied, no exception will be thrown.
   * @return A new consumer wrapping the supplied <code>onConsumer</code> argument, but which translates any thrown
   * exceptions.
   */
  public static final <T,@NonNull CE extends RuntimeException,@NonNull TE extends RuntimeException> Consumer<T> mapEx(final Consumer<T> onConsumer, final Class<CE> catchClass, final @Nullable Consumer<CE> exceptionConsumer, final @Nullable Function<CE,TE> throwInstead) {
    return (t) -> {
      try {
        onConsumer.accept(t);
        return;
      } catch (RuntimeException re) {
        if (catchClass.isInstance(re)) {
          if (exceptionConsumer != null) {
            try {
              exceptionConsumer.accept(Objects.requireNonNull(catchClass.cast(re)));
            } catch (Exception e) {}
          }
          if (throwInstead != null) throw throwInstead.apply(Objects.requireNonNull(catchClass.cast(re)));
          return;
        }
        throw re;
      }
    };
  }

}
