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
package org.sonar.cxx.sensors.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxMSCoverageSensorTest {

  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private SensorContextTester context;
  private CxxLanguage language;
  private MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
  }

  @Test
  public void shouldReportCorrectCoverage() {
    context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/motorcontroller/motorcontroller.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/rootfinder/rootfinder.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);

    int[] oneHitlinesA = new int[]{12, 14, 16, 19, 20, 21, 23, 25, 26, 27, 28};
    for (int oneHitline : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:source/rootfinder/rootfinder.cpp", oneHitline)).isEqualTo(1);
    }

    int[] oneHitlinesB = new int[]{9, 10, 11, 14, 15, 16, 19, 20, 21, 24, 25, 26, 29, 30, 31};
    for (int oneHitline : oneHitlinesB) {
      assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", oneHitline)).isEqualTo(1);
    }
  }

  @Test
  public void shouldConsumeEmptyReport() {
    context = SensorContextTester.create(fs.baseDir());

    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/MSCoverage/empty-report.xml");
    context.setSettings(settings);

    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "source/motorcontroller/motorcontroller.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);
    assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", 1)).isNull();

  }
  
  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    context = SensorContextTester.create(fs.baseDir());
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.describe(descriptor);

    SoftAssertions softly = new SoftAssertions(); 
    softly.assertThat(descriptor.name()).isEqualTo(language.getName() + " CoverageSensor");
    softly.assertThat(descriptor.languages()).containsOnly(language.getKey());
    softly.assertAll();
  }
  
}
