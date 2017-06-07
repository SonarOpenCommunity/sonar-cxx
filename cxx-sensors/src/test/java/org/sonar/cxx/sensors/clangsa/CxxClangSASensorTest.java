
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
package org.sonar.cxx.sensors.clangsa;

import org.sonar.cxx.sensors.clangsa.CxxClangSASensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;

import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;

public class CxxClangSASensorTest {

  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldIgnoreIssuesIfResourceNotFound() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject2"));
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxClangSASensor.REPORT_PATH_KEY))
            .thenReturn(new String [] { fs.baseDir().getAbsolutePath() + "/clangsa-reports/clangsa-empty.plist" });
    when(language.IsRecoveryEnabled()).thenReturn(true);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldReportCorrectViolations() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject2"));

    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxClangSASensor.REPORT_PATH_KEY))
            .thenReturn(new String [] { fs.baseDir().getAbsolutePath() + "/clangsa-reports/clangsa-report.plist" });
    when(language.IsRecoveryEnabled()).thenReturn(true);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "src/lib/component1.cc").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(2);
  }

  @Test
  public void invalidReportReportsNoIssues() {
    SensorContextTester context = SensorContextTester.create(new File("src/samples/SampleProject2"));

    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxClangSASensor.REPORT_PATH_KEY))
            .thenReturn(new String [] { fs.baseDir().getAbsolutePath() + "/clangsa-reports/clangsa-empty.plist" });
    when(language.IsRecoveryEnabled()).thenReturn(true);

    CxxClangSASensor sensor = new CxxClangSASensor(language);
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "src/lib/component1.cc").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")));
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(0);
  }

}
