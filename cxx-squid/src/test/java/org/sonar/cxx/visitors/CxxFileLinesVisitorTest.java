/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.squidbridge.api.SourceFile;

class CxxFileLinesVisitorTest {

  private SourceFile sourceFile;

  @BeforeEach
  public void setUp() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/visitors/ncloc.cc", ".", "");
    sourceFile = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), new CxxFileLinesVisitor());
  }

  @Test
  void testLinesOfCode() throws IOException {
    Set<Integer> testLines = Stream.of(
      8, 10, 14, 16, 17, 21, 22, 23, 26, 31, 34, 35, 42, 44, 45, 49, 51, 53, 55, 56,
      58, 59, 63, 65, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 79, 82, 84, 86, 87, 89,
      90, 95, 98, 99, 100, 102, 107, 108, 109, 110, 111, 113, 115, 118, 119, 124, 126)
      .collect(Collectors.toCollection(HashSet::new));
    List<Integer> linesOfCode = (List<Integer>) sourceFile.getData(CxxMetric.NCLOC_DATA);
    var softly = new SoftAssertions();
    softly.assertThat(linesOfCode).containsExactlyInAnyOrderElementsOf(testLines);
    softly.assertAll();
  }

  @Test
  void testExecutableLinesOfCode() throws IOException {
    List<Integer> executableLines = (List<Integer>) sourceFile.getData(CxxMetric.EXECUTABLE_LINES_DATA);
    assertThat(executableLines).containsExactlyInAnyOrder(
      10, 26, 34, 35, 56, 59, 69, 70, 72, 73,
      75, 76, 79, 87, 90, 98, 102, 118, 119, 126);
  }

}
