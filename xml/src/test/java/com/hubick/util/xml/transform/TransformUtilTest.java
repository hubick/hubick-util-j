/*
 * Copyright 2018-2020 by Chris Hubick. All Rights Reserved.
 * 
 * This work is licensed under the terms of the "GNU AFFERO GENERAL PUBLIC LICENSE" version 3, as published by the Free
 * Software Foundation <http://www.gnu.org/licenses/>, plus additional permissions, a copy of which you should have
 * received in the file LICENSE.txt.
 */

package com.hubick.util.xml.transform;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.junit.jupiter.api.*;

import org.eclipse.jdt.annotation.*;

import static org.junit.jupiter.api.Assertions.*;


/**
 * JUnit tests for {@link TransformUtil}.
 */
@NonNullByDefault
public class TransformUtilTest {

  /**
   * Test {@link TransformUtil#parseTemplates(URL)}.
   * 
   * @throws Exception If there was a problem parsing the templates.
   */
  @Test
  public void testParseTemplates() throws Exception {
    final URL templatesURL = Objects.requireNonNull(TransformUtilTest.class.getResource("TestTemplates.xsl"), "TestTemplates.xsl not found");
    final Templates testTemplates = TransformUtil.parseTemplates(templatesURL);
    final StringWriter stringWriter = new StringWriter();
    testTemplates.newTransformer().transform(new StreamSource(new StringReader("<TestSourceElement>Hello World!</TestSourceElement>")), new StreamResult(stringWriter));
    assertEquals("<TestResultElement>Hello World!</TestResultElement>", stringWriter.toString());
    return;
  }

}
