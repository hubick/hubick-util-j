/*
 * Copyright 2014-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.core.collection;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.junit.jupiter.api.*;

import com.hubick.util.core.io.*;

import org.eclipse.jdt.annotation.*;


/**
 * JUnit tests for {@link CollUtil}.
 */
@NonNullByDefault
public class CollUtilTest {

  /**
   * Test {@linkplain Serializable serialization} of results.
   * 
   * @throws Exception If something went wrong.
   */
  @Test
  public void testSerialization() throws Exception {
    @SuppressWarnings("unchecked")
    final Function<@Nullable Integer,String> readString = (Function<@Nullable Integer,String> & Serializable)(i) -> String.valueOf(i);
    @SuppressWarnings("unchecked")
    final Function<@Nullable String,Integer> writeInteger = (Function<@Nullable String,Integer> & Serializable)(s) -> (s != null) ? Integer.valueOf(s) : 0;
    final ObjectOutputStream oos = new ObjectOutputStream(IOUtil.NULL_OUTPUT_STREAM);
    oos.writeObject(CollUtil.chain(Arrays.asList(new CopyOnWriteArrayMap<String,String>(), new CopyOnWriteArrayMap<String,String>())));
    oos.writeObject(CollUtil.toMap(System.getProperties()));
    oos.writeObject(CollUtil.mapCollection(new ArrayList<Integer>(), readString, writeInteger));
    oos.writeObject(CollUtil.mapList(new ArrayList<Integer>(), readString, writeInteger));
    oos.writeObject(CollUtil.mapSet(new HashSet<Integer>(), readString, writeInteger));
    oos.writeObject(CollUtil.mapEntry(new AbstractMap.SimpleImmutableEntry<Integer,Integer>(1, 1), readString, readString, writeInteger));
    oos.writeObject(CollUtil.mapMap(new CopyOnWriteArrayMap<Integer,Integer>(), readString, writeInteger, readString, writeInteger));
    return;
  }

}
