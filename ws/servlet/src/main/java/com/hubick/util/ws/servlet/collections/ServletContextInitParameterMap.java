/*
 * Copyright 2012-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.ws.servlet.collections;

import java.util.*;

import org.eclipse.jdt.annotation.*;

import javax.servlet.*;


/**
 * A {@link Map} implementation backed by {@link ServletContext} {@linkplain ServletContext#getInitParameterNames() init
 * params}.
 */
@NonNullByDefault
public class ServletContextInitParameterMap extends AbstractServletMap<String> {
  /**
   * The {@link ServletContext} from which {@linkplain ServletContext#getInitParameterNames() init params} will be
   * retrieved.
   */
  protected final ServletContext servletContext;

  /**
   * Construct a new <code>ServletContextInitParameterMap</code> using the given <code>servletConfig</code>.
   * 
   * @param servletContext The {@link ServletContext} from which {@linkplain ServletContext#getInitParameterNames() init
   * params} will be retrieved.
   * @throws IllegalArgumentException If <code>servletContext</code> is <code>null</code>.
   */
  public ServletContextInitParameterMap(final ServletContext servletContext) throws IllegalArgumentException {
    this.servletContext = servletContext;
    return;
  }

  @Override
  protected Object getLock(final boolean readOnly) {
    return servletContext;
  }

  @Override
  protected Enumeration<String> getServletMapKeyNames() throws IllegalStateException {
    return servletContext.getInitParameterNames();
  }

  @Override
  protected @Nullable String getServletMapValue(final String name) throws IllegalStateException {
    return servletContext.getInitParameter(name);
  }

  @Override
  protected void removeServletMapValue(final String name) throws UnsupportedOperationException, IllegalStateException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void setServletMapValue(final String name, final @Nullable String o) throws UnsupportedOperationException, IllegalStateException {
    throw new UnsupportedOperationException();
  }

}
