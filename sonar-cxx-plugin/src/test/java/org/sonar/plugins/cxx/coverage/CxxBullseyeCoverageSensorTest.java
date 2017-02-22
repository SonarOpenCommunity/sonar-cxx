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
package org.sonar.plugins.cxx.coverage;

import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.coverage.CoverageType;
import org.sonar.plugins.cxx.utils.CxxUtils;

public class CxxBullseyeCoverageSensorTest {
  private CxxCoverageSensor sensor;
  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  //@Test
  public void shouldReportCorrectCoverage() {
    Settings settings = new Settings();
    
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    
    settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/coverage-result-bullseye.xml");
    settings.setProperty(CxxCoverageSensor.IT_REPORT_PATH_KEY, "coverage-reports/bullseye/coverage-result-bullseye.xml");
    settings.setProperty(CxxCoverageSensor.OVERALL_REPORT_PATH_KEY, "coverage-reports/bullseye/coverage-result-bullseye.xml");

    context.fileSystem().add(new DefaultInputFile("myProjectKey", "src/testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));


    context.fileSystem().add(new DefaultInputFile("myProjectKey", "src/testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "source_1.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "src/testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "src/testclass.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "main.cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "testclass.h").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"));

    sensor = new CxxCoverageSensor(settings, new CxxCoverageCache());
    sensor.execute(context);
    assertThat(context.lineHits("myProjectKey:src/testclass.cpp", CoverageType.UNIT, 7)).isEqualTo(1);
    assertThat(context.lineHits("myProjectKey:main.cpp", CoverageType.UNIT, 7)).isEqualTo(1);
    assertThat(context.lineHits("myProjectKey:testclass.cpp", CoverageType.UNIT, 7)).isEqualTo(1);
  }

  //@Test @todo
  public void shoulParseTopLevelFiles() {
    Settings settings = new Settings();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-win.xml");
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/anotherincludeattop.h")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/test/test.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/test2/test2.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/main.c")).setLanguage("cpp"));
    } else {
      settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/bullseye-coverage-report-data-in-root-node-linux.xml");
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/test/anotherincludeattop.h").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/test/test/test.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/test/test2/test2.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/test/main.c").setLanguage("cpp"));
    }
    sensor = new CxxCoverageSensor(settings, new CxxCoverageCache());
    sensor.execute(context);
    verify(context, times(28)).newCoverage();
  }

  //@Test @todo
  public void shoulCorrectlyHandleDriveLettersWithoutSlash() {
    Settings settings = new Settings();
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    if (TestUtils.isWindows()) {
      settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash-win.xml");
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/main.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas/test.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/randomfoldernamethatihopeknowmachinehas2/test2.c")).setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", CxxUtils.normalizePath("C:/anotherincludeattop.h")).setLanguage("cpp"));
    } else {
      settings.setProperty(CxxCoverageSensor.REPORT_PATH_KEY, "coverage-reports/bullseye/bullseye-coverage-drive-letter-without-slash-linux.xml");
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/main.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/test/test.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/test2/test2.c").setLanguage("cpp"));
      context.fileSystem().add(new DefaultInputFile("myProjectKey", "/c/anotherincludeattop.h").setLanguage("cpp"));
    }
    sensor = new CxxCoverageSensor(settings, new CxxCoverageCache());
    sensor.execute(context);
    verify(context, times(28)).newCoverage();
  }
}
