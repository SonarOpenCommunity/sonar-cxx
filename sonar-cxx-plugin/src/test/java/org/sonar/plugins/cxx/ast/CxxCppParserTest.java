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
package org.sonar.plugins.cxx.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.InputFile;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxCppParserTest {

  private static final int CLASS_TEST_CPP_STATEMENTS_COUNT = 5;
  private static final int CLASS_TEST_CPP_DECL_COUNT = 7;
  private static final int CLASS_TEST_CPP_COMMENTS_COUNT = 2;
  private static final String CLASS_TEST_CPP_PATH = "/org/sonar/plugins/cxx/ast/ClassTest.cpp";

  private InputFile classTestCpp;

  @Before
  public void setup() {
    classTestCpp = mock(InputFile.class);
    when(classTestCpp.getFile()).thenReturn( TestUtils.loadResource(CLASS_TEST_CPP_PATH) );
  }

  @Test (expected = CxxCppParserException.class)
  public void parseNullFileTest() throws CxxCppParserException {
    CxxCppParser parser = new CxxCppParser();
    parser.parseFile(null);
  }

  @Test (expected = CxxCppParserException.class)
  public void parseNotExistingFileTest() throws CxxCppParserException {
    InputFile nonExistingFile = mock(InputFile.class);
    when(nonExistingFile.getFile()).thenReturn( new File("nonExistant") );

    CxxCppParser parser = new CxxCppParser();
    parser.parseFile(nonExistingFile);
  }

  @Test
  public void parseExistingFileTest() throws CxxCppParserException, IOException {
    String fileCode = FileUtils.readFileToString(classTestCpp.getFile());

    CxxCppParser parser = new CxxCppParser();
    CxxCppParsedFile parsedFile = parser.parseFile(classTestCpp);

    assertTrue(parsedFile != null);
    assertTrue(parsedFile.getAst() != null);
    assertEquals(fileCode, parsedFile.getFileContent());
    assertEquals(classTestCpp, parsedFile.getInputFile());
  }

  @Test
  public void parseClassTestCppFileTest() throws CxxCppParserException, ExpansionOverlapsBoundaryException {
    CxxCppParser parser = new CxxCppParser();
    CxxCppParsedFile parsedFile = parser.parseFile(classTestCpp);    
    
    IASTTranslationUnit ast = parsedFile.getAst();
    assertTrue(ast != null);
    
    assertEquals(CLASS_TEST_CPP_COMMENTS_COUNT, ast.getComments().length);
    assertEquals(CLASS_TEST_CPP_DECL_COUNT, ast.getDeclarations().length);
    assertEquals(CLASS_TEST_CPP_STATEMENTS_COUNT, ast.getAllPreprocessorStatements().length);    
  }
  
}
