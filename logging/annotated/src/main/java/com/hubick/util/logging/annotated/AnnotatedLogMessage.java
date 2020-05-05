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
 * A log message which can be {@linkplain LogAnnotation#annotate(AnnotatedLogMessage, String, Object, Level, boolean)
 * annotated} with additional information.
 */
@NonNullByDefault
public interface AnnotatedLogMessage {

  /**
   * The {@link Level} at which this message should be logged. This value will also be used as the default when
   * {@linkplain LogAnnotation#annotate(AnnotatedLogMessage, String, Object, Level, boolean) annotating}, if no specific
   * level is provided.
   * 
   * @return The log {@link Level} for this message.
   */
  public @Nullable Level getAnnotatedLogMessageLevel();

  /**
   * Get the &quot;main&quot; log message associated with this object.
   * 
   * @return The log message.
   */
  public String getAnnotatedLogMessage();

  /**
   * Get the {@link Map} used to store annotations for each log {@link Level}.
   * 
   * @return The annotation Map.
   */
  public SortedMap<Level,List<Map.Entry<String,Object>>> getLogMessageAnnotations();

}
