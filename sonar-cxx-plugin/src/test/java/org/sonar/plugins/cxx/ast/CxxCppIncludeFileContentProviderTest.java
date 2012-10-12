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

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.junit.Test;
import org.sonar.plugins.cxx.TestUtils;

public class CxxCppIncludeFileContentProviderTest {

  private static final String CLASS_TEST_H_PATH = "/org/sonar/plugins/cxx/ast/ClassTest.h";
  
  @Test
  public void getContentForAbsoluteFilePathTest() throws URISyntaxException {
    File classTestHeaderFile = TestUtils.loadResource(CLASS_TEST_H_PATH);
    
    CxxCppIncludeFileContentProvider includeProvider = new CxxCppIncludeFileContentProvider();
    InternalFileContent fileContent = includeProvider.getContentForInclusion(classTestHeaderFile.getAbsolutePath());
    
    assertTrue(fileContent != null);
    assertTrue(fileContent.getSource().getLength() > 0);
    assertEquals(classTestHeaderFile.getAbsolutePath(), fileContent.getFileLocation());
  }
    
}
