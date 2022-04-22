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
package org.sonar.cxx.visitors;

import java.io.IOException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.api.SourceFile;

class CxxFunctionComplexityVisitorTest {

  @Test
  void testPublishMeasuresForFile() throws IOException {

    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.FUNCTION_COMPLEXITY_THRESHOLD,
                    "5");
    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/FunctionComplexity.cc",
                                        ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFileConfig(tester.asInputFile(), squidConfig);

    var softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS)).isEqualTo(4);
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS_LOC)).isEqualTo(44);
    softly.assertAll();
  }

  @Test
  void testPublishMeasuresForEmptyFile() throws IOException {

    var tester = CxxFileTesterHelper.create("src/test/resources/metrics/EmptyFile.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleInputFile(tester.asInputFile());

    var softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS)).isZero();
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS_LOC)).isZero();
    softly.assertAll();
  }

}
