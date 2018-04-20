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
package org.sonar.cxx.sensors.valgrind;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.rats.CxxRatsRuleRepository;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxValgrindSensorTest {

  private CxxValgrindSensor sensor;
  private DefaultFileSystem fs;
  private CxxLanguage language;

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
    assertThat(context.allAnalysisErrors().size() == 0).isTrue();
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

  @Test
  public void sensorDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(descriptor.name()).isEqualTo(language.getName() + " ValgrindSensor");
    softly.assertThat(descriptor.languages()).containsOnly(language.getKey());
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
