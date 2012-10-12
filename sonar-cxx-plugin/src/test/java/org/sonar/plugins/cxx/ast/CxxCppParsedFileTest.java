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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.InputFile;

public class CxxCppParsedFileTest {

  private InputFile inputFile;
  private IASTTranslationUnit ast;
  
  private static String DEFAULT_FILE_CONTENT = "<Sample c++ code>";
  
  @Before
  public void setup() {
    inputFile = mock(InputFile.class);
    ast = mock(IASTTranslationUnit.class);
  }
  
  @Test
  public void getASTTest() {
    CxxCppParsedFile parsedFile = new CxxCppParsedFile(inputFile, ast, DEFAULT_FILE_CONTENT);
    assertEquals(ast, parsedFile.getAst());
  }

  @Test
  public void getInputFileTest() {
    CxxCppParsedFile parsedFile = new CxxCppParsedFile(inputFile, ast, DEFAULT_FILE_CONTENT);
    assertEquals(inputFile, parsedFile.getInputFile());
  }
  
  @Test
  public void getFileContentTest() {
    CxxCppParsedFile parsedFile = new CxxCppParsedFile(inputFile, ast, DEFAULT_FILE_CONTENT);
    assertEquals(DEFAULT_FILE_CONTENT, parsedFile.getFileContent());
  }
  
}
