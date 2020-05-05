/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml;

import java.net.*;

import javax.activation.*;
import javax.xml.*;
import javax.xml.namespace.*;

import org.w3c.dom.*;

import com.hubick.util.core.*;
import com.hubick.util.core.net.*;

import com.hubick.util.xml.dom.*;

import org.eclipse.jdt.annotation.*;


/**
 * XML Utilities.
 */
@NonNullByDefault
public abstract class XMLUtil {
  /**
   * The <a href="http://www.w3.org/">W3C</a> &quot;<a href="http://www.w3.org/2001/XMLSchema">XML Schema</a>&quot;
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI.
   * 
   * @see XMLConstants#W3C_XML_SCHEMA_NS_URI
   */
  public static final URI W3C_XML_SCHEMA_NS_URI = URI.create(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  /**
   * The <a href="http://www.w3.org/">W3C</a> &quot;<a href="http://www.w3.org/2001/XMLSchema">XML Schema</a>&quot;
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> prefix String.
   * 
   * @see #W3C_XML_SCHEMA_NS_URI
   */
  public static final String W3C_XML_SCHEMA_NS_PREFIX = "xs";
  /**
   * The <a href="http://www.w3.org/">W3C</a> &quot;<a href="http://www.w3.org/2001/XMLSchema-instance">XML Schema
   * instance</a>&quot; <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI.
   * 
   * @see XMLConstants#W3C_XML_SCHEMA_INSTANCE_NS_URI
   */
  public static final URI W3C_XML_SCHEMA_INSTANCE_NS_URI = URI.create(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
  /**
   * The <a href="http://www.w3.org/">W3C</a> &quot;<a href="http://www.w3.org/2001/XMLSchema-instance">XML Schema
   * instance</a>&quot; <a href="http://www.w3.org/TR/xml-names/">namespace</a> prefix String.
   * 
   * @see #W3C_XML_SCHEMA_INSTANCE_NS_URI
   */
  public static final String W3C_XML_SCHEMA_INSTANCE_NS_PREFIX = "xsi";
  /**
   * The &quot;<a href="http://www.w3.org/XML/1998/namespace"><code>xml</code></a>&quot;
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI.
   * 
   * @see XMLConstants#XML_NS_URI
   */
  public static final URI XML_NS_URI = URI.create(XMLConstants.XML_NS_URI);
  /**
   * The &quot;<a href="http://www.w3.org/2000/xmlns/"><code>xmlns</code></a>&quot; attribute
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI.
   * 
   * @see XMLConstants#XMLNS_ATTRIBUTE_NS_URI
   */
  public static final URI XMLNS_ATTRIBUTE_NS_URI = URI.create(XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
  /**
   * The <a href="http://www.w3.org/">W3C</a> &quot;<a href="http://www.w3.org/TR/xlink/">XLink</a>&quot;
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI.
   */
  public static final URI XLINK_NS_URI = URI.create("http://www.w3.org/1999/xlink");
  /**
   * The <a href="http://www.w3.org/">W3C</a> &quot;<a href="http://www.w3.org/TR/xlink/">XLink</a>&quot;
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> prefix String.
   */
  public static final String XLINK_NS_PREFIX = "xlink";
  /**
   * The {@link MimeType} Object for the <code>"application/xml"</code> mime type.
   */
  public static final MimeType APPLICATION_XML_MIME_TYPE;
  static {
    try {
      APPLICATION_XML_MIME_TYPE = new MimeType("application", "xml");
    } catch (MimeTypeParseException mtpe) {
      throw new RuntimeException(mtpe);
    }
  }
  /**
   * The {@link MimeType} Object for the <code>"text/xml"</code> mime type.
   */
  public static final MimeType TEXT_XML_MIME_TYPE;
  static {
    try {
      TEXT_XML_MIME_TYPE = new MimeType("text", "xml");
    } catch (MimeTypeParseException mtpe) {
      throw new RuntimeException(mtpe);
    }
  }

  /**
   * Parse the supplied <code>qName</code> String into a {@link QName}.
   * 
   * @param nsURI The namespace URI the supplied <code>qName</code> belongs to.
   * @param qName A String of the format <code>&quot;prefix:localPart&quot;</code> to parse.
   * @return A {@link QName} containing the parsed <code>qName</code> value.
   * @throws IllegalArgumentException If the supplied qName is malformed.
   */
  public static final QName parseQName(final @Nullable Object nsURI, final String qName) throws IllegalArgumentException {
    final int i = qName.indexOf(':');
    final String prefix;
    if (i < 1) {
      prefix = XMLConstants.DEFAULT_NS_PREFIX;
    } else {
      prefix = qName.substring(0, i);
    }
    final String localPart;
    if (i < 0) {
      localPart = qName;
    } else if (i + 1 < qName.length()) {
      localPart = qName.substring(i + 1);
    } else {
      throw new IllegalArgumentException("QName value '" + qName + "' has null local part");
    }
    return new QName((nsURI != null) ? nsURI.toString() : XMLConstants.NULL_NS_URI, localPart, prefix);
  }

  /**
   * Print a {@link QName} by concatenating the given {@linkplain QName#getPrefix() prefix} and
   * {@linkplain QName#getLocalPart() localPart} together.
   * 
   * @param qName The {@link QName} to be printed.
   * @return A String containing the printed name.
   */
  public static final String printQName(final QName qName) {
    return (!XMLConstants.DEFAULT_NS_PREFIX.equals(qName.getPrefix())) ? (qName.getPrefix() + ':' + qName.getLocalPart()) : qName.getLocalPart();
  }

  /**
   * Is the <code>name</code> argument a valid <a href="http://www.w3.org/TR/xml11/#NT-Name">XML name</a> String?
   * 
   * @param name The String to validate.
   * @param nameTestDoc A temporary {@link Document} which can be used to test the name for validity.
   * @return <code>true</code> if the given <code>name</code> is a valid XML name String.
   */
  protected static final boolean isValidXMLName(final String name, final Document nameTestDoc) {
    try {
      nameTestDoc.createAttribute(name);
    } catch (DOMException dome) { // I don't think there is a better way to do this without adding dependencies?
      return false;
    }
    return true;
  }

  /**
   * Encode an arbitrary String into a valid <a href="http://www.w3.org/TR/xml11/#NT-Name">XML 1.1 name</a> by
   * {@linkplain StringUtil#hexEscape(long, char, int) hex escaping} any characters which aren't valid, using an '_'
   * character to signify an escape sequence.
   * 
   * @param unencoded The String to be encoded.
   * @return The encoded String.
   * @see #xmlNameDecode(String)
   */
  public static final String xmlNameEncode(final String unencoded) {
    final Document nameTestDoc = DOMUtil.newDocument();
    nameTestDoc.setXmlVersion("1.1");

    if ((unencoded.indexOf('_') < 0) && (unencoded.indexOf('%') < 0) && (isValidXMLName(unencoded, nameTestDoc))) return unencoded; // Shortcut if whole input is compatible.

    final StringBuffer result = new StringBuffer(unencoded.length()); // We need to build this result char-by-char, escaping as we go.

    final char[] testBuffer = new char[2]; // Use this for faster testString creation below.
    testBuffer[0] = 'A';

    for (int i = 0; i < unencoded.length(); i++) {
      final char c = unencoded.charAt(i);

      final String testString;
      if (i == 0) {
        testString = String.valueOf(c);
      } else {
        testBuffer[1] = c;
        testString = new String(testBuffer);
      }

      if ((c != '_') && (c != '%') && (isValidXMLName(testString, nameTestDoc))) {
        result.append(c);
      } else {
        result.append(StringUtil.hexEscape(c, '_', 2));
      }

    }
    return result.toString();

  }

  /**
   * Decode the original String from one which has been {@linkplain #xmlNameEncode(String) encoded} into a valid
   * <a href="http://www.w3.org/TR/xml11/#NT-Name">XML name</a>
   * 
   * @param encoded The String to be decoded.
   * @return The decoded String.
   * @see #xmlNameEncode(String)
   */
  public static final String xmlNameDecode(final String encoded) {
    return NetUtil.urlDecode(encoded.replace('_', '%'));
  }

  /**
   * Is the supplied {@link MimeType} that of an XML document?
   * 
   * @param mimeType The {@link MimeType} in question
   * @return <code>true</code> if the <code>mimeType</code> argument is an XML type.
   */
  public static final boolean isXML(final @Nullable MimeType mimeType) {
    if (mimeType == null) return false;
    final String subType = mimeType.getSubType();
    if (subType == null) return false;
    if (subType.endsWith("+xml")) return true;
    final String primaryType = mimeType.getPrimaryType();
    if (primaryType == null) return false;
    if (primaryType.equals("application")) {
      if (subType.equals("xml")) return true;
    } else if (primaryType.equals("text")) {
      if (subType.equals("xml")) return true;
    }
    return false;
  }

}
