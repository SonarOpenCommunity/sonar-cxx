/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx.valgrind;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

import java.util.Set;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.cxx.TestUtils;

public class CxxValgrindSensorTest {
  private CxxValgrindSensor sensor;
  private SensorContext context;
  private Project project;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    RuleFinder ruleFinder = TestUtils.mockRuleFinder();
    sensor = new CxxValgrindSensor(ruleFinder, mock(Configuration.class), mock(RulesProfile.class));
    context = mock(SensorContext.class);
    Resource resourceMock = mock(Resource.class);
    when(context.getResource(any(Resource.class))).thenReturn(resourceMock);
  }

  @Test
  public void shouldNotThrowWhenGivenValidData() {
    sensor.analyse(project, context);
  }

  @Test
  public void shouldSaveViolationIfErrorIsInside() {
    Set<ValgrindError> valgrindErrors = new HashSet<ValgrindError>();
    valgrindErrors.add(mockValgrindError(true));
    sensor.saveErrors(project, context, valgrindErrors);
    verify(context, times(1)).saveViolation(any(Violation.class));
  }

  @Test
  public void shouldNotSaveViolationIfErrorIsOutside() {
    Set<ValgrindError> valgrindErrors = new HashSet<ValgrindError>();
    valgrindErrors.add(mockValgrindError(false));
    sensor.saveErrors(project, context, valgrindErrors);
    verify(context, times(0)).saveViolation(any(Violation.class));
  }

  private ValgrindError mockValgrindError(boolean inside){
    ValgrindError error = mock(ValgrindError.class);
    ValgrindFrame frame = inside == true ? generateValgrindFrame() : null;
    when(error.getLastOwnFrame((anyString()))).thenReturn(frame);
    return error;
  }

  private ValgrindFrame generateValgrindFrame(){
    return new ValgrindFrame("ip", "obj", "fn", "dir", "file", 1);
  }
}
