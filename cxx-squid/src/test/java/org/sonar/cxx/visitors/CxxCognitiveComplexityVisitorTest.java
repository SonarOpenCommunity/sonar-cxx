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
package org.sonar.cxx.visitors;

import java.io.IOException;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.squidbridge.api.SourceFile;

class CxxCognitiveComplexityVisitorTest {

  @Test
  void ifStatement() throws IOException {
    assertThat(testFile("src/test/resources/visitors/if.cc")).isEqualTo(1);
  }

  @Test
  void ifElse() throws IOException {
    assertThat(testFile("src/test/resources/visitors/if_else.cc")).isEqualTo(2);
  }

  @Test
  void ifElseIf() throws IOException {
    assertThat(testFile("src/test/resources/visitors/if_else_if.cc")).isEqualTo(2);
  }

  @Test
  void ifElseIfElse() throws IOException {
    assertThat(testFile("src/test/resources/visitors/if_else_if_else.cc")).isEqualTo(3);
  }

  @Test
  void ternary() throws IOException {
    assertThat(testFile("src/test/resources/visitors/ternary.cc")).isEqualTo(1);
  }

  @Test
  void nesting() throws IOException {
    assertThat(testFile("src/test/resources/visitors/nesting.cc")).isEqualTo(20);
  }

  @Test
  void switchStatement() throws IOException {
    assertThat(testFile("src/test/resources/visitors/switch.cc")).isEqualTo(1);
  }

  @Test
  void forStatement() throws IOException {
    assertThat(testFile("src/test/resources/visitors/for.cc")).isEqualTo(1);
  }

  @Test
  void whileStatement() throws IOException {
    assertThat(testFile("src/test/resources/visitors/while.cc")).isEqualTo(1);
  }

  @Test
  void doWhile() throws IOException {
    assertThat(testFile("src/test/resources/visitors/do_while.cc")).isEqualTo(1);
  }

  @Test
  void catchStatement() throws IOException {
    assertThat(testFile("src/test/resources/visitors/catch.cc")).isEqualTo(1);
  }

  @Test
  void multipleCatch() throws IOException {
    assertThat(testFile("src/test/resources/visitors/multiple_catch.cc")).isEqualTo(3);
  }

  @Test
  void gotoStatement() throws IOException {
    assertThat(testFile("src/test/resources/visitors/goto.cc")).isEqualTo(1);
  }

  @Test
  void binaryLogical() throws IOException {
    assertThat(testFile("src/test/resources/visitors/binary_logical.cc")).isEqualTo(2);
  }

  @Test
  void binaryLogicalMixed() throws IOException {
    assertThat(testFile("src/test/resources/visitors/binary_logical_mixed.cc")).isEqualTo(4);
  }

  @Test
  void binaryLogicalMixedAlternative() throws IOException {
    assertThat(testFile("src/test/resources/visitors/binary_logical_mixed_alternative.cc")).isEqualTo(4);
  }

  @Test
  void binaryLogicalNot() throws IOException {
    assertThat(testFile("src/test/resources/visitors/binary_logical_not.cc")).isEqualTo(3);
  }

  @Test
  void lambda() throws IOException {
    assertThat(testFile("src/test/resources/visitors/lambda.cc")).isEqualTo(2);
  }

  @Test
  void sumOfPrimes() throws IOException {
    assertThat(testFile("src/test/resources/visitors/sum_of_primes.cc")).isEqualTo(7);
  }

  @Test
  void getWords() throws IOException {
    assertThat(testFile("src/test/resources/visitors/get_words.cc")).isEqualTo(1);
  }

  @Test
  void overriddenSymbolFrom() throws IOException {
    assertThat(testFile("src/test/resources/visitors/overridden_symbol_from.cc")).isEqualTo(19);
  }

  @Test
  void addVersion() throws IOException {
    assertThat(testFile("src/test/resources/visitors/add_version.cc")).isEqualTo(35);
  }

  @Test
  void addVersionMacro() throws IOException {
    assertThat(testFile("src/test/resources/visitors/add_version_macro.cc")).isEqualTo(1);
  }

  @Test
  void toRegexp() throws IOException {
    assertThat(testFile("src/test/resources/visitors/to_regexp.cc")).isEqualTo(20);
  }

  @Test
  void template() throws IOException {
    assertThat(testFile("src/test/resources/visitors/template.cc")).isZero();
  }

  @Test
  void inline() throws IOException {
    assertThat(testFile("src/test/resources/visitors/inline.cc")).isEqualTo(5);
  }

  private int testFile(String fileName) throws IOException {
    var tester = CxxFileTesterHelper.create(fileName, ".", "");
    SourceFile sourceFile = CxxAstScanner.scanSingleInputFile(tester.asInputFile());

    return (sourceFile.getInt(CxxMetric.COGNITIVE_COMPLEXITY));
  }

}
