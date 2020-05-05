/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core;

import java.io.*;
import java.lang.reflect.*;
import java.math.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.regex.*;

import org.eclipse.jdt.annotation.*;


/**
 * Miscellaneous Utilities.
 */
@NonNullByDefault
public abstract class MiscUtil {

  /**
   * {@linkplain Optional#get() Get} the value from the supplied {@link Optional} if {@linkplain Optional#isPresent()
   * present}, or else return {@code null}. The primary use of this method is to prevent the warning that would result
   * by an attempt to call {@link Optional#orElse(Object)} on a {@link NonNull} instance with a {@code null} argument.
   * 
   * @param <T> The type of value being returned.
   * @param optional The {@link Optional} instance containing the desired value.
   * @return The value or {@code null}.
   */
  public static final <T> @Nullable T orElseNull(final Optional<T> optional) {
    return (optional.isPresent()) ? optional.get() : null;
  }

  /**
   * {@linkplain Pattern#compile(String, int) Compile} a {@linkplain Pattern#CASE_INSENSITIVE case insensitive}
   * {@link Pattern} from a String.
   * 
   * @param regex The expression to be compiled.
   * @return The given regular expression compiled into a Pattern with the {@link Pattern#CASE_INSENSITIVE} flag.
   * @throws PatternSyntaxException If the expression's syntax is invalid.
   * @see Pattern#compile(String, int)
   */
  public static final Pattern compilePatternCI(final String regex) throws PatternSyntaxException {
    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  /**
   * {@linkplain Throwable#printStackTrace(PrintWriter) Print} the stack trace for a {@link Throwable} to a
   * {@link String}.
   * 
   * @param throwable The {@link Throwable} instance to print a stack trace for.
   * @return A String containing the {@linkplain Throwable#printStackTrace(PrintWriter) stack trace}.
   */
  public static final String printStackTrace(final Throwable throwable) {
    final StringWriter stringWriter = new StringWriter();
    try (final PrintWriter printWriter = new PrintWriter(stringWriter);) {
      throwable.printStackTrace(printWriter);
    }
    return stringWriter.toString();
  }

  /**
   * {@linkplain Properties#load(InputStream) Load} a {@link Properties} from a {@link URL}.
   * 
   * @param defaults The default values to be used for {@linkplain Properties#Properties(Properties) constructing} the
   * result.
   * @param url The {@link URL} to {@linkplain Properties#load(InputStream) load} data from.
   * @return The new Properties instance created from the given <code>url</code>.
   * @throws UncheckedIOException If an {@link IOException} is encountered while loading data.
   * @see Properties#load(InputStream)
   * @see #loadProperties(URL)
   */
  public static final Properties loadProperties(final @Nullable Properties defaults, final URL url) throws UncheckedIOException {
    final Properties result = new Properties(defaults);
    try (final InputStream is = url.openStream()) {
      result.load(is);
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
    return result;
  }

  /**
   * {@linkplain Properties#load(InputStream) Load} a {@link Properties} from a {@link URL}.
   * 
   * @param url The {@link URL} to {@linkplain Properties#load(InputStream) load} data from.
   * @return The new Properties instance created from the given <code>url</code>.
   * @throws UncheckedIOException If an {@link IOException} is encountered while loading data.
   * @see Properties#load(InputStream)
   * @see #loadProperties(Properties, URL)
   */
  public static final Properties loadProperties(final URL url) throws UncheckedIOException {
    return loadProperties(null, url);
  }

  /**
   * <p>
   * Create a {@link FutureTask} whose {@link FutureTask#cancel(boolean) cancel} and {@link FutureTask#isDone() isDone}
   * methods provide Guaranteed Completion.
   * </p>
   * 
   * <p>
   * The default implementation of FutureTask provides no real guarantee that it's task execution thread is
   * interruptible and that it will not remain/resume running after a call to {@link FutureTask#cancel(boolean) cancel},
   * even if {@link FutureTask#isDone() isDone} then returns <code>true</code>. This class changes that, in that it's
   * implementation of {@link FutureTask#cancel(boolean) cancel} and {@link FutureTask#isDone() isDone} provide a
   * guarantee that it's execution thread is not running - either through having been canceled before starting, through
   * interruption, or at the very least, waiting on normal completion.
   * </p>
   * 
   * @param <V> The result type returned by the FutureTask's get methods.
   * @param callable The task to execute when the {@link FutureTask} is {@linkplain FutureTask#run() run}.
   * @return A {@link FutureTask} instance having guaranteed completion semantics.
   */
  public static final <V> FutureTask<V> mkFutureTaskGC(final Callable<V> callable) {
    return new FutureTask<V>(callable) {
      /**
       * A reference to the {@link Thread} currently {@linkplain #run() running} this task.
       */
      protected final AtomicReference<@Nullable Thread> running = new AtomicReference<@Nullable Thread>(null);

      /**
       * Repeatedly <code>{@link Thread#sleep(long)}</code> until <code>{@link #isDone()} == true</code>.
       */
      protected void sleepUntilDone() {
        while (!isDone()) {
          try {
            Thread.sleep(25);
          } catch (InterruptedException ie) {}
        }
        return;
      }

      @Override
      public boolean cancel(final boolean mayInterruptIfRunning) {
        final boolean cancelled = super.cancel(mayInterruptIfRunning);
        if (Thread.currentThread() != running.get()) { // avoid deadlock if task thread tries to cancel itself
          sleepUntilDone();
        }
        return cancelled;
      }

      @Override
      public boolean isDone() {
        return (running.get() == null) && super.isDone();
      }

      @Override
      public void run() throws RejectedExecutionException {
        final boolean setRunning = running.compareAndSet(null, Thread.currentThread());
        if (!setRunning) {
          // This task is already being run by some thread...
          if (Thread.currentThread() == running.get()) {
            throw new RejectedExecutionException("Task thread attempted to re-run it's own Task");
          }
          sleepUntilDone();
          return;
        }
        try {
          super.run();
        } finally {
          running.set(null);
        }
        return;
      }

    };
  } // mkFutureTaskGC

  /**
   * Construct a new instance of an &quot;MD5&quot; based {@link MessageDigest}. This method is primary to trap the
   * {@link NoSuchAlgorithmException} which should never be thrown for this standard algorithm.
   * 
   * @return A {@link MessageDigest} implementing the &quot;MD5&quot; algorithm.
   */
  public static final MessageDigest getMD5MessageDigest() {
    try {
      return MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException nsae) { // should never happen, MD5 is a "standard algorithm"
      throw new RuntimeException(nsae.getClass().getName() + ":" + nsae.getMessage(), nsae);
    }
  }

  /**
   * Get an {@link OutputStream} implementation for easy writing to the supplied {@link MessageDigest}.
   * 
   * @param messageDigest The {@link MessageDigest} you wish to {@linkplain MessageDigest#update(byte[]) update}.
   * @return An {@link OutputStream} that can be used to {@linkplain MessageDigest#update(byte[]) update} the supplied
   * <code>messageDigest</code>.
   */
  public static final OutputStream mkOutputStream(final MessageDigest messageDigest) {
    class MessageDigestOutputStream extends OutputStream {

      @Override
      public void write(byte[] b) throws IOException {
        messageDigest.update(b);
        return;
      }

      @Override
      public void write(int b) throws IOException {
        messageDigest.update((byte)b);
        return;
      }

    };
    return new MessageDigestOutputStream();
  } // mkOutputStream(MessageDigest)

  /**
   * Get a hex encoded String containing the {@linkplain MessageDigest#digest() digested} data.
   * 
   * @param messageDigest The {@link MessageDigest} the {@linkplain MessageDigest#digest() digest} should be retrieved
   * from.
   * @return A {@linkplain BigInteger#toString(int) String} representation of the {@linkplain MessageDigest#digest()
   * digest} using radix <code>16</code>.
   * @see BigInteger#toString(int)
   */
  public static final String toHexString(final MessageDigest messageDigest) {
    return new BigInteger(1, messageDigest.digest()).toString(16);
  }

  /**
   * {@linkplain Class#forName(String) Get} a {@link Class} for the specified <code>className</code>, mapping
   * exceptions.
   * 
   * @param <B> The base type, of which the returned Class will be a {@link Class#asSubclass(Class) subclass}.
   * @param <T> The type of the Class being instantiated.
   * @param <CNFE> The type of {@link Throwable} to be thrown if a {@link ClassNotFoundException} is encountered.
   * @param <CCE> The type of {@link Throwable} to be thrown if a {@link ClassCastException} is encountered.
   * @param baseClass The base {@link Class}, of which the returned Class will be a {@link Class#asSubclass(Class)
   * subclass}.
   * @param className The {@linkplain Class#getName() name} of the {@link Class} object to instantiate.
   * @param cnfeMapper When a {@link ClassNotFoundException} is encountered, this constructor method reference will be
   * used to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return
   * an alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised
   * to the exception argument passed in.
   * @param cceMapper When a {@link ClassCastException} is encountered, this constructor method reference will be used
   * to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return an
   * alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised to
   * the exception argument passed in.
   * @return The {@link Class} object for the specified <code>className</code>.
   * @throws CNFE If a {@link ClassNotFoundException} was encountered.
   * @throws CCE If a {@link ClassCastException} was encountered.
   */
  @SuppressWarnings("unchecked")
  public static final <B,T extends B,CNFE extends Throwable,CCE extends Throwable> Class<T> classForName(final Class<B> baseClass, final String className, final Function<? super ClassNotFoundException,? extends CNFE> cnfeMapper, final Function<? super ClassCastException,? extends CNFE> cceMapper) throws CNFE, CCE {
    try {
      return (Class<T>)Class.forName(className).<B> asSubclass(baseClass);
    } catch (ClassNotFoundException cnfe) {
      throw cnfeMapper.apply(cnfe);
    } catch (ClassCastException cce) {
      throw cceMapper.apply(cce);
    }
  }

  /**
   * {@linkplain Class#getConstructor(Class...) Get} a {@link Constructor} from the specified {@link Class clazz},
   * mapping exceptions.
   * 
   * @param <T> The type of class from which the constructor is being retrieved.
   * @param <NSME> The type of {@link Throwable} to be thrown if a {@link NoSuchMethodException} is encountered.
   * @param <SE> The type of {@link Throwable} to be thrown if a {@link SecurityException} is encountered.
   * @param clazz The {@link Class} to {@linkplain Class#getConstructor(Class...) retrieve} the {@link Constructor}
   * from.
   * @param nsmeMapper When a {@link NoSuchMethodException} is encountered, this constructor method reference will be
   * used to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return
   * an alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised
   * to the exception argument passed in.
   * @param seMapper When a {@link SecurityException} is encountered, this constructor method reference will be used to
   * construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return an
   * alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised to
   * the exception argument passed in.
   * @param parameterTypes The parameter array used to identify the appropriate {@link Constructor}.
   * @return The specified {@link Constructor}.
   * @throws NSME If a {@link NoSuchMethodException} was encountered.
   * @throws SE If a {@link SecurityException} was encountered.
   * @see Class#getConstructor(Class...)
   */
  public static final <T,NSME extends Throwable,SE extends Throwable> Constructor<T> getConstructor(final Class<T> clazz, final Function<? super NoSuchMethodException,? extends NSME> nsmeMapper, final Function<? super SecurityException,? extends SE> seMapper, final Class<?> @Nullable... parameterTypes) throws NSME, SE {
    try {
      return clazz.getConstructor(parameterTypes);
    } catch (NoSuchMethodException nsme) {
      throw nsmeMapper.apply(nsme);
    } catch (SecurityException se) {
      throw seMapper.apply(se);
    }
  }

  /**
   * {@linkplain Constructor#newInstance(Object...) Instantiate} an Object using the given {@link Constructor}, mapping
   * exceptions.
   * 
   * @param <T> The type of object being constructed.
   * @param <IE> The type of {@link Throwable} to be thrown if a {@link InstantiationException} is encountered.
   * @param <IAE> The type of {@link Throwable} to be thrown if a {@link IllegalAccessException} is encountered.
   * @param <IRE> The type of {@link Throwable} to be thrown if a {@link IllegalArgumentException} is encountered.
   * @param <ITE> The type of {@link Throwable} to be thrown if a {@link InvocationTargetException} is encountered.
   * @param constructor The {@link Constructor} to be used for {@linkplain Constructor#newInstance(Object...)
   * instantiation}.
   * @param ieMapper When an {@link InstantiationException} is encountered, this constructor method reference will be
   * used to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return
   * an alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised
   * to the exception argument passed in.
   * @param iaeMapper When an {@link IllegalAccessException} is encountered, this constructor method reference will be
   * used to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return
   * an alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised
   * to the exception argument passed in.
   * @param ireMapper When an {@link IllegalArgumentException} is encountered, this constructor method reference will be
   * used to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will return
   * an alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause} initialised
   * to the exception argument passed in.
   * @param iteClass When an {@link InvocationTargetException} is encountered, if it's {@linkplain Throwable#getCause()
   * cause} is {@linkplain Class#isInstance(Object) an instance} of this Class, then it will be
   * {@linkplain Class#cast(Object) cast} and thrown without being mapped by the <code>iteMapper</code>.
   * @param iteMapper When an {@link InvocationTargetException} is encountered, this constructor method reference will
   * be used to construct an alternate {@link Throwable} to be thrown instead. Normally this {@link Function} will
   * return an alternate exception which has been constructed to have it's {@linkplain Throwable#getCause() cause}
   * initialised to the exception argument passed in.
   * @param initargs The array of objects to be passed as arguments to the constructor call.
   * @return The instantiated object.
   * @throws IE If an {@link InstantiationException} was encountered.
   * @throws IAE If an {@link IllegalAccessException} was encountered.
   * @throws IRE If an {@link IllegalArgumentException} was encountered.
   * @throws ITE If an {@link InvocationTargetException} was encountered.
   * @see Constructor#newInstance(Object...)
   */
  public static final <T,@NonNull IE extends Throwable,@NonNull IAE extends Throwable,@NonNull IRE extends Throwable,@NonNull ITE extends Throwable> T newInstance(final Constructor<T> constructor, final Function<? super InstantiationException,? extends IE> ieMapper, final Function<? super IllegalAccessException,? extends IAE> iaeMapper, final Function<? super IllegalArgumentException,? extends IRE> ireMapper, final Class<ITE> iteClass, final Function<? super InvocationTargetException,? extends ITE> iteMapper, final Object @Nullable... initargs) throws IE, IAE, IRE, ITE {
    try {
      return constructor.newInstance(initargs);
    } catch (InstantiationException ie) {
      throw ieMapper.apply(ie);
    } catch (IllegalAccessException iae) {
      throw iaeMapper.apply(iae);
    } catch (IllegalArgumentException ire) {
      throw ireMapper.apply(ire);
    } catch (InvocationTargetException ite) {
      final Throwable cause = ite.getCause();
      if ((cause != null) && (iteClass.isInstance(cause))) throw Objects.requireNonNull(iteClass.cast(cause));
      throw iteMapper.apply(ite);
    }
  }

}
