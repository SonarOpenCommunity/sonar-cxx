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
package org.sonar.plugins.cxx.clangtidy;

import org.sonar.plugins.cxx.cppcheck.*;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cxx.TestUtils;

import org.junit.Before;
import org.junit.Test;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.cxx.CxxPlugin;

public class CxxClangTidySensorTest {

  private Settings settings;
  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    settings = new Settings();
  }

  @Test
  public void shouldIgnoreIssuesIfResourceNotFound() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject"));
    settings.setProperty(CxxClangTidySensor.REPORT_PATH_KEY, fs.baseDir().getAbsolutePath() + 
      "/clang-tidy-reports/cpd.report.txt");
    settings.setProperty(CxxPlugin.ERROR_RECOVERY_KEY, "True");
    CxxClangTidySensor sensor = new CxxClangTidySensor(settings);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }  
  
  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject"));
    settings.setProperty(CxxClangTidySensor.REPORT_PATH_KEY, fs.baseDir().getAbsolutePath() + 
      "/clang-tidy-reports/cpd.report.txt");
    settings.setProperty(CxxPlugin.ERROR_RECOVERY_KEY, "True");
    CxxClangTidySensor sensor = new CxxClangTidySensor(settings);
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void invalidReportReportsNoIssues() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject"));
    settings.setProperty(CxxClangTidySensor.REPORT_PATH_KEY, fs.baseDir().getAbsolutePath() + 
      "/clang-tidy-reports/cpd.report-empty.txt");
    settings.setProperty(CxxPlugin.ERROR_RECOVERY_KEY, "True");
    CxxClangTidySensor sensor = new CxxClangTidySensor(settings);
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }  
  
}
