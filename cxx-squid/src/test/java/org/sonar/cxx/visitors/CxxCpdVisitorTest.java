/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
import java.io.UnsupportedEncodingException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;

class CxxCpdVisitorTest {

  private SourceFile sourceFile;

  @BeforeEach
  public void scanFile() throws UnsupportedEncodingException, IOException {
    var tester = CxxFileTesterHelper.create("src/test/resources/visitors/cpd.cc", ".", "");
    var squidConfig = new CxxSquidConfiguration();
    squidConfig
      .add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.CPD_IGNORE_LITERALS, "true");
    squidConfig
      .add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.CPD_IGNORE_IDENTIFIERS, "true");

    sourceFile = CxxAstScanner.scanSingleInputFile(tester.asInputFile(), new CxxCpdVisitor(squidConfig));
  }

  @Test
  void testCpdTokens() throws Exception {
    List<CxxCpdVisitor.CpdToken> data = (List<CxxCpdVisitor.CpdToken>) sourceFile.getData(CxxMetric.CPD_TOKENS_DATA);
    assertThat(data).hasSize(391);
  }

}
