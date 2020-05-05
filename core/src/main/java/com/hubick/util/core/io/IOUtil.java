/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.io;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.eclipse.jdt.annotation.*;


/**
 * Input/Output Utilities.
 */
@NonNullByDefault
public abstract class IOUtil {
  /**
   * The {@link Charset} Object for the <code>"UTF-8"</code> charset.
   */
  public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
  /**
   * The {@link Charset} Object for the <code>"UTF-16"</code> charset.
   */
  public static final Charset UTF_16_CHARSET = Charset.forName("UTF-16");
  /**
   * The {@link Charset} Object for the <code>"US-ASCII"</code> charset.
   */
  public static final Charset US_ASCII_CHARSET = Charset.forName("US-ASCII");
  /**
   * An {@link OutputStream} implementation which simply discards the output.
   */
  public static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {

    @Override
    public void write(final int b) throws IOException {
      return;
    }

  };

  /**
   * {@linkplain Reader#read(char[]) Read} <em>all</em> the supplied {@link Reader}'s content into a String.
   * 
   * @param reader The {@link Reader} to read from.
   * @return A String containing all the content supplied by the given <code>reader</code>.
   */
  public static final String toString(final Reader reader) {
    try (Scanner scanner = new Scanner(reader)) {
      return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
    }
  }

  /**
   * Pipe the data from the <code>inputStream</code> to the <code>outputStream</code>.
   * 
   * @param inputStream The {@link InputStream} to read data from.
   * @param outputStream The {@link OutputStream} to send data to.
   * @param bufferSize The size of the working buffer used to copy data between the <code>inputStream</code> and the
   * <code>outputStream</code>.
   * @param flushEachBuffer Should the <code>outputStream</code> be {@linkplain OutputStream#flush() flushed} after each
   * buffer is written?
   * @param readLimit The number of bytes which should be read from the <code>inputStream</code>, else <code>-1</code>
   * to read all available data.
   * @throws PipeException If there was a problem performing I/O.
   */
  public static final void pipe(final @Nullable InputStream inputStream, final OutputStream outputStream, int bufferSize, final boolean flushEachBuffer, final long readLimit) throws PipeException {
    if (inputStream == null) return;
    if (bufferSize <= 0) bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int bytesRead = -1;
    long bytesRemaining = readLimit;
    try {
      if (readLimit >= 0) {
        bytesRead = inputStream.read(buffer, 0, (int)Math.min(buffer.length, bytesRemaining));
        if (bytesRead >= 0) bytesRemaining -= bytesRead;
      } else {
        bytesRead = inputStream.read(buffer);
      }
    } catch (IOException ioe) {
      throw new PipeException(ioe, true);
    }
    while (bytesRead > 0) {
      try {
        outputStream.write(buffer, 0, bytesRead);
        if (flushEachBuffer) outputStream.flush();
      } catch (IOException ioe) {
        throw new PipeException(ioe, false);
      }
      try {
        if (readLimit >= 0) {
          bytesRead = inputStream.read(buffer, 0, (int)Math.min(buffer.length, bytesRemaining));
          if (bytesRead >= 0) bytesRemaining -= bytesRead;
        } else {
          bytesRead = inputStream.read(buffer);
        }
      } catch (IOException ioe) {
        throw new PipeException(ioe, true);
      }
    }
    return;
  }

  /**
   * {@linkplain Serializable Serialize} the given object into a byte array.
   * 
   * @param serializable The object to be {@linkplain Serializable serialized}.
   * @return A byte array containing the serialized object.
   * @see ObjectOutputStream#writeObject(Object)
   */
  public static final byte[] serialize(final Serializable serializable) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (final ObjectOutputStream oos = new ObjectOutputStream(baos);) {
      oos.writeObject(serializable);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    return baos.toByteArray();
  }

  /**
   * {@linkplain Serializable Deserialize} the given object from a byte array.
   * 
   * @param <S> The type of object being deserialized.
   * @param serialized The byte array containing the serialized object.
   * @param serializedClass The {@link Class} of the object being deserialized.
   * @return The deserialized object.
   * @throws ClassNotFoundException If the {@link Class} of the object being deserialized could not be found.
   * @see ObjectInputStream#readObject()
   */
  public static final <@NonNull S extends Serializable> S deserialize(byte[] serialized, final Class<S> serializedClass) throws ClassNotFoundException {
    final InputStream in = new ByteArrayInputStream(serialized);
    try {
      final ObjectInputStream ois = new ObjectInputStream(in);
      return Objects.requireNonNull(serializedClass.cast(ois.readObject()));
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Use {@linkplain #serialize(Serializable) serialization} and {@linkplain #deserialize(byte[], Class)
   * deserialization} to create a clone of the given object.
   * 
   * @param <S> The type of object being cloned.
   * @param serializable The object being cloned.
   * @return The cloned object.
   */
  @SuppressWarnings("unchecked")
  public static final <S extends Serializable> S mkSerialClone(final S serializable) {
    try {
      return deserialize(serialize(serializable), (Class<S>)serializable.getClass());
    } catch (ClassNotFoundException cnfe) {
      throw new RuntimeException(cnfe);
    }
  }

  /**
   * <p>
   * Signals that a problem has occurred during the piping of data from the input to the output.
   * </p>
   * 
   * <p>
   * This exception class allows the recipient to examine which side of the pipe, reading or writing, threw the
   * underlying IOException.
   * </p>
   */
  public static class PipeException extends UncheckedIOException {
    /**
     * @see #wasReading()
     */
    protected final boolean reading;

    /**
     * Construct a <code>PipeException</code> with the specified <code>cause</code>.
     * 
     * @param cause The reason for this <code>PipeException</code>.
     * @param reading <code>true</code> if this exception's <code>cause</code> occurred during reading,
     * <code>false</code> if it was during writing.
     */
    protected PipeException(final @Nullable IOException cause, final boolean reading) {
      super(cause);
      this.reading = reading;
      return;
    }

    /**
     * Was this exception caused by reading?
     * 
     * @return <code>true</code> if this exceptions {@linkplain Throwable#getCause() cause} is due to reading from the
     * input stream, or <code>false</code> if it is due to writing to the output stream.
     */
    public boolean wasReading() {
      return reading;
    }

  } // PipeException

}
