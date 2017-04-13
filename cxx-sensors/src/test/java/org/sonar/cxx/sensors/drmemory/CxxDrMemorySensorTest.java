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
package org.sonar.cxx.sensors.drmemory;

import org.sonar.cxx.sensors.drmemory.CxxDrMemorySensor;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxDrMemorySensorTest {

  private DefaultFileSystem fs;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void shouldIgnoreAViolationWhenTheResourceCouldntBeFoundV1() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxDrMemorySensor.REPORT_PATH_KEY)).thenReturn(new String[] {"drmemory-reports/drmemory-result-SAMPLE-V1.txt"});    
    
    context.fileSystem().add(new DefaultInputFile("myProjectKey", "sources/utils/code_chunks.cpp").setLanguage("cpp").setLanguage("cpp").initMetadata("asd\nasdas\nasda\n"));
    CxxDrMemorySensor sensor = new CxxDrMemorySensor(language);
    sensor.execute(context);
    assertThat(context.allIssues()).hasSize(1);
  }
}
