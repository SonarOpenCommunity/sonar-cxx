/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Test;
import org.sonar.plugins.cxx.TestUtils;

public class CxxUtilsTest {

  @Test
  public void whenPathIsAbsoluteAndOutsideBaseDirShouldUseDriveLetterWindows() {
    if (TestUtils.isWindows()) {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("m:\\abc\\abs\\", "c:\\src\\abs\\abc.xml");
      assertThat(scanner.getBaseDir().toLowerCase()).isEqualTo("c:\\src\\abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    } else {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("/abc/abs", "/src/abs/abc.xml");
      assertThat(scanner.getBaseDir()).isEqualTo("/src/abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    }
  }

  @Test
  public void whenPathIsAbsoluteAndIsInBaseDirShouldShouldUseBaseDir() {
    if (TestUtils.isWindows()) {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("x:\\src\\abs\\", "x:\\src\\abs\\abc.xml");
      assertThat(scanner.getBaseDir()).isEqualTo("x:\\src\\abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    } else {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("/src/abs", "/src/abs/abc.xml");
      assertThat(scanner.getBaseDir()).isEqualTo("/src/abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    }
  }

  @Test
  public void whenPathIsAbsoluteAndIsOutsideProjectShouldShouldUseRootOrDrive() {
    if (TestUtils.isWindows()) {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("c:\\mmm\\mmmm\\", "c:\\src\\abs\\abc.xml");
      assertThat(scanner.getBaseDir().toLowerCase()).isEqualTo("c:\\src\\abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    } else {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("/bbb/dddd", "abc.xml");
      assertThat(scanner.getBaseDir()).isEqualTo("/bbb/dddd");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    }
  }

  @Test
  public void whenPathIsRelativeAndIsInsideProjectShouldUseProjectDir() {
    if (TestUtils.isWindows()) {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("x:\\src\\abs\\", "abc.xml");
      assertThat(scanner.getBaseDir()).isEqualTo("x:\\src\\abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    } else {
      CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("/src/abs", "abc.xml");
      assertThat(scanner.getBaseDir()).isEqualTo("/src/abs");
      assertThat(scanner.getPattern()).isEqualTo("abc.xml");
    }
  }

  //@Test
  public void checkregex() throws IOException {
    CxxSearchPathData scanner = CxxUtils.GetDirectoryScannerForReport("x:\\src\\abs\\", "E:\\reports\\reports-rats\\*.xml");
    Collection<Path> paths = CxxFileFinder.FindFiles(scanner.getBaseDir(), scanner.getPattern(), false);

    assertThat(scanner.getBaseDir()).isEqualTo("x:\\src\\abs");
    assertThat(scanner.getPattern()).isEqualTo("abc.xml");
  }

  //@Test
  public void testWithRealDataInTest() {
    List<File> reports = new ArrayList<>();
    CxxUtils.GetReportForBaseDirAndPattern("e:/path", "BuildLogTS.*", reports);
    assertThat(reports.size()).isEqualTo(1);
  }
}
