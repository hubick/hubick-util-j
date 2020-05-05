/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.logging.annotated.impl;

import java.util.*;
import java.util.logging.*;

import org.eclipse.jdt.annotation.*;

import com.hubick.util.logging.annotated.*;


/**
 * A simple default implementation of the {@link AnnotatedLogMessage} interface.
 */
@NonNullByDefault
public class AnnotatedLogMessageImpl implements AnnotatedLogMessage {
  /**
   * A {@link Comparator} implementation for {@link Level} objects. This object can be supplied when constructing a
   * {@link SortedMap} while implementing message {@linkplain AnnotatedLogMessage#getLogMessageAnnotations()
   * annotations}.
   */
  public static final Comparator<Level> LEVEL_COMPARATOR = (l1, l2) -> Integer.valueOf(l1.intValue()).compareTo(Integer.valueOf(l2.intValue()));

  /**
   * @see #getAnnotatedLogMessageLevel()
   */
  protected final @Nullable Level level;
  /**
   * @see #getAnnotatedLogMessage()
   */
  protected final String message;
  /**
   * @see #getLogMessageAnnotations()
   */
  protected final SortedMap<Level,List<Map.Entry<String,Object>>> annotations = new TreeMap<Level,List<Map.Entry<String,Object>>>(LEVEL_COMPARATOR);

  /**
   * Construct a new <code>AnnotatedLogMessageImpl</code>.
   * 
   * @param level The log {@link Level} for this message.
   * @param message The log message.
   */
  public AnnotatedLogMessageImpl(final @Nullable Level level, final String message) {
    this.level = level;
    this.message = message;
    //TODO return; // https://bugs.openjdk.java.net/browse/JDK-8036775
  }

  @Override
  public @Nullable Level getAnnotatedLogMessageLevel() {
    return level;
  }

  @Override
  public String getAnnotatedLogMessage() {
    return message;
  }

  @Override
  public SortedMap<Level,List<Map.Entry<String,Object>>> getLogMessageAnnotations() {
    return annotations;
  }

  @Override
  public String toString() {
    return LogAnnotation.toString(this, Level.ALL).get();
  }

}
