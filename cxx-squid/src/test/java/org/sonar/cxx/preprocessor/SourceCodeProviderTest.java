/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.preprocessor;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SourceCodeProviderTest {
  private SourceCodeProvider codeProvider = new SourceCodeProvider();
  private File expected = new File(new File("src/test/resources/codeprovider/source.hh").getAbsolutePath());
  
  @Test
  public void getting_code_with_abspath() {
    String abspath = expected.getAbsolutePath();
    assertEquals(expected, codeProvider.getSourceCodeFile(abspath, null));
  }

  @Test
  public void getting_code_with_filename_and_cwd() {
    File cwd = new File("src/test/resources/codeprovider");
    assertEquals(expected, codeProvider.getSourceCodeFile("source.hh", cwd.getAbsolutePath()));
  }

  @Test
  public void getting_code_with_relpath_and_cwd() {
    File cwd = new File("src/test/resources");
    assertEquals(expected, codeProvider.getSourceCodeFile("codeprovider/source.hh", cwd.getAbsolutePath()));
  }

  @Test
  public void getting_code_with_relpath_containing_backsteps_and_cwd() {
    String cwd = new File("src/test/resources/codeprovider/folder").getAbsolutePath();
    assertEquals(expected, codeProvider.getSourceCodeFile("../source.hh", cwd));
  }
  
  @Test
  public void getting_code_with_filename_and_absolute_code_location() {
    String cwd = new File("src/test/resources").getAbsolutePath();
    String baseDir = new File("src/test").getAbsolutePath();
    String file = "source.hh";
    String includeRoot = new File("src/test/resources/codeprovider").getAbsolutePath();
    
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected, codeProvider.getSourceCodeFile(file, cwd));
  }
  
  @Test
  public void getting_code_with_filename_and_relative_code_location() {
    String cwd = new File("src/test/resources").getAbsolutePath();
    String baseDir = new File("src/test").getAbsolutePath();
    String file = "source.hh";
    
    codeProvider.setIncludeRoots(Arrays.asList("resources/codeprovider"), baseDir);
    assertEquals(expected, codeProvider.getSourceCodeFile(file, cwd));
  }

  @Test
  public void getting_source_code() {
    assertEquals("source code\n", codeProvider.getSourceCode(expected));
  }
}
