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
 * A {@link Map} implementation backed by {@link ServletConfig} {@linkplain ServletConfig#getInitParameterNames() init
 * params}.
 */
@NonNullByDefault
public class ServletConfigInitParameterMap extends AbstractServletMap<String> {
  /**
   * The {@link ServletConfig} from which {@linkplain ServletConfig#getInitParameterNames() init params} will be
   * retrieved.
   */
  protected final ServletConfig servletConfig;

  /**
   * Construct a new <code>ServletConfigInitParameterMap</code> using the given <code>servletConfig</code>.
   * 
   * @param servletConfig The {@link ServletConfig} from which {@linkplain ServletConfig#getInitParameterNames() init
   * params} will be retrieved.
   */
  public ServletConfigInitParameterMap(final ServletConfig servletConfig) {
    this.servletConfig = servletConfig;
    return;
  }

  @Override
  protected Object getLock(final boolean readOnly) {
    return servletConfig;
  }

  @Override
  protected Enumeration<String> getServletMapKeyNames() throws IllegalStateException {
    return servletConfig.getInitParameterNames();
  }

  @Override
  protected @Nullable String getServletMapValue(final String name) throws IllegalStateException {
    return servletConfig.getInitParameter(name);
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
   * Get a Map containing <em>all</em> configuration parameters applicable to the given servlet.
   * 
   * @param servletConfig The {@link ServletConfig} for the servlet.
   * @return A Map of parameters.
   */
  public static final Map<String,String> getConfigParams(final ServletConfig servletConfig) {
    final Map<String,String> systemParams = CollUtil.toMap(System.getProperties());
    final Map<String,String> contextParams = new ServletContextInitParameterMap(servletConfig.getServletContext());
    final Map<String,String> servletParams = new ServletConfigInitParameterMap(servletConfig);
    return CollUtil.chain(Arrays.asList(systemParams, contextParams, servletParams));
  }

}
