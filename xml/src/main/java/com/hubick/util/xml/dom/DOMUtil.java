/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.dom;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javax.xml.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.bootstrap.*;
import org.w3c.dom.ls.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.hubick.util.core.collection.*;

import com.hubick.util.xml.*;

import org.eclipse.jdt.annotation.*;


/**
 * DOM Utilities.
 */
@NonNullByDefault
public abstract class DOMUtil {
  /**
   * A shared {@linkplain DocumentBuilderFactory#isNamespaceAware() namespace-aware}
   * {@linkplain DocumentBuilderFactory#isValidating() non-validating} {@link DocumentBuilderFactory}. Access to this
   * factory should be synchronized for thread safety.
   */
  protected static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    DOCUMENT_BUILDER_FACTORY.setValidating(false);
    try {
      DOCUMENT_BUILDER_FACTORY.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE); // Ignore the external DTD completely.
    } catch (java.lang.Exception e) {}
  }
  /**
   * A shared {@link DOMImplementationLS}.
   */
  protected static final DOMImplementationLS DOM_IMPLEMENTATION_LS;
  static {
    try {
      DOM_IMPLEMENTATION_LS = Objects.requireNonNull((DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS"));
    } catch (Exception e) {
      throw new RuntimeException(e.getClass().getName() + ": " + e.getMessage(), e);
    }
  }
  /**
   * A shared {@link ErrorHandler} instance.
   */
  protected static final ErrorHandler ERROR_HANDLER = new DefaultHandler();

  /**
   * Create a new DOM {@link Document}.
   * 
   * @return The new {@link Document}.
   * @see DocumentBuilderFactory#newDocumentBuilder()
   * @see DocumentBuilder#newDocument()
   */
  public static final Document newDocument() {
    final DocumentBuilder documentBuilder;
    try {
      synchronized (DOCUMENT_BUILDER_FACTORY) {
        documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
      }
    } catch (ParserConfigurationException pce) { // Should never happen with our internal factory.
      throw new RuntimeException(pce);
    }
    return documentBuilder.newDocument();
  }

  /**
   * {@linkplain DocumentBuilder#parse(InputSource) Parse} a {@link Document} from an {@link InputSource}.
   * 
   * @param source The {@link InputSource} to be parsed.
   * @return The parsed {@link Document}.
   * @throws UncheckedIOException If an {@link IOException} occurred trying to read the document.
   * @throws LSException If a {@link SAXException} occurred parsing the document.
   * @see DocumentBuilderFactory#newDocumentBuilder()
   * @see DocumentBuilder#parse(InputSource)
   */
  public static final Document parseDocument(final InputSource source) throws UncheckedIOException, LSException {
    final DocumentBuilder builder;
    try {
      synchronized (DOCUMENT_BUILDER_FACTORY) {
        builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
      }
    } catch (ParserConfigurationException pce) { // Should never happen with our internal factory.
      throw new RuntimeException(pce);
    }
    builder.setErrorHandler(ERROR_HANDLER);
    try {
      return builder.parse(source);
    } catch (SAXException saxe) {
      final LSException lse = new LSException(LSException.PARSE_ERR, saxe.getMessage());
      lse.initCause(saxe);
      throw lse;
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }

  /**
   * {@linkplain #parseDocument(InputSource) Parse} a {@link Document} from a String containing the content.
   * 
   * @param source The String to be parsed.
   * @return The parsed {@link Document}.
   * @throws LSException If a {@link SAXException} occurred parsing the document.
   * @see #parseDocument(InputSource)
   */
  public static final Document parseDocument(final String source) throws LSException {
    return parseDocument(new InputSource(new StringReader(source)));
  }

  /**
   * {@linkplain DocumentBuilder#parse(InputSource) Parse} a {@link Document} from a {@link URL}.
   * 
   * @param url The {@link URL} to be parsed.
   * @return The parsed {@link Document}.
   * @throws UncheckedIOException If an {@link IOException} occurred trying to read the document.
   * @throws LSException If a {@link SAXException} occurred parsing the document.
   * @see DocumentBuilderFactory#newDocumentBuilder()
   * @see DocumentBuilder#parse(InputSource)
   */
  public static final Document parseDocument(final URL url) throws UncheckedIOException, LSException {
    final DocumentBuilder builder;
    try {
      synchronized (DOCUMENT_BUILDER_FACTORY) {
        builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
      }
    } catch (ParserConfigurationException pce) { // Should never happen with our internal factory.
      throw new RuntimeException(pce);
    }
    builder.setEntityResolver(new EntityResolver() {

      @Override
      public @Nullable InputSource resolveEntity(@Nullable String publicId, String systemId) throws IOException {
        return new InputSource(new URL(url, systemId).toExternalForm());
      }

    });
    try {
      final InputSource inputSource = new InputSource(url.openStream());
      builder.setErrorHandler(ERROR_HANDLER);
      return builder.parse(inputSource);
    } catch (SAXException saxe) {
      final LSException lse = new LSException(LSException.PARSE_ERR, saxe.getMessage());
      lse.initCause(saxe);
      throw lse;
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }

  /**
   * {@linkplain DOMImplementationLS#createLSInput() Create} an {@link LSInput} for a {@link URL}.
   * 
   * @param url The {@link URL} to use as the {@linkplain LSInput#setBaseURI(String) base URI}.
   * @return The created {@link LSInput}.
   * @throws UncheckedIOException If an {@link IOException} occurred trying to {@linkplain URL#openStream() open} the
   * <code>url</code>.
   * @see DOMImplementationLS#createLSInput()
   */
  public static final LSInput createLSInput(final URL url) throws UncheckedIOException {
    final LSInput result;
    synchronized (DOM_IMPLEMENTATION_LS) {
      result = DOM_IMPLEMENTATION_LS.createLSInput();
    }
    result.setBaseURI(url.toString());
    try {
      result.setByteStream(url.openStream());
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
    return result;
  }

  /**
   * {@linkplain DOMImplementationLS#createLSOutput() Create} an {@link LSOutput} for an {@link OutputStream}.
   * 
   * @param os The {@link OutputStream} to use as the {@linkplain LSOutput#setByteStream(OutputStream) byte stream}.
   * @return The created {@link LSOutput}.
   * @see DOMImplementationLS#createLSOutput()
   */
  public static final LSOutput createLSOutput(final OutputStream os) {
    final LSOutput result;
    synchronized (DOM_IMPLEMENTATION_LS) {
      result = DOM_IMPLEMENTATION_LS.createLSOutput();
    }
    result.setByteStream(os);
    return result;
  }

  /**
   * {@linkplain DOMImplementationLS#createLSSerializer() Create} and configure a new {@link LSSerializer}.
   * 
   * @param printXMLDeclaration Should the returned serializer emit an XML declaration?
   * @param printNamespaceDeclarations Should the returned serializer print XML Namespace declarations?
   * @param prettyPrint Should the returned serializer emit XML formatted with whitespace to be human readable?
   * @return The new {@link LSSerializer}.
   * @see DOMImplementationLS#createLSSerializer()
   */
  public static final LSSerializer createLSSerializer(final boolean printXMLDeclaration, final boolean printNamespaceDeclarations, final boolean prettyPrint) {
    final LSSerializer lsSerializer;
    synchronized (DOM_IMPLEMENTATION_LS) {
      lsSerializer = DOM_IMPLEMENTATION_LS.createLSSerializer();
    }
    final DOMConfiguration config = lsSerializer.getDomConfig();
    if (config.canSetParameter("xml-declaration", Boolean.valueOf(printXMLDeclaration))) config.setParameter("xml-declaration", Boolean.valueOf(printXMLDeclaration));
    if (config.canSetParameter("namespace-declarations", Boolean.valueOf(printNamespaceDeclarations))) config.setParameter("namespace-declarations", Boolean.valueOf(printNamespaceDeclarations));
    if (config.canSetParameter("format-pretty-print", Boolean.valueOf(prettyPrint))) config.setParameter("format-pretty-print", Boolean.valueOf(prettyPrint));
    return lsSerializer;
  }

  /**
   * A wrapper around {@link Node#getOwnerDocument()} which can handle the supplied <code>node</code> being a
   * {@link Document}.
   * 
   * @param node The {@link Node} whose document is desired.
   * @return The {@link Document} which {@linkplain Node#getOwnerDocument() owns} the supplied <code>node</code>, or the
   * <code>node</code> itself.
   */
  public static final Document getDocument(final Node node) {
    return (node instanceof Document) ? (Document)node : Objects.requireNonNull(node.getOwnerDocument());
  }

  /**
   * {@linkplain LSSerializer#writeToString(Node) Write} out a {@link String} from a {@link Node} using the default
   * options.
   * 
   * @param node The {@link Node} to serialize.
   * @return The serialized <code>node</code> XML.
   * @throws DOMException If the result String is too long.
   * @throws LSException If the serializer was unable to {@linkplain LSSerializer#writeToString(Node) write} the
   * <code>node</code>.
   * @see LSSerializer#writeToString(Node)
   */
  public static final String toString(final Node node) throws DOMException, LSException {
    final LSSerializer serializer;
    synchronized (DOM_IMPLEMENTATION_LS) {
      serializer = DOM_IMPLEMENTATION_LS.createLSSerializer();
    }
    synchronized (getDocument(node)) {
      return serializer.writeToString(node);
    }
  }

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} if a {@link Node} is of a specified
   * {@link Node#getNodeType() nodeType}.
   * 
   * @param nodeType The {@linkplain Node#getNodeType() type} of {@link Node} desired, or <code>-1</code> if it doesn't
   * matter.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Node} being
   * {@linkplain Predicate#test(Object) tested} is non-<code>null</code> and of the specified {@link Node#getNodeType()
   * nodeType}.
   */
  public static final Predicate<@Nullable Node> testType(final short nodeType) {
    return (node) -> {
      if (nodeType < 0) return true;
      if (node == null) return false;
      synchronized (getDocument(node)) {
        return node.getNodeType() == nodeType;
      }
    };
  }

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} if a {@link Node} is in a specified
   * {@link Node#getNamespaceURI() nsURI}.
   * 
   * @param <N> The type of {@link Node} being tested.
   * @param nsURI The {@linkplain Node#getNamespaceURI() namespace} of {@link Node} desired, or <code>null</code> if it
   * doesn't matter.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Node} being
   * {@linkplain Predicate#test(Object) tested} is non-<code>null</code> and in the specified
   * {@link Node#getNamespaceURI() nsURI}.
   */
  public static final <N extends @Nullable Node> Predicate<N> testNS(final @Nullable URI nsURI) {
    return (node) -> {
      if (nsURI == null) return true;
      if (node == null) return false;
      final String nodeNamespace;
      synchronized (getDocument(node)) {
        nodeNamespace = node.getNamespaceURI();
      }
      return (nodeNamespace != null) && nsURI.toString().equals(nodeNamespace);
    };
  }

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} if a {@link Node} has a specified
   * {@link Node#getLocalName() localName}.
   * 
   * @param <N> The type of {@link Node} being tested.
   * @param localName The {@linkplain Node#getLocalName() name} of {@link Node} desired, or <code>null</code> if it
   * doesn't matter.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Node} being
   * {@linkplain Predicate#test(Object) tested} is non-<code>null</code> and has the specified
   * {@link Node#getLocalName() localName}.
   */
  public static final <N extends @Nullable Node> Predicate<N> testName(final @Nullable String localName) {
    return (node) -> {
      if (localName == null) return true;
      if (node == null) return false;
      final String name;
      synchronized (getDocument(node)) {
        name = node.getLocalName();
      }
      return (name != null) && name.equals(localName);
    };
  }

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} if an {@link Attr} has a specified
   * {@link Attr#getValue() value}.
   * 
   * @param value The {@linkplain Attr#getValue() value} of {@link Attr} desired, or <code>null</code> if it doesn't
   * matter.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Attr} being
   * {@linkplain Predicate#test(Object) tested} is non-<code>null</code> and has the specified {@link Attr#getValue()
   * value}.
   */
  public static final Predicate<@Nullable Attr> testAttrValue(final @Nullable String value) {
    return (attr) -> {
      if (value == null) return true;
      if (attr == null) return false;
      final String v;
      synchronized (getDocument(attr)) {
        v = attr.getValue();
      }
      return v.equals(value);
    };
  }

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} if the {@link Element}'s
   * {@linkplain #getAttrs(Node) attributes} {@linkplain Stream#filter(Predicate) contains} {@linkplain Stream#findAny()
   * any} {@linkplain Optional#isPresent() match} for the specified <code>attrTest</code>.
   * 
   * @param attrTest A {@link Predicate} to {@linkplain Predicate#test(Object) test} for the desired {@link Attr}, or
   * <code>null</code> if it doesn't matter.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Element} being
   * {@linkplain Predicate#test(Object) tested} has an {@link Attr} matching the <code>attrTest</code>.
   */
  public static final Predicate<@Nullable Element> testAttr(final @Nullable Predicate<@Nullable Attr> attrTest) {
    return (element) -> (attrTest != null) ? getAttrs(element).filter(attrTest).findAny().isPresent() : true;
  }

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} if an {@link Element} has an
   * {@linkplain #testAttr(Predicate) attribute} {@linkplain #testName(String) named} <code>id</code> with the specified
   * {@link #testAttrValue(String) id} value.
   * 
   * @param id The <code>id</code> {@linkplain #testAttrValue(String) value} of {@link Element} desired, or
   * <code>null</code> if it doesn't matter.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Element} being
   * {@linkplain Predicate#test(Object) tested} has an <code>id</code> {@link Attr} with the specified
   * {@linkplain #testAttrValue(String) value}.
   */
  public static final Predicate<@Nullable Element> testIDAttr(final @Nullable String id) {
    return testAttr((id != null) ? DOMUtil.<@Nullable Attr> testName("id").and(testAttrValue(id)) : null);
  }

  /**
   * A convenience method to {@linkplain Predicate#test(Object) test} if a {@link Node} is of a specified
   * {@link #testType(short) nodeType}, has a specified {@link #testNS(URI) nsURI}, and a specified
   * {@link #testName(String) localName}.
   * 
   * @param node The {@link Node} being {@linkplain Predicate#test(Object) tested}.
   * @param nodeType The {@linkplain #testType(short) type} of the desired Node, or <code>-1</code> if it doesn't
   * matter.
   * @param nsURI The {@linkplain #testNS(URI) namespace} of the desired Node, or <code>null</code> if it doesn't
   * matter.
   * @param localName The {@linkplain #testName(String) name} of the desired Node, or <code>null</code> if it doesn't
   * matter.
   * @return <code>true</code> if the supplied <code>node</code> matches the criteria.
   * @see #testType(short)
   * @see #testNS(URI)
   * @see #testName(String)
   * @see Predicate#test(Object)
   */
  public static final boolean testNode(final @Nullable Node node, final short nodeType, final @Nullable URI nsURI, final @Nullable String localName) {
    return testType(nodeType).and(testNS(nsURI)).and(testName(localName)).test(node);
  }

  /**
   * Get a {@link Stream} containing the {@linkplain Node#getChildNodes() children} of the supplied {@link Node node}.
   * 
   * @param node The {@link Node} whose {@linkplain Node#getChildNodes() children} are desired.
   * @return A {@link Stream} of {@link Node}'s containing the {@linkplain Node#getChildNodes() children} of the
   * supplied <code>node</code>.
   * @see #getAttrs(Node)
   */
  public static final Stream<? extends Node> getChildNodes(final @Nullable Node node) {
    return (node != null) ? StreamSupport.stream(new NodeListSpliterator(getDocument(node), node.getChildNodes()), false) : Collections.<@NonNull Node> emptyList().stream();
  }

  /**
   * {@linkplain Stream#filter(Predicate) Filter} the supplied {@link Stream} of {@link Node}'s for those whose
   * {@linkplain #testType(short) type} is {@link Node#ELEMENT_NODE}.
   * 
   * @param nodes The {@link Stream} of {@link Node}'s to {@linkplain Stream#filter(Predicate) filter}.
   * @return A {@linkplain Stream#filter(Predicate) filtered} {@link Stream} of {@link Element}'s.
   */
  public static final Stream<Element> filterElements(final Stream<? extends Node> nodes) {
    return nodes.filter(testType(Node.ELEMENT_NODE)).map((node) -> (Element)node);
  }

  /**
   * Get the {@linkplain #getChildNodes(Node) child} {@linkplain #filterElements(Stream) elements} of the supplied
   * <code>node</code>.
   * 
   * @param node The {@link Node} whose {@linkplain #getChildNodes(Node) children} should be retrieved.
   * @return A {@link Stream} of {@link Element}'s that are {@linkplain #getChildNodes(Node) children} of the supplied
   * <code>node</code>.
   */
  public static final Stream<Element> getChildElements(final @Nullable Node node) {
    return filterElements(getChildNodes(node));
  }

  /**
   * Get a {@link Stream} of {@link Element}'s which are {@linkplain #getChildElements(Node) children} of the supplied
   * <code>parent</code>, and match the given criteria.
   * 
   * @param parent The {@link Node} whose {@linkplain #getChildElements(Node) children} we want.
   * @param nsURI The {@linkplain #testNS(URI) namespace} URI of the desired Element's, or <code>null</code> if it
   * doesn't matter.
   * @param localName The {@linkplain #testName(String) name} of the desired Element's, or <code>null</code> if it
   * doesn't matter.
   * @return An {@link Stream} of {@link Element}'s which are {@linkplain #getChildElements(Node) children} of the
   * supplied <code>parent</code>, and match the given criteria.
   * @see #getChildElements(Node)
   * @see #testNS(URI)
   * @see #testName(String)
   */
  public static final Stream<Element> getChildElements(final @Nullable Node parent, final @Nullable URI nsURI, final @Nullable String localName) {
    return getChildElements(parent).filter(testNS(nsURI)).filter(testName(localName));
  }

  /**
   * Get the {@linkplain Stream#findFirst() first} {@link Element} which is a {@linkplain #getChildElements(Node) child}
   * of the supplied <code>parent</code>, and matches the given criteria.
   * 
   * @param parent The {@link Node} whose {@linkplain #getChildElements(Node) child} we want.
   * @param nsURI The {@linkplain #testNS(URI) namespace} URI of the desired Element, or <code>null</code> if it doesn't
   * matter.
   * @param localName The {@linkplain #testName(String) name} of the desired Element, or <code>null</code> if it doesn't
   * matter.
   * @return An {@link Element} which is a {@linkplain #getChildElements(Node) child} of the supplied
   * <code>parent</code>, and matches the given criteria, or else <code>null</code> if none exists.
   * @see #getChildElements(Node)
   * @see #testNS(URI)
   * @see #testName(String)
   */
  public static final Optional<Element> getChildElement(final @Nullable Node parent, final @Nullable URI nsURI, final @Nullable String localName) {
    return getChildElements(parent, nsURI, localName).findFirst();
  }

  /**
   * Get a {@link Stream} containing the {@linkplain Node#getAttributes() attributes} of the supplied {@link Node node}.
   * 
   * @param node The {@link Node} whose {@linkplain Node#getAttributes() attributes} are desired.
   * @return A {@link Stream} of {@link Attr}'s containing the {@linkplain Node#getAttributes() attributes} of the
   * supplied <code>node</code>.
   * @see #getChildNodes(Node)
   */
  public static final Stream<Attr> getAttrs(final @Nullable Node node) {
    if (node != null) {
      final NamedNodeMap attrs = node.getAttributes();
      if (attrs != null) return StreamSupport.stream(new AttrSpliterator(getDocument(node), attrs), false);
    }
    return Collections.<@NonNull Attr> emptyList().stream();
  }

  /**
   * If the <code>node</code> have an {@linkplain #getAttrs(Node) attribute} matching the <code>nsURI</code>,
   * <code>localName</code>, and <code>value</code> given, return it.
   * 
   * @param node The {@link Node} being tested for the desired Attr.
   * @param nsURI The {@linkplain #testNS(URI) namespace} URI of the desired Attr, or <code>null</code> if it doesn't
   * matter.
   * @param localName The {@linkplain #testName(String) name} of the desired Attr, or <code>null</code> if it doesn't
   * matter.
   * @return The {@link Attr} matching the criteria, or else <code>null</code> if none do.
   * @see #getAttrs(Node)
   * @see #testNS(URI)
   * @see #testName(String)
   */
  public static final Optional<Attr> getAttr(final @Nullable Node node, final @Nullable URI nsURI, final @Nullable String localName) {
    return getAttrs(node).filter(testNS(nsURI)).filter(testName(localName)).findAny();
  }

  /**
   * Create a {@link Text} Node from the supplied <code>text</code> and append it to the given <code>parent</code>.
   * 
   * @param parent The {@link Node} to {@linkplain Node#appendChild(Node) append} the <code>text</code> to.
   * @param text The String containing the text value to add.
   * @return The {@link Text} Node that was {@linkplain Document#createTextNode(String) created} from the given
   * <code>text</code>.
   * @throws DOMException If there was a problem {@linkplain Node#appendChild(Node) appending} the created Text to the
   * <code>parent</code>.
   * @see Document#createTextNode(String)
   * @see Node#appendChild(Node)
   */
  public static final Text appendText(final Node parent, final String text) throws DOMException {
    final Document document = getDocument(parent);
    synchronized (document) {
      final Text t = document.createTextNode(text);
      parent.appendChild(t);
      return t;
    }
  }

  /**
   * Create a {@link Comment} node from the supplied <code>data</code> and append it to the given <code>parent</code>.
   * 
   * @param parent The {@link Node} to add the <code>data</code> to.
   * @param data The String containing the value to add.
   * @return The {@link Comment} node that was created from the given <code>data</code>.
   * @throws DOMException If there was a problem {@linkplain Node#appendChild(Node) appending} the created Comment to
   * the <code>parent</code>.
   * @see Document#createComment(String)
   * @see Node#appendChild(Node)
   */
  public static final Comment appendComment(final Node parent, final String data) throws DOMException {
    final Document document = getDocument(parent);
    synchronized (document) {
      final Comment c = document.createComment(data);
      parent.appendChild(c);
      return c;
    }
  }

  /**
   * Create a {@link CDATASection} node from the supplied <code>data</code> and append it to the given
   * <code>parent</code>.
   * 
   * @param parent The {@link Node} to add the <code>data</code> to.
   * @param data The String containing the value to add.
   * @return The {@link CDATASection} node that was created from the given <code>data</code>.
   * @throws DOMException If there was a problem {@linkplain Document#createCDATASection(String) creating} the
   * CDATASection or {@linkplain Node#appendChild(Node) appending} it to the <code>parent</code>.
   * @see Document#createCDATASection(String)
   * @see Node#appendChild(Node)
   */
  public static final CDATASection appendCDATA(final Node parent, final String data) throws DOMException {
    final Document document = getDocument(parent);
    synchronized (document) {
      final CDATASection c = document.createCDATASection(data);
      parent.appendChild(c);
      return c;
    }
  }

  /**
   * Examine the supplied <code>node</code>, and optionally it's {@linkplain Node#getParentNode() parents}, looking for
   * a declaration of the given <code>namespace</code>.
   * 
   * @param node The {@link Node} to examine for the <code>namespace</code> declaration.
   * @param nsURI The {@link URI} of the <a href="http://www.w3.org/TR/xml-names/">namespace</a> whose declaration we
   * are looking for.
   * @param inclDefaultDeclarations <code>true</code> if a default namespace declaration should be returned,
   * <code>false</code> if the returned declaration must specify a prefix.
   * @param recurseParents If the <code>namespace</code> declaration isn't found on the given <code>node</code>, should
   * it's {@linkplain Node#getParentNode() parents} also be checked?
   * @return An {@link Attr} containing a declaration for the given <code>namespace</code> argument.
   * @see Node#getAttributes()
   * @see Attr#getNamespaceURI()
   * @see XMLConstants#XMLNS_ATTRIBUTE
   * @see Attr#getValue()
   */
  public static final Optional<Attr> getNSDeclAttr(final @Nullable Node node, final URI nsURI, final boolean inclDefaultDeclarations, final boolean recurseParents) {
    final Optional<Attr> declAttr = getAttrs(node).filter(testNS(XMLUtil.XMLNS_ATTRIBUTE_NS_URI)).filter(testAttrValue(nsURI.toString())).filter((attr) -> inclDefaultDeclarations || (!XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getLocalName()))).findAny();
    return declAttr.isPresent() ? declAttr : (((recurseParents) && (node != null)) ? getNSDeclAttr(node.getParentNode(), nsURI, inclDefaultDeclarations, recurseParents) : Optional.empty());
  }

  /**
   * Create a <code>namespace</code> declaration {@link Attr} on the supplied <code>element</code>.
   * 
   * @param element The {@link Element} upon which to {@linkplain Document#createAttributeNS(String, String) create} the
   * given <code>namespace</code> declaration.
   * @param nsURI The {@link URI} of the <a href="http://www.w3.org/TR/xml-names/">namespace</a> to declare.
   * @param nsPrefix Declare the <code>namespace</code> using this value as the prefix, if <code>null</code> a default
   * namespace declaration will be created.
   * @return An {@link Attr} containing a declaration for the given <code>namespace</code> argument.
   * @throws DOMException If there was a problem {@linkplain Document#createAttributeNS(String, String) creating} the
   * Attr, {@linkplain #appendText(Node, String) appending} the namespace text, or
   * {@linkplain Element#setAttributeNodeNS(Attr) setting} it on the <code>element</code>.
   * @see Document#createAttributeNS(String, String)
   * @see Element#setAttributeNodeNS(Attr)
   * @see XMLConstants#XMLNS_ATTRIBUTE
   */
  public static final Attr setNSDeclAttr(final Element element, final URI nsURI, final @Nullable String nsPrefix) throws DOMException {
    final Document document = getDocument(element);
    synchronized (document) {
      final Attr declAttr;
      if ((nsPrefix != null) && (!nsPrefix.isEmpty())) {
        declAttr = document.createAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ':' + nsPrefix);
      } else {
        declAttr = document.createAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE);
      }
      appendText(declAttr, nsURI.toString());
      element.setAttributeNodeNS(declAttr);
      return declAttr;
    }
  }

  /**
   * Get the location URI for the given <code>namespace</code>.
   * 
   * @param schemaLocation The {@link XMLUtil#W3C_XML_SCHEMA_INSTANCE_NS_URI XSI} <code>schemaLocation</code>
   * {@link Attr} to retrieve the location from.
   * @param nsURI The {@link URI} of the <a href="http://www.w3.org/TR/xml-names/">namespace</a> of the desired
   * location.
   * @return The {@link URI} the <code>schemaLocation</code> Attr specifies for the given <code>namespace</code>.
   * @throws DOMException If there was a problem {@linkplain Node#getTextContent() retrieving} the text of the
   * <code>schemaLocation</code>.
   * @throws URISyntaxException If there was a problem {@linkplain URI#URI(String) constructing} the URI for the given
   * <code>namespace</code> location.
   */
  public static final Optional<URI> getSchemaLocation(final @Nullable Attr schemaLocation, final URI nsURI) throws DOMException, URISyntaxException {
    final String schemaLocationString = (schemaLocation != null) ? schemaLocation.getTextContent() : null;
    if (schemaLocationString == null) return Optional.empty();
    final String[] nsLocations = schemaLocationString.split(" ");
    for (int i = 0; i < nsLocations.length; i += 2) {
      if (nsURI.toString().equals(nsLocations[i])) {
        return (i < nsLocations.length - 1) ? Optional.of(new URI(nsLocations[i + 1])) : Optional.empty();
      }
    }
    return Optional.empty();
  }

  /**
   * Set a <code>schemaLocation</code> Attr on the given <code>element</code> declaring the supplied
   * <code>schemaLocation</code> for the specified <code>namespace</code>.
   * 
   * @param element The {@link Element} to set the schemaLocation Attr on.
   * @param nsURI The {@link URI} of the <a href="http://www.w3.org/TR/xml-names/">namespace</a> for the specified
   * <code>schemaLocation</code>.
   * @param schemaLocation The {@link URI} where the schema for the given <code>namespace</code> can be found.
   * @return The {@link XMLUtil#W3C_XML_SCHEMA_INSTANCE_NS_URI XSI} <code>schemaLocation</code> {@link Attr} containing
   * the specified <code>namespace</code> <code>schemaLocation</code>.
   * @throws DOMException If there was a problem {@linkplain #setNSDeclAttr(Element, URI, String) delcaring} the
   * {@link XMLUtil#W3C_XML_SCHEMA_INSTANCE_NS_URI XSI} namespace, {@linkplain #getSchemaLocation(Attr, URI) retrieving}
   * an existing schema location, {@linkplain Document#createAttributeNS(String, String) creating} the
   * <code>schemaLocation</code> Attr, {@linkplain Element#setAttributeNodeNS(Attr) setting} it on the
   * <code>element</code>, {@linkplain #removeChildren(Node) removing} existing locations or
   * {@linkplain #appendText(Node, String) appending} the new locations.
   * @throws URISyntaxException If there was a problem {@linkplain #getSchemaLocation(Attr, URI) retrieving} an existing
   * location.
   * @see #getSchemaLocation(Attr, URI)
   * @see XMLUtil#W3C_XML_SCHEMA_INSTANCE_NS_URI
   */
  public static final Attr setSchemaLocation(final Element element, final URI nsURI, final URI schemaLocation) throws DOMException, URISyntaxException {
    final Document document = getDocument(element);
    synchronized (document) {
      final Attr existingSchemaLocationAttr = element.getAttributeNodeNS(XMLUtil.W3C_XML_SCHEMA_INSTANCE_NS_URI.toString(), "schemaLocation");
      if (existingSchemaLocationAttr == null) {
        Optional<Attr> xsiNSDecl = getNSDeclAttr(element, XMLUtil.W3C_XML_SCHEMA_INSTANCE_NS_URI, false, true);
        if (!xsiNSDecl.isPresent()) xsiNSDecl = Optional.of(setNSDeclAttr(element, XMLUtil.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLUtil.W3C_XML_SCHEMA_INSTANCE_NS_PREFIX));
        final Attr newSchemaLocationAttr = document.createAttributeNS(XMLUtil.W3C_XML_SCHEMA_INSTANCE_NS_URI.toString(), xsiNSDecl.get().getLocalName() + ":schemaLocation");
        appendText(newSchemaLocationAttr, nsURI.toString() + ' ' + schemaLocation.toString());
        element.setAttributeNodeNS(newSchemaLocationAttr);
        return newSchemaLocationAttr;
      }
      final Optional<URI> existingLocation = getSchemaLocation(existingSchemaLocationAttr, nsURI);
      if (existingLocation.isPresent()) return existingSchemaLocationAttr;
      final String existingSchemaLocations = existingSchemaLocationAttr.getTextContent();
      removeChildren(existingSchemaLocationAttr);
      appendText(existingSchemaLocationAttr, existingSchemaLocations + ' ' + nsURI.toString() + ' ' + schemaLocation.toString());
      return existingSchemaLocationAttr;
    }
  }

  /**
   * <p>
   * {@linkplain Document#createAttributeNS(String, String) Create} an {@link Attr} with the given <code>nsURI</code>,
   * <code>nsPrefix</code>, and <code>localName</code>, taking care of namespace declarations, and set it on to the
   * <code>parent</code> Element.
   * </p>
   * 
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI to use for the created Attr.
   * @param nsPrefix The namespace prefix to use for the created Attr.
   * @param localName The {@linkplain Attr#getLocalName() local name} to use for the created Attr.
   * @param value If not <code>null</code>, this value will be {@linkplain Attr#setValue(String) set} on the Attr.
   * @param parent The {@link Element} which will serve as {@linkplain Attr#getOwnerElement() owner} to the created
   * Attr.
   * @return The {@link Attr} just {@linkplain Document#createAttributeNS(String, String) created}.
   * @throws IllegalArgumentException If the given <code>nsURI</code> needs to be declared and the <code>nsPrefix</code>
   * is <code>null</code>.
   * @throws DOMException If there was a problem {@linkplain Document#createAttributeNS(String, String) creating} the
   * Attr, {@linkplain #setNSDeclAttr(Element, URI, String) declaring} it's <code>nsURI</code>, or
   * {@linkplain Element#setAttributeNodeNS(Attr) setting} it on the <code>parent</code>.
   * @see #createAttr(URI, String, String, String, Element, Document, boolean, boolean, URI)
   */
  public static final Attr createAttr(final @Nullable URI nsURI, final @Nullable String nsPrefix, final String localName, final @Nullable String value, final Element parent) throws IllegalArgumentException, DOMException {
    return createAttr(nsURI, nsPrefix, localName, value, parent, getDocument(parent), false, true);
  }

  /**
   * <p>
   * {@linkplain Document#createAttributeNS(String, String) Create} an {@link Attr} with the given <code>nsURI</code>,
   * <code>nsPrefix</code>, and <code>localName</code>, taking care of namespace declarations, and (optionally) set it
   * on to the <code>parent</code> Element.
   * </p>
   * 
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI to use for the created Attr.
   * @param nsPrefix The namespace prefix to use for the created Attr.
   * @param localName The {@linkplain Attr#getLocalName() local name} to use for the created Attr.
   * @param value If not <code>null</code>, this value will be {@linkplain Attr#setValue(String) set} on the Attr.
   * @param parent The {@link Element} which will serve as {@linkplain Attr#getOwnerElement() owner} to the created
   * Attr.
   * @param document The {@link Document} which will be used to {@linkplain Document#createAttributeNS(String, String)
   * create} the new Element.
   * @param declareNamespaceOnDocumentElement If the <code>namespace</code> of the created Attr needs to be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}, should it be declared on the <code>parent</code>
   * Element, or on the {@linkplain Document#getDocumentElement() document Element}?
   * @param setOnParent Should the newly created Attr be {@linkplain Element#setAttributeNodeNS(Attr) set} on the
   * supplied <code>parent</code>?
   * @return The {@link Attr} just {@linkplain Document#createAttributeNS(String, String) created}.
   * @throws IllegalArgumentException If the given <code>nsURI</code> needs to be declared and the <code>nsPrefix</code>
   * is <code>null</code>.
   * @throws DOMException If there was a problem {@linkplain Document#createAttributeNS(String, String) creating} the
   * Attr, {@linkplain #setNSDeclAttr(Element, URI, String) declaring} it's <code>nsURI</code>, or
   * {@linkplain Element#setAttributeNodeNS(Attr) setting} it on the <code>parent</code>.
   * @see #createAttr(URI, String, String, String, Element, Document, boolean, boolean, URI)
   */
  public static final Attr createAttr(final @Nullable URI nsURI, final @Nullable String nsPrefix, final String localName, final @Nullable String value, final @Nullable Element parent, final Document document, final boolean declareNamespaceOnDocumentElement, final boolean setOnParent) throws IllegalArgumentException, DOMException {
    try {
      return createAttr(nsURI, nsPrefix, localName, value, parent, document, declareNamespaceOnDocumentElement, setOnParent, null);
    } catch (URISyntaxException urise) { // this should only occurr when supplying a schemaLocation
      throw new RuntimeException(urise.getClass().getName() + ":" + urise.getMessage(), urise);
    }
  }

  /**
   * <p>
   * {@linkplain Document#createAttributeNS(String, String) Create} an {@link Attr} with the given <code>nsURI</code>,
   * <code>nsPrefix</code>, and <code>localName</code>, taking care of namespace declarations, and (optionally) set it
   * on to the <code>parent</code> Element.
   * </p>
   * 
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI to use for the created Attr.
   * @param nsPrefix The namespace prefix to use for the created Attr. If <code>null</code>, and a namespace declaration
   * is required, a default namespace will be then be declared.
   * @param localName The {@linkplain Attr#getLocalName() local name} to use for the created Attr.
   * @param value If not <code>null</code>, this value will be {@linkplain Attr#setValue(String) set} on the Attr.
   * @param parent The {@link Element} which will serve as {@linkplain Attr#getOwnerElement() owner} to the created
   * Attr. This is also used for namespace {@linkplain #getNSDeclAttr(Node, URI, boolean, boolean) lookup} if needed.
   * @param document The {@link Document} which will be used to {@linkplain Document#createAttributeNS(String, String)
   * create} the new Element.
   * @param declareNamespaceOnDocumentElement If the <code>namespace</code> of the created Attr needs to be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}, should it be declared on the <code>parent</code>
   * Element, or on the {@linkplain Document#getDocumentElement() document Element}?
   * @param setOnParent Should the newly created Attr be {@linkplain Element#setAttributeNodeNS(Attr) set} on the
   * supplied <code>parent</code>?
   * @param schemaLocation If not <code>null</code>, and the <code>namespace</code> of the created Attr needs to be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}, this location will be
   * {@linkplain #setSchemaLocation(Element, URI, URI) set} on the same Element as the declaration.
   * @return The {@link Attr} just {@linkplain Document#createAttributeNS(String, String) created}.
   * @throws IllegalArgumentException If the given <code>nsURI</code> needs to be declared and the <code>nsPrefix</code>
   * is <code>null</code>.
   * @throws DOMException If there was a problem {@linkplain Document#createAttributeNS(String, String) creating} the
   * Attr, {@linkplain #setNSDeclAttr(Element, URI, String) declaring} it's <code>nsURI</code>, or
   * {@linkplain Element#setAttributeNodeNS(Attr) setting} it on the <code>parent</code>.
   * @throws URISyntaxException If there was a problem {@linkplain #setSchemaLocation(Element, URI, URI) setting} the
   * <code>schemaLocation</code>.
   * @see Document#createAttributeNS(String, String)
   * @see #getNSDeclAttr(Node, URI, boolean, boolean)
   * @see #setNSDeclAttr(Element, URI, String)
   * @see #setSchemaLocation(Element, URI, URI)
   */
  public static final Attr createAttr(final @Nullable URI nsURI, final @Nullable String nsPrefix, final String localName, final @Nullable String value, final @Nullable Element parent, final Document document, final boolean declareNamespaceOnDocumentElement, final boolean setOnParent, final @Nullable URI schemaLocation) throws IllegalArgumentException, DOMException, URISyntaxException {
    final String effectiveNSURI = (nsURI != null) ? nsURI.toString() : XMLConstants.NULL_NS_URI;

    final Attr attr;
    synchronized (document) {

      if ((nsURI == null) || (XMLUtil.XML_NS_URI.equals(nsURI)) || (XMLUtil.XMLNS_ATTRIBUTE_NS_URI.equals(nsURI))) { // Don't need to worry about NS decl.

        attr = document.createAttributeNS(effectiveNSURI, ((nsPrefix != null) && (!nsPrefix.isEmpty())) ? (nsPrefix + ':' + localName) : localName);

      } else {

        final Optional<Attr> existingNSPrefixDecl = getNSDeclAttr(parent, nsURI, false, true); // Did some parent node declare this NS prefix already?
        if (existingNSPrefixDecl.isPresent()) { // Yep, use that declaration.

          attr = document.createAttributeNS(effectiveNSURI, existingNSPrefixDecl.get().getLocalName() + ':' + localName);

        } else { // Nope, we need to declare an NS prefix then.

          if ((nsPrefix == null) || (nsPrefix.isEmpty())) throw new IllegalArgumentException("null nsPrefix"); // Unlike Element's, Attr's can't be declared using a default XMLNS decl, thus a prefix must be specified.

          attr = document.createAttributeNS(effectiveNSURI, nsPrefix + ':' + localName);

          final Element declarationElement = ((declareNamespaceOnDocumentElement) && (document.getDocumentElement() != null)) ? document.getDocumentElement() : parent;
          if (declarationElement != null) {
            setNSDeclAttr(declarationElement, nsURI, nsPrefix);
            if (schemaLocation != null) setSchemaLocation(declarationElement, nsURI, schemaLocation);
          }

        }

      }

      if (value != null) attr.setValue(value);
      if ((setOnParent) && (parent != null)) parent.setAttributeNodeNS(attr);

    }
    return attr;
  }

  /**
   * {@linkplain Document#createElementNS(String, String) Create} an {@link Element} with the given <code>nsURI</code>,
   * <code>nsPrefix</code>, and <code>localName</code>, taking care of namespace declarations, and append it to the
   * <code>parent</code> Node.
   * 
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> {@link URI} to use for the created
   * Element.
   * @param nsPrefix The namespace prefix to use for the created Element.
   * @param localName The {@linkplain Element#getLocalName() local name} to use for the created Element.
   * @param parent The {@link Node} which will serve as {@linkplain Node#getParentNode() parent} to the created Element.
   * @return The {@link Element} just {@linkplain Document#createElementNS(String, String) created}.
   * @throws DOMException If there was a problem {@linkplain Document#createElementNS(String, String) creating} the
   * child Element, {@linkplain #setNSDeclAttr(Element, URI, String) declaring} it's <code>namespace</code>, or
   * {@linkplain Node#appendChild(Node) appending} it to the <code>parent</code>.
   * @see #createElement(URI, String, String, Node, Document, boolean, boolean, URI)
   */
  public static final Element createElement(final @Nullable URI nsURI, final @Nullable String nsPrefix, final String localName, final Node parent) throws DOMException {
    return createElement(nsURI, nsPrefix, localName, parent, getDocument(parent), false, true);
  }

  /**
   * {@linkplain Document#createElementNS(String, String) Create} an {@link Element} with the given <code>nsURI</code>,
   * <code>nsPrefix</code>, and <code>localName</code>, taking care of namespace declarations, and (optionally) append
   * it to the <code>parent</code> Node.
   * 
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> {@link URI} to use for the created
   * Element.
   * @param nsPrefix The namespace prefix to use for the created Element.
   * @param localName The {@linkplain Element#getLocalName() local name} to use for the created Element.
   * @param parent The {@link Node} which will serve as {@linkplain Node#getParentNode() parent} to the created Element.
   * @param document The {@link Document} which will be used to {@linkplain Document#createElementNS(String, String)
   * create} the new Element.
   * @param declareNamespaceOnDocumentElement If the <code>nsURI</code> of the created Element needs to be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}, should it be declared on the new Element itself, or on
   * the {@linkplain Document#getDocumentElement() document Element}?
   * @param appendToParent Should the newly created Element be {@linkplain Node#appendChild(Node) appended} to the
   * supplied <code>parent</code>?
   * @return The {@link Element} just {@linkplain Document#createElementNS(String, String) created}.
   * @throws DOMException If there was a problem {@linkplain Document#createElementNS(String, String) creating} the
   * child Element, {@linkplain #setNSDeclAttr(Element, URI, String) declaring} it's <code>nsURI</code>, or
   * {@linkplain Node#appendChild(Node) appending} it to the <code>parent</code>.
   * @see #createElement(URI, String, String, Node, Document, boolean, boolean, URI)
   */
  public static final Element createElement(final @Nullable URI nsURI, final @Nullable String nsPrefix, final String localName, final @Nullable Node parent, final Document document, final boolean declareNamespaceOnDocumentElement, final boolean appendToParent) throws DOMException {
    try {
      return createElement(nsURI, nsPrefix, localName, parent, document, declareNamespaceOnDocumentElement, appendToParent, null);
    } catch (URISyntaxException urise) { // this should only occurr when supplying a schemaLocation
      throw new RuntimeException(urise.getClass().getName() + ":" + urise.getMessage(), urise);
    }
  }

  /**
   * <p>
   * {@linkplain Document#createElementNS(String, String) Create} an {@link Element} with the given <code>nsURI</code>,
   * <code>nsPrefix</code>, and <code>localName</code>, taking care of namespace declarations, and (optionally) append
   * it to the <code>parent</code> Node.
   * </p>
   * 
   * <p>
   * Namespace Handling: If a <code>parent</code> Node is supplied in the same <code>nsURI</code>, this method will use
   * that <code>parent</code>'s namespace prefix if it has one, else assume a default has been declared. If a
   * <code>parent</code> Node is supplied, but isn't in the requested <code>nsURI</code>, this method will use that
   * <code>parent</code> to perform a {@linkplain #getNSDeclAttr(Node, URI, boolean, boolean) lookup} of any existing
   * prefix declared for the requested <code>nsURI</code>. If the <code>parent</code> Node is <code>null</code>, or
   * there is no existing prefix declaration, the requested <code>nsURI</code> will then be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}. The new declaration will use the supplied
   * <code>nsPrefix</code> if present, else create a default <code>nsURI</code> declaration.
   * </p>
   * 
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI to use for the created Element.
   * @param nsPrefix The namespace prefix to use for the created Element. This will only be used if the
   * <code>parent</code> is <code>null</code> or an existing prefix is not already in scope for the requested
   * <code>namespace</code>. If <code>null</code>, and a namespace {@linkplain #setNSDeclAttr(Element, URI, String)
   * declaration} is required, a default namespace will be then be declared.
   * @param localName The {@linkplain Element#getLocalName() local name} to use for the created Element.
   * @param parent The {@link Node} which will serve as {@linkplain Node#getParentNode() parent} to the created Element.
   * This is also used for namespace {@linkplain #getNSDeclAttr(Node, URI, boolean, boolean) lookup} if needed.
   * @param document The {@link Document} which will be used to {@linkplain Document#createElementNS(String, String)
   * create} the new Element.
   * @param declareNamespaceOnDocumentElement If the <code>nsURI</code> of the created Element needs to be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}, should it be declared on the new Element itself, or on
   * the {@linkplain Document#getDocumentElement() document Element}?
   * @param appendToParent Should the newly created Element be {@linkplain Node#appendChild(Node) appended} to the
   * supplied <code>parent</code>?
   * @param schemaLocation If not <code>null</code>, and the <code>nsURI</code> of the created Element needs to be
   * {@linkplain #setNSDeclAttr(Element, URI, String) declared}, this location will be
   * {@linkplain #setSchemaLocation(Element, URI, URI) set} on the same Element as the declaration.
   * @return The {@link Element} just {@linkplain Document#createElementNS(String, String) created}.
   * @throws DOMException If there was a problem {@linkplain Document#createElementNS(String, String) creating} the
   * child Element, {@linkplain #setNSDeclAttr(Element, URI, String) declaring} it's <code>nsURI</code>, or
   * {@linkplain Node#appendChild(Node) appending} it to the <code>parent</code>.
   * @throws URISyntaxException If there was a problem {@linkplain #setSchemaLocation(Element, URI, URI) setting} the
   * <code>schemaLocation</code>.
   * @see Document#createElementNS(String, String)
   * @see #getNSDeclAttr(Node, URI, boolean, boolean)
   * @see #setNSDeclAttr(Element, URI, String)
   * @see #setSchemaLocation(Element, URI, URI)
   */
  public static final Element createElement(final @Nullable URI nsURI, final @Nullable String nsPrefix, final String localName, final @Nullable Node parent, final Document document, final boolean declareNamespaceOnDocumentElement, final boolean appendToParent, final @Nullable URI schemaLocation) throws DOMException, URISyntaxException {

    final String effectiveNSURI = (nsURI != null) ? nsURI.toString() : XMLConstants.NULL_NS_URI;

    final Element element;
    synchronized (document) {

      if ((nsURI == null) || ((parent != null) && (effectiveNSURI.equals(parent.getNamespaceURI())))) { // No namespace, or already in that namespace, yay.

        if ((parent != null) && (parent.getPrefix() != null)) {
          element = document.createElementNS(effectiveNSURI, parent.getPrefix() + ':' + localName);
        } else {
          element = document.createElementNS(effectiveNSURI, localName);
        }

      } else { // Uh oh, not in that namespace...

        final Optional<Attr> existingNSPrefixDecl = getNSDeclAttr(parent, nsURI, false, true); // Did some parent node declare this prefix already?
        if (existingNSPrefixDecl.isPresent()) { // yep, use that declaration

          element = document.createElementNS(effectiveNSURI, existingNSPrefixDecl.get().getLocalName() + ':' + localName);

        } else { // nope, declare a namespace then

          element = document.createElementNS(effectiveNSURI, (nsPrefix != null) ? (nsPrefix + ':' + localName) : localName);

          final Element documentElement = document.getDocumentElement();
          final Element declarationElement = ((declareNamespaceOnDocumentElement) && (documentElement != null) && (nsPrefix != null)) ? documentElement : element;
          setNSDeclAttr(declarationElement, nsURI, nsPrefix);
          if (schemaLocation != null) setSchemaLocation(declarationElement, nsURI, schemaLocation);

        }

      }

      if ((appendToParent) && (parent != null)) parent.appendChild(element);

    }
    return element;
  }

  /**
   * {@linkplain Node#removeChild(Node) Remove} all {@linkplain Node#getChildNodes() child nodes} from the supplied
   * <code>parent</code>.
   * 
   * @param parent The {@link Node} whose children should be {@linkplain Node#removeChild(Node) removed}.
   * @throws DOMException If there was a problem {@linkplain Node#removeChild(Node) removing} one of the children.
   * @see Node#getChildNodes()
   * @see Node#removeChild(Node)
   */
  public static final void removeChildren(final Node parent) throws DOMException {
    synchronized (getDocument(parent)) {
      Node child = null;
      while ((child = parent.getLastChild()) != null) {
        parent.removeChild(child);
      }
    }
    return;
  }

  /**
   * Remove any unnecessary namespace declarations from the given <code>newChild</code> {@link Node}. This method will
   * check to see if <code>newChild</code> it's parent are both not {@linkplain Node#getPrefix() prefixed} and in the
   * same namespace, and if they are, and <code>newChild</code> declares that namespace as a default, that declaration
   * will be removed.
   * 
   * @param newChild The {@link Node} to check for unnecessary declarations.
   * @throws DOMException If there was a problem {@linkplain Element#getAttributeNodeNS(String, String) retrieving} or
   * {@linkplain NamedNodeMap#removeNamedItemNS(String, String) removing} a namespace declaration.
   */
  public static final void removeUnnecessaryNSDecl(final Node newChild) throws DOMException {
    synchronized (getDocument(newChild)) {
      if (newChild.getPrefix() != null) return;
      final Node parent = newChild.getParentNode();
      if ((parent == null) || (parent.getPrefix() != null) || (!Objects.equals(newChild.getNamespaceURI(), parent.getNamespaceURI()))) return;
      final Optional<Attr> defaultNSDeclAttr = getAttrs(newChild).filter(testNS(XMLUtil.XMLNS_ATTRIBUTE_NS_URI)).filter(testName(XMLConstants.XMLNS_ATTRIBUTE)).filter(testAttrValue(newChild.getNamespaceURI())).findAny();
      if (!defaultNSDeclAttr.isPresent()) return;
      Objects.requireNonNull(newChild.getAttributes()).removeNamedItemNS(XMLUtil.XMLNS_ATTRIBUTE_NS_URI.toString(), XMLConstants.XMLNS_ATTRIBUTE);
    }
    return;
  }

  /**
   * {@linkplain Node#appendChild(Node) Appends} copies of all the {@linkplain Node#getChildNodes() child nodes} from
   * <code>sourceParent</code> to <code>destParent</code>, {@linkplain Node#cloneNode(boolean) cloning} or
   * {@linkplain Document#importNode(Node, boolean) importing} them as required, as well as
   * {@linkplain #removeUnnecessaryNSDecl(Node) removing} any unnecessary namespace declarations..
   * 
   * @param sourceParent The {@link Node} whose {@linkplain Node#getChildNodes() children} should be copied and
   * {@linkplain Node#appendChild(Node) appended} to <code>destParent</code>.
   * @param destParent The {@link Node} which will have the {@linkplain Node#getChildNodes() children} of
   * <code>sourceParent</code> copied and {@linkplain Node#appendChild(Node) appended} to it.
   * @throws DOMException If there was a problem {@linkplain Document#importNode(Node, boolean) importing} or
   * {@linkplain Node#appendChild(Node) appending} one of the children to <code>destParent</code>.
   * @see Node#getChildNodes()
   * @see Node#appendChild(Node)
   * @see Document#importNode(Node, boolean)
   * @see Node#cloneNode(boolean)
   * @see #removeUnnecessaryNSDecl(Node)
   */
  public static final void copyChildren(final Node sourceParent, final Node destParent) throws DOMException {
    final Document sourceDocument = getDocument(sourceParent);
    final Document destDocument = getDocument(destParent);
    final Document[] documents = new Document[] { sourceDocument, destDocument };
    Arrays.sort(documents, CollUtil.mkHashCodeComparator()); // avoid deadlock
    final boolean needToImport = sourceDocument != destDocument;
    synchronized (documents[0]) {
      synchronized (documents[1]) {
        final NodeList sourceNodeList = sourceParent.getChildNodes();
        for (int i = 0; i < sourceNodeList.getLength(); i++) {
          final Node sourceNode = Objects.requireNonNull(sourceNodeList.item(i));
          final Node destNode = (needToImport) ? destDocument.importNode(sourceNode, true) : sourceNode.cloneNode(true);
          destParent.appendChild(destNode);
          removeUnnecessaryNSDecl(destNode);
        }
      }
    }
    return;
  }

  /**
   * {@linkplain Node#appendChild(Node) Appends} all the {@linkplain Node#getChildNodes() child nodes} from
   * <code>sourceParent</code> to <code>destParent</code>, {@linkplain Document#adoptNode(Node) adopting} them as
   * required, as well as {@linkplain #removeUnnecessaryNSDecl(Node) removing} any unnecessary namespace declarations..
   * 
   * @param sourceParent The {@link Node} whose {@linkplain Node#getChildNodes() children} should be
   * {@linkplain Node#appendChild(Node) appended} to <code>destParent</code>.
   * @param destParent The {@link Node} which will have the {@linkplain Node#getChildNodes() children} of
   * <code>sourceParent</code> {@linkplain Node#appendChild(Node) appended} to it.
   * @throws DOMException If there was a problem {@linkplain Document#adoptNode(Node) adopting} or
   * {@linkplain Node#appendChild(Node) appending} one of the children to <code>destParent</code>.
   * @see Node#getChildNodes()
   * @see Node#appendChild(Node)
   * @see Document#adoptNode(Node)
   * @see #removeUnnecessaryNSDecl(Node)
   */
  public static final void moveChildren(final Node sourceParent, final Node destParent) throws DOMException {
    final Document sourceDocument = getDocument(sourceParent);
    final Document destDocument = getDocument(destParent);
    final Document[] documents = new Document[] { sourceDocument, destDocument };
    Arrays.sort(documents, CollUtil.mkHashCodeComparator()); // avoid deadlock
    final boolean needToAdopt = sourceDocument != destDocument;
    synchronized (documents[0]) {
      synchronized (documents[1]) {
        Node sourceNode = null;
        while ((sourceNode = sourceParent.getFirstChild()) != null) {
          final Node destNode = (needToAdopt) ? Objects.requireNonNull(destDocument.adoptNode(sourceNode), "Failed to adopt child node") : sourceNode;
          destParent.appendChild(destNode);
          removeUnnecessaryNSDecl(destNode);
        }
      }
    }
    return;
  }

  /**
   * {@linkplain Node#appendChild(Node) Append} the <code>newChild</code> to the supplied <code>destParent</code> Node,
   * {@linkplain Document#importNode(Node, boolean) importing} it if required, as well as
   * {@linkplain #removeUnnecessaryNSDecl(Node) removing} any unnecessary namespace declarations.
   * 
   * @param destParent The parent Node the <code>newChild</code> is to be {@linkplain Node#appendChild(Node) appended}
   * to.
   * @param newChild The Node to be appended. If the Node is a Document, it's {@linkplain Document#getDocumentElement()
   * document element} will be appended instead.
   * @throws DOMException If there was a problem {@linkplain Document#importNode(Node, boolean) importing},
   * {@linkplain Node#appendChild(Node) appending} the result Node, or {@linkplain #removeUnnecessaryNSDecl(Node)
   * removing} a namespace declaration.
   * @see Document#importNode(Node, boolean)
   * @see Node#cloneNode(boolean)
   * @see Node#appendChild(Node)
   * @see #removeUnnecessaryNSDecl(Node)
   */
  public static final void appendChild(final Node destParent, final Node newChild) throws DOMException {
    final Node resolvedNewChild = (newChild instanceof Document) ? ((Document)newChild).getDocumentElement() : newChild;
    if (resolvedNewChild == null) return;

    final Document parentDocument = getDocument(destParent);
    final Document resultDocument = getDocument(resolvedNewChild);
    final Document[] documents = new Document[] { parentDocument, resultDocument };
    Arrays.sort(documents, CollUtil.mkHashCodeComparator()); // avoid deadlock
    final boolean needToImport = parentDocument != resultDocument;
    synchronized (documents[0]) {
      synchronized (documents[1]) {
        final Node destNode = (needToImport) ? parentDocument.importNode(resolvedNewChild, true) : resolvedNewChild.cloneNode(true);
        destParent.appendChild(destNode);
        removeUnnecessaryNSDecl(destNode);

      }
    }
    return;
  }

  /**
   * Set the <code>xml:lang</code> Attr on the given <code>element</code> to the value of the given <code>locale</code>.
   * 
   * @param element The {@link Element} on which to declare the language.
   * @param locale The {@link Locale} of the language to declare, if <code>null</code> an empty attribute will be
   * declared.
   * @throws DOMException If there was a problem {@linkplain #createAttr(URI, String, String, String, Element) setting}
   * the attribute.
   * @see #createAttr(URI, String, String, String, Element)
   */
  public static final void setXMLLangAttr(final Element element, final @Nullable Locale locale) throws DOMException {
    createAttr(XMLUtil.XML_NS_URI, XMLConstants.XML_NS_PREFIX, "lang", ((locale != null) && (!Locale.ROOT.equals(locale))) ? locale.toLanguageTag() : "", element);
    return;
  }

  /**
   * Retrieve the value of the <code>xml:lang</code> Attr from the supplied <code>element</code>.
   * 
   * @param element The {@link Element} from which to retrieve the <code>xml:lang</code> value.
   * @param defaultLocale The {@link Locale} to return should the <code>element</code> not specify a language.
   * @return The value of the <code>xml:lang</code> attribute, parsed as a {@link Locale}.
   * @throws DOMException If there was a problem {@linkplain Element#getAttributeNodeNS(String, String) retreiving} the
   * lang attribute.
   */
  public static final Locale getXMLLangAttr(final @Nullable Element element, final Locale defaultLocale) throws DOMException {
    final Optional<Attr> xmlLangAttr = getAttrs(element).filter(testNS(XMLUtil.XML_NS_URI)).filter(testName("lang")).findAny();
    return (xmlLangAttr.isPresent()) ? Locale.forLanguageTag(Objects.requireNonNull(xmlLangAttr.get().getValue())) : defaultLocale;
  }

  /**
   * A {@link Spliterator}&lt;{@link Node}&gt; implementation wrapping a {@link NodeList}.
   */
  private static final class NodeListSpliterator implements Spliterator<Node> {
    /**
     * The {@link Document} to synchronize on.
     */
    private final Document document;
    /**
     * The wrapped {@link NodeList}.
     */
    private final NodeList nodeList;
    /**
     * The maximum {@link #nodeList} {@linkplain #item(Document, NodeList, int) item} {@link #index} exposed by this
     * wrapper.
     */
    private final int limit;
    /**
     * @see #characteristics()
     */
    private final int characteristics;
    /**
     * The next {@linkplain #item(Document, NodeList, int) item} to be {@link #tryAdvance(Consumer) processed}.
     */
    private int index;

    /**
     * Construct a new <code>NodeListSpliterator</code>.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param nodeList The wrapped {@link NodeList}.
     */
    public NodeListSpliterator(final Document document, final NodeList nodeList) {
      this(document, nodeList, 0, getLength(document, nodeList));
      return;
    }

    /**
     * Construct a new <code>NodeListSpliterator</code>.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param nodeList The wrapped {@link NodeList}.
     * @param start The first <code>nodeList</code> {@linkplain NodeList#item(int) item} to be
     * {@link #tryAdvance(Consumer) processed}.
     * @param limit The last <code>nodeList</code> {@linkplain NodeList#item(int) item} to be
     * {@link #tryAdvance(Consumer) processed}.
     */
    public NodeListSpliterator(final Document document, final NodeList nodeList, final int start, final int limit) {
      this.document = document;
      this.nodeList = nodeList;
      this.limit = limit;
      this.characteristics = 0 | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
      this.index = start;
      return;
    }

    /**
     * Get the {@linkplain NodeList#getLength() length} of the supplied <code>nodeList</code> while synchronizing on the
     * supplied <code>document</code> for thread-safety.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param nodeList The {@link NodeList} to retrieve the {@linkplain NodeList#getLength() length} of.
     * @return The {@linkplain NodeList#getLength() length} of the supplied <code>nodeList</code>.
     * @see NodeList#getLength()
     */
    private static final int getLength(final Document document, final NodeList nodeList) {
      synchronized (document) {
        return nodeList.getLength();
      }
    }

    /**
     * Get the {@linkplain NodeList#item(int) item} from the supplied <code>nodeList</code> at the specified
     * <code>index</code> while synchronizing on the supplied <code>document</code> for thread-safety.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param nodeList The {@link NodeList} to retrieve the {@linkplain NodeList#item(int) item} from.
     * @param index The index of the {@linkplain NodeList#item(int) item} to retrieve.
     * @return The {@linkplain NodeList#item(int) item} at the specified <code>index</code>.
     * @throws IndexOutOfBoundsException If the index is invalid.
     */
    private static final Node item(final Document document, final NodeList nodeList, final int index) throws IndexOutOfBoundsException {
      final Node result;
      synchronized (document) {
        result = nodeList.item(index);
      }
      if (result == null) throw new IndexOutOfBoundsException(String.valueOf(index));
      return result;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Node> action) {
      if ((index < 0) || (index >= limit)) return false;
      action.accept(item(document, nodeList, index++));
      return true;
    }

    @Override
    public @Nullable Spliterator<Node> trySplit() {
      int start = index;
      int middle = (start + limit) >>> 1;
      return (start < middle) ? new NodeListSpliterator(document, nodeList, start, index = middle) : null;
    }

    @Override
    public long estimateSize() {
      return limit - index;
    }

    @Override
    public int characteristics() {
      return characteristics;
    }

    @Override
    public void forEachRemaining(final Consumer<? super Node> action) {
      int i;
      if ((getLength(document, nodeList) >= limit) && ((i = index) >= 0) && (i < (index = limit))) {
        do {
          action.accept(item(document, nodeList, i));
        } while (++i < limit);
      }
      return;
    }

  } // NodeListSpliterator

  /**
   * A {@link Spliterator}&lt;{@link Attr}&gt; implementation wrapping a {@link NamedNodeMap}.
   */
  private static final class AttrSpliterator implements Spliterator<Attr> {
    /**
     * The {@link Document} to synchronize on.
     */
    private final Document document;
    /**
     * The wrapped {@link NamedNodeMap}.
     */
    private final NamedNodeMap namedNodeMap;
    /**
     * The maximum {@link #namedNodeMap} {@linkplain #item(Document, NamedNodeMap, int) item} {@link #index} exposed by
     * this wrapper.
     */
    private final int limit;
    /**
     * @see #characteristics()
     */
    private final int characteristics;
    /**
     * The next {@linkplain #item(Document, NamedNodeMap, int) item} to be {@link #tryAdvance(Consumer) processed}.
     */
    private int index;

    /**
     * Construct a new <code>AttrSpliterator</code>.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param namedNodeMap The wrapped {@link NamedNodeMap}.
     */
    public AttrSpliterator(final Document document, final NamedNodeMap namedNodeMap) {
      this(document, namedNodeMap, 0, getLength(document, namedNodeMap));
      return;
    }

    /**
     * Construct a new <code>AttrSpliterator</code>.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param namedNodeMap The wrapped {@link NamedNodeMap}.
     * @param start The first <code>namedNodeMap</code> {@linkplain NodeList#item(int) item} to be
     * {@link #tryAdvance(Consumer) processed}.
     * @param limit The last <code>namedNodeMap</code> {@linkplain NodeList#item(int) item} to be
     * {@link #tryAdvance(Consumer) processed}.
     */
    public AttrSpliterator(final Document document, final NamedNodeMap namedNodeMap, final int start, final int limit) {
      this.document = document;
      this.namedNodeMap = namedNodeMap;
      this.limit = limit;
      this.characteristics = 0 | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
      this.index = start;
      return;
    }

    /**
     * Get the {@linkplain NamedNodeMap#getLength() length} of the supplied <code>namedNodeMap</code> while
     * synchronizing on the supplied <code>document</code> for thread-safety.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param namedNodeMap The {@link NamedNodeMap} to retrieve the {@linkplain NamedNodeMap#getLength() length} of.
     * @return The {@linkplain NamedNodeMap#getLength() length} of the supplied <code>namedNodeMap</code>.
     * @see NamedNodeMap#getLength()
     */
    private static final int getLength(final Document document, final NamedNodeMap namedNodeMap) {
      synchronized (document) {
        return namedNodeMap.getLength();
      }
    }

    /**
     * Get the {@linkplain NamedNodeMap#item(int) item} from the supplied <code>namedNodeMap</code> at the specified
     * <code>index</code> while synchronizing on the supplied <code>document</code> for thread-safety.
     * 
     * @param document The {@link Document} to synchronize on.
     * @param namedNodeMap The {@link NamedNodeMap} to retrieve the {@linkplain NamedNodeMap#item(int) item} from.
     * @param index The index of the {@linkplain NamedNodeMap#item(int) item} to retrieve.
     * @return The {@linkplain NamedNodeMap#item(int) item} at the specified <code>index</code>.
     * @throws IndexOutOfBoundsException If the index is invalid.
     */
    private static final Attr item(final Document document, final NamedNodeMap namedNodeMap, final int index) throws IndexOutOfBoundsException {
      final Attr result;
      synchronized (document) {
        result = (Attr)namedNodeMap.item(index);
      }
      if (result == null) throw new IndexOutOfBoundsException(String.valueOf(index));
      return result;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Attr> action) {
      if ((index < 0) || (index >= limit)) return false;
      action.accept(item(document, namedNodeMap, index++));
      return true;
    }

    @Override
    public @Nullable Spliterator<Attr> trySplit() {
      int start = index;
      int middle = (start + limit) >>> 1;
      return (start < middle) ? new AttrSpliterator(document, namedNodeMap, start, index = middle) : null;
    }

    @Override
    public long estimateSize() {
      return limit - index;
    }

    @Override
    public int characteristics() {
      return characteristics;
    }

    @Override
    public void forEachRemaining(final Consumer<? super Attr> action) {
      int i;
      if ((getLength(document, namedNodeMap) >= limit) && ((i = index) >= 0) && (i < (index = limit))) {
        do {
          action.accept(item(document, namedNodeMap, i));
        } while (++i < limit);
      }
      return;
    }

  } // AttrSpliterator

}
