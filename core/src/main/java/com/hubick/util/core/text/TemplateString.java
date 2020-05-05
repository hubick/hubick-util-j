/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.text;

import java.util.*;
import java.util.regex.*;

import org.eclipse.jdt.annotation.*;


/**
 * <p>
 * A template String containing named variable references which can be dereferenced to {@linkplain #format(Map, String)
 * format} an output String.
 * </p>
 * 
 * <p>
 * For example, given the template String <code>&quot;Say {what} to {who}!&quot;</code>, and an argument Map containing
 * <code>[&quot;what&quot;=&quot;hello&quot;,&quot;who&quot;=&quot;world&quot;]</code>, the
 * {@linkplain #format(Map, String) formatted} result would be <code>&quot;Say hello to world!&quot;</code>
 * </p>
 */
@NonNullByDefault
public class TemplateString implements CharSequence {
  /**
   * The {@link Pattern} used to locate variable escapes within the provided template String.
   */
  protected static final Pattern VARIABLE_ESCAPE_PATTERN = Pattern.compile("\\{(\\w[-\\w\\.\\:]*)\\}");
  /**
   * The unresolved template String upon which this instance is based.
   */
  protected final String template;
  /**
   * The segments comprising this template. These are either regular text segments, in which case the
   * {@link #variableSegments} at the same index will be <code>false</code>, or they represent an argument
   * {@linkplain java.util.Map.Entry#getKey() key}.
   */
  protected final String[] segments;
  /**
   * Is the {@linkplain #segments segment} at the same index an argument reference?
   */
  protected final boolean[] variableSegments;

  /**
   * Construct a new <code>TemplateString</code>.
   * 
   * @param template The String to parse as a template.
   */
  public TemplateString(final String template) {
    this.template = Objects.requireNonNull(template);

    final Matcher matcher = VARIABLE_ESCAPE_PATTERN.matcher(template);
    final ArrayList<String> segments = new ArrayList<String>();
    final ArrayList<Boolean> variableSegments = new ArrayList<Boolean>();
    int ti = 0;
    while (matcher.find()) {
      if (matcher.start() > ti) {
        segments.add(template.substring(ti, matcher.start()));
        variableSegments.add(Boolean.FALSE);
      }
      segments.add(Objects.requireNonNull(matcher.group(1)));
      variableSegments.add(Boolean.TRUE);
      ti = matcher.end();
    }
    if (ti < template.length()) {
      segments.add(template.substring(ti, template.length()));
      variableSegments.add(Boolean.FALSE);
    }
    this.segments = segments.toArray(new String[segments.size()]);
    this.variableSegments = new boolean[variableSegments.size()];
    for (int si = 0; si < this.variableSegments.length; si++) {
      this.variableSegments[si] = variableSegments.get(si).booleanValue();
    }

    return;
  }

  @Override
  public char charAt(final int index) {
    return template.charAt(index);
  }

  @Override
  public int length() {
    return template.length();
  }

  @Override
  public CharSequence subSequence(final int start, final int end) throws IndexOutOfBoundsException {
    return template.subSequence(start, end);
  }

  /**
   * Format this template into an output String by using the supplied <code>arguments</code> to resolve any variable
   * references.
   * 
   * @param arguments A Map used to resolve any variable references.
   * @param defaultValue If a variable reference has no value in the <code>arguments</code> Map, this value will be
   * used. If <code>null</code>, the String <code>&quot;null&quot;</code> will be used.
   * @return The final formatted String with variables resolved.
   */
  public String format(final @Nullable Map<String,String> arguments, final @Nullable String defaultValue) {
    final StringBuilder sb = new StringBuilder();
    for (int si = 0; si < segments.length; si++) {
      if (variableSegments[si]) {
        final String value = (arguments != null) ? arguments.get(segments[si]) : null;
        sb.append((value != null) ? value : defaultValue);
      } else {
        sb.append(segments[si]);
      }
    }
    return sb.toString();
  }

  /**
   * Format this template into an output String by using the supplied <code>arguments</code> to resolve any variable
   * references.
   * 
   * @param arguments A Map used to resolve any variable references.
   * @return The final formatted String with variables resolved.
   * @throws MissingFormatArgumentException If a variable reference has no value in the <code>arguments</code> Map.
   */
  public String format(final @Nullable Map<String,String> arguments) throws MissingFormatArgumentException {
    final StringBuilder sb = new StringBuilder();
    for (int si = 0; si < segments.length; si++) {
      if (variableSegments[si]) {
        if ((arguments == null) || (!arguments.containsKey(segments[si]))) throw new MissingFormatArgumentException("No argument supplied for '" + segments[si] + "' variable reference");
        sb.append(arguments.get(segments[si]));
      } else {
        sb.append(segments[si]);
      }
    }
    return sb.toString();
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof TemplateString)) return false;
    return template.equals(((TemplateString)obj).template);
  }

  @Override
  public int hashCode() {
    return template.hashCode();
  }

  @Override
  public String toString() {
    return template.toString();
  }

}
