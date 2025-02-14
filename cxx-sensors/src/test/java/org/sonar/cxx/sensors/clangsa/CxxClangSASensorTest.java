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
package org.sonar.cxx.sensors.clangsa;

import com.google.common.collect.Iterables;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.Issue.Flow;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxClangSASensorTest {

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
  }

  @Test
  void shouldIgnoreIssuesIfResourceNotFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxClangSASensor.REPORT_PATH_KEY, "clangsa-reports/clangsa-empty.plist");
    context.setSettings(settings);

    var sensor = new CxxClangSASensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldReportCorrectViolations() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxClangSASensor.REPORT_PATH_KEY, "clangsa-reports/clangsa-report.plist");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("src/lib/component0.cc", 9));
    context.fileSystem().add(createTestInputFile("src/lib/component1.cc", 3));

    var sensor = new CxxClangSASensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(3);
  }

  @Test
  void shouldReportCorrectFlows() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxClangSASensor.REPORT_PATH_KEY,
      "clangsa-reports/clangsa-report.plist");
    context.setSettings(settings);

    var testFile0 = createTestInputFile("src/lib/component0.cc", 100);
    context.fileSystem().add(testFile0);
    context.fileSystem().add(createTestInputFile("src/lib/component1.cc", 100));

    var sensor = new CxxClangSASensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(3);

    {
      Issue issue = Iterables.get(context.allIssues(), 0);
      assertThat(issue.flows()).hasSize(1);
      Flow flow = issue.flows().get(0);
      assertThat(flow.locations()).hasSize(2);

      // flow locations are enumerated backwards - from the final to the root location
      {
        IssueLocation issueLocation = flow.locations().get(1);
        assertThat(issueLocation.inputComponent()).isEqualTo(testFile0);
        assertThat(issueLocation.message()).isEqualTo("'a' declared without an initial value");
        assertThat(issueLocation.textRange().start().line()).isEqualTo(5);
        assertThat(issueLocation.textRange().end().line()).isEqualTo(5);
      }

      {
        IssueLocation issueLocation = flow.locations().get(0);
        assertThat(issueLocation.inputComponent()).isEqualTo(testFile0);
        assertThat(issueLocation.message()).isEqualTo("Branch condition evaluates to a garbage value");
        assertThat(issueLocation.textRange().start().line()).isEqualTo(6);
        assertThat(issueLocation.textRange().end().line()).isEqualTo(6);
      }
    }

    // paths with just one element are not reported as flows to avoid
    // presenting 1-element flows in SonarQube UI
    {
      Issue issue = Iterables.get(context.allIssues(), 1);
      assertThat(issue.flows()).hasSize(1);
    }

    {
      Issue issue = Iterables.get(context.allIssues(), 2);
      assertThat(issue.flows()).hasSize(1);
    }
  }

  @Test
  void invalidReportReportsNoIssues() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxClangSASensor.REPORT_PATH_KEY, "clangsa-reports/clangsa-reportXYZ.plist");
    context.setSettings(settings);

    context.fileSystem().add(createTestInputFile("src/lib/component1.cc", 3));

    var sensor = new CxxClangSASensor().setWebApi(null);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxClangSASensor().setWebApi(null);
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Clang Static Analyzer report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxClangSARuleRepository.KEY);
    softly.assertAll();
  }

}
