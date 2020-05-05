/*
 * Copyright 2013-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.logging.annotated.examples;

import java.util.*;
import java.util.logging.*;

import org.eclipse.jdt.annotation.*;

import com.hubick.util.logging.annotated.*;
import com.hubick.util.logging.annotated.impl.*;


/**
 * An example of how to use {@link LogAnnotation} with exceptions.
 */
@NonNullByDefault
public class AnnotatedExceptionExample {
  /**
   * The {@link Logger} for this class.
   */
  protected static final Logger LOGGER = Logger.getLogger(AnnotatedExceptionExample.class.getName());

  /**
   * Extract the value of the required configuration parameter from the program arguments.
   * 
   * @param args The arguments to the program.
   * @param index The index of the desired configuration parameter.
   * @param paramName The name of the desired configuration parameter.
   * @return The value of the configuration argument at the given index.
   * @throws ConfigurationException If there was a problem retrieving the required argument.
   */
  public static final String getRequiredConfigParam(final @NonNull String[] args, final int index, final String paramName) throws ConfigurationException {
    try {
      return args[index];
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      final ConfigurationException ce = new ConfigurationException("A required configuration parameter was not supplied", aioobe);
      throw LogAnnotation.annotate(LogAnnotation.annotate(ce, "Index", Integer.valueOf(index)), "ParamName", paramName); // You might not want to nest calls like this if you have a lot of annotations, but it can be a convenient shorthand.
    }
  }

  /**
   * Get the message to be output by this program for the given user.
   * 
   * @param user The user we are building a message for.
   * @param args The arguments to the program.
   * @return The message that should be displayed to the user.
   * @throws ConfigurationException If there was a problem creating the message.
   */
  public static final String getMessage(final @Nullable String user, final @NonNull String[] args) throws ConfigurationException {
    try {
      final StringBuffer message = new StringBuffer();

      final String greeting = getRequiredConfigParam(args, 0, "greeting");
      message.append(greeting);

      message.append(' ');
      message.append((user != null) ? user : "anonymous");

      return message.toString();
    } catch (ConfigurationException ce) {
      throw LogAnnotation.annotate(ce, "User", user);
    }
  }

  /**
   * Execute this example. By default, it should print a configuration exception message, annotated with the 'Index',
   * 'ParamName', and 'User' in effect at the time the exception occurred.
   * 
   * @param args The arguments provided to this example program.
   */
  public static void main(final @NonNull String[] args) {
    try {
      System.out.println(getMessage(System.getProperty("user.name"), args));
    } catch (ConfigurationException ce) {
      LogAnnotation.log(LOGGER, ce, AnnotatedExceptionExample.class, ce);
    }
    return;
  }

  /**
   * A custom example exception class for problems with software configuration, implementing {@link AnnotatedLogMessage}
   * so that it may be {@link LogAnnotation#annotate(AnnotatedLogMessage, String, Object) annotated} with pertinent
   * information about the context in which it occurred.
   */
  public static class ConfigurationException extends Exception implements AnnotatedLogMessage {
    /**
     * @see #getLogMessageAnnotations()
     */
    protected final SortedMap<Level,List<Map.Entry<String,Object>>> annotations = new TreeMap<Level,List<Map.Entry<String,Object>>>(AnnotatedLogMessageImpl.LEVEL_COMPARATOR);

    /**
     * Construct a new <code>ConfigurationException</code> instance.
     * 
     * @param message The detail message.
     * @param cause The exception which lead to this configuration exception.
     */
    public ConfigurationException(String message, final Throwable cause) {
      super(message, cause);
      return;
    }

    @Override
    public @Nullable Level getAnnotatedLogMessageLevel() {
      return Level.SEVERE;
    }

    @Override
    public String getAnnotatedLogMessage() {
      return getClass().getSimpleName() + ": " + getMessage();
    }

    @Override
    public SortedMap<Level,List<Map.Entry<String,Object>>> getLogMessageAnnotations() {
      return annotations;
    }

  } // ConfigurationException

}
