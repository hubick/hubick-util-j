/*
 * Copyright 2015-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.dom;

import java.util.*;
import java.util.stream.*;

import org.w3c.dom.*;

import org.junit.jupiter.api.*;

import org.eclipse.jdt.annotation.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * JUnit tests for {@link DOMUtil}.
 */
@NonNullByDefault
public class DOMUtilTest {

  /**
   * Test util methods returning a {@link Stream}.
   */
  @Test
  public void testStream() {
    final Document testDocument = DOMUtil.newDocument();
    final Element testElement = DOMUtil.createElement(null, null, "test", testDocument);
    DOMUtil.createElement(null, null, "e1", testElement);
    DOMUtil.createElement(null, null, "e2", testElement);
    DOMUtil.createElement(null, null, "e3", testElement);
    DOMUtil.createElement(null, null, "e4", testElement);
    DOMUtil.createElement(null, null, "e5", testElement);
    DOMUtil.createElement(null, null, "e6", testElement);
    final Iterator<String> i = DOMUtil.getChildElements(testElement).<@NonNull String> map((e) -> Objects.requireNonNull(e.getLocalName())).iterator();
    assertEquals("e1", i.next());
    assertEquals("e2", i.next());
    assertEquals("e3", i.next());
    assertEquals("e4", i.next());
    assertEquals("e5", i.next());
    assertEquals("e6", i.next());
    return;
  }

}
