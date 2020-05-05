/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.transform;

import java.io.*;
import java.net.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;

import org.w3c.dom.ls.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.eclipse.jdt.annotation.*;


/**
 * Transformation Utilities.
 */
@NonNullByDefault
public class TransformUtil {
  /**
   * A shared {@link SAXParserFactory} instance.
   */
  protected static final SAXParserFactory SAX_PARSER_FACTORY;
  static {
    SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
  }
  /**
   * A shared {@link ErrorHandler} instance.
   */
  protected static final ErrorHandler ERROR_HANDLER = new DefaultHandler();
  /**
   * A shared {@link ErrorListener} implementation.
   */
  protected static final ErrorListener ERROR_LISTENER = new ErrorListener() {

    @Override
    public void warning(TransformerException exception) throws TransformerException {
      return;
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
      return;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      throw exception;
    }

  };

  /**
   * Parse a set of {@link Templates} from a {@link URL}.
   * 
   * @param url The {@link URL} of the templates to be parsed.
   * @return The parsed {@link Templates}.
   * @throws UncheckedIOException If an {@link IOException} was encountered {@linkplain URL#openStream() opening} the
   * URL.
   * @throws LSException If a {@link ParserConfigurationException}, {@link TransformerConfigurationException}, or
   * {@link SAXException} occurred while parsing.
   */
  public static final Templates parseTemplates(final URL url) throws UncheckedIOException, LSException {
    try {
      final InputSource templatesInputSource = new InputSource(url.openStream());
      final SAXParser saxParser;
      synchronized (SAX_PARSER_FACTORY) {
        saxParser = SAX_PARSER_FACTORY.newSAXParser();
      }
      final XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
      xmlReader.setEntityResolver(new EntityResolver() {

        @Override
        public @Nullable InputSource resolveEntity(@Nullable String publicId, String systemId) throws SAXException, IOException {
          return new InputSource(new URL(url, systemId).toExternalForm());
        }

      });
      xmlReader.setErrorHandler(ERROR_HANDLER);
      final Source templatesSource = new SAXSource(xmlReader, templatesInputSource);
      templatesSource.setSystemId(url.toString());
      final TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setErrorListener(ERROR_LISTENER);
      return transformerFactory.newTemplates(templatesSource);
    } catch (ParserConfigurationException | TransformerConfigurationException | SAXException e) {
      final LSException lse = new LSException(LSException.PARSE_ERR, e.getMessage());
      lse.initCause(e);
      throw lse;
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }

}
