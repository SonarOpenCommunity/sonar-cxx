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
package org.sonar.cxx.sensors.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;

class CxxReportSensor_getReports_Test {

  private static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPaths";

  @TempDir
  File tempDir;

  private final MapSettings settings = new MapSettings();

  @Test
  void testAbsoluteInsideBasedir() throws IOException {
    var absReportFile = new File(tempDir, "path/to/report.xml").getAbsoluteFile();
    FileUtils.touch(absReportFile);

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(1);
  }

  @Test
  void testAbsoluteOutsideBasedir() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(1);
  }

  @Test
  void testAbsoluteOutsideBasedirWithGlobbing() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(1);
  }

  @Test
  void testAbsoluteOutsideBasedirAndRelative() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");

    var relativeReport = "path/to/report.xml";
    FileUtils.touch(new File(tempDir, relativeReport));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + "," + relativeReport);
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(2);
  }

  @Test
  void testAbsoluteOutsideBasedirWithGlobbingAndRelativeWithGlobbing() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    FileUtils.touch(new File(tempDir, "report.xml"));
    FileUtils.touch(new File(tempDir, "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(tempDir, "path/to/a/report.xml"));
    FileUtils.touch(new File(tempDir, "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(tempDir, "path/to/some/reports/2.xml"));
    FileUtils.touch(new File(tempDir, "some/reports/a"));
    FileUtils.touch(new File(tempDir, "some/reports/b"));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + ",**/*.xml");
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(6);
  }

  @Test
  void testAbsoluteOutsideBasedirWithGlobbingAndNestedRelativeWithGlobbing() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    var absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    FileUtils.touch(new File(tempDir, "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(tempDir, "path/to/a/report.xml"));
    FileUtils.touch(new File(tempDir, "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(tempDir, "path/to/some/reports/2.xml"));
    FileUtils.touch(new File(tempDir, "some/reports/a.xml"));
    FileUtils.touch(new File(tempDir, "some/reports/b.xml"));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + ",path/**/*.xml");
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(5);
  }

  @Test
  void testRelativeBackticksOutsideBasedirThenBackInside() throws IOException {
    FileUtils.touch(new File(tempDir, "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(tempDir, "path/to/a/report.xml"));
    FileUtils.touch(new File(tempDir, "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(tempDir, "path/to/some/reports/2.xml"));

    settings.setProperty(REPORT_PATH_KEY, "../" + tempDir.getName() + "/path/**/*.xml");
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).hasSize(4);
  }

  @Test
  void testRelativeExcessiveBackticks() throws IOException {
    FileUtils.touch(new File(tempDir, "path/to/supercoolreport.xml"));

    // Might be valid if java.io.tmpdir is nested excessively deep -- not likely
    settings.setProperty(REPORT_PATH_KEY, "../../../../../../../../../../../../../../../../../../../../../../../../"
                                            + "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../"
                                          + "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../"
                                          + "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../*.xml");
    var context = SensorContextTester.create(tempDir);
    context.setSettings(settings);

    List<File> reports = CxxUtils.getFiles(context, REPORT_PATH_KEY);
    assertThat(reports).isEmpty();
  }

}
