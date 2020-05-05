/*
 * Copyright 2017-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.stream;

import java.util.*;

import javax.xml.*;
import javax.xml.namespace.*;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.stax.*;

import org.eclipse.jdt.annotation.*;


/**
 * An {@link XMLStreamWriter} which, by default, simply forwards events to another one.
 */
@NonNullByDefault
public class XMLStreamWriterAdapter implements XMLStreamWriter {
  protected final XMLStreamWriter streamWriter;

  /**
   * Construct a new <code>XMLStreamWriterAdapter</code>.
   * 
   * @param streamWriter The {@link XMLStreamWriter} events should be forwarded to.
   */
  public XMLStreamWriterAdapter(final XMLStreamWriter streamWriter) {
    this.streamWriter = Objects.requireNonNull(streamWriter);
    return;
  }

  @Override
  public @Nullable Object getProperty(final String name) throws IllegalArgumentException {
    return streamWriter.getProperty(name);
  }

  @Override
  public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
    streamWriter.setNamespaceContext(context);
    return;
  }

  @Override
  public @Nullable NamespaceContext getNamespaceContext() {
    return streamWriter.getNamespaceContext();
  }

  @Override
  public void setDefaultNamespace(final @Nullable String uri) throws XMLStreamException {
    streamWriter.setDefaultNamespace(uri);
    return;
  }

  @Override
  public void setPrefix(final String prefix, final @Nullable String uri) throws XMLStreamException {
    streamWriter.setPrefix(prefix, uri);
    return;
  }

  @Override
  public @Nullable String getPrefix(final String uri) throws XMLStreamException {
    return streamWriter.getPrefix(uri);
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    streamWriter.writeStartDocument();
    return;
  }

  @Override
  public void writeStartDocument(final String version) throws XMLStreamException {
    streamWriter.writeStartDocument(version);
    return;
  }

  @Override
  public void writeStartDocument(final @Nullable String encoding, final String version) throws XMLStreamException {
    streamWriter.writeStartDocument(encoding, version);
    return;
  }

  @Override
  public void writeDTD(final String dtd) throws XMLStreamException {
    streamWriter.writeDTD(dtd);
    return;
  }

  @Override
  public void writeEntityRef(final String name) throws XMLStreamException {
    streamWriter.writeEntityRef(name);
    return;
  }

  @Override
  public void writeNamespace(final @Nullable String prefix, final String namespaceURI) throws XMLStreamException {
    streamWriter.writeNamespace(prefix, namespaceURI);
    return;
  }

  @Override
  public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException {
    streamWriter.writeDefaultNamespace(namespaceURI);
    return;
  }

  @Override
  public void writeStartElement(final String localName) throws XMLStreamException {
    streamWriter.writeStartElement(localName);
    return;
  }

  @Override
  public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException {
    streamWriter.writeStartElement(namespaceURI, localName);
    return;
  }

  @Override
  public void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
    streamWriter.writeStartElement(prefix, localName, namespaceURI);
    return;
  }

  @Override
  public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException {
    streamWriter.writeEmptyElement(namespaceURI, localName);
    return;
  }

  @Override
  public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException {
    streamWriter.writeEmptyElement(prefix, localName, namespaceURI);
    return;
  }

  @Override
  public void writeEmptyElement(final String localName) throws XMLStreamException {
    streamWriter.writeEmptyElement(localName);
    return;
  }

  @Override
  public void writeAttribute(final String localName, final String value) throws XMLStreamException {
    streamWriter.writeAttribute(localName, value);
    return;
  }

  @Override
  public void writeAttribute(final @Nullable String prefix, final @Nullable String namespaceURI, final String localName, final String value) throws XMLStreamException {
    streamWriter.writeAttribute(prefix, namespaceURI, localName, value);
    return;
  }

  @Override
  public void writeAttribute(final @Nullable String namespaceURI, final String localName, final String value) throws XMLStreamException {
    streamWriter.writeAttribute(namespaceURI, localName, value);
    return;
  }

  @Override
  public void writeCharacters(final String text) throws XMLStreamException {
    streamWriter.writeCharacters(text);
    return;
  }

  @Override
  public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException {
    streamWriter.writeCharacters(text, start, len);
    return;
  }

  @Override
  public void writeComment(final @Nullable String data) throws XMLStreamException {
    streamWriter.writeComment(data);
    return;
  }

  @Override
  public void writeProcessingInstruction(final String target) throws XMLStreamException {
    streamWriter.writeProcessingInstruction(target);
    return;
  }

  @Override
  public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException {
    streamWriter.writeProcessingInstruction(target, data);
    return;
  }

  @Override
  public void writeCData(final String data) throws XMLStreamException {
    streamWriter.writeCData(data);
    return;
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    streamWriter.writeEndElement();
    return;
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    streamWriter.writeEndDocument();
    return;
  }

  @Override
  public void flush() throws XMLStreamException {
    streamWriter.flush();
    return;
  }

  @Override
  public void close() throws XMLStreamException {
    streamWriter.close();
    return;
  }

  /**
   * Return an {@link XMLStreamWriterAdapter} which does not forward start/end document calls to the wrapped
   * {@link XMLStreamWriter}.
   * 
   * @param streamWriter The {@link XMLStreamWriter} to forward (all other) events to.
   * @return The new {@link XMLStreamWriterAdapter}.
   */
  public static final XMLStreamWriterAdapter disableDocumentEvents(final XMLStreamWriter streamWriter) {
    return new XMLStreamWriterAdapter(streamWriter) {

      @Override
      public void writeStartDocument() throws XMLStreamException {
        return;
      }

      @Override
      public void writeStartDocument(final String version) throws XMLStreamException {
        return;
      }

      @Override
      public void writeStartDocument(final @Nullable String encoding, final String version) throws XMLStreamException {
        return;
      }

      @Override
      public void writeEndDocument() throws XMLStreamException {
        return;
      }

    };
  }

  /**
   * Return an {@link XMLStreamWriterAdapter} which attempts to prevent namespace errors while
   * {@linkplain Transformer#transform(Source, Result) transforming} to a {@link StAXResult}.
   * 
   * @param streamWriter The {@link XMLStreamWriter} to forward events to.
   * @return The new {@link XMLStreamWriterAdapter}.
   */
  public static final XMLStreamWriterAdapter fixStAXResultTransform(final XMLStreamWriter streamWriter) {
    return new XMLStreamWriterAdapter(streamWriter) {

      @Override
      public void setPrefix(final String prefix, final @Nullable String uri) throws XMLStreamException {
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) return;
        streamWriter.setPrefix(prefix, uri);
        return;
      }

    };
  }

}
