/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.preprocessor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

public class SourceCodeProviderTest {

  private final SourceCodeProvider codeProvider = new SourceCodeProvider();
  private final File expected = new File(new File("src/test/resources/codeprovider/source.hh").getAbsolutePath());

  // ////////////////////////////////////////////////////////////////////////////
  // Behaviour in the absolute path case
  @Test
  public void getting_file_with_abspath() {
    // lookup with absolute pathes should ignore the value of current
    // working directory and should work the same in the quouted and
    // unquoted case

    String path = expected.getAbsolutePath();
    String dummycwd = new File("src/test/resources/codeprovider").getAbsolutePath();

    assertEquals(expected, codeProvider.getSourceCodeFile(path, null, true));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, null, false));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  // ////////////////////////////////////////////////////////////////////////////
  // Relpathes, standard behaviour: equal for quoted and angle cases
  // We have following cases here:
  // Include string form | Include root form |
  // | absolute | relative |
  // simple | 1 | 2 |
  // -------------------------------------------
  // rel. path | 3 | 4 |
  @Test
  public void getting_file_relpath_case1() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "source.hh";
    String includeRoot = new File("src/test/resources/codeprovider").getAbsolutePath();

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case2() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "source.hh";
    String includeRoot = "resources/codeprovider";

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case3() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "codeprovider/source.hh";
    String includeRoot = new File("src/test/resources").getAbsolutePath();

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case4() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "codeprovider/source.hh";
    String includeRoot = "resources";

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  // ////////////////////////////////////////////////////////////////////////////
  // Special behaviour in the quoted case
  // Lookup in the current directory. Has to fail for the angle case
  @Test
  public void getting_file_with_filename_and_cwd() {
    String cwd = new File("src/test/resources/codeprovider").getAbsolutePath();
    String path = "source.hh";
    assertEquals(expected, codeProvider.getSourceCodeFile(path, cwd, true));
    assertEquals(null, codeProvider.getSourceCodeFile(path, cwd, false));
  }

  @Test
  public void getting_file_with_relpath_and_cwd() {
    String cwd = new File("src/test/resources").getAbsolutePath();
    String path = "codeprovider/source.hh";
    assertEquals(expected, codeProvider.getSourceCodeFile(path, cwd, true));
    assertEquals(null, codeProvider.getSourceCodeFile(path, cwd, false));
  }

  @Test
  public void getting_file_with_relpath_containing_backsteps_and_cwd() {
    String cwd = new File("src/test/resources/codeprovider/folder").getAbsolutePath();
    String path = "../source.hh";
    assertEquals(expected, codeProvider.getSourceCodeFile(path, cwd, true));
    assertEquals(null, codeProvider.getSourceCodeFile(path, cwd, false));
  }

  @Test
  public void getting_source_code() {
    assertEquals("source code", codeProvider.getSourceCode(expected));
  }
}
