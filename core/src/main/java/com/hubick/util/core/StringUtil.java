/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

import org.eclipse.jdt.annotation.*;


/**
 * String Utilities.
 */
@NonNullByDefault
public abstract class StringUtil {
  /**
   * A {@link Pattern} that can be used to {@linkplain Pattern#split(CharSequence) split} a set of elements separated by
   * commas and whitespace within a String.
   */
  public static final Pattern COMMA_SEPARATED_PATTERN = Pattern.compile("[\\s]*,[\\s]*");
  /**
   * A {@link Pattern} that can be used to {@linkplain Pattern#split(CharSequence) split} a set of elements separated
   * using "Camel Case" within a String.
   */
  public static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
  /**
   * A {@link Pattern} that can be used to {@linkplain Matcher#replaceAll(String) replace} characters that have been
   * escaped using a backslashes ('\').
   * 
   * @see #splitFuncEB(char)
   */
  protected static final Pattern BACKSLASH_ESCAPED_CHAR_PATTERN = Pattern.compile("\\\\(.)");
  /**
   * A {@link Function} which will encode a {@link Stream} of components into a single String, separated by dashes
   * ('-').
   * 
   * @see #PARSE_DASH_DELIMITED_STRING_FUNCTION
   */
  public static final Function<Stream<String>,String> PRINT_DASH_DELIMITED_STRING_FUNCTION = (stream) -> stream.map((t) -> t.replace("-", "--")).collect(Collectors.joining("-"));
  /**
   * A {@link Function} which will decode a {@link Stream} of components, separated by dashes ('-'), from a single
   * String.
   * 
   * @see #PRINT_DASH_DELIMITED_STRING_FUNCTION
   * @see #splitFuncED(char)
   */
  public static final Function<String,Stream<String>> PARSE_DASH_DELIMITED_STRING_FUNCTION = splitFuncED('-');
  /**
   * A {@link Function} which will encode a {@link Stream} of components into a single String, separated by dots ('.').
   * 
   * @see #PARSE_DOT_DELIMITED_STRING_FUNCTION
   */
  public static final Function<Stream<String>,String> PRINT_DOT_DELIMITED_STRING_FUNCTION = (stream) -> stream.map((t) -> t.replace("\\", "\\\\")).map((t) -> t.replace(".", "\\.")).collect(Collectors.joining("."));
  /**
   * A {@link Function} which will decode a {@link Stream} of components, separated by dots ('.'), from a single String.
   * 
   * @see #PRINT_DOT_DELIMITED_STRING_FUNCTION
   * @see #splitFuncEB(char)
   */
  public static final Function<String,Stream<String>> PARSE_DOT_DELIMITED_STRING_FUNCTION = splitFuncEB('.');

  /**
   * Evaluate two Strings for {@linkplain String#equals(Object) equality}, tolerating <code>null</code> values.
   * 
   * @param string1 The first String
   * @param string2 The second String
   * @param caseSensitive If <code>false</code>, {@linkplain String#equalsIgnoreCase(String) ignore case} in the
   * comparison.
   * @return <code>true</code> if the strings are equal according the the criteria, or both <code>null</code>.
   * @see String#equals(Object)
   */
  public static final boolean equal(final @Nullable String string1, final @Nullable String string2, final boolean caseSensitive) {
    if (string1 == null) return (string2 == null);
    else if (string2 == null) return false;
    else if (string1 == string2) return true;
    return (caseSensitive) ? string1.equals(string2) : string1.equalsIgnoreCase(string2);
  }

  /**
   * {@linkplain String#trim() Trim} the given <code>string</code>.
   * 
   * @param string The string to be trimmed.
   * @return The trimmed string, or <code>null</code> if it was null.
   * @see String#trim()
   */
  public static final @Nullable String trim(final @Nullable String string) {
    return (string != null) ? string.trim() : null;
  }

  /**
   * Return the given <code>string</code>, or <code>null</code> if it's {@linkplain String#isEmpty() empty}.
   * 
   * @param string The string to be evaluated.
   * @return The string value, or <code>null</code>.
   * @see String#isEmpty()
   */
  public static final @Nullable String mkNull(final @Nullable String string) {
    return (string != null) ? (string.isEmpty() ? null : string) : null;
  }

