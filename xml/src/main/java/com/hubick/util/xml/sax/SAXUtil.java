/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.sax;

import java.net.*;
import java.util.*;
import java.util.function.*;

import javax.xml.*;
import javax.xml.namespace.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;

import com.hubick.util.core.*;

import com.hubick.util.xml.*;

import org.eclipse.jdt.annotation.*;


/**
 * <a href="http://www.saxproject.org/">SAX</a> Utilities.
 */
@NonNullByDefault
public abstract class SAXUtil {
  /**
   * The <a href="http://www.saxproject.org/">SAX</a> property used to {@linkplain XMLReader#setProperty(String, Object)
   * set} a {@link LexicalHandler} on the {@link XMLReader} which a {@link SAXParser} is
   * {@linkplain SAXParser#getXMLReader() using}. Doing this allows the {@link LexicalHandler} to receive events for
   * {@linkplain LexicalHandler#comment(char[], int, int) comments} and {@linkplain LexicalHandler#startCDATA() CDATA}
   * sections, etc.
   */
  public static final String SAX_LEXICAL_HANDLER_PROP = "http://xml.org/sax/properties/lexical-handler";
  /**
   * The <a href="http://www.saxproject.org/">SAX</a> property used to {@linkplain XMLReader#setProperty(String, Object)
   * set} a {@link DeclHandler} on the {@link XMLReader} which a {@link SAXParser} is
   * {@linkplain SAXParser#getXMLReader() using}. Doing this allows the {@link DeclHandler} to receive events for DTD
   * declaration elements.
   */
  public static final String SAX_DECL_HANDLER_PROP = "http://xml.org/sax/properties/declaration-handler";
  /**
   * A shared {@linkplain SAXParserFactory#isNamespaceAware() namespace-aware}
   * {@linkplain SAXParserFactory#isValidating() non-validating} {@link SAXParserFactory}.
   */
  protected static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
  static {
    SAX_PARSER_FACTORY.setNamespaceAware(true);
    SAX_PARSER_FACTORY.setValidating(false);
    try {
      SAX_PARSER_FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); // Ignore the external DTD completely.
    } catch (java.lang.Exception e) {}
  }

  /**
   * Create a new {@link SAXParser}. The parser will be {@linkplain SAXParserFactory#setNamespaceAware(boolean)
   * namespace aware}, {@linkplain SAXParserFactory#setValidating(boolean) validating}, won't load external DTD's, and
   * will throw exceptions on parsing errors.
   * 
   * @return The new {@link SAXParser}.
   */
  public static final SAXParser newSAXParser() {
    final SAXParser saxParser;
    try {
      synchronized (SAX_PARSER_FACTORY) {
        saxParser = SAX_PARSER_FACTORY.newSAXParser();
      }
    } catch (org.xml.sax.SAXException saxe) {
      throw new RuntimeException(saxe);
    } catch (ParserConfigurationException pce) {
      throw new RuntimeException(pce);
    }
    return saxParser;
  }

  /**
   * Set the specified attribute <code>value</code>.
   * 
   * @param attributes The {@link Attributes} upon which the <code>value</code> should be
   * {@linkplain AttributesImpl#setAttribute(int, String, String, String, String, String) set}.
   * @param nsURI The <a href="http://www.w3.org/TR/xml-names/">namespace</a> {@link URI} to use for the created
   * attribute.
   * @param nsPrefix The namespace prefix to use for the created attribute.
   * @param localName The {@linkplain Attributes#getLocalName(int) local name} to use for the created attribute.
   * @param value The new value of the attribute. If <code>null</code> any existing attribute will be
   * {@linkplain AttributesImpl#removeAttribute(int) removed}.
   * @return The new {@link AttributesImpl} based on the old <code>attributes</code>, but with the new
   * <code>value</code> added.
   * @see AttributesImpl#setAttribute(int, String, String, String, String, String)
   * @see AttributesImpl#removeAttribute(int)
   */
  public static final AttributesImpl setAttribute(final @Nullable Attributes attributes, final @Nullable Object nsURI, final @Nullable String nsPrefix, final String localName, final @Nullable Object value) {
    final AttributesImpl newAttributes;
    final int existingIndex;
    if (attributes != null) {
      newAttributes = (attributes instanceof AttributesImpl) ? (AttributesImpl)attributes : new AttributesImpl(attributes);
      existingIndex = attributes.getIndex((nsURI != null) ? nsURI.toString() : XMLConstants.NULL_NS_URI, localName);
    } else {
      newAttributes = new AttributesImpl();
      existingIndex = -1;
    }
    if (value != null) {
      if (existingIndex >= 0) {
        newAttributes.setAttribute(existingIndex, (nsURI != null) ? nsURI.toString() : XMLConstants.NULL_NS_URI, localName, (nsPrefix != null) ? nsPrefix + ':' + localName : localName, "CDATA", value.toString());
      } else {
        newAttributes.addAttribute((nsURI != null) ? nsURI.toString() : XMLConstants.NULL_NS_URI, localName, (nsPrefix != null) ? nsPrefix + ':' + localName : localName, "CDATA", value.toString());
      }
    } else {
      if (existingIndex >= 0) newAttributes.removeAttribute(existingIndex);
    }
    return newAttributes;
  }

  /**
   * Create a {@link Map} exposing the supplied {@link Attributes}.
   * 
   * @param attributes The {@link Attributes} to be exposed as a Map.
   * @return A {@link Map} of the supplied {@link Attributes}.
   */
  public static final Map<QName,String> toMap(final Attributes attributes) {
    final AttributesImpl attributesImpl = (attributes instanceof AttributesImpl) ? (AttributesImpl)attributes : new AttributesImpl(attributes);
    class AttributesMap extends AbstractMap<QName,String> {
      @Override
      public Set<Map.Entry<QName,String>> entrySet() {
        return new AbstractSet<Map.Entry<QName,String>>() {

          @Override
          public Iterator<Map.Entry<QName,String>> iterator() {
            return new Iterator<Map.Entry<QName,String>>() {
              /**
               * The index of the last attribute returned by this iterator.
               */
              protected int lastIndex = -1;

              @Override
              public boolean hasNext() {
                return lastIndex + 1 <= attributesImpl.getLength() - 1;
              }

              @Override
              public Map.Entry<QName,String> next() {
                if (!hasNext()) throw new IllegalStateException();
                final SimpleImmutableEntry<QName,String> nextEntry = new SimpleImmutableEntry<QName,String>(XMLUtil.parseQName(attributesImpl.getURI(lastIndex + 1), Objects.requireNonNull(attributesImpl.getQName(lastIndex + 1))), Objects.requireNonNull(attributesImpl.getValue(lastIndex + 1)));
                lastIndex++;
                return nextEntry;
              }

              @Override
              public void remove() {
                if ((lastIndex < 0) || (lastIndex > attributesImpl.getLength() - 1)) throw new IllegalStateException();
                attributesImpl.removeAttribute(lastIndex);
                return;
              }

            };
          } // iterator()

          @Override
          public int size() {
            return attributesImpl.getLength();
          }

          @Override
          public boolean isEmpty() {
            return attributesImpl.getLength() == 0;
          }

          @Override
          public void clear() {
            attributesImpl.clear();
            return;
          }

        };
      } // entrySet()
    };
    return new AttributesMap();
  } // toMap(Attributes)

  /**
   * Get a {@link Predicate} which can {@linkplain Predicate#test(Object) test} a set of {@link Attributes} for one with
   * a given {@linkplain Attributes#getURI(int) namespace}, {@linkplain Attributes#getLocalName(int) local name}, or
   * {@linkplain Attributes#getValue(int) value}.
   * 
   * @param namespace The {@linkplain Attributes#getURI(int) namespace} of the required attribute.
   * @param localName The {@linkplain Attributes#getLocalName(int) local name} of the required attribute.
   * @param value The {@linkplain Attributes#getValue(int) value} of the required attribute.
   * @param caseSensitiveValue Supply <code>false</code> if case should be {@linkplain String#equalsIgnoreCase(String)
   * ignored} when examining the attribute <code>value</code>.
   * @return A {@link Predicate} only returning <code>true</code> if the {@link Attributes} being
   * {@linkplain Predicate#test(Object) tested} meet the supplied criterion.
   */
  public static final Predicate<@Nullable Attributes> testAttr(final @Nullable URI namespace, final String localName, final @Nullable String value, final boolean caseSensitiveValue) {
    return (attributes) -> {
      if (attributes == null) return false;
      final int attrIndex = attributes.getIndex((namespace != null) ? namespace.toString() : XMLConstants.NULL_NS_URI, localName);
      if (attrIndex < 0) return false;
      if ((value != null) && (!StringUtil.equal(value, attributes.getValue(attrIndex), caseSensitiveValue))) return false;
      return true;
    };
  }

  /**
   * Set the <code>xml:lang</code> Attr in the given <code>attributes</code> to the value of the given
   * <code>locale</code>.
   * 
   * @param attributes The {@link Attributes} in which to declare the language.
   * @param locale The {@link Locale} of the language to declare, if <code>null</code> an empty attribute will be
   * declared.
   * @return The new {@link AttributesImpl} based on the old <code>attributes</code>, but with the language attribute
   * added.
   */
  public static final AttributesImpl setXMLLangAttr(final @Nullable Attributes attributes, final @Nullable Locale locale) {
    return setAttribute(attributes, XMLUtil.XML_NS_URI, XMLConstants.XML_NS_PREFIX, "lang", ((locale != null) && (!Locale.ROOT.equals(locale))) ? locale.toLanguageTag() : "");
  }

  /**
   * Retrieve the value of the <code>xml:lang</code> attribute from the supplied <code>attributes</code> list.
   * 
   * @param attributes The {@link Attributes} from which to retrieve the <code>xml:lang</code> value.
   * @param defaultLocale The {@link Locale} to return should the <code>attributes</code> not specify a language.
   * @return The value of the <code>xml:lang</code> attribute, parsed as a {@link Locale}.
   * @see Attributes#getValue(String, String)
   */
  public static final Optional<Locale> getXMLLangAttr(final @Nullable Attributes attributes, final @Nullable Locale defaultLocale) {
    final Optional<Locale> xmlLangAttr = Optional.ofNullable(attributes).map((attrs) -> attrs.getValue(XMLConstants.XML_NS_URI, "lang")).map(Locale::forLanguageTag);
    return xmlLangAttr.isPresent() ? xmlLangAttr : Optional.ofNullable(defaultLocale);
  }

}
