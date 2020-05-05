/*
 * Copyright 2007-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.ws.rs;

import java.io.*;
import java.net.*;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import javax.activation.*;

import org.eclipse.jdt.annotation.*;

import javax.ws.rs.core.*;


/**
 * JAX-RS Utilities.
 */
@NonNullByDefault
public abstract class RESTUtil {

  /**
   * Evaluate request preconditions based on the passed in value.
   * 
   * @param request The {@link Request} to evaluate against.
   * @param lastModified The date the resource was last modified.
   * @param eTag The current ETag for the resource.
   * @return <code>null</code> if the preconditions are met or a ResponseBuilder if the preconditions are not met.
   * @see Request#evaluatePreconditions(Date, EntityTag)
   */
  public static final javax.ws.rs.core.Response.@Nullable ResponseBuilder evaluatePreconditions(final Request request, final @Nullable Instant lastModified, final @Nullable EntityTag eTag) {
    if ((lastModified != null) && (eTag != null)) {
      return request.evaluatePreconditions(Date.from(lastModified), eTag);
    } else if (lastModified != null) {
      return request.evaluatePreconditions(Date.from(lastModified));
    } else if (eTag != null) {
      return request.evaluatePreconditions(eTag);
    }
    return null;
  }

  /**
   * Create a duplicate of a {@link CacheControl} instance.
   * 
   * @param cacheControl The {@link CacheControl} instance to duplicate.
   * @return The duplicate {@link CacheControl}.
   */
  public static final CacheControl copy(final CacheControl cacheControl) {
    final CacheControl result = new CacheControl();
    result.setPrivate(cacheControl.isPrivate());
    result.getPrivateFields().addAll(cacheControl.getPrivateFields());
    result.setNoCache(cacheControl.isNoCache());
    result.getNoCacheFields().addAll(cacheControl.getNoCacheFields());
    result.setNoStore(cacheControl.isNoStore());
    result.setNoTransform(cacheControl.isNoTransform());
    result.setMustRevalidate(cacheControl.isMustRevalidate());
    result.setProxyRevalidate(cacheControl.isProxyRevalidate());
    result.setMaxAge(cacheControl.getMaxAge());
    result.setSMaxAge(cacheControl.getSMaxAge());
    result.getCacheExtension().putAll(cacheControl.getCacheExtension());
    return result;
  }

  /**
   * Return <code>null</code> if the input {@link CacheControl} contains no values.
   * 
   * @param cacheControl The {@link CacheControl} to check for values.
   * @return The supplied <code>cacheControl</code> argument, or <code>null</code> if it contained no values.
   */
  public static final @Nullable CacheControl mkNull(final @Nullable CacheControl cacheControl) {
    return ((cacheControl == null) || (cacheControl.isPrivate()) || (cacheControl.isNoCache()) || (cacheControl.isNoStore()) || (cacheControl.isNoTransform()) || (cacheControl.isMustRevalidate()) || (cacheControl.isProxyRevalidate()) || (cacheControl.getMaxAge() >= 0) || (cacheControl.getSMaxAge() >= 0) || (!cacheControl.getCacheExtension().isEmpty())) ? cacheControl : null;
  }

  /**
   * Get a {@link CacheControl} initialized to be empty.
   * 
   * @return The empty {@link CacheControl}.
   */
  public static final CacheControl emptyCacheControl() {
    final CacheControl result = new CacheControl();
    result.setNoTransform(false);
    return result;
  }

  /**
   * Return a {@link MediaType} corresponding to the supplied {@link MimeType}.
   * 
   * @param mimeType The {@link MimeType} to {@link MediaType#valueOf(String) convert} into a {@link MediaType}.
   * @return The corresponding {@link MimeType}, or <code>null</code> if the given <code>mimeType</code> was
   * <code>null</code>.
   */
  public static final MediaType getMediaType(final MimeType mimeType) {
    return MediaType.valueOf(mimeType.toString());
  }

