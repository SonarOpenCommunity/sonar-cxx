/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.ast.visitors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.InputFile;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.ast.CxxCppParser;
import org.sonar.plugins.cxx.ast.CxxCppParserException;

public class CxxXmlOutputVisitorTest {

  private static final String TEST_FILE = "/org/sonar/plugins/cxx/ast/ClassTest.cpp";
  private CxxCppParser parser;
  private InputFile inputFile;
  
  @Before
  public void setup() throws URISyntaxException {
    parser = new CxxCppParser();
    inputFile = mock(InputFile.class);
    when(inputFile.getFile()).thenReturn( TestUtils.loadResource(TEST_FILE) );
  }
  
  @Test
  public void produceXmlOutputTest() throws CxxCppParserException, IOException {
    File outputFile = File.createTempFile("ClassTest", ".xml");
    outputFile.deleteOnExit();
    
    CxxXmlOutputVisitor visitor = new CxxXmlOutputVisitor(outputFile);    
    parser.parseFile(inputFile).getAst().accept(visitor);
    
    assertTrue(outputFile.length() > 0);
  }
  
  
}
