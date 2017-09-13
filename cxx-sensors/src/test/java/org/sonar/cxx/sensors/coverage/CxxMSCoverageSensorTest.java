/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.Version;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxMSCoverageSensorTest {
  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;
  private SensorContextTester context;
  private CxxLanguage language;
  private Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private static final Version SQ_6_2 = Version.create(6, 2);

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
//    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY)).thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATHS_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxCoverageSensor.IT_REPORT_PATH_KEY);
    when(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY)).thenReturn("sonar.cxx." + CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY);
  }

 @Test
  public void shouldReportCorrectCoverage() {
    context = SensorContextTester.create(fs.baseDir());
    context.setSonarQubeVersion(SQ_6_2);

    Settings settings = new Settings();
//    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");    
    settings.setProperty(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY), "true");
    context.setSettings(settings);

    context.fileSystem().add(new DefaultInputFile("ProjectKey", "source/motorcontroller/motorcontroller.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("ProjectKey", "source/rootfinder/rootfinder.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

    sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
    sensor.execute(context, linesOfCodeByFile);
    
    int[] oneHitlinesA = new int[] {12, 14, 16, 19, 20, 21, 23, 25, 26, 27, 28};
    for (int oneHitline : oneHitlinesA) {
      assertThat(context.lineHits("ProjectKey:source/rootfinder/rootfinder.cpp", CoverageType.UNIT, oneHitline)).isEqualTo(1);
    }

    int[] oneHitlinesB = new int[] {9, 10, 11, 14, 15, 16, 19, 20, 21, 24, 25, 26, 29, 30, 31};
    for (int oneHitline : oneHitlinesB) {
      assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", CoverageType.UNIT, oneHitline)).isEqualTo(1);
    }

  }


 @Test
 public void shouldReportCorrectCoverageSQ62() {
   context = SensorContextTester.create(fs.baseDir());
   context.setSonarQubeVersion(SQ_6_2);

   Settings settings = new Settings();
//   settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATHS_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");
   settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");
   settings.setProperty(language.getPluginProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY), "coverage-reports/MSCoverage/MSCoverage.xml");
   settings.setProperty(language.getPluginProperty(CxxCoverageSensor.FORCE_ZERO_COVERAGE_KEY), "true");
   context.setSettings(settings);

   context.fileSystem().add(new DefaultInputFile("ProjectKey", "source/motorcontroller/motorcontroller.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
   context.fileSystem().add(new DefaultInputFile("ProjectKey", "source/rootfinder/rootfinder.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

   sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
   sensor.execute(context, linesOfCodeByFile);
   
   int[] oneHitlinesA = new int[] {12, 14, 16, 19, 20, 21, 23, 25, 26, 27, 28};
   for (int oneHitline : oneHitlinesA) {
     assertThat(context.lineHits("ProjectKey:source/rootfinder/rootfinder.cpp", CoverageType.UNIT, oneHitline)).isEqualTo(1);
}

   int[] oneHitlinesB = new int[] {9, 10, 11, 14, 15, 16, 19, 20, 21, 24, 25, 26, 29, 30, 31};
   for (int oneHitline : oneHitlinesB) {
     assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", CoverageType.UNIT, oneHitline)).isEqualTo(1);
   }
 }
 
 //@Test(expected=NullPointerException.class)
 @Test
 public void shouldConsumeEmptyReport() {
   context = SensorContextTester.create(fs.baseDir());
   context.setSonarQubeVersion(SQ_6_2);

   Settings settings = new Settings();
   settings.setProperty(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY), "coverage-reports/MSCoverage/empty-report.xml");
   context.setSettings(settings);
  
   context.setSettings(settings);
  
   context.fileSystem().add(new DefaultInputFile("ProjectKey", "source/motorcontroller/motorcontroller.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
   sensor = new CxxCoverageSensor(new CxxCoverageCache(), language, context);
   sensor.execute(context, linesOfCodeByFile);
   assertThat(context.lineHits("ProjectKey:source/motorcontroller/motorcontroller.cpp", CoverageType.UNIT, 1)).isNull();
 
 }
}

