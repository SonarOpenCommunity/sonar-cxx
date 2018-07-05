/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.sonar.cxx.CxxAstScanner;
import org.sonar.cxx.CxxFileTester;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.api.SourceFile;

import com.sonar.sslr.api.Grammar;

public class CxxFunctionSizeVisitorTest {

  @Test
  public void testPublishMeasuresForFile() throws IOException {

    CxxLanguage language = CxxFileTesterHelper.mockCxxLanguage();
    when(language.getIntegerOption(CxxFunctionSizeVisitor.FUNCTION_SIZE_THRESHOLD_KEY)).thenReturn(Optional.of(5));

    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/metrics/FunctionComplexity.cc",
        ".", "");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, language);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.BIG_FUNCTIONS)).isEqualTo(4);
    softly.assertThat(file.getInt(CxxMetric.LOC_IN_FUNCTIONS)).isEqualTo(55);
    softly.assertThat(file.getInt(CxxMetric.BIG_FUNCTIONS_LOC)).isEqualTo(44);
    softly.assertAll();
  }

  @Test
  public void testPublishMeasuresForEmptyFile() throws IOException {
    CxxLanguage language = CxxFileTesterHelper.mockCxxLanguage();
    CxxFileTester tester = CxxFileTesterHelper.CreateCxxFileTester("src/test/resources/metrics/EmptyFile.cc", ".", "");
    SourceFile file = CxxAstScanner.scanSingleFile(tester.cxxFile, tester.sensorContext, language);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(file.getInt(CxxMetric.BIG_FUNCTIONS)).isEqualTo(0);
    softly.assertThat(file.getInt(CxxMetric.BIG_FUNCTIONS_LOC)).isEqualTo(0);
    softly.assertAll();
  }
}
