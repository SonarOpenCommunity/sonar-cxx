/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.clangtidy;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxClangTidySensorTest {

  private DefaultFileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings.setProperty(CxxClangTidySensor.REPORT_ENCODING_DEF, StandardCharsets.UTF_8.name());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
  }

  @Test
  void shouldIgnoreIssuesIfResourceNotFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.error.txt"
    );
    context.setSettings(settings);

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void shouldReportDefaultRuleId() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-default-rule-id.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("""
                    asd
                    asdasdfghtzsdfghjuio
                    asda
                    """)
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(4);
    var issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("clang-diagnostic-error");
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("clang-diagnostic-error");
    assertThat(issuesList.get(2).ruleKey().rule()).isEqualTo("clang-diagnostic-warning");
    assertThat(issuesList.get(3).ruleKey().rule()).isEqualTo("clang-diagnostic-unknown");
  }

  @Test
  void shouldReportSameIssueInSameLineWithDifferentColumn() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-cols.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("""
                    asd
                        output[outputPos++] = table[((input[inputPos + 1] & 0x0f) << 2) | (input[inputPos + 2] >> 6)];
                    asda
                    """)
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(4);
    var issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("hicpp-signed-bitwise");
    assertThat(issuesList.get(0).primaryLocation().textRange().start().lineOffset()).isEqualTo(32);
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("hicpp-signed-bitwise");
    assertThat(issuesList.get(1).primaryLocation().textRange().start().lineOffset()).isEqualTo(33);
    assertThat(issuesList.get(2).ruleKey().rule()).isEqualTo("hicpp-signed-bitwise");
    assertThat(issuesList.get(2).primaryLocation().textRange().start().lineOffset()).isEqualTo(34);
    assertThat(issuesList.get(3).ruleKey().rule()).isEqualTo("hicpp-signed-bitwise");
    assertThat(issuesList.get(3).primaryLocation().textRange().start().lineOffset()).isEqualTo(71);
  }

  @Test
  void shouldRemoveDuplicateIssues() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-duplicates.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("""
                    asd
                                                   _identityFunction,
                                                   _identityFunction) {
                    asda
                    """)
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(2);
    var issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("clang-diagnostic-uninitialized");
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("clang-diagnostic-uninitialized");
  }

  @Test
  void shouldReportLineIfColumnIsInvalid() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-warning.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("""
                    asd
                    X
                    asda
                    """)
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
    var issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("readability-inconsistent-declaration-parameter-name");
  }

  @Test
  void shouldReportIssuesInFirstAndLastColumn() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-min-max-cols.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("0123456789\n")
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(2);
    var issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("first-column");
    assertThat(issuesList.get(0).primaryLocation().textRange().start().lineOffset()).isZero();
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("last-column");
    assertThat(issuesList.get(1).primaryLocation().textRange().start().lineOffset()).isEqualTo(9);
  }

  @Test
  void shouldReportAliasRuleIds() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-alias-rule-ids.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("""
                    asd
                        output[outputPos++] = table[((input[inputPos + 1] & 0x0f) << 2) | (input[inputPos + 2] >> 6)];
                    asda
                    """)
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(3);
    var issuesList = new ArrayList<Issue>(context.allIssues());
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("cppcoreguidelines-avoid-magic-numbers");
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("readability-magic-numbers");
    assertThat(issuesList.get(2).ruleKey().rule()).isEqualTo("test");
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "clang-tidy-reports/cpd.report-error.txt",
    "clang-tidy-reports/cpd.report-fatal-error.txt",
    "clang-tidy-reports/cpd.report-warning.txt",
    "clang-tidy-reports/cpd.report-nodiscard.txt"
  })
  void shouldReport(String reportFile) {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxClangTidySensor.REPORT_PATH_KEY, reportFile);
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("asd\nasdasdgghs\nasda\n")
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  void shouldReportFlow() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-note.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("ab\nab\nab\nab\nab\nab\nabcdefg\nabdefgrqwe\nab\nab\nabcdefghijklm\n")
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.allIssues()).hasSize(1); // one issue
    softly.assertThat(context.allIssues().iterator().next().flows()).hasSize(1); // with one flow
    softly.assertThat(context.allIssues().iterator().next().flows().get(0).locations()).hasSize(4); // with four items
    softly.assertAll();
  }

  @Test
  void invalidReportReportsNoIssues() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(
      CxxClangTidySensor.REPORT_PATH_KEY,
      "clang-tidy-reports/cpd.report-empty.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
      .create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx")
      .initMetadata("asd\nasdas\nasda\n")
      .build()
    );

    var sensor = new CxxClangTidySensor();
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    var sensor = new CxxClangTidySensor();
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Clang-Tidy report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxClangTidyRuleRepository.KEY);
    softly.assertAll();
  }

}
