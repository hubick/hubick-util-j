/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.html;

import java.net.*;

import javax.activation.*;

import org.eclipse.jdt.annotation.*;


/**
 * <a href="http://www.w3.org/TR/html/">HTML</a> Utilities.
 */
@NonNullByDefault
public abstract class HTMLUtil {
  /**
   * The <a href="http://www.w3.org/">W3C</a> <a href="http://www.w3.org/TR/html/">HTML</a>
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> prefix String.
   */
  public static final String HTML_NS_PREFIX = "html";
  /**
   * The <a href="http://www.w3.org/">W3C</a> <a href="http://www.w3.org/TR/html/">HTML</a>
   * <a href="http://www.w3.org/TR/xml-names/">namespace</a> URI.
   */
  public static final URI HTML_NS_URI = URI.create("http://www.w3.org/1999/xhtml");
  /**
   * The <a href="http://www.w3.org/">W3C</a> <a href="http://www.w3.org/TR/html/">HTML</a> document type name String.
   */
  public static final String HTML_DOCTYPE_NAME = "html";
  /**
   * The <a href="http://www.w3.org/">W3C</a> <a href="http://www.w3.org/TR/html/">HTML</a> legacy document type
   * PublicID String.
   */
  public static final URI HTML_SYSTEM_ID = URI.create("about:legacy-compat");
  /**
   * The {@link MimeType} Object for the <code>"application/xhtml+xml"</code> mime type.
   */
  public static final MimeType APPLICATION_XHTML_XML_MIME_TYPE;
  static {
    try {
      APPLICATION_XHTML_XML_MIME_TYPE = new MimeType("application", "xhtml+xml");
    } catch (MimeTypeParseException mtpe) {
      throw new RuntimeException(mtpe);
    }
  }
  /**
   * The {@link MimeType} Object for the <code>"text/html"</code> mime type.
   */
  public static final MimeType TEXT_HTML_MIME_TYPE;
  static {
    try {
      TEXT_HTML_MIME_TYPE = new MimeType("text", "html");
    } catch (MimeTypeParseException mtpe) {
      throw new RuntimeException(mtpe);
    }
  }

  /**
   * Is the supplied {@link MimeType} that of an <a href="http://www.w3.org/TR/html/">HTML</a> document?
   * 
   * @param mimeType The {@link MimeType} in question
   * @return <code>true</code> if the <code>mimeType</code> argument is an HTML type.
   */
  public static final boolean isHTML(final @Nullable MimeType mimeType) {
    if (mimeType == null) return false;
    final String primaryType = mimeType.getPrimaryType();
    final String subType = mimeType.getSubType();
    if ((primaryType == null) || (subType == null)) return false;
    if (primaryType.equals("application")) {
      if (subType.equals("html")) return true;
      if (subType.equals("xhtml+xml")) return true;
    } else if (primaryType.equals("text")) {
      if (subType.equals("html")) return true;
    }
    return false;
  }

}