  public static final <K,V> MultivaluedMap<K,V> synchronizedMultivaluedMap(final MultivaluedMap<K,V> multivaluedMap) {
    class SynchronizedMultivaluedMap extends AbstractMap<K,List<V>> implements MultivaluedMap<K,V>, Serializable {

      @Override
      public int size() {
        synchronized (multivaluedMap) {
          return multivaluedMap.size();
        }
      }

      @Override
      public boolean isEmpty() {
        synchronized (multivaluedMap) {
          return multivaluedMap.isEmpty();
        }
      }

      @Override
      public boolean containsKey(final @Nullable Object key) {
        synchronized (multivaluedMap) {
          return multivaluedMap.containsKey(key);
        }
      }

      @Override
      public boolean containsValue(final @Nullable Object value) {
        synchronized (multivaluedMap) {
          return multivaluedMap.containsValue(value);
        }
      }

      @Override
      public @Nullable List<V> get(final @Nullable Object key) {
        synchronized (multivaluedMap) {
          final List<V> value = multivaluedMap.get(key);
          return (value != null) ? Collections.unmodifiableList(new CopyOnWriteArrayList<V>(value)) : null;
        }
      }

      @Override
      public @Nullable List<V> put(final K key, final @Nullable List<V> value) {
        final List<V> oldValue;
        synchronized (multivaluedMap) {
          oldValue = multivaluedMap.put(key, value);
        }
        return (oldValue != null) ? Collections.unmodifiableList(oldValue) : null;
      }

      @Override
      public @Nullable List<V> remove(final @Nullable Object key) {
        final List<V> oldValue;
        synchronized (multivaluedMap) {
          oldValue = multivaluedMap.remove(key);
        }
        return (oldValue != null) ? Collections.unmodifiableList(oldValue) : null;
      }

      @Override
      public void putAll(final Map<? extends K,? extends @Nullable List<V>> m) {
        synchronized (multivaluedMap) {
          multivaluedMap.putAll(m);
        }
        return;
      }

      @Override
      public void clear() {
        synchronized (multivaluedMap) {
          multivaluedMap.clear();
        }
        return;
      }

      @Override
      public Set<K> keySet() {
        synchronized (multivaluedMap) {
          final Set<K> copy = new CopyOnWriteArraySet<K>(multivaluedMap.keySet());
          return Collections.unmodifiableSet(copy);
        }
      }

      @Override
      public Set<Entry<K,List<V>>> entrySet() {
        final Map<K,List<V>> copy = new ConcurrentHashMap<K,List<V>>(multivaluedMap);
        return Collections.unmodifiableSet(copy.entrySet());
      }

      @Override
      public void putSingle(final K key, final V value) {
        synchronized (multivaluedMap) {
          multivaluedMap.putSingle(key, value);
        }
        return;
      }

      @Override
      public void add(final K key, final V value) {
        synchronized (multivaluedMap) {
          multivaluedMap.add(key, value);
        }
        return;
      }

      @Override
      public @Nullable V getFirst(final K key) {
        synchronized (multivaluedMap) {
          return multivaluedMap.getFirst(key);
        }
      }

      @Override
      public void addAll(final K key, @SuppressWarnings("unchecked") final V... newValues) {
        synchronized (multivaluedMap) {
          multivaluedMap.addAll(key, newValues);
        }
        return;
      }

      @Override
      public void addAll(final K key, final List<V> valueList) {
        synchronized (multivaluedMap) {
          multivaluedMap.addAll(key, valueList);
        }
        return;
      }

      @Override
      public void addFirst(final K key, final V value) {
        synchronized (multivaluedMap) {
          multivaluedMap.addFirst(key, value);
        }
        return;
      }

      @Override
      public boolean equalsIgnoreValueOrder(final @Nullable MultivaluedMap<K,V> otherMap) {
        synchronized (multivaluedMap) {
          return multivaluedMap.equalsIgnoreValueOrder(otherMap);
        }
      }

      @Override
      public String toString() {
        synchronized (multivaluedMap) {
          return multivaluedMap.toString();
        }
      }

    };
    return new SynchronizedMultivaluedMap();
  }

