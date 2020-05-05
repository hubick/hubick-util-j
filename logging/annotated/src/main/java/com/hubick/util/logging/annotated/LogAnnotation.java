/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.logging.annotated;

import java.util.*;
import java.util.logging.*;

import org.eclipse.jdt.annotation.*;


/**
 * A utility class providing static methods for
 * {@linkplain #annotate(AnnotatedLogMessage, String, Object, Level, boolean) annotating},
 * {@linkplain #toString(AnnotatedLogMessage, Level) printing}, and
 * {@linkplain #log(Logger, AnnotatedLogMessage, Class, Throwable) logging} an {@link AnnotatedLogMessage} instance.
 */
@NonNullByDefault
public abstract class LogAnnotation {

  /**
   * Add an {@linkplain AnnotatedLogMessage#getLogMessageAnnotations() annotation} to a {@link AnnotatedLogMessage}
   * object.
   * 
   * @param <M> The type of message object.
   * @param message The message being annotated.
   * @param key The key for the annotation.
   * @param value The value of the annotation, or <code>null</code> if any existing value(s) for this <code>key</code>
   * should be removed.
   * @param level Log this annotation at something other than the
   * {@linkplain AnnotatedLogMessage#getAnnotatedLogMessageLevel() default} message {@link Level}. {@link Level#OFF} can
   * be used for annotations which should always be logged, but only if the message itself is being logged.
   * @param multivalued Should this <code>key</code> be annotated with multiple values, or should any existing value be
   * overwritten with the new value?
   * @return The <code>message</code> object.
   */
  public static final <M extends AnnotatedLogMessage> M annotate(final M message, final String key, final @Nullable Object value, final @Nullable Level level, final boolean multivalued) {
    final SortedMap<Level,List<Map.Entry<String,Object>>> annotations = message.getLogMessageAnnotations();

    // Is there an existing entry for this key?
    for (Map.Entry<Level,List<Map.Entry<String,Object>>> levelAnnotations : annotations.entrySet()) {
      final Iterator<Map.Entry<String,Object>> annotationsIter = levelAnnotations.getValue().iterator();
      while (annotationsIter.hasNext()) {
        final Map.Entry<String,Object> annotation = annotationsIter.next();
        if (!key.equals(annotation.getKey())) continue;
        if ((value == null) || (!multivalued) || (annotation.getValue() == value)) annotationsIter.remove();
      }
    }

    if (value == null) return message;

    Level effectiveLevel = (level != null) ? level : message.getAnnotatedLogMessageLevel();
    if (effectiveLevel == null) effectiveLevel = Level.INFO;

    final @Nullable List<Map.Entry<String,Object>> originalValues = annotations.get(effectiveLevel);
    final List<Map.Entry<String,Object>> values;
    if (originalValues != null) {
      values = originalValues;
    } else {
      values = new ArrayList<Map.Entry<String,Object>>(1);
      message.getLogMessageAnnotations().put(effectiveLevel, values);
    }
    values.add(new AbstractMap.SimpleImmutableEntry<String,Object>(key, value));

    return message;
  }

  /**
   * Add an {@linkplain AnnotatedLogMessage#getLogMessageAnnotations() annotation} to a {@link AnnotatedLogMessage}
   * object. This annotation will be single-valued and use the
   * {@linkplain AnnotatedLogMessage#getAnnotatedLogMessageLevel() default} level.
   * 
   * @param <M> The type of message object.
   * @param message The message being annotated.
   * @param key The key for the annotation.
   * @param value The value of the annotation, or <code>null</code> if any existing value(s) for this <code>key</code>
   * should be removed.
   * @return The <code>message</code> object.
   */
  public static final <M extends AnnotatedLogMessage> M annotate(final M message, final String key, final @Nullable Object value) {
    return annotate(message, key, value, null, false);
  }

  /**
   * Get the effective log {@link Level} for this {@link Logger}.
   * 
   * @param logger The {@link Logger} to get the effective level for.
   * @return The level in effect for this <code>logger</code>.
   * @see Logger#getLevel()
   */
  protected static final Level getEffectiveLevel(final @Nullable Logger logger) {
    if (logger == null) return Level.INFO;
    final Level loggerLevel = logger.getLevel();
    if (loggerLevel != null) return loggerLevel;
    return getEffectiveLevel(logger.getParent());
  }

  /**
   * Would the supplied <code>messageLevel</code> be {@linkplain Logger#isLoggable(Level) loggable} by a Logger with the
   * given <code>logLevel</code>?
   * 
   * @param messageLevel The {@link Level} for which we want to determine if it would be
   * {@linkplain Logger#isLoggable(Level) logged}.
   * @param logLevel The {@linkplain Logger#getLevel() effective level} of the logger.
   * @return <code>true</code> if that <code>messageLevel</code> would be logged.
   */
  protected static final boolean isLoggable(final @Nullable Level messageLevel, final Level logLevel) {
    return ((messageLevel != null) && (messageLevel.intValue() >= logLevel.intValue()));
  }

