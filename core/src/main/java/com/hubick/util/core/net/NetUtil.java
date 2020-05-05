/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

import org.eclipse.jdt.annotation.*;

import com.hubick.util.core.io.*;


/**
 * Network Utilities.
 */
@NonNullByDefault
public abstract class NetUtil {
  /**
   * A {@link Pattern} for <code>&amp;</code>.
   * 
   * @see #getQueryParams(URI)
   */
  protected static final Pattern AMPERSAND_PATTERN = Pattern.compile("&");

  /**
   * A utility method for {@link URL#URL(String)} which enables method references by throwing an
   * {@link UncheckedIOException} wrapping any {@link MalformedURLException} (checked).
   * 
   * @param spec The String to parse as a URL.
   * @return The newly constructed {@link URL}.
   * @throws UncheckedIOException If a {@link MalformedURLException} was encountered during {@linkplain URL#URL(String)
   * construction}.
   * @see URL#URL(String)
   */
  public static final URL newURL(final String spec) throws UncheckedIOException {
    try {
      return new URL(spec);
    } catch (MalformedURLException mue) {
      throw new UncheckedIOException(mue.getClass().getSimpleName() + ": " + spec, mue);
    }
  }

  /**
   * {@linkplain URLEncoder#encode(String, String) Encode} the <code>unencoded</code> source String into one which is
   * <a href="http://www.w3.org/TR/html40/interact/forms.html#form-content-type">
   * <code>application/x-www-form-urlencoded</code></a> using the {@linkplain IOUtil#UTF_8_CHARSET UTF-8} charset.
   * 
   * @param unencoded The String to be encoded.
   * @return The encoded String.
   * @see URLEncoder#encode(String, String)
   */
  public static final String formURLEncode(final String unencoded) {
    try {
      return URLEncoder.encode(unencoded, IOUtil.UTF_8_CHARSET.name());
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee.getMessage(), uee);
    }
  }

  /**
   * Encode the <code>unencoded</code> source String according to <a href="http://www.ietf.org/rfc/rfc1738.txt">RFC
   * 1738</a> using the {@linkplain IOUtil#UTF_8_CHARSET UTF-8} charset.
   * 
   * @param unencoded The String to be encoded.
   * @return The encoded String.
   * @see #formURLEncode(String)
   */
  public static final String urlEncode(final String unencoded) {
    return formURLEncode(unencoded).replace("+", "%20");
  }

  /**
   * Encode a Map of source values according to <a href="http://www.ietf.org/rfc/rfc1738.txt">RFC 1738</a> using the
   * {@linkplain IOUtil#UTF_8_CHARSET UTF-8} charset.
   * 
   * @param queryParams A Map of values to be {@linkplain #urlEncode(String) encoded}.
   * @return A String of encoded values, separated by '&amp;'.
   * @see #urlEncode(String)
   */
  public static final String urlEncode(final Map<@NonNull ?,? extends @Nullable Object> queryParams) {
    return queryParams.entrySet().stream().flatMap((entry) -> (entry.getValue() instanceof Iterable<?>) ? StreamSupport.stream(((Iterable<?>)Objects.requireNonNull(entry.getValue(), "concurrent modification")).spliterator(), false).map((i) -> new AbstractMap.SimpleImmutableEntry<Object,@Nullable Object>(entry.getKey(), i)) : Collections.singleton(entry).stream()).map((entry) -> urlEncode(String.valueOf(entry.getKey())) + '=' + ((entry.getValue() != null) ? urlEncode(String.valueOf(entry.getValue())) : "")).collect(Collectors.joining("&"));
  }

  /**
   * {@linkplain URLDecoder#decode(String, String) Decode} a <a href="http://www.ietf.org/rfc/rfc1738.txt">RFC 1738</a>
   * or <a href="http://www.w3.org/TR/html40/interact/forms.html#form-content-type">
   * <code>application/x-www-form-urlencoded</code></a> String (using the {@linkplain IOUtil#UTF_8_CHARSET UTF-8}
   * charset).
   * 
   * @param encoded The String to be decoded.
   * @return The decoded String.
   * @see URLDecoder#decode(String, String)
   */
  public static final String urlDecode(final String encoded) {
    try {
      return URLDecoder.decode(encoded, IOUtil.UTF_8_CHARSET.name());
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee.getMessage(), uee);
    }
  }

  /**
   * {@linkplain #urlDecode(String) Decode} a URI {@linkplain URI#getRawQuery() query}.
   * 
   * @param uri The {@link URI} to retrieve the {@linkplain URI#getRawQuery() query} from.
   * @return A Map of {@linkplain #urlDecode(String) decoded} params.
   * @see #urlDecode(String)
   */
  public static final Map<String,List<String>> getQueryParams(final @Nullable URI uri) {
    return ((uri != null) && (uri.getRawQuery() != null)) ? AMPERSAND_PATTERN.splitAsStream(uri.getRawQuery()).map((param) -> new AbstractMap.SimpleImmutableEntry<String,String>((param.indexOf('=') >= 0) ? param.substring(0, param.indexOf('=')) : param, (param.indexOf('=') >= 0) ? param.substring(param.indexOf('=') + 1) : "")).map((param) -> new AbstractMap.SimpleImmutableEntry<String,String>(urlDecode(param.getKey()), urlDecode(param.getValue()))).collect(Collectors.groupingBy(Map.Entry::getKey, ConcurrentSkipListMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toCollection(CopyOnWriteArrayList::new)))) : Collections.emptyMap();
  }

  /**
   * {@linkplain #getQueryParams(URI) Parse} a <code>string</code> argument which has been encoded in {@link URI}
   * {@linkplain URI#getRawQuery() query} format.
   * 
   * @param string The String to be parsed.
   * @return A Map of {@linkplain #getQueryParams(URI) parsed} params.
   * @throws IllegalArgumentException If the given <code>string</code> violates RFC 2396.
   */
  public static final Map<String,List<String>> parseQueryParams(final @Nullable String string) throws IllegalArgumentException {
    return getQueryParams(URI.create("?" + ((string != null) ? string : "")));
  }

}
