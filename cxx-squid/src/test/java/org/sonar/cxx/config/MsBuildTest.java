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
package org.sonar.cxx.config;

import java.io.File;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sonar.api.internal.apachecommons.lang3.SystemUtils;

/**
 * These tests ensure that the relative paths in the INCLUDES are correctly converted to absolute paths. The project
 * directory is used as the base directory. The project directory is extracted regardless of the language of the log
 * file.
 *
 * @author rudolfgrauberger
 */
class MsBuildTest {

  private static final String REFERENCE_DETAILED_LOG = "src/test/resources/msbuild/msbuild-detailed-en.txt";
  private static final String UNIQUE_FILE = "C:\\Development\\Source\\Cpp\\Dummy\\src\\main.cpp";
  private static final String VC_CHARSET = "UTF8";

  @BeforeAll
  public static void setUp() {
    Assumptions.assumeTrue(SystemUtils.IS_OS_WINDOWS);
  }

  @Test
  void relativeIncludesFromReferenceLog() {

    List<String> includes = getIncludesForReferenceLogFile();

    var softly = new SoftAssertions();

    // Absolute path
    softly.assertThat(includes).contains("C:\\Development\\Source\\ThirdParty\\VS2017\\Firebird-2.5.8\\include");
    // Relative paths
    softly.assertThat(includes).contains("C:\\Development\\Source\\ThirdParty\\VS2017\\Boost-1.67.0");
    softly.assertThat(includes).contains("C:\\Development\\Source\\Cpp\\Dummy\\includes");
    softly.assertThat(includes).contains("C:\\Development\\Source\\Cpp\\Dummy\\release");
    softly.assertThat(includes).hasSize(4);
    softly.assertAll();
  }

  @Test
  void relativeIncludesVS2019ReferenceLog() {

    final var REFERENCE_LOG = "src/test/resources/msbuild/msbuild-azure-devops-en.txt";
    List<String> includes = getIncludesForUniqueFile(REFERENCE_LOG);

    var softly = new SoftAssertions();
    // Absolute path
    softly.assertThat(includes).contains("C:\\agent\\_work\\1\\s\\_Globals\\Include");
    softly.assertThat(includes).hasSize(1);
    softly.assertAll();
  }

  @Test
  void relativeIncludesFromGermanLog() {

    List<String> refIncludes = getIncludesForReferenceLogFile();
    List<String> includes = getIncludesForUniqueFile("src/test/resources/msbuild/msbuild-detailed-de.txt");

    var softly = new SoftAssertions();

    softly.assertThat(includes).containsExactlyInAnyOrderElementsOf(refIncludes);
    softly.assertAll();
  }

  @Test
  void relativeIncludesFromFrenchLog() {

    List<String> refIncludes = getIncludesForReferenceLogFile();
    List<String> includes = getIncludesForUniqueFile("src/test/resources/msbuild/msbuild-detailed-fr.txt");

    var softly = new SoftAssertions();

    softly.assertThat(includes).containsExactlyInAnyOrderElementsOf(refIncludes);
    softly.assertAll();
  }

  private List<String> getIncludesForReferenceLogFile() {
    return getIncludesForUniqueFile(REFERENCE_DETAILED_LOG);
  }

  private List<String> getIncludesForUniqueFile(String log) {
    var squidConfig = new CxxSquidConfiguration();
    var logFile = new File(log);

    var parser = new MsBuild(squidConfig);
    parser.parse(logFile, ".", VC_CHARSET);

    return squidConfig.getValues(UNIQUE_FILE, CxxSquidConfiguration.INCLUDE_DIRECTORIES);
  }
}
