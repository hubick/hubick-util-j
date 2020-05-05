/*
 * Copyright 2014-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.net;

import java.util.*;

import org.junit.jupiter.api.*;

import org.eclipse.jdt.annotation.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * JUnit tests for {@link NetUtil}.
 */
@NonNullByDefault
public class NetUtilTest {

  /**
   * Test round-tripping a set of query parameters through {@link NetUtil#parseQueryParams(String)} and
   * {@link NetUtil#urlEncode(Map)}.
   */
  @Test
  public void testQueryParamsRoundTrip() {
    final String roundTripString = "Param1Key=Param1Value&Param2Key=Param2Value1&Param2Key=Param2Value2&Param3Key=&Param4Key=Param4Value";
    assertEquals(roundTripString, NetUtil.urlEncode(NetUtil.parseQueryParams(roundTripString)));
    return;
  }

  /**
   * Test {@link NetUtil#parseQueryParams(String)} builds an ordered Map.
   */
  @Test
  public void testQueryParamsReordering() {
    assertEquals("Param1Key=Param1Value&Param2Key=Param2Value", NetUtil.urlEncode(NetUtil.parseQueryParams("Param2Key=Param2Value&Param1Key=Param1Value")));
    return;
  }

  /**
   * Test {@link NetUtil#parseQueryParams(String)} error recovery.
   */
  @Test
  public void testQueryParamsErrorRecovery() {
    assertEquals("Param1Key=&Param2Key=Param2Value", NetUtil.urlEncode(NetUtil.parseQueryParams("Param1Key&Param2Key=Param2Value")));
    return;
  }

  /**
   * Test {@link NetUtil#urlEncode(Map)} <code>null</code> handling.
   */
  @Test
  public void testURLEncodeNullHandling() {
    final Map<String,@Nullable String> queryParams = new HashMap<String,@Nullable String>();
    queryParams.put("Param1Key", null);
    queryParams.put("Param2Key", "Param2Value");
    assertEquals("Param1Key=&Param2Key=Param2Value", NetUtil.urlEncode(queryParams));
    return;
  }

}
