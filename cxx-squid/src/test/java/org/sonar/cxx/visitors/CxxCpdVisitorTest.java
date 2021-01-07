/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.utils.TestUtils;
import org.sonar.squidbridge.api.SourceFile;

public class CxxCpdVisitorTest {

  private SourceFile sourceFile;

  @Before
  public void scanFile() throws UnsupportedEncodingException, IOException {
    File baseDir = TestUtils.loadResource("/visitors");
    var file = new File(baseDir, "cpd.cc");
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.CPD_IGNORE_LITERALS, "true");
    squidConfig
      .add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.CPD_IGNORE_IDENTIFIERS, "true");

    var cpdVisitor = new CxxCpdVisitor(squidConfig);
    sourceFile = CxxAstScanner.scanSingleFile(file, cpdVisitor);
  }

  @Test
  public void testCpdTokens() throws Exception {
    List<CxxCpdVisitor.CpdToken> data = (List<CxxCpdVisitor.CpdToken>) sourceFile.getData(CxxMetric.CPD_TOKENS_DATA);
    assertThat(data).hasSize(391);
  }

}
