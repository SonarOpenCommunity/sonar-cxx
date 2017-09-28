/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.cxx.sensors.coverage;

import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.coverage.CxxCoverageCache;
import static org.fest.assertions.Assertions.assertThat;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxBullseyeCoverageSensorTest {
  private static final Logger LOG = Loggers.get(CxxBullseyeCoverageSensorTest.class);
  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private CxxLanguage language;
  
  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
//    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY))
//    .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATHS_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.IT_REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.OVERALL_REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY))
    .thenReturn("sonar.cxx." + CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY);

    //    when(language.hasKey(CxxCoverageSensor.REPORT_PATHS_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.IT_REPORT_PATH_KEY)).thenReturn(true);
    when(language.hasKey(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY)).thenReturn(true);
  }

  @Test
  public void shouldReportCorrectCoverage() {
    String coverageReport = "coverage-reports/bullseye/coverage-result-bullseye.xml";
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    if (TestUtils.isWindows()) {
      Settings settings = new Settings();
      settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), coverageReport);
  //    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY), coverageReport);
  //    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY), coverageReport);
  //    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), coverageReport);
  
      context.setSettings(settings);
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "source_1.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "src/testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "src/testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
  
      sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
      sensor.execute(context, linesOfCodeByFile);
  
      assertThat(context.lineHits("ProjectKey:main.cpp", CoverageType.UNIT, 7)).isEqualTo(1);

      int[] zeroHitLines = new int[] { 5, 10, 15, 17, 28, 32, 35, 40, 41, 44};
      for (int line : zeroHitLines) {
        LOG.debug("Check zero line coverage: {}", line);
        assertThat(context.lineHits("ProjectKey:source_1.cpp", CoverageType.UNIT, line)).isEqualTo(0);
      }
  
      int[] oneHitlinesA = new int[] { 7, 12, 17, 30};
      for (int line : oneHitlinesA) {
        LOG.debug("Check line coverage: {}", line);
        assertThat(context.lineHits("ProjectKey:testclass.cpp", CoverageType.UNIT, line)).isEqualTo(1);
        assertThat(context.lineHits("ProjectKey:src/testclass.cpp", CoverageType.UNIT, line)).isEqualTo(1);
      }
      int[] fullCoveredTwoCondition = new int [] { 34, 43, 46};
      for (int line : fullCoveredTwoCondition) {
        LOG.debug("Check full covered two conditions - line: {}", line);
        assertThat(context.conditions("ProjectKey:testclass.cpp", CoverageType.UNIT, line)).isEqualTo(2);
        assertThat(context.conditions("ProjectKey:src/testclass.cpp", CoverageType.UNIT, line)).isEqualTo(2);
        assertThat(context.coveredConditions("ProjectKey:testclass.cpp", CoverageType.UNIT, line)).isEqualTo(2);
        assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", CoverageType.UNIT, line)).isEqualTo(2);
        assertThat(context.lineHits("ProjectKey:testclass.cpp", CoverageType.UNIT, line)).isEqualTo(1);
        assertThat(context.lineHits("ProjectKey:src/testclass.cpp", CoverageType.UNIT, line)).isEqualTo(1);
      }

      LOG.debug("Check full covered four conditions - line: 42");
      assertThat(context.conditions("ProjectKey:testclass.cpp", CoverageType.UNIT, 42)).isEqualTo(4);
      assertThat(context.conditions("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 42)).isEqualTo(4);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", CoverageType.UNIT, 42)).isEqualTo(4);
      assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 42)).isEqualTo(4);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", CoverageType.UNIT, 42)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 42)).isEqualTo(1);

      LOG.debug("Check partial covered two condition - line: 19");
      assertThat(context.conditions("ProjectKey:testclass.cpp", CoverageType.UNIT, 19)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", CoverageType.UNIT, 19)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", CoverageType.UNIT, 19)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 19)).isEqualTo(1);

      LOG.debug("Check full covered six conditions - line: 37");
      assertThat(context.conditions("ProjectKey:testclass.cpp", CoverageType.UNIT, 37)).isEqualTo(6);
      assertThat(context.conditions("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 37)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:testclass.cpp", CoverageType.UNIT, 37)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 37)).isEqualTo(6);
      assertThat(context.lineHits("ProjectKey:testclass.cpp", CoverageType.UNIT, 37)).isEqualTo(1);
      assertThat(context.lineHits("ProjectKey:src/testclass.cpp", CoverageType.UNIT, 37)).isEqualTo(1);
    }
  }

  @Test
  public void shouldParseTopLevelFiles() {
    // read top level folder name from report file
    String coverageReport = "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-win.xml";
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    if (TestUtils.isWindows()) {
      Settings settings = new Settings();
      settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), coverageReport);

      context.setSettings(settings);
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "randomfoldernamethatihopeknowmachinehas/test/test.c").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "randomfoldernamethatihopeknowmachinehas/test2/test2.c").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
      context.fileSystem().add(new DefaultInputFile("ProjectKey", "randomfoldernamethatihopeknowmachinehas/main.c").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

      sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
      sensor.execute(context, linesOfCodeByFile);

      assertThat(context.lineHits("ProjectKey:randomfoldernamethatihopeknowmachinehas/test/test.c", CoverageType.UNIT, 4)).isEqualTo(1);
      assertThat(context.conditions("ProjectKey:randomfoldernamethatihopeknowmachinehas/test/test.c", CoverageType.UNIT, 7)).isEqualTo(2);
    }
  }

  @Test
  public void shouldReportAllProbes() {

    String coverageReport = "coverage-reports/bullseye/bullseye-coverage-Linux-V8.9.60.xml";
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    if (TestUtils.isWindows()) {
      Settings settings = new Settings();
      settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), coverageReport);

      context.setSettings(settings);
      String[] fileList = new String [] {
          "covfile/import/cereal/archives/json.hpp",
          "covfile/import/jpeg-compressor/src/jpgd.cpp",
          "covfile/src/main/vr_io/src/ModalityLUTJson.cpp",
          "covfile/src/test/vr_core/Image_unittest.cpp",
          "covfile/import/cereal/external/base64.hpp",
          "covfile/import/cereal/details/traits.hpp",
          "covfile/import/jpeg-compressor/src/jpge.cpp",
          "covfile/src/main/vr_io/src/RenderingParamJson.cpp",
          "covfile/import/cereal/external/rapidjson/prettywriter.h",
          "covfile/import/cereal/external/rapidjson/document.h",
          "covfile/src/main/main/main.cpp",
          "covfile/import/cereal/details/static_object.hpp",
          "covfile/src/main/vr_core/src/VR.cpp"
      };
      
      StringBuilder sourceContent = new StringBuilder();
      sourceContent.append("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
      for (int i = 0; i<4000; i++) {
        sourceContent.append('\n');
      }
      
      for (String filepath: fileList) {
        context.fileSystem().add(new DefaultInputFile("ProjectKey", filepath).setLanguage("cpp").initMetadata(sourceContent.toString()));
      }
      sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
      sensor.execute(context, linesOfCodeByFile);

      int[] coveredCondition = new int [] { 496, 524};

      for (int line : coveredCondition) {
        LOG.debug("Check conditions line: {}", line);
        assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, line)).isEqualTo(2);
        assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, line)).isEqualTo(2);
      }

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 530)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 530)).isEqualTo(5);

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 483)).isEqualTo(6);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 483)).isEqualTo(3);

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 552)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 552)).isEqualTo(1);

      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 495)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 495)).isEqualTo(1);

      LOG.debug("Switch-label probe");
      assertThat(context.lineHits("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 474)).isEqualTo(0);
      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 474)).isEqualTo(1);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 474)).isEqualTo(0);
      assertThat(context.lineHits("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 475)).isEqualTo(1);
      assertThat(context.conditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 475)).isEqualTo(1);
      assertThat(context.coveredConditions("ProjectKey:covfile/import/cereal/archives/json.hpp", CoverageType.UNIT, 475)).isEqualTo(1);

      LOG.debug("Try and catch probe on one line");
      assertThat(context.lineHits("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", CoverageType.UNIT, 39)).isEqualTo(1);
      assertThat(context.conditions("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", CoverageType.UNIT, 39)).isEqualTo(2);
      assertThat(context.coveredConditions("ProjectKey:covfile/src/main/vr_core/src/VR.cpp", CoverageType.UNIT, 39)).isEqualTo(1);
    }
  }

}

