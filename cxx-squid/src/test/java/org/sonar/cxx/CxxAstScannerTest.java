/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.api.SourceProject;
import org.sonar.cxx.squidbridge.indexer.QueryByType;

class CxxAstScannerTest {

  @Test
  void files() throws IOException {

    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/trivial.cc", ".", "");
    CxxFileTesterHelper.add(tester, "src/test/resources/metrics/trivial.cc", "");

    var scanner = CxxAstScanner.create(new CxxSquidConfiguration());
    scanner.scanFiles(new ArrayList<>(Arrays.asList(
      new File("src/test/resources/metrics/trivial.cc"),
      new File("src/test/resources/metrics/classes.cc")))
    );
    var project = (SourceProject) scanner.getIndex().search(new QueryByType(SourceProject.class)).iterator().next();
    assertThat(project.getInt(CxxMetric.FILES)).isEqualTo(2);
  }

  @Test
  void comments() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/comments.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    var softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.COMMENT_LINES)).isEqualTo(6);
    softly.assertThat(file.getNoSonarTagLines()).contains(8).hasSize(1);
    softly.assertAll();
  }

  @Test
  void lines() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/classes.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.LINES)).isEqualTo(7);
  }

  @Test
  void linesOfCode() throws IOException {

    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/classes.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.LINES_OF_CODE)).isEqualTo(5);
  }

  @Test
  void statements() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/statements.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.STATEMENTS)).isEqualTo(4);
  }

  @Test
  void functions() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/functions.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.FUNCTIONS)).isEqualTo(2);
  }

  @Test
  void classes() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/classes.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.CLASSES)).isEqualTo(2);
  }

  @Test
  void namespaces() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/namespace.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.CLASSES)).isEqualTo(3);
    assertThat(file.getInt(CxxMetric.FUNCTIONS)).isEqualTo(6); // = functions and methods
  }

  @ParameterizedTest
  @CsvSource({
    "'src/test/resources/metrics/complexity.cc', 14",
    "'src/test/resources/metrics/complexity_alternative.cc', 14",
    "'src/test/resources/metrics/complexity_macro.cc', 1"
  })
  void complexity(String source, int result) throws IOException {
    var tester = CxxFileTesterHelper.create(source, ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.COMPLEXITY)).isEqualTo(result);
  }

  @Test
  void errorRecoveryDeclaration() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/parser/bad/error_recovery_declaration.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getInt(CxxMetric.FUNCTIONS)).isEqualTo(2);
  }

  @Test
  void nosonarComments() throws IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/nosonar.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());
    assertThat(file.getNoSonarTagLines()).containsOnly(3, 6, 9, 11);
  }

}
