/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.Issue.Flow;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.TestUtils;

import com.google.common.collect.Iterables;

public class CxxClangSASensorTest {

  private DefaultFileSystem fs;
  private CxxLanguage language;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxClangSASensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxClangSASensor.REPORT_PATH_KEY);
    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.TRUE));
  }

  @Test
  public void shouldIgnoreIssuesIfResourceNotFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxClangSASensor.REPORT_PATH_KEY), "clangsa-reports/clangsa-empty.plist");
    context.setSettings(settings);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxClangSASensor.REPORT_PATH_KEY), "clangsa-reports/clangsa-report.plist");
    context.setSettings(settings);

    /*
     * 2 issues
     */
    DefaultInputFile testFile0 = TestInputFileBuilder.create("ProjectKey", "src/lib/component0.cc").setLanguage("cpp")
      .initMetadata("asd\nasdas\nasda\n").build();
    /*
     * 1 issue
     */
    DefaultInputFile testFile1 = TestInputFileBuilder.create("ProjectKey", "src/lib/component1.cc").setLanguage("cpp")
      .initMetadata("asd\nasdas\nasda\n").build();

    context.fileSystem().add(testFile0);
    context.fileSystem().add(testFile1);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(3);
  }

  @Test
  public void shouldReportCorrectMetrics() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxClangSASensor.REPORT_PATH_KEY), "clangsa-reports/clangsa-report.plist");
    context.setSettings(settings);

    /*
     * 2 issues
     */
    DefaultInputFile testFile0 = TestInputFileBuilder.create("ProjectKey", "src/lib/component0.cc").setLanguage("cpp")
      .initMetadata("asd\nasdas\nasda\n").build();
    /*
     * 1 issue
     */
    DefaultInputFile testFile1 = TestInputFileBuilder.create("ProjectKey", "src/lib/component1.cc").setLanguage("cpp")
      .initMetadata("asd\nasdas\nasda\n").build();

    context.fileSystem().add(testFile0);
    context.fileSystem().add(testFile1);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    sensor.execute(context);

    // assert that the files were annotated with a new measurement (metric) for
    // number of ClangSA issues
    SoftAssertions softly = new SoftAssertions();
    Measure<Integer> nrOfIssuesFile0 = context.<Integer>measure(testFile0.key(),
      language.getMetric(CxxMetricsFactory.Key.CLANG_SA_SENSOR_ISSUES_KEY));
    softly.assertThat(nrOfIssuesFile0.value()).isEqualTo(2);

    Measure<Integer> nrOfIssuesFile1 = context.<Integer>measure(testFile1.key(),
      language.getMetric(CxxMetricsFactory.Key.CLANG_SA_SENSOR_ISSUES_KEY));
    softly.assertThat(nrOfIssuesFile1.value()).isEqualTo(1);

    // assert that the module is annotated with the total sum of ClangSA issues
    Measure<Integer> nrOfIssuesModule = context.<Integer>measure(context.module().key(),
      language.getMetric(CxxMetricsFactory.Key.CLANG_SA_SENSOR_ISSUES_KEY));
    softly.assertThat(nrOfIssuesModule.value()).isEqualTo(3);
    softly.assertAll();
  }

  private String generateTestFileContents(int linesNum, int lineLen) {
    String line = RandomStringUtils.randomAscii(lineLen);
    return String.join("\n", Collections.nCopies(linesNum, line));
  }

  @Test
  public void shouldReportCorrectFlows() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxClangSASensor.REPORT_PATH_KEY),
        "clangsa-reports/clangsa-report.plist");
    context.setSettings(settings);

    /*
     * 2 issues
     */
    DefaultInputFile testFile0 = TestInputFileBuilder.create("ProjectKey", "src/lib/component0.cc").setLanguage("cpp")
        .setContents(generateTestFileContents(100, 80)).build();
    /*
     * 1 issue
     */
    DefaultInputFile testFile1 = TestInputFileBuilder.create("ProjectKey", "src/lib/component1.cc").setLanguage("cpp")
        .setContents(generateTestFileContents(100, 80)).build();

    context.fileSystem().add(testFile0);
    context.fileSystem().add(testFile1);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
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
        assertThat(issueLocation.textRange().start().line()).isEqualTo(31);
        assertThat(issueLocation.textRange().end().line()).isEqualTo(31);
      }

      {
        IssueLocation issueLocation = flow.locations().get(0);
        assertThat(issueLocation.inputComponent()).isEqualTo(testFile0);
        assertThat(issueLocation.message()).isEqualTo("Branch condition evaluates to a garbage value");
        assertThat(issueLocation.textRange().start().line()).isEqualTo(32);
        assertThat(issueLocation.textRange().end().line()).isEqualTo(32);
      }
    }

    // paths with just one element are not reported as flows to avoid
    // presenting 1-element flows in SonarQube UI
    {
      Issue issue = Iterables.get(context.allIssues(), 1);
      assertThat(issue.flows()).hasSize(0);
    }

    {
      Issue issue = Iterables.get(context.allIssues(), 2);
      assertThat(issue.flows()).hasSize(0);
    }
  }

  @Test
  public void invalidReportReportsNoIssues() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxClangSASensor.REPORT_PATH_KEY), "clangsa-reports/clangsa-reportXYZ.plist");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "src/lib/component1.cc")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    CxxClangSASensor sensor = new CxxClangSASensor(language);
    sensor.describe(descriptor);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo(language.getName() + " ClangSASensor");
    softly.assertThat(descriptor.languages()).containsOnly(language.getKey());
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxClangSARuleRepository.getRepositoryKey(language));
    softly.assertAll();
  }

}
