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
package org.sonar.cxx.sensors.other;

import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxOtherSensorTest {

  @RegisterExtension
  private final LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  void shouldReportCorrectViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-ok.xml");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("sources/utils/code_chunks.cpp", 3));
    context.fileSystem().add(createTestInputFile("sources/utils/utils.cpp", 3));

    var sensor = new CxxOtherSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(2);
  }

  @Test
  void shouldReportFileLevelViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY,
      "externalrules-reports/externalrules-result-filelevelviolation.xml");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("sources/utils/code_chunks.cpp", 3));

    var sensor = new CxxOtherSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  void shouldReportProjectLevelViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY,
      "externalrules-reports/externalrules-result-projectlevelviolation.xml");
    context.setSettings(settings);

    var sensor = new CxxOtherSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  void shouldThrowExceptionWhenReportEmpty() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-empty.xml");
    context.setSettings(settings);
    var sensor = new CxxOtherSensor().setWebApi(null);

    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      sensor.execute(context);
    });
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldReportNoViolationsIfNoReportFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/noreport.xml");
    context.setSettings(settings);

    var sensor = new CxxOtherSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldThrowInCaseOfATrashyReport() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, false);
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-result-invalid.xml");
    context.setSettings(settings);
    var sensor = new CxxOtherSensor().setWebApi(null);

    IllegalStateException thrown = catchThrowableOfType(IllegalStateException.class, () -> {
      sensor.execute(context);
    });
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void shouldReportOnlyOneViolationAndRemoveDuplicates() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-with-duplicates.xml");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("sources/utils/code_chunks.cpp", 3));

    var sensor = new CxxOtherSensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxOtherSensor().setWebApi(null);
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX other analyser report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxOtherRepository.KEY);
    softly.assertAll();
  }

}
