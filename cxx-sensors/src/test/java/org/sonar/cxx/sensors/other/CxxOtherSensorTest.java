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
package org.sonar.cxx.sensors.other;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxOtherSensorTest {

  private CxxOtherSensor sensor;
  private DefaultFileSystem fs;
  private CxxLanguage language;
  private MapSettings settings = new MapSettings();
  @Rule
  public LogTester logTester = new LogTester();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();

    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxOtherSensor.REPORT_PATH_KEY);
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-result-ok.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(2);
  }

  @Test
  public void shouldReportFileLevelViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-result-filelevelviolation.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportProjectLevelViolations() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-result-projectlevelviolation.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowExceptionWhenReportEmpty() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.FALSE));

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-result-empty.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldReportNoViolationsIfNoReportFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/noreport.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowInCaseOfATrashyReport() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    when(language.IsRecoveryEnabled()).thenReturn(Optional.of(Boolean.FALSE));

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-result-invalid.xml");
    context.setSettings(settings);

    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
  }

  @Test
  public void shouldReportOnlyOneViolationAndRemoveDuplicates() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-with-duplicates.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldReportNothing() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    when(language.getPluginProperty("other.xslt.1.stylesheet")).thenReturn("other.xslt.1.stylesheet");
    when(language.getPluginProperty("other.xslt.1.inputs")).thenReturn("other.xslt.1.inputs");
    when(language.getPluginProperty("other.xslt.1.outputs")).thenReturn("other.xslt.1.outputs");

    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(logTester.logs(LoggerLevel.ERROR)).isEmpty();
  }

  @Test
  public void shouldNotCreateMessage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    when(language.getPluginProperty("other.xslt.1.stylesheet")).thenReturn("something");

    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldCreateMissingStylesheetMessage() {
    logTester.clear();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    when(language.getPluginProperty("other.xslt.1.stylesheet")).thenReturn("something");
    when(language.getPluginProperty("other.xslt.1.outputs")).thenReturn("outputs");

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-reports/externalrules-with-duplicates.xml");
    settings.setProperty("outputs", "outputs");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("something is not defined.");

  }

  @Test
  public void shouldCreateMissingInputKeyMessage() {
    logTester.clear();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    when(language.getPluginProperty("other.xslt.1.stylesheet")).thenReturn("something");
    when(language.getPluginProperty("other.xslt.1.outputs")).thenReturn("something");

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "something");
    settings.setProperty("something", "something");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("inputKey is not defined.");
  }

  @Test
  public void shouldCreateEmptyInputsMessage() {
    logTester.clear();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    when(language.getPluginProperty("other.xslt.1.stylesheet")).thenReturn("something");
    when(language.getPluginProperty("other.xslt.1.inputs")).thenReturn("someInput");

    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "something");
    settings.setProperty("something", "something");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());
    sensor = new CxxOtherSensor(language);
    sensor.execute(context);
    assertThat(logTester.logs(LoggerLevel.ERROR)).contains("someInput file is not defined.");
  }

  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor = new CxxOtherSensor(language);
    sensor.describe(descriptor);

    SoftAssertions softly = new SoftAssertions(); 
    softly.assertThat(descriptor.name()).isEqualTo(language.getName() + " ExternalRulesSensor");
    softly.assertThat(descriptor.languages()).containsOnly(language.getKey());
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxOtherRepository.KEY);
    softly.assertAll();
  }
  
}
