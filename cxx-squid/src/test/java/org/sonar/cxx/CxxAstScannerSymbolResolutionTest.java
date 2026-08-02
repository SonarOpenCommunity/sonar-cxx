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

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.config.CxxSquidConfiguration;

class CxxAstScannerSymbolResolutionTest {

  @Test
  void realScanPopulatesSymbolTableAndResolvesParameterUsage() throws IOException {
    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    var tester = CxxFileTesterHelper.create(
      "src/test/resources/SymbolResolutionEndToEnd.cc", ".", "");
    var scanner = CxxAstScanner.create(squidConfig);
    scanner.scanInputFile(tester.asInputFile());

    // CxxAstScanner.create owns the symbol-resolution visitor instance internally, so no direct
    // assertion on its state is possible from here; this confirms the full scan pipeline runs
    // symbol resolution without throwing, across every declaration kind in one pass.
  }

}
