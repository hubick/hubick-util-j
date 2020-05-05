/*
 * Copyright 2015-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.collection;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;

import com.hubick.util.core.io.*;

import org.eclipse.jdt.annotation.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * JUnit tests for {@link CopyOnWriteArrayMap}.
 */
@NonNullByDefault
public class CopyOnWriteArrayMapTest {

  /**
   * {@linkplain Iterator Iteration} tests.
   */
  @Test
  public void testIteration() {
    final CopyOnWriteArrayMap<String,String> m = new CopyOnWriteArrayMap<String,String>();
    m.put("k1", "v1.0");
    m.put("k2", "v2.0");

    final Iterator<String> i1 = m.values().iterator();
    assertEquals("v1.0", i1.next());

    m.put("k2", "v2.1");

    assertEquals("v2.1", m.get("k2"));

    assertEquals("v2.0", i1.next());
    assertEquals(false, i1.hasNext());

    final Iterator<String> i2 = m.values().iterator();
    assertEquals("v1.0", i2.next());
    assertEquals("v2.1", i2.next());

    m.put("k3", "v3.0");

    assertEquals(false, i1.hasNext());
    assertEquals(false, i2.hasNext());

    final Iterator<Map.Entry<String,String>> i3 = m.entrySet().iterator();

    m.remove("k1");

    assertEquals("v1.0", i3.next().getValue());

    final Iterator<String> i4 = m.values().iterator();

    assertEquals("v2.1", i4.next());
    assertEquals("v3.0", i4.next());

    return;
  }

  /**
   * Test if the Map is {@linkplain Serializable}.
   * 
   * @throws Exception If something went wrong.
   */
  @Test
  public void testSerialization() throws Exception {
    final CopyOnWriteArrayMap<String,String> m = new CopyOnWriteArrayMap<String,String>();
    m.put("k1", "v1.0");
    new ObjectOutputStream(IOUtil.NULL_OUTPUT_STREAM).writeObject(m);
    return;
  }

}
