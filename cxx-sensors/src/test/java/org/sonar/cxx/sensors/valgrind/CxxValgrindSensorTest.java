/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import java.util.Collections;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.sensors.utils.TestUtils;
import static org.sonar.cxx.sensors.utils.TestUtils.createTestInputFile;

class CxxValgrindSensorTest {

  private DefaultFileSystem fs;
  private CxxValgrindSensor sensor;

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    sensor = new CxxValgrindSensor();
    sensor.setWebApi(null);
  }

  @Test
  void shouldNotThrowWhenGivenValidData() {
    var context = SensorContextTester.create(fs.baseDir());
    sensor.execute(context);

    assertThat(context.allAnalysisErrors()).isEmpty();
  }

  @Test
  void shouldSaveViolationIfErrorIsInside() {
    var context = SensorContextTester.create(fs.baseDir());
    context.fileSystem().add(createTestInputFile("dir/file", 3));

    sensor.execute(context); // set context
    var valgrindErrors = new HashSet<ValgrindError>();
    valgrindErrors.add(mockValgrindError(true));
    sensor.saveErrors(valgrindErrors);

    assertThat(context.allIssues()).hasSize(1);
  }

  @Test
  void shouldNotSaveViolationIfErrorIsOutside() {
    var context = SensorContextTester.create(fs.baseDir());
    sensor.execute(context); // set context
    var valgrindErrors = new HashSet<ValgrindError>();
    valgrindErrors.add(mockValgrindError(false));
    sensor.saveErrors(valgrindErrors);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void sensorDescriptor() {
    var descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);

    var softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo("CXX Valgrind report import");
    softly.assertThat(descriptor.languages()).containsOnly("cxx", "cpp", "c++", "c");
    softly.assertThat(descriptor.ruleRepositories()).containsOnly(CxxValgrindRuleRepository.KEY);
    softly.assertAll();
  }

  private ValgrindError mockValgrindError(boolean inside) {
    ValgrindStack stack = mock(ValgrindStack.class);
    ValgrindFrame frame = inside ? generateValgrindFrame() : null;
    when(stack.getLastOwnFrame(anyString())).thenReturn(frame);

    ValgrindError error = mock(ValgrindError.class);
    when(error.getKind()).thenReturn("valgrind-error");
    when(error.getStacks()).thenReturn(Collections.singletonList(stack));
    return error;
  }

  private ValgrindFrame generateValgrindFrame() {
    return new ValgrindFrame("ip", "obj", "fn", "dir", "file", "1");
  }

}
