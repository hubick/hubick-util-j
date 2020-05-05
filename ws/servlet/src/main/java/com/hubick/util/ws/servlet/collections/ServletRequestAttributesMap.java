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

import javax.servlet.*;


/**
 * A {@link Map} implementation backed by {@link ServletRequest} {@linkplain ServletRequest#getAttributeNames()
 * attributes}.
 */
@NonNullByDefault
public class ServletRequestAttributesMap extends AbstractServletMap<Object> {
  /**
   * The {@link ServletRequest} from which {@linkplain ServletRequest#getAttributeNames() attributes} will be retrieved.
   */
  protected final ServletRequest servletRequest;

  /**
   * Construct a new <code>ServletRequestAttributesMap</code> using the given <code>servletRequest</code>.
   * 
   * @param servletRequest The {@link ServletRequest} from which {@linkplain ServletRequest#getAttributeNames()
   * attributes} will be retrieved.
   * @throws IllegalArgumentException If <code>servletRequest</code> is <code>null</code>.
   */
  public ServletRequestAttributesMap(final ServletRequest servletRequest) throws IllegalArgumentException {
    this.servletRequest = servletRequest;
    return;
  }

  @Override
  protected Object getLock(final boolean readOnly) {
    return servletRequest;
  }

  @Override
  protected Enumeration<String> getServletMapKeyNames() throws IllegalStateException {
    return servletRequest.getAttributeNames();
  }

  @Override
  protected @Nullable Object getServletMapValue(final String name) throws IllegalStateException {
    return servletRequest.getAttribute(name);
  }

  @Override
  protected void setServletMapValue(final String name, final @Nullable Object o) throws UnsupportedOperationException, IllegalStateException {
    servletRequest.setAttribute(name, o);
    return;
  }

  @Override
  protected void removeServletMapValue(final String name) throws UnsupportedOperationException, IllegalStateException {
    servletRequest.removeAttribute(name);
    return;
  }

}
