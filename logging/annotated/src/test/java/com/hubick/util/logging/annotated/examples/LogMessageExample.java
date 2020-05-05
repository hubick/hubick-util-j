/*
 * Copyright 2013-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.logging.annotated.examples;

import java.io.*;
import java.util.logging.*;

import org.eclipse.jdt.annotation.*;

import com.hubick.util.logging.annotated.*;
import com.hubick.util.logging.annotated.impl.*;


/**
 * A simple example of using an {@link AnnotatedLogMessage}.
 */
@NonNullByDefault
public class LogMessageExample {

  /**
   * A sample base class for demonstrating the use of an {@link AnnotatedLogMessage} during {@linkplain #init()
   * initialization} and {@linkplain #close() closure}.
   */
  public abstract static class MyBaseClass implements AutoCloseable {
    /**
     * Some thing we can use as an example log annotation.
     */
    private final String baseThing;

    /**
     * Construct a new <code>MyBaseClass</code>.
     * 
     * @param baseThing Some thing we can use as an example log annotation.
     */
    protected MyBaseClass(final String baseThing) {
      this.baseThing = baseThing;
      return;
    }

    /**
     * Initialize this object.
     * 
     * @param message The {@link AnnotatedLogMessage} that will be logged when this object has been successfully
     * initialized, which can be annotated with any pertinent information.
     * @throws Exception If there was an error during initialization.
     */
    protected void initInternal(final AnnotatedLogMessage message) throws Exception {
      LogAnnotation.annotate(message, "BaseThing", baseThing);
      return;
    }

    /**
     * Initialize this object.
     * 
     * @throws Exception If there was an error during initialization
     */
    public final void init() throws Exception {
      final AnnotatedLogMessage message = new AnnotatedLogMessageImpl(Level.INFO, getClass().getSimpleName() + " successfully initialized"); // Level.INFO used for example purposes, but in reality, you might want to use Level.FINE or something.
      initInternal(message);
      LogAnnotation.log(Logger.getLogger(getClass().getName()), message, getClass());
      return;
    }

    /**
     * Close this object.
     * 
     * @param message The {@link AnnotatedLogMessage} that will be logged when this object has been closed, which can be
     * annotated with any pertinent information.
     */
    protected void closeInternal(final AnnotatedLogMessage message) {
      return;
    }

    @Override
    public final void close() {
      final AnnotatedLogMessage message = new AnnotatedLogMessageImpl(Level.INFO, getClass().getSimpleName() + " closed"); // Level.INFO used for example purposes, but in reality, you might want to use Level.FINE or something.
      closeInternal(message);
      LogAnnotation.log(Logger.getLogger(getClass().getName()), message, getClass());
      return;
    }

    @Override
    public String toString() {
      return "[BaseThing=" + baseThing + "]";
    }

  } // MyBaseClass

  /**
   * A sample child class for demonstrating the use of an {@link AnnotatedLogMessage} during {@linkplain #init()
   * initialization} and {@linkplain #close() closure}.
   */
  public static class MyChildClass extends MyBaseClass {
    /**
     * Some thing we can use as an example log annotation.
     */
    private final String childThing;

    /**
     * Construct a new <code>MyChildClass</code>.
     * 
     * @param baseThing Some thing we can use as an example log annotation.
     * @param childThing Some thing we can use as an example log annotation.
     */
    public MyChildClass(final String baseThing, final String childThing) {
      super(baseThing);
      this.childThing = childThing;
      return;
    }

    @Override
    protected void initInternal(final AnnotatedLogMessage message) throws Exception {
      super.initInternal(message);
      LogAnnotation.annotate(message, "ChildThing", childThing);
      return;
    }

    @Override
    protected void closeInternal(final AnnotatedLogMessage message) {
      super.closeInternal(message);
      try {
        throw new IOException("Timeout waiting for lock on ChildThing (pretend example)");
      } catch (IOException ioe) {
        LogAnnotation.annotate(message, "ChildThing", childThing);
        LogAnnotation.annotate(message, ioe.getClass().getSimpleName() + " closing ChildThing", ioe);
      }
      return;
    }

    @Override
    public String toString() {
      return super.toString() + "[ChildThing=" + childThing + "]";
    }

  } // MyChildClass

  /**
   * Execute this example. You should see annotated log messages showing the initialization and closure of the
   * 'MyChildClass' instance.
   * 
   * @param args The arguments provided to this example program.
   * @throws Exception If there was a problem executing the program.
   */
  public static void main(final @NonNull String[] args) throws Exception {
    try (MyChildClass myChild = new MyChildClass("Example base thing", "Example child thing")) {
      myChild.init();
      System.out.println("MyChild=" + myChild.toString());
    }
    return;
  }

}