  /**
   * Get the effective log {@link Level} for the supplied {@link AnnotatedLogMessage}. The effective level defaults to
   * the main {@linkplain AnnotatedLogMessage#getAnnotatedLogMessageLevel() message level}, but will be raised to match
   * a higher level if any such {@linkplain AnnotatedLogMessage#getLogMessageAnnotations() annotations} are contained
   * within the message.
   * 
   * @param message The {@link AnnotatedLogMessage} to determine the level for.
   * @return The effective level of the supplied message.
   */
  protected static final @Nullable Level getEffectiveLevel(final AnnotatedLogMessage message) {
    Level messageLevel = message.getAnnotatedLogMessageLevel();
    if (messageLevel == null) {
      final SortedMap<Level,List<Map.Entry<String,Object>>> annotations = message.getLogMessageAnnotations();
      for (Level annotationsLevel : annotations.keySet()) {
        if (annotationsLevel == Level.OFF) continue;
        if ((messageLevel == null) || (annotationsLevel.intValue() > messageLevel.intValue())) messageLevel = annotationsLevel;
      }
    }
    return messageLevel;
  }

  /**
   * Create a String representation of the given {@link AnnotatedLogMessage}.
   * 
   * @param message The {@link AnnotatedLogMessage} to create a String representation of.
   * @param level An indication of the {@link Level} of detail desired in the returned String (defaults to
   * {@link Level#INFO}).
   * @return The String representation of the Message if it would be logged at the given <code>level</code>.
   */
  public static final Optional<String> toString(final AnnotatedLogMessage message, final @Nullable Level level) {
    final Level logLevel = (level != null) ? level : Level.INFO;
    final Level messageLevel = getEffectiveLevel(message);
    if (!isLoggable(messageLevel, logLevel)) return Optional.empty();

    final StringBuffer sb = new StringBuffer();

    sb.append(message.getAnnotatedLogMessage());

    boolean first = true;
    for (Map.Entry<Level,List<Map.Entry<String,Object>>> levelAnnotations : message.getLogMessageAnnotations().entrySet()) {
      if (!isLoggable(levelAnnotations.getKey(), logLevel)) continue;
      for (Map.Entry<String,Object> annotation : levelAnnotations.getValue()) {
        if ((first) && (sb.length() > 0)) sb.append(' ');
        sb.append('[');
        sb.append(annotation.getKey());
        sb.append("='");
        sb.append(String.valueOf(annotation.getValue()).replace("\\", "\\\\").replace("'", "\\'"));
        sb.append("']");
        first = false;
      }
    }

    return Optional.of(sb.toString());
  }

  /**
   * Construct a {@link LogRecord} for the given {@link AnnotatedLogMessage} and {@linkplain Logger#log(LogRecord) log}
   * it to the supplied {@link Logger}.
   * 
   * @param <M> The type of message object.
   * @param logger Where the message should be {@linkplain Logger#log(LogRecord) logged}.
   * @param message The {@link AnnotatedLogMessage} object to be logged.
   * @param sourceClass The {@linkplain LogRecord#setSourceClassName(String) source class name} that issued the logging
   * request.
   * @param thrown A {@linkplain LogRecord#setThrown(Throwable) thrown} object, associated with the logging event.
   * @return The <code>message</code> object.
   */
  public static final <M extends AnnotatedLogMessage> M log(final Logger logger, final M message, final @Nullable Class<?> sourceClass, final @Nullable Throwable thrown) {
    final Level loggerLevel = getEffectiveLevel(logger);
    final Optional<String> messageString = toString(message, loggerLevel);
    if (!messageString.isPresent()) return message;

    final Level messageLevel = getEffectiveLevel(message);
    final LogRecord logRecord = new LogRecord(messageLevel, messageString.get());
    if (sourceClass != null) logRecord.setSourceClassName(sourceClass.getName());
    if (thrown != null) logRecord.setThrown(thrown);

    logger.log(logRecord);

    return message;
  }

  /**
   * Construct a {@link LogRecord} for the given {@link AnnotatedLogMessage} and {@linkplain Logger#log(LogRecord) log}
   * it to the supplied {@link Logger}.
   * 
   * @param <M> The type of message object.
   * @param logger Where the message should be {@linkplain Logger#log(LogRecord) logged}.
   * @param message The {@link AnnotatedLogMessage} object to be logged.
   * @param sourceClass The {@linkplain LogRecord#setSourceClassName(String) source class name} that issued the logging
   * request.
   * @return The <code>message</code> object.
   */
  public static final <M extends AnnotatedLogMessage> M log(final Logger logger, final M message, final @Nullable Class<?> sourceClass) {
    return log(logger, message, sourceClass, null);
  }

}
