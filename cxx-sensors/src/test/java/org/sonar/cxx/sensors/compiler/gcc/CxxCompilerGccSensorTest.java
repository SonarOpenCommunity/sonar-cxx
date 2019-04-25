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
package org.sonar.cxx.sensors.compiler.gcc;

import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.compiler.CxxCompilerSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCompilerGccSensorTest {

  private DefaultFileSystem fs;
  private CxxLanguage language;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxCompilerGccSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxCompilerGccSensor.REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCompilerGccSensor.REPORT_CHARSET_DEF)).thenReturn("sonar.cxx." + CxxCompilerGccSensor.REPORT_CHARSET_DEF);
    when(language.getPluginProperty(CxxCompilerGccSensor.REPORT_REGEX_DEF)).thenReturn("sonar.cxx." + CxxCompilerGccSensor.REPORT_REGEX_DEF);
  }

  @Test
  public void sensorDescriptorGcc() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    CxxCompilerGccSensor sensor = new CxxCompilerGccSensor(language);
    sensor.describe(descriptor);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo(language.getName() + " CxxCompilerGccSensor");
    softly.assertThat(descriptor.languages()).containsOnly(language.getKey());
    softly.assertThat(descriptor.ruleRepositories())
      .containsOnly(CxxCompilerGccRuleRepository.getRepositoryKey(language));
    softly.assertAll();
  }

  @Test
  public void shouldReportCorrectGccViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCompilerGccSensor.REPORT_PATH_KEY), "compiler-reports/build.gcclog");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "src/zipmanager.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxCompilerSensor sensor = new CxxCompilerGccSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(4);
  }

  @Test
  public void shouldReportCorrectGccViolationsWithOrWithoutIds() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCompilerGccSensor.REPORT_PATH_KEY), "compiler-reports/build-warning-without-id.gcclog");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "main.c")
      .setLanguage("c").initMetadata("asd\nasdas\nasda\n").build());

    CxxCompilerSensor sensor = new CxxCompilerGccSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(2);
    ArrayList<Issue> issuesList = new ArrayList<>(context.allIssues());
    // warning without activation switch (no id) should be mapped to the "default" rule
    assertThat(issuesList.get(0).ruleKey().rule()).isEqualTo("default");
    // warning with activation switch should be mapped to the matching rule
    assertThat(issuesList.get(1).ruleKey().rule()).isEqualTo("-Wunused-variable");
  }
  
}
