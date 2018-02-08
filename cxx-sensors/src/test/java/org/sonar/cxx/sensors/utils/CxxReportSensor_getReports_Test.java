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
package org.sonar.cxx.sensors.utils;


import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.config.internal.MapSettings;

public class CxxReportSensor_getReports_Test {

  private static final String REPORT_PATH_KEY = "sonar.cxx.cppcheck.reportPath";
  private MapSettings settings = new MapSettings();

  @Rule
  public TemporaryFolder base = new TemporaryFolder();

  @Test
  public void testAbsoluteInsideBasedir() throws IOException {
    File absReportFile = new File(base.getRoot(), "path/to/report.xml").getAbsoluteFile();
    FileUtils.touch(absReportFile);

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(1);
  }

  @Test
  public void testAbsoluteOutsideBasedir() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(1);
  }

  @Test
  public void testAbsoluteOutsideBasedirWithGlobbing() {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString());

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(2);
  }

  @Test
  public void testAbsoluteOutsideBasedirAndRelative() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-V2.xml");

    String relativeReport = "path/to/report.xml";
    FileUtils.touch(new File(base.getRoot(), relativeReport));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + "," + relativeReport);

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(2);
  }

  @Test
  public void testAbsoluteOutsideBasedirWithGlobbingAndRelativeWithGlobbing() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    FileUtils.touch(new File(base.getRoot(), "report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/a/report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/2.xml"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/a"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/b"));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + ",**/*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(7);
  }

  @Test
  public void testAbsoluteOutsideBasedirWithGlobbingAndNestedRelativeWithGlobbing() throws IOException {
    File absReportsProject = TestUtils.loadResource("/org/sonar/cxx/sensors/reports-project").getAbsoluteFile();
    File absReportFile = new File(absReportsProject, "cppcheck-reports/cppcheck-result-SAMPLE-*.xml");

    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/a/report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/2.xml"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/a.xml"));
    FileUtils.touch(new File(base.getRoot(), "some/reports/b.xml"));

    settings.setProperty(REPORT_PATH_KEY, absReportFile.toString() + ",path/**/*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(6);
  }

  @Test
  public void testRelativeBackticksOutsideBasedirThenBackInside() throws IOException {
    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/a/report.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/1.xml"));
    FileUtils.touch(new File(base.getRoot(), "path/to/some/reports/2.xml"));

    settings.setProperty(REPORT_PATH_KEY, "../" + base.getRoot().getName() + "/path/**/*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(4);
  }

  @Test
  public void testRelativeExcessiveBackticks() throws IOException {
    FileUtils.touch(new File(base.getRoot(), "path/to/supercoolreport.xml"));

    // Might be valid if java.io.tmpdir is nested excessively deep -- not likely    
    settings.setProperty(REPORT_PATH_KEY, "../../../../../../../../../../../../../../../../../../../../../../../../"
      + "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../"
      + "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../"
      + "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../*.xml");

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), base.getRoot(), REPORT_PATH_KEY);
    assertThat(reports.size()).isEqualTo(0);
  }
}
