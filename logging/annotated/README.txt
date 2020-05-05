Annotated Logging
-----------------

A Java library for incrementally creating log messages (java.util.logging.LogRecord) via key+value pair "annotations" (unrelated to Java source code annotations).

This facility allows a single log message to be created additively, by code spread across multiple contexts/classes/methods/etc.


Annotations
-----------

An "annotation" is defined in the dictionary as "a note added by way of comment or explanation".

The core of this library is the AnnotatedLogMessage interface, defining an object containing a log message which may be "annotated" with multiple key+value pairs to provide such additional information.

You interact with AnnotatedLogMessage implementations through static utility methods provided by the LogAnnotation class.


Basic Logging
-------------

The first way to use this library is for basic logging...

For example, let's say you have an init() method in a base class.  You can create an AnnotatedLogMessage for logging it's initialization...

public final void init() {
  final AnnotatedLogMessage message = new AnnotatedLogMessageImpl(Level.INFO, getClass().getSimpleName() + " successfully initialized");
  initInternal(message);
  LogAnnotation.log(Logger.getLogger(getClass().getName()), message, getClass());
}

Then, while the object is being initialized, code in that base class or any of it's child classes can all annotate that same message with information about what parameters the object was initialized with:

protected void initInternal(AnnotatedLogMessage message) throws Exception {
  super.initInternal(message);
  ...
  LogAnnotation.annotate(message, "SomeThing", someThing);
  return;
}

When the message is logged, it will display both the main message as well as the annotations.


Exception Logging
-----------------

The second (primary, and most useful) way to use this library is for exception logging...

When you create a custom exception class, you also make it an AnnotatedLogMessage:

public class ConfigurationException extends Exception implements AnnotatedLogMessage {...}

Then, if that exception is thrown, each method in the stack can annotate it with information about the context in which it occurred before propagating it:

try {
  ...
} catch (ConfigurationException ce) {
  throw LogAnnotation.annotate(ce, "SomeThing", someThing);
}

And eventually propagation up the stack will stop when the exception is caught, at which point it can be logged, printing the main exception message along with all it's annotations:

try {
...
} catch (ConfigurationException ce) {
  LogAnnotation.log(Logger.getLogger(getClass().getName()), ce, getClass(), ce);
}


Documentation and Examples
--------------------------

More detailed documentation is included in the Javadoc for each class, and there are complete usage examples in the 'com.hubick.util.logging.annotated.examples' package under the 'src/test/java/' folder.
