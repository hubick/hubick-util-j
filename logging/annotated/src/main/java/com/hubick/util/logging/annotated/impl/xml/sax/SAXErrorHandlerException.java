/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.logging.annotated.impl.xml.sax;

import java.util.*;
import java.util.logging.*;

import org.eclipse.jdt.annotation.*;

import org.xml.sax.*;

import com.hubick.util.logging.annotated.*;
import com.hubick.util.logging.annotated.impl.*;


/**
 * A {@link SAXException} extension designed for use as a SAX {@link ErrorHandler} which constructs it's
 * {@link SAXException#getMessage() exception message} from an {@link AnnotatedLogMessage} containing <em>all</em>
 * {@linkplain #warning(SAXParseException) warnings}, {@linkplain #error(SAXParseException) errors}, and
 * {@linkplain #fatalError(SAXParseException) fatal errors} received, and then throws itself in case of a
 * non-recoverable error.
 */
@NonNullByDefault
public class SAXErrorHandlerException extends SAXException implements ErrorHandler, AnnotatedLogMessage {
  /**
   * {@link org.xml.sax.SAXException#getCause()} returns it's own exception member, but doesn't override
   * {@link Throwable#initCause(Throwable)} to update it, so we have to override both to fix it.
   */
  private @Nullable Exception cause = this;
  /**
   * Should parsing be discontinued by throwing this {@link org.xml.sax.SAXException} in the case of
   * {@linkplain #warning(SAXParseException) warnings}?
   */
  protected final boolean throwWarnings;
  /**
   * Should parsing be discontinued by throwing this {@link org.xml.sax.SAXException} in the case of
   * {@linkplain #error(SAXParseException) errors}?
   */
  protected final boolean throwErrors;
  /**
   * Should parsing be discontinued by throwing this {@link org.xml.sax.SAXException} in the case of
   * {@linkplain #fatalError(SAXParseException) fatal errors}?
   */
  protected final boolean throwFatalErrors;
  /**
   * @see #getLogMessageAnnotations()
   */
  protected final SortedMap<Level,List<Map.Entry<String,Object>>> annotations = new TreeMap<Level,List<Map.Entry<String,Object>>>(AnnotatedLogMessageImpl.LEVEL_COMPARATOR);

  /**
   * Construct a new <code>SAXErrorHandlerException</code>.
   * 
   * @param throwWarnings Should parsing be discontinued by throwing this {@link org.xml.sax.SAXException} in the case
   * of {@linkplain #warning(SAXParseException) warnings}?
   * @param throwErrors Should parsing be discontinued by throwing this {@link org.xml.sax.SAXException} in the case of
   * {@linkplain #error(SAXParseException) errors}?
   * @param throwFatalErrors Should parsing be discontinued by throwing this {@link org.xml.sax.SAXException} in the
   * case of {@linkplain #fatalError(SAXParseException) fatal errors}?
   * @param inputSource Error messages will be constructed using this {@link InputSource#getPublicId()} for referencing
   * the {@link InputSource} resource being parsed.
   */
  public SAXErrorHandlerException(final boolean throwWarnings, final boolean throwErrors, final boolean throwFatalErrors, final @Nullable InputSource inputSource) {
    super(SAXErrorHandlerException.class.getSimpleName(), null);
    this.throwWarnings = throwWarnings;
    this.throwErrors = throwErrors;
    this.throwFatalErrors = throwFatalErrors;
    if (inputSource != null) LogAnnotation.annotate(this, "source", inputSource.getPublicId(), Level.OFF, false);
    return;
  }

  @Override
  public @Nullable Throwable getCause() {
    return (cause == this) ? null : cause;
  }

  @Override
  public synchronized Throwable initCause(final @Nullable Throwable cause) throws IllegalStateException, IllegalArgumentException {
    super.initCause(cause);
    this.cause = (Exception)cause;
    return this;
  }

  /**
   * Search the {@linkplain Throwable#getCause() cause} tree of the supplied {@link Throwable} object, looking for a
   * cause of this class.
   * 
   * @param throwable The leaf of the cause tree we are searching.
   * @return A <code>SAXErrorHandlerException</code> cause, or <code>null</code> if none was found.
   */
  protected static final @Nullable SAXErrorHandlerException getSAXErrorHandlerExceptionCause(final @Nullable Throwable throwable) {
    return ((throwable != null) && (throwable.getCause() != null)) ? ((SAXErrorHandlerException.class.isInstance(throwable.getCause())) ? SAXErrorHandlerException.class.cast(throwable.getCause()) : getSAXErrorHandlerExceptionCause(throwable.getCause())) : null;
  }

  @Override
  public void warning(final SAXParseException exception) throws org.xml.sax.SAXException {
    final boolean exceptionCauseIsThis = getSAXErrorHandlerExceptionCause(exception) == this;
    if (!exceptionCauseIsThis) LogAnnotation.annotate(this, "warning", exception, Level.WARNING, true);
    if (throwWarnings) {
      if (!exceptionCauseIsThis) initCause(exception);
      if (exceptionCauseIsThis) throw exception;
      throw new SAXException(null, this); // Wrap 'this' so stack trace is from here, not where 'this' was constructed.
    }
    return;
  }

  @Override
  public void error(final SAXParseException exception) throws org.xml.sax.SAXException {
    final boolean exceptionCauseIsThis = getSAXErrorHandlerExceptionCause(exception) == this;
    if (!exceptionCauseIsThis) LogAnnotation.annotate(this, "error", exception, Level.SEVERE, true);
    if (throwErrors) {
      if (!exceptionCauseIsThis) initCause(exception);
      if (exceptionCauseIsThis) throw exception;
      throw new SAXException(null, this); // Wrap 'this' so stack trace is from here, not where 'this' was constructed.
    }
    return;
  }

  @Override
  public void fatalError(final SAXParseException exception) throws org.xml.sax.SAXException {
    final boolean exceptionCauseIsThis = getSAXErrorHandlerExceptionCause(exception) == this;
    if (!exceptionCauseIsThis) LogAnnotation.annotate(this, "fatal", exception, Level.SEVERE, true);
    if (throwFatalErrors) {
      if (!exceptionCauseIsThis) initCause(exception);
      if (exceptionCauseIsThis) throw exception;
      throw new SAXException(null, this); // Wrap 'this' so stack trace is from here, not where 'this' was constructed.
    }
    return;
  }

  @Override
  public @Nullable String getMessage() {
    return LogAnnotation.toString(this, Level.INFO).get();
  }

  @Override
  public String getAnnotatedLogMessage() {
    return Objects.requireNonNull(super.getMessage());
  }

  @Override
  public @Nullable Level getAnnotatedLogMessageLevel() {
    return null; // == max annotation level.
  }

  @Override
  public SortedMap<Level,List<Map.Entry<String,Object>>> getLogMessageAnnotations() {
    return annotations;
  }

  @Override
  public String toString() {
    return getClass().getName() + ": " + LogAnnotation.toString(this, Level.INFO).get();
  }

}
