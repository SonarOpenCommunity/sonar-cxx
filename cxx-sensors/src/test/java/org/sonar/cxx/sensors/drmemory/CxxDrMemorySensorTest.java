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
package org.sonar.cxx.sensors.drmemory;

import java.nio.charset.Charset;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxDrMemorySensorTest {

  private DefaultFileSystem fs;
  private CxxLanguage language;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxDrMemorySensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxDrMemorySensor.REPORT_PATH_KEY);
  }

  @Test
  public void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV1() {

    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .initMetadata("asd\nasdas\nasda\n").setCharset(Charset.forName("UTF-8")).build();

    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    context.settings().setProperty(language.getPluginProperty(CxxDrMemorySensor.REPORT_PATH_KEY),
      "drmemory-reports/drmemory-result-SAMPLE-V1.txt");
    context.fileSystem().add(inputFile);

    CxxDrMemorySensor sensor = new CxxDrMemorySensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }
  
  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    CxxDrMemorySensor sensor = new CxxDrMemorySensor(language);
    sensor.describe(descriptor);

    SoftAssertions softly = new SoftAssertions(); 
    softly.assertThat(descriptor.name()).isEqualTo(language.getName() + " DrMemorySensor");
    softly.assertThat(descriptor.languages()).containsOnly(language.getKey());
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxDrMemoryRuleRepository.KEY);
    softly.assertAll();
  }
  
}