  public static final HttpHeaders synchronizedHttpHeaders(final HttpHeaders httpHeaders) {
    class SynchronizedHttpHeaders implements HttpHeaders, Serializable {

      @Override
      public @Nullable List<String> getRequestHeader(final String name) {
        synchronized (httpHeaders) {
          final List<String> requestHeader = httpHeaders.getRequestHeader(name);
          return (requestHeader != null) ? Collections.unmodifiableList(new CopyOnWriteArrayList<String>(requestHeader)) : null;
        }
      }

      @Override
      public @Nullable String getHeaderString(final String name) {
        synchronized (httpHeaders) {
          return httpHeaders.getHeaderString(name);
        }
      }

      @Override
      public MultivaluedMap<String,String> getRequestHeaders() {
        synchronized (httpHeaders) {
          return synchronizedMultivaluedMap(httpHeaders.getRequestHeaders());
        }
      }

      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        synchronized (httpHeaders) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<MediaType>(httpHeaders.getAcceptableMediaTypes()));
        }
      }

      @Override
      public List<Locale> getAcceptableLanguages() {
        synchronized (httpHeaders) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<Locale>(httpHeaders.getAcceptableLanguages()));
        }
      }

      @Override
      public @Nullable MediaType getMediaType() {
        synchronized (httpHeaders) {
          return httpHeaders.getMediaType();
        }
      }

      @Override
      public @Nullable Locale getLanguage() {
        synchronized (httpHeaders) {
          return httpHeaders.getLanguage();
        }
      }

      @Override
      public Map<String,Cookie> getCookies() {
        synchronized (httpHeaders) {
          return Collections.unmodifiableMap(new ConcurrentHashMap<String,Cookie>(httpHeaders.getCookies()));
        }
      }

      @Override
      public @Nullable Date getDate() {
        synchronized (httpHeaders) {
          return httpHeaders.getDate();
        }
      }

      @Override
      public int getLength() {
        synchronized (httpHeaders) {
          return httpHeaders.getLength();
        }
      }

      @Override
      public String toString() {
        synchronized (httpHeaders) {
          return httpHeaders.toString();
        }
      }

    };
    return new SynchronizedHttpHeaders();
  }

  public static final Request synchronizedRequest(final Request request) {
    class SynchronizedRequest implements Request, Serializable {

      @Override
      public javax.ws.rs.core.Response.@Nullable ResponseBuilder evaluatePreconditions() {
        synchronized (request) {
          return request.evaluatePreconditions();
        }
      }

      @Override
      public javax.ws.rs.core.Response.@Nullable ResponseBuilder evaluatePreconditions(final EntityTag eTag) {
        synchronized (request) {
          return request.evaluatePreconditions(eTag);
        }
      }

      @Override
      public javax.ws.rs.core.Response.@Nullable ResponseBuilder evaluatePreconditions(final Date lastModified) {
        synchronized (request) {
          return request.evaluatePreconditions(lastModified);
        }
      }

      @Override
      public javax.ws.rs.core.Response.@Nullable ResponseBuilder evaluatePreconditions(final Date lastModified, final EntityTag eTag) {
        synchronized (request) {
          return request.evaluatePreconditions(lastModified, eTag);
        }
      }

      @Override
      public String getMethod() {
        synchronized (request) {
          return request.getMethod();
        }
      }

      @Override
      public @Nullable Variant selectVariant(final List<Variant> variants) throws IllegalArgumentException {
        synchronized (request) {
          return request.selectVariant(variants);
        }
      }

      @Override
      public String toString() {
        synchronized (request) {
          return request.toString();
        }
      }

    };
    return new SynchronizedRequest();
  }

  public static final SecurityContext synchronizedSecurityContext(final SecurityContext securityContext) {
    class SynchronizedSecurityContext implements SecurityContext, Serializable {

      @Override
      public @Nullable String getAuthenticationScheme() {
        synchronized (securityContext) {
          return securityContext.getAuthenticationScheme();
        }
      }

      @Override
      public @Nullable Principal getUserPrincipal() {
        synchronized (securityContext) {
          return securityContext.getUserPrincipal();
        }
      }

      @Override
      public boolean isSecure() {
        synchronized (securityContext) {
          return securityContext.isSecure();
        }
      }

      @Override
      public boolean isUserInRole(final String role) {
        synchronized (securityContext) {
          return securityContext.isUserInRole(role);
        }
      }

      @Override
      public String toString() {
        synchronized (securityContext) {
          return securityContext.toString();
        }
      }

    };
    return new SynchronizedSecurityContext();
  }

  public static final UriInfo synchronizedUriInfo(final UriInfo uriInfo) {
    class SynchronizedUriInfo implements UriInfo, Serializable {

      @Override
      public URI getAbsolutePath() {
        synchronized (uriInfo) {
          return uriInfo.getAbsolutePath();
        }
      }

      @Override
      public UriBuilder getAbsolutePathBuilder() {
        synchronized (uriInfo) {
          return uriInfo.getAbsolutePathBuilder();
        }
      }

      @Override
      public URI getBaseUri() {
        synchronized (uriInfo) {
          return uriInfo.getBaseUri();
        }
      }

      @Override
      public UriBuilder getBaseUriBuilder() {
        synchronized (uriInfo) {
          return uriInfo.getBaseUriBuilder();
        }
      }

      @Override
      public List<Object> getMatchedResources() {
        synchronized (uriInfo) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<Object>(uriInfo.getMatchedResources()));
        }
      }

      @Override
      public List<String> getMatchedURIs() {
        synchronized (uriInfo) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<String>(uriInfo.getMatchedURIs()));
        }
      }

      @Override
      public List<String> getMatchedURIs(final boolean decode) {
        synchronized (uriInfo) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<String>(uriInfo.getMatchedURIs(decode)));
        }
      }

      @Override
      public String getPath() {
        synchronized (uriInfo) {
          return uriInfo.getPath();
        }
      }

      @Override
      public String getPath(final boolean decode) {
        synchronized (uriInfo) {
          return uriInfo.getPath(decode);
        }
      }

      @Override
      public MultivaluedMap<String,String> getPathParameters() {
        synchronized (uriInfo) {
          return synchronizedMultivaluedMap(uriInfo.getPathParameters());
        }
      }

      @Override
      public MultivaluedMap<String,String> getPathParameters(final boolean decode) {
        synchronized (uriInfo) {
          return synchronizedMultivaluedMap(uriInfo.getPathParameters(decode));
        }
      }

      @Override
      public List<PathSegment> getPathSegments() {
        synchronized (uriInfo) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<PathSegment>(uriInfo.getPathSegments()));
        }
      }

      @Override
      public List<PathSegment> getPathSegments(final boolean decode) {
        synchronized (uriInfo) {
          return Collections.unmodifiableList(new CopyOnWriteArrayList<PathSegment>(uriInfo.getPathSegments(decode)));
        }
      }

      @Override
      public MultivaluedMap<String,String> getQueryParameters() {
        synchronized (uriInfo) {
          return synchronizedMultivaluedMap(uriInfo.getQueryParameters());
        }
      }

      @Override
      public MultivaluedMap<String,String> getQueryParameters(final boolean decode) {
        synchronized (uriInfo) {
          return synchronizedMultivaluedMap(uriInfo.getQueryParameters(decode));
        }
      }

      @Override
      public URI getRequestUri() {
        synchronized (uriInfo) {
          return uriInfo.getRequestUri();
        }
      }

      @Override
      public UriBuilder getRequestUriBuilder() {
        synchronized (uriInfo) {
          return uriInfo.getRequestUriBuilder();
        }
      }

      @Override
      public String toString() {
        synchronized (uriInfo) {
          return uriInfo.toString();
        }
      }

      @Override
      public URI resolve(final URI uri) {
        synchronized (uriInfo) {
          return uriInfo.resolve(uri);
        }
      }

      @Override
      public URI relativize(final URI uri) {
        synchronized (uriInfo) {
          return uriInfo.relativize(uri);
        }
      }

    };
    return new SynchronizedUriInfo();
  }

  /**
   * Response Utilities.
   */
  public abstract static class Response {

    /**
     * An enumeration of the status codes missing from javax.ws.rs.core.Response.Status.
     */
    public enum Status implements javax.ws.rs.core.Response.StatusType {
      /**
       * 100 Continue, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1">HTTP/1.1
       * documentation</a>.
       */
      CONTINUE(100, "Continue", javax.ws.rs.core.Response.Status.Family.INFORMATIONAL),
      /**
       * 101 Switching Protocols, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.2">HTTP/1.1 documentation</a>.
       */
      SWITCHING_PROTOCOLS(101, "Switching Protocols", javax.ws.rs.core.Response.Status.Family.INFORMATIONAL),
      /**
       * 203 Non-Authoritative Information, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.4">HTTP/1.1 documentation</a>.
       */
      NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information", javax.ws.rs.core.Response.Status.Family.SUCCESSFUL),
      /**
       * 205 Reset Content, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.6">HTTP/1.1
       * documentation</a>.
       */
      RESET_CONTENT(205, "Reset Content", javax.ws.rs.core.Response.Status.Family.SUCCESSFUL),
      /**
       * 206 Partial Content, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.7">HTTP/1.1
       * documentation</a>.
       */
      PARTIAL_CONTENT(206, "Partial Content", javax.ws.rs.core.Response.Status.Family.SUCCESSFUL),
      /**
       * 300 Multiple Choices, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.1">HTTP/1.1
       * documentation</a>.
       */
      MULTIPLE_CHOICES(300, "Multiple Choices", javax.ws.rs.core.Response.Status.Family.REDIRECTION),
      /**
       * 302 Found, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">HTTP/1.1
       * documentation</a>.
       */
      FOUND(302, "Found", javax.ws.rs.core.Response.Status.Family.REDIRECTION),
      /**
       * 305 Use Proxy, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.6">HTTP/1.1
       * documentation</a>.
       */
      USE_PROXY(305, "Use Proxy", javax.ws.rs.core.Response.Status.Family.REDIRECTION),
      /**
       * 402 Payment Required, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.3">HTTP/1.1
       * documentation</a>.
       */
      PAYMENT_REQUIRED(402, "Payment Required", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 405 Method Not Allowed, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">HTTP/1.1
       * documentation</a>.
       */
      METHOD_NOT_ALLOWED(405, "Method Not Allowed", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 407 Proxy-Authentication Required, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.8">HTTP/1.1 documentation</a>.
       */
      PROXY_AUTHENTICATION_REQUIRED(407, "Proxy-Authentication Required", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 408 Request Timeout, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.9">HTTP/1.1
       * documentation</a>.
       */
      REQUEST_TIMEOUT(408, "Request Timeout", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 411 Length Required, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.12">HTTP/1.1
       * documentation</a>.
       */
      LENGTH_REQUIRED(411, "Length Required", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 413 Request Entity Too Large, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.14">HTTP/1.1 documentation</a>.
       */
      REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 414 Request URI Too Long, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.15">HTTP/1.1 documentation</a>.
       */
      REQUEST_URI_TOO_LONG(414, "Request URI Too Long", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 416 Requested Range Not Satisfiable, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.17">HTTP/1.1 documentation</a>.
       */
      REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 417 Expectation Failed, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.18">HTTP/1.1 documentation</a>.
       */
      EXPECTATION_FAILED(417, "Expectation Failed", javax.ws.rs.core.Response.Status.Family.CLIENT_ERROR),
      /**
       * 501 Not Implemented, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2">HTTP/1.1
       * documentation</a>.
       */
      NOT_IMPLEMENTED(501, "Not Implemented", javax.ws.rs.core.Response.Status.Family.SERVER_ERROR),
      /**
       * 502 Bad Gateway, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.3">HTTP/1.1
       * documentation</a>.
       */
      BAD_GATEWAY(502, "Bad Gateway", javax.ws.rs.core.Response.Status.Family.SERVER_ERROR),
      /**
       * 504 Gateway Timeout, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.5">HTTP/1.1
       * documentation</a>.
       */
      GATEWAY_TIMEOUT(504, "Gateway Timeout", javax.ws.rs.core.Response.Status.Family.SERVER_ERROR),
      /**
       * 505 HTTP Version Not Supported, see
       * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.6">HTTP/1.1 documentation</a>.
       */
      HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported", javax.ws.rs.core.Response.Status.Family.SERVER_ERROR);
      /**
       * @see #getStatusCode()
       */
      private final int code;
      /**
       * @see #getReasonPhrase()
       */
      private final String reason;
      /**
       * @see #getFamily()
       */
      private javax.ws.rs.core.Response.Status.Family family;

      /**
       * Construct a new <code>Status</code>.
       * 
       * @param status See {@link #getStatusCode()}.
       * @param reason See {@link #getReasonPhrase()}.
       * @param family See {@link #getFamily()}.
       */
      Status(final int status, final String reason, final javax.ws.rs.core.Response.Status.Family family) {
        this.code = status;
        this.reason = reason;
        this.family = family;
        return;
      }

      @Override
      public int getStatusCode() {
        return code;
      }

      @Override
      public String getReasonPhrase() {
        return reason;
      }

      @Override
      public javax.ws.rs.core.Response.Status.Family getFamily() {
        return family;
      }

      @Override
      public String toString() {
        return getReasonPhrase();
      }

      /**
       * Convert a numerical status code into the corresponding Status.
       * 
       * @param statusCode The numerical status code.
       * @return The matching Status or <code>null</code> is no matching Status is defined.
       * @see javax.ws.rs.core.Response.Status#fromStatusCode(int)
       */
      public static javax.ws.rs.core.Response.@Nullable StatusType fromStatusCode(final int statusCode) {
        final javax.ws.rs.core.Response.Status stockStatus = javax.ws.rs.core.Response.Status.fromStatusCode(statusCode);
        if (stockStatus != null) return stockStatus;
        for (Status s : Status.values()) {
          if (s.code == statusCode) {
            return s;
          }
        }
        return null;
      }

    } // Response.Status

  } // Response

}
