module com.hubick.util.logging.annotated {
  requires transitive java.logging;
  requires transitive org.eclipse.jdt.annotation;
  requires static java.xml;

  exports com.hubick.util.logging.annotated;
  exports com.hubick.util.logging.annotated.impl;
  exports com.hubick.util.logging.annotated.impl.xml.sax;
}
