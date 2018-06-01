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

import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxTestwellCtcParserTest {

  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private SensorContextTester context;
  private CxxLanguage language;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
  }

  @Test
  public void shouldReportCorrectCoverage() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/TestwellCTC/report_small_v8.txt");
    context.setSettings(settings);
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "HGBuildNumberLookup.cpp")
      .setLanguage("cpp").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);
    
    assertThat(context.lineHits("ProjectKey:HGBuildNumberLookup.cpp", 42)).isEqualTo(10);
    assertThat(context.coveredConditions("ProjectKey:HGBuildNumberLookup.cpp", 50)).isEqualTo(1);
    assertThat(context.coveredConditions("ProjectKey:HGBuildNumberLookup.cpp", 56)).isEqualTo(2);
    
  }

  @Test
  public void shouldConsumeLargeReport() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/TestwellCTC/report_big.txt");
    context.setSettings(settings);
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "test-wildmatch.c")
      .setLanguage("cpp").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "credential-store.c")
      .setLanguage("cpp").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);
    
    assertThat(context.lineHits("ProjectKey:test-wildmatch.c", 3)).isEqualTo(209);
    assertThat(context.coveredConditions("ProjectKey:test-wildmatch.c", 6)).isEqualTo(2);
    assertThat(context.conditions("ProjectKey:credential-store.c", 78)).isEqualTo(8);

  }

  @Test
  public void shouldConsumeEmptyReport() {
    context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/TestwellCTC/report_empty.txt");
    context.setSettings(settings);
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "test-wildmatch.c")
      .setLanguage("cpp").initMetadata("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n").build());
    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context);
    
    assertThat(context.lineHits("ProjectKey:test-wildmatch.c", 3)).isNull();

  }
}