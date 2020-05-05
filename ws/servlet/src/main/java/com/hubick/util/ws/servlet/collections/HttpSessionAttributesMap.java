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

import javax.servlet.http.*;


/**
 * A {@linkplain Map} implementation backed by {@linkplain HttpServletRequest#getSession(boolean) session}
 * {@linkplain HttpSession#getAttributeNames() attributes}.
 */
@NonNullByDefault
public class HttpSessionAttributesMap extends AbstractServletMap<Object> {
  /**
   * The {@link HttpServletRequest} from which the {@linkplain HttpServletRequest#getSession(boolean) session} will be
   * retrieved.
   */
  protected final HttpServletRequest servletRequest;

  /**
   * Construct a new <code>HttpSessionAttributesMap</code> using the given <code>servletRequest</code>.
   * 
   * @param servletRequest The {@link HttpServletRequest} from which the
   * {@linkplain HttpServletRequest#getSession(boolean) session} will be retrieved. A session will not be created unless
   * an attribute is set.
   * @throws IllegalArgumentException If <code>servletRequest</code> is <code>null</code>.
   */
  public HttpSessionAttributesMap(final HttpServletRequest servletRequest) throws IllegalArgumentException {
    this.servletRequest = servletRequest;
    return;
  }

  /**
   * {@linkplain HttpServletRequest#getSession(boolean) Get} the session, locking on the {@link #servletRequest} while
   * doing so.
   * 
   * @return The current session, or <code>null</code> if there isn't one.
   */
  protected @Nullable HttpSession getSession() {
    synchronized (servletRequest) {
      return servletRequest.getSession(false);
    }
  }

  /**
   * {@linkplain HttpServletRequest#getSession(boolean) Get} the session, locking on the {@link #servletRequest} while
   * doing so.
   * 
   * @return The current session.
   */
  protected HttpSession createSession() {
    synchronized (servletRequest) {
      return servletRequest.getSession();
    }
  }

  @Override
  protected Object getLock(final boolean readOnly) {
    final HttpSession httpSession = readOnly ? getSession() : createSession();
    return (httpSession != null) ? httpSession : servletRequest;
  }

  @Override
  protected Enumeration<String> getServletMapKeyNames() throws IllegalStateException {
    final HttpSession httpSession = getSession();
    return (httpSession != null) ? httpSession.getAttributeNames() : Collections.emptyEnumeration();
  }

  @Override
  protected @Nullable Object getServletMapValue(final String name) throws IllegalStateException {
    final HttpSession httpSession = getSession();
    return (httpSession != null) ? httpSession.getAttribute(name) : null;
  }

  @Override
  protected void setServletMapValue(final String name, final @Nullable Object o) throws UnsupportedOperationException, IllegalStateException {
    createSession().setAttribute(name, o);
    return;
  }

  @Override
  protected void removeServletMapValue(final String name) throws UnsupportedOperationException, IllegalStateException {
    final HttpSession httpSession = getSession();
    if (httpSession != null) httpSession.removeAttribute(name);
    return;
  }

}
