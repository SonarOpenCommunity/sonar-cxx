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
package org.sonar.cxx.sensors.valgrind;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxValgrindSensorTest {

  private CxxValgrindSensor sensor;
  private DefaultFileSystem fs;
  private CxxLanguage language;
  private MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    sensor = new CxxValgrindSensor(language);
  }

  @Test
  public void shouldNotThrowWhenGivenValidData() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    sensor.execute(context);
    assertThat(context.allAnalysisErrors().size()==0).isTrue();
  }

  @Test
  public void shouldSaveViolationIfErrorIsInside() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Set<ValgrindError> valgrindErrors = new HashSet<>();
    valgrindErrors.add(mockValgrindError(true));
    context.fileSystem().add(TestInputFileBuilder.create("myProjectKey", "dir/file").setLanguage("cpp").initMetadata(new String("asd\nasdas\nasda\n")).build());
    sensor.saveErrors(context, valgrindErrors);
    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  public void shouldNotSaveViolationIfErrorIsOutside() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    Set<ValgrindError> valgrindErrors = new HashSet<>();
    valgrindErrors.add(mockValgrindError(false));
    sensor.saveErrors(context, valgrindErrors);
    assertThat(context.allIssues()).hasSize(0);
  }

  private ValgrindError mockValgrindError(boolean inside) {
    ValgrindError error = mock(ValgrindError.class);
    when(error.getKind()).thenReturn("valgrind-error");
    ValgrindFrame frame = inside == true ? generateValgrindFrame() : null;
    when(error.getLastOwnFrame((anyString()))).thenReturn(frame);
    return error;
  }

  private ValgrindFrame generateValgrindFrame() {
    return new ValgrindFrame("ip", "obj", "fn", "dir", "file", "1");
  }
}

