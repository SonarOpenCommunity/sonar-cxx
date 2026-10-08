/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;

class CxxSymbolHighlighterVisitorTest {

  @Test
  @SuppressWarnings("unchecked")
  void resolvedLocalVariablesArePublishedWithDeclarationAndUsages() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverFunctionLocals.cc", ".", "");

    SourceFile sourceFile = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    var references =
      (List<CxxSymbolHighlighterVisitor.SymbolReference>) sourceFile.getData(CxxMetric.SYMBOL_TABLE_DATA);

    assertThat(references).hasSize(3);
    references.forEach(reference -> assertThat(reference.usages()).hasSize(1));
  }

  @Test
  @SuppressWarnings("unchecked")
  void tokenSpanReportsLineAndColumnOfTheUnderlyingToken() throws IOException {
    // "a" in "int add(int a, int b) {" starts at line 1, column 12 (0-based).
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverFunctionLocals.cc", ".", "");

    SourceFile sourceFile = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    var references =
      (List<CxxSymbolHighlighterVisitor.SymbolReference>) sourceFile.getData(CxxMetric.SYMBOL_TABLE_DATA);
    var aDeclaration = references.stream()
      .map(CxxSymbolHighlighterVisitor.SymbolReference::declaration)
      .filter(span -> span.startLine() == 1 && span.startLineOffset() == 12)
      .findFirst()
      .orElseThrow(() -> new AssertionError("No declaration span found for 'a' at line 1, col 12."));

    assertThat(aDeclaration.endLine()).isEqualTo(1);
    assertThat(aDeclaration.endLineOffset()).isEqualTo(13);
  }

  @Test
  @SuppressWarnings("unchecked")
  void declarationsInsideMacroExpansionsAreNotPublished() throws IOException {
    // Macro-expanded tokens keep the invocation's line but not its real column range (see
    // libuv's SOCKOPT_SETTER); such spans crash SonarQube's bounds validation if published.
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverMacroExpandedDeclaration.cc", ".", "");

    SourceFile sourceFile = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    var references =
      (List<CxxSymbolHighlighterVisitor.SymbolReference>) sourceFile.getData(CxxMetric.SYMBOL_TABLE_DATA);
    var lineNineteenLength = 19;
    if (references != null) {
      for (var reference : references) {
        assertLineOffsetsAreWithinRealSource(reference.declaration(), lineNineteenLength);
        for (var usage : reference.usages()) {
          assertLineOffsetsAreWithinRealSource(usage, lineNineteenLength);
        }
      }
    }
  }

  private static void assertLineOffsetsAreWithinRealSource(
      CxxSymbolHighlighterVisitor.TokenSpan span, int lineNineteenLength) {
    if (span.startLine() == 19) {
      assertThat(span.startLineOffset()).isLessThanOrEqualTo(lineNineteenLength);
      assertThat(span.endLineOffset()).isLessThanOrEqualTo(lineNineteenLength);
    }
  }

  @Test
  void fileWithNoResolvedUsagesPublishesNoReferences() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverForwardDeclaration.cc", ".", "");

    SourceFile sourceFile = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    var references =
      (List<CxxSymbolHighlighterVisitor.SymbolReference>) sourceFile.getData(CxxMetric.SYMBOL_TABLE_DATA);

    assertThat(references).isEmpty();
  }

  @Test
  void symbolTableDisabledBySquidConfigPublishesNoData() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES,
      CxxSquidConfiguration.SYMBOL_TABLE_ENABLED, "false");
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverFunctionLocals.cc", ".", "");

    SourceFile sourceFile = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    assertThat(sourceFile.getData(CxxMetric.SYMBOL_TABLE_DATA)).isNull();
  }
}
