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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxCompilerSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();
  private SensorContextTester context;
  private CxxCompilerSensorMock sensor;

  @BeforeEach
  public void setUp() {
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
    fs = TestUtils.mockFileSystem();
    context = SensorContextTester.create(fs.baseDir());
    context.setSettings(settings);
    sensor = new CxxCompilerSensorMock(context);
  }

  @Test
  void testFileNotFound() {
    var report = new File("");
    sensor.setRegex("(?<test>.*)");
    sensor.testExecuteReport(report);
    var log = logTester.logs().toString();
    assertThat(log).contains("FileNotFoundException");
  }

  @Test
  void testRegexEmpty() {
    var report = new File("");
    sensor.testExecuteReport(report);
    var log = logTester.logs().toString();
    assertThat(log).contains("empty custom regular expression");
  }

  @Test
  void testRegexInvalid() {
    var report = new File(fs.baseDir(), "compiler-reports/VC-report.vclog");
    sensor.setRegex("(?<test>*)");
    sensor.testExecuteReport(report);
    var log = logTester.logs().toString();
    assertThat(log).contains("PatternSyntaxException");
  }

  @Test
  void testRegexNamedGroupMissing() {
    var report = new File(fs.baseDir(), "compiler-reports/VC-report.vclog");
    sensor.setRegex(".*");
    sensor.testExecuteReport(report);
    var log = logTester.logs().toString();
    assertThat(log).contains("contains no named-capturing group");
  }

  private class CxxCompilerSensorMock extends CxxCompilerSensor {

    private String regex = "";

    public CxxCompilerSensorMock(SensorContext context) {
      this.context = context;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
    }

    public void testExecuteReport(File report) {
      executeReport(report);
    }

    public void setRegex(String regex) {
      this.regex = regex;
    }

    @Override
    protected String getCompilerKey() {
      return "XXX";
    }

    @Override
    protected String getEncoding() {
      return StandardCharsets.UTF_8.name();
    }

    @Override
    protected String getRegex() {
      return regex;
    }

    @Override
    protected String getReportPathsKey() {
      return "cxx.reportPaths";
    }

    @Override
    protected String getRuleRepositoryKey() {
      return "cxx.XXX";
    }

  }

}
