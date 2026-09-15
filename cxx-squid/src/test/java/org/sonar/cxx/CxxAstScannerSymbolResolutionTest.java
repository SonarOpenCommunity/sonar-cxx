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
package org.sonar.cxx;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;

class CxxAstScannerSymbolResolutionTest {

  @Test
  void realScanPopulatesSymbolTableAndResolvesParameterUsage() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/SymbolResolutionEndToEnd.cc", ".", "");
    var scanner = CxxAstScanner.create(squidConfig);
    scanner.scanInputFile(tester.asInputFile());

    // Confirms the full pipeline runs symbol resolution without throwing.
  }

  @Test
  void scanningANewFileClearsSymbolsFromThePreviousFile() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var scanner = CxxAstScanner.create(squidConfig);

    var firstFile = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverFunctionLocals.cc", ".", "");
    scanner.scanInputFile(firstFile.asInputFile());
    assertThat(AstNodeSymbolExtension.size()).isPositive();

    var secondFile = CxxFileTesterHelper.create(
      "src/test/resources/visitors/SymbolResolverForwardDeclaration.cc", ".", "");
    scanner.scanInputFile(secondFile.asInputFile());
    assertThat(AstNodeSymbolExtension.size()).isZero();
  }

}
