/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxSquidConfiguration;
import org.sonar.cxx.CxxFileTester;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.api.SourceFile;

public class CxxFunctionComplexityVisitorTest {

  @Test
  public void testPublishMeasuresForFile() throws IOException {

    CxxSquidConfiguration squidConfig = new CxxSquidConfiguration();
    squidConfig.setFunctionComplexityThreshold(5);

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/metrics/FunctionComplexity.cc",
                                                                   ".", "");
    SourceFile file = CxxAstScanner.scanSingleFileConfig(tester.asFile(), squidConfig);

    var softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS)).isEqualTo(4);
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS_LOC)).isEqualTo(44);
    softly.assertAll();
  }

  @Test
  public void testPublishMeasuresForEmptyFile() throws IOException {

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/metrics/EmptyFile.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.asFile());

    var softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS)).isEqualTo(0);
    softly.assertThat(file.getInt(CxxMetric.COMPLEX_FUNCTIONS_LOC)).isEqualTo(0);
    softly.assertAll();
  }

}