  /**
   * Get the {@linkplain String#valueOf(Object) string value} of the supplied <code>object</code>.
   * 
   * @param object The object to be converted to a String.
   * @return The {@linkplain String#valueOf(Object) string value} of the supplied object.
   * @see String#valueOf(Object)
   */
  public static final @Nullable String toString(final @Nullable Object object) {
    return (object != null) ? String.valueOf(object) : null;
  }

  /**
   * Get the {@linkplain String#valueOf(Object) string value} of the supplied <code>object</code>.
   * 
   * @param object The object to be converted to a String.
   * @param defaultValue The value to be returned if the supplied <code>object</code> is <code>null</code>.
   * @return The {@linkplain String#valueOf(Object) string value} of the supplied object.
   * @see String#valueOf(Object)
   */
  public static final String toString(final @Nullable Object object, final @Nullable String defaultValue) {
    return Optional.ofNullable(object).map(String::valueOf).orElse(String.valueOf(defaultValue));
  }

  /**
   * Get a {@link Function} that will split out a {@link Stream} of components which has been formatted into a single
   * String using the specified delimiter (Escaped using two consecutive Duplicate delimiters).
   * 
   * @param delimiter The delimiter used to separate components.
   * @return A {@link Function} to perform the splitting.
   */
  public static final Function<String,Stream<String>> splitFuncED(final char delimiter) {
    return (t) -> {
      final ArrayList<String> r = new ArrayList<String>();
      final StringBuffer comp = new StringBuffer();
      for (int i = 0; i < t.length(); i++) {
        final char c = t.charAt(i);
        if (c == delimiter) {
          if ((i + 1 < t.length()) && (t.charAt(i + 1) == delimiter)) {
            comp.append(c);
            i++; // skip that next character we just handled
          } else {
            r.add(comp.toString());
            comp.setLength(0);
          }
        } else {
          comp.append(c);
        }
      }
      r.add(comp.toString());
      return r.stream();
    };
  }

  /**
   * Get a Function that will split out a List of components has been formatted into a single String using the specified
   * delimiter (Escaped using Backslashes).
   * 
   * @param delimiter The delimiter used to separate components.
   * @return A {@link Function} to perform the splitting.
   * @throws IllegalArgumentException If the supplied delimiter is a backslash ('\').
   */
  public static final Function<String,Stream<String>> splitFuncEB(final char delimiter) throws IllegalArgumentException {
    if (delimiter == '\\') throw new IllegalArgumentException("Cannot use backslash escaping with a backslash delimiter");
    final Pattern splitPattern = Pattern.compile("(?<!\\\\)" + Pattern.quote(String.valueOf(delimiter))); // "zero-width negative lookbehind".
    return (t) -> splitPattern.splitAsStream(t).map((comp) -> BACKSLASH_ESCAPED_CHAR_PATTERN.matcher(comp).replaceAll("$1"));
  }

  /**
   * Create a hex escape sequence from the supplied input <code>value</code>.
   * 
   * @param value The value being escaped.
   * @param escapeChar The character which will be prepended to the result.
   * @param escapedHexLength The number of hex digits to be used in the sequence (the result will be zero padded).
   * @return The hex encoded result String.
   * @throws IllegalArgumentException If <code>escapedHexLength</code> is less than <code>1</code> or if the supplied
   * <code>value</code> is too large to be encoded into a hex string of length <code>escapedHexLength</code>.
   */
  public static final String hexEscape(final long value, final char escapeChar, final int escapedHexLength) throws IllegalArgumentException {
    if (escapedHexLength < 1) throw new IllegalArgumentException("Invalid escapedHexLength");
    final String hs = Long.toHexString(value).toUpperCase();
    if (hs.length() > escapedHexLength) throw new IllegalArgumentException("The value '" + value + "' is too large to be encoded into a " + escapedHexLength + " character hex string");
    final StringBuffer sb = new StringBuffer(escapedHexLength + 1);
    sb.append(escapeChar);
    for (int i = hs.length(); i < escapedHexLength; i++) {
      sb.append('0');
    }
    sb.append(hs);
    return sb.toString();
  }

}
