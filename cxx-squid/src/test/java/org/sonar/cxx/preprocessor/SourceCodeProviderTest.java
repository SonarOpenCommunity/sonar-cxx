/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
import java.nio.file.Paths;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class SourceCodeProviderTest {

  private final File expected1 = new File(new File("src/test/resources/codeprovider/source.hh").getAbsolutePath());
  private final File expected2 = new File(new File("src/test/resources/codeprovider/source").getAbsolutePath());
  private final File root = new File(new File("src/test/resources/codeprovider").getAbsolutePath());

  // ////////////////////////////////////////////////////////////////////////////
  // Behavior in the absolute path case
  @Test
  public void getting_file_with_abspath() {
    // lookup with absolute paths should ignore the value of current
    // working directory and should work the same in the quoted and
    // unquoted case

    var codeProvider = new SourceCodeProvider(new File("src/test/resources/codeprovider/dummy.cpp"));
    String path = expected1.getAbsolutePath();

    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected1);
  }

  // ////////////////////////////////////////////////////////////////////////////
  // Relpathes, standard behavior: equal for quoted and angle cases
  // We have following cases here:
  // Include string form | Include root form |
  //           | absolute | relative |
  // simple    | 1        | 2        |
  // -------------------------------------------
  // rel. path | 3        | 4        |
  @Test
  public void getting_file_relpath_case1() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("src/test/resources/codeprovider").toAbsolutePath().toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected1);
  }

  @Test
  public void getting_file_relpath_case1_without_extension() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("src/test/resources/codeprovider").toAbsolutePath().toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "source";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected2);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected2);
  }

  @Test
  public void getting_file_relpath_case2() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("resources/codeprovider").toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected1);
  }

  @Test
  public void getting_file_relpath_case2_without_extension() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("resources/codeprovider").toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "source";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected2);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected2);
  }

  @Test
  public void getting_file_relpath_case3() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("src/test/resources").toAbsolutePath().toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "codeprovider/source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected1);
  }

  @Test
  public void getting_file_relpath_case3_without_extension() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("src/test/resources").toAbsolutePath().toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "codeprovider/source";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected2);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected2);
  }

  @Test
  public void getting_file_relpath_case4() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("resources").toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "codeprovider/source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected1);
  }

  @Test
  public void getting_file_relpath_case4_without_extension() {
    var codeProvider = new SourceCodeProvider(new File("dummy"));

    var includeRoot = Paths.get("resources").toString();
    String baseDir = new File("src/test").getAbsolutePath();
    codeProvider.setIncludeRoots(Arrays.asList(includeRoot), baseDir);

    var path = "codeprovider/source";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected2);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isEqualTo(expected2);
  }

  // ////////////////////////////////////////////////////////////////////////////
  // Special behavior in the quoted case
  // Lookup in the current directory. Has to fail for the angle case
  @Test
  public void getting_file_with_filename_and_cwd() {
    var codeProvider = new SourceCodeProvider(new File("src/test/resources/codeprovider/dummy.cpp"));

    var path = "source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isNull();
  }

  @Test
  public void getting_file_with_relpath_and_cwd() {
    var codeProvider = new SourceCodeProvider(new File("src/test/resources/dummy.cpp"));

    var path = "codeprovider/source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isNull();
  }

  @Test
  public void getting_file_with_relpath_containing_backsteps_and_cwd() {
    var codeProvider = new SourceCodeProvider(
      new File("src/test/resources/codeprovider/folder/dummy.cpp"));

    var path = "../source.hh";
    assertThat(codeProvider.getSourceCodeFile(path, true)).isEqualTo(expected1);
    assertThat(codeProvider.getSourceCodeFile(path, false)).isNull();
  }

  @Test
  public void getting_source_code1() throws IOException {
    var codeProvider = new SourceCodeProvider(new File("dummy"));
    assertThat(codeProvider.getSourceCode(expected1, Charset.defaultCharset())).isEqualTo("source code");
  }

  @Test
  public void getting_source_code2() throws IOException {
    var codeProvider = new SourceCodeProvider(new File("dummy"));
    assertThat(codeProvider.getSourceCode(expected2, Charset.defaultCharset())).isEqualTo("source code");
  }

  @Test
  public void getting_source_code_utf_8() throws IOException {
    var codeProvider = new SourceCodeProvider(new File("dummy"));
    assertThat(codeProvider.getSourceCode(new File(root, "./utf-8.hh"),
                                          Charset.defaultCharset())).isEqualTo("UTF-8");
  }

  @Test
  public void getting_source_code_utf_8_bom() throws IOException {
    var codeProvider = new SourceCodeProvider(new File("dummy"));
    assertThat(codeProvider.getSourceCode(new File(root, "./utf-8-bom.hh"),
                                          Charset.defaultCharset())).isEqualTo("UTF-8-BOM");
  }

  @Test
  public void getting_source_code_utf_16_le_bom() throws IOException {
    var codeProvider = new SourceCodeProvider(new File("dummy"));
    assertThat(
      codeProvider.getSourceCode(new File(root, "./utf-16le-bom.hh"),
                                 Charset.defaultCharset())).isEqualTo("UTF-16LE-BOM");
  }

}
