/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxClangTidySensorTest {

  private DefaultFileSystem fs;
  private CxxLanguage language;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();

    when(language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY)).
            thenReturn("sonar.cxx." + CxxClangTidySensor.REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxClangTidySensor.REPORT_CHARSET_DEF)).
            thenReturn("UTF-8");
    when(language.IsRecoveryEnabled()).
            thenReturn(Optional.of(Boolean.TRUE));
  }

  @Test
  public void shouldIgnoreIssuesIfResourceNotFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
            language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY),
            "clang-tidy-reports/cpd.error.txt"
    );
    context.setSettings(settings);

    CxxClangTidySensor sensor = new CxxClangTidySensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldReportErrors() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
            language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY),
            "clang-tidy-reports/cpd.report-error.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
            .create("ProjectKey", "sources/utils/code_chunks.cpp")
            .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n")
            .build());

    CxxClangTidySensor sensor = new CxxClangTidySensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportWarnings() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
            language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY),
            "clang-tidy-reports/cpd.report-warning.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
            .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxClangTidySensor sensor = new CxxClangTidySensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportNodiscard() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
            language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY),
            "clang-tidy-reports/cpd.report-nodiscard.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
            .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxClangTidySensor sensor = new CxxClangTidySensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportFlow() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
            language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY),
            "clang-tidy-reports/cpd.report-note.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
            .create("ProjectKey", "sources/utils/code_chunks.cpp")
            .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n")
            .build());

    CxxClangTidySensor sensor = new CxxClangTidySensor(language);
    sensor.execute(context);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.allIssues()).hasSize(1); // one issue
    softly.assertThat(context.allIssues().iterator().next().flows()).hasSize(1); // with one flow
    softly.assertThat(context.allIssues().iterator().next().flows().get(0).locations()).hasSize(4); // with four items
    softly.assertAll();
  }

  @Test
  public void invalidReportReportsNoIssues() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(
            language.getPluginProperty(CxxClangTidySensor.REPORT_PATH_KEY),
            "clang-tidy-reports/cpd.report-empty.txt"
    );
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder
            .create("ProjectKey", "sources/utils/code_chunks.cpp")
            .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n")
            .build());

    CxxClangTidySensor sensor = new CxxClangTidySensor(language);

    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    CxxClangTidySensor sensor = new CxxClangTidySensor(language);
    sensor.describe(descriptor);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(descriptor.name())
            .isEqualTo(language.getName() + " ClangTidySensor");
    softly.assertThat(descriptor.languages())
            .containsOnly(language.getKey());
    softly.assertThat(descriptor.ruleRepositories())
            .containsOnly(CxxClangTidyRuleRepository.getRepositoryKey(language));
    softly.assertAll();
  }

}
