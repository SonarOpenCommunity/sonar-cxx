/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SourceCodeProviderTest {

  private final SourceCodeProvider codeProvider = new SourceCodeProvider();
  private final File expected1 = new File(new File("src/test/resources/codeprovider/source.hh").getAbsolutePath());
  private final File expected2 = new File(new File("src/test/resources/codeprovider/source").getAbsolutePath());

  // ////////////////////////////////////////////////////////////////////////////
  // Behavior in the absolute path case
  @Test
  public void getting_file_with_abspath() {
    // lookup with absolute paths should ignore the value of current
    // working directory and should work the same in the quoted and
    // unquoted case

    String path = expected1.getAbsolutePath();
    String dummycwd = new File("src/test/resources/codeprovider").getAbsolutePath();

    assertEquals(expected1, codeProvider.getSourceCodeFile(path, null, true));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, null, false));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  // ////////////////////////////////////////////////////////////////////////////
  // Relpathes, standard behavior: equal for quoted and angle cases
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
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case1_without_extension() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "source";
    String includeRoot = new File("src/test/resources/codeprovider").getAbsolutePath();

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case2() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "source.hh";
    String includeRoot = "resources/codeprovider";

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case2_without_extension() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "source";
    String includeRoot = "resources/codeprovider";

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case3() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "codeprovider/source.hh";
    String includeRoot = new File("src/test/resources").getAbsolutePath();

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case3_without_extension() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "codeprovider/source";
    String includeRoot = new File("src/test/resources").getAbsolutePath();

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case4() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "codeprovider/source.hh";
    String includeRoot = "resources";

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  @Test
  public void getting_file_relpath_case4_without_extension() {
    String baseDir = new File("src/test").getAbsolutePath();
    String dummycwd = "/";
    String path = "codeprovider/source";
    String includeRoot = "resources";

    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, true));
    assertEquals(expected2, codeProvider.getSourceCodeFile(path, dummycwd, false));
  }

  // ////////////////////////////////////////////////////////////////////////////
  // Special behavior in the quoted case
  // Lookup in the current directory. Has to fail for the angle case
  @Test
  public void getting_file_with_filename_and_cwd() {
    String cwd = new File("src/test/resources/codeprovider").getAbsolutePath();
    String path = "source.hh";
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, cwd, true));
    assertEquals(null, codeProvider.getSourceCodeFile(path, cwd, false));
  }

  @Test
  public void getting_file_with_relpath_and_cwd() {
    String cwd = new File("src/test/resources").getAbsolutePath();
    String path = "codeprovider/source.hh";
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, cwd, true));
    assertEquals(null, codeProvider.getSourceCodeFile(path, cwd, false));
  }

  @Test
  public void getting_file_with_relpath_containing_backsteps_and_cwd() {
    String cwd = new File("src/test/resources/codeprovider/folder").getAbsolutePath();
    String path = "../source.hh";
    assertEquals(expected1, codeProvider.getSourceCodeFile(path, cwd, true));
    assertEquals(null, codeProvider.getSourceCodeFile(path, cwd, false));
  }

  @Test
  public void getting_source_code1() throws IOException {
    assertEquals("source code", codeProvider.getSourceCode(expected1, Charset.defaultCharset()));
  }

  @Test
  public void getting_source_code2() throws IOException {
    assertEquals("source code", codeProvider.getSourceCode(expected2, Charset.defaultCharset()));
  }
}
