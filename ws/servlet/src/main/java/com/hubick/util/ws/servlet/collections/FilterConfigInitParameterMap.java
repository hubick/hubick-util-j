/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.ws.servlet.collections;

import java.util.*;

import org.eclipse.jdt.annotation.*;

import com.hubick.util.core.collection.*;

import javax.servlet.*;


/**
 * A {@link Map} implementation backed by {@link FilterConfig} {@linkplain FilterConfig#getInitParameterNames() init
 * params}.
 */
@NonNullByDefault
public class FilterConfigInitParameterMap extends AbstractServletMap<String> {
  /**
   * The {@link FilterConfig} from which {@linkplain FilterConfig#getInitParameterNames() init params} will be
   * retrieved.
   */
  protected final FilterConfig filterConfig;

  /**
   * Construct a new <code>FilterConfigInitParameterMap</code> using the given <code>filterConfig</code>.
   * 
   * @param filterConfig The {@link FilterConfig} from which {@linkplain FilterConfig#getInitParameterNames() init
   * params} will be retrieved.
   */
  public FilterConfigInitParameterMap(final FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
    return;
  }

  @Override
  protected Object getLock(final boolean readOnly) {
    return filterConfig;
  }

  @Override
  protected Enumeration<String> getServletMapKeyNames() throws IllegalStateException {
    return filterConfig.getInitParameterNames();
  }

  @Override
  protected @Nullable String getServletMapValue(final String name) throws IllegalStateException {
    return filterConfig.getInitParameter(name);
  }

  @Override
  protected void removeServletMapValue(final String name) throws UnsupportedOperationException, IllegalStateException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void setServletMapValue(final String name, final @Nullable String o) throws UnsupportedOperationException, IllegalStateException {
    throw new UnsupportedOperationException();
  }

  /**
   * Get a Map containing <em>all</em> configuration parameters applicable to the given filter.
   * 
   * @param filterConfig The {@link FilterConfig} for the filter.
   * @return A Map of parameters.
   */
  public static final Map<String,String> getConfigParams(final FilterConfig filterConfig) {
    final Map<String,String> systemParams = CollUtil.toMap(System.getProperties());
    final Map<String,String> contextParams = new ServletContextInitParameterMap(filterConfig.getServletContext());
    final Map<String,String> filterParams = new FilterConfigInitParameterMap(filterConfig);
    return CollUtil.chain(Arrays.asList(systemParams, contextParams, filterParams));
  }

}
