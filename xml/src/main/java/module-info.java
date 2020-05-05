module com.hubick.util.xml {
  requires transitive org.eclipse.jdt.annotation;
  requires transitive jakarta.activation;
  requires transitive java.xml;
  requires transitive com.hubick.util.core;

  exports com.hubick.util.xml;
  exports com.hubick.util.xml.dom;
  exports com.hubick.util.xml.html;
  exports com.hubick.util.xml.sax;
  exports com.hubick.util.xml.stream;
  exports com.hubick.util.xml.transform;
}
