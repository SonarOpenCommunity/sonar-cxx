/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.valgrind;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CxxValgrindSensorTest {
  private CxxValgrindSensor sensor;
  private SensorContext context;
  private Project project;
  private Issuable issuable;
  private ResourcePerspectives perspectives;

  @Before
  public void setUp() {
    project = TestUtils.mockProject();
    issuable = TestUtils.mockIssuable();
    perspectives = TestUtils.mockPerspectives(issuable);
    sensor = new CxxValgrindSensor(perspectives, new Settings(), TestUtils.mockFileSystem(), mock(RulesProfile.class), TestUtils.mockReactor());
    context = mock(SensorContext.class);
    File resourceMock = mock(File.class);
    when(context.getResource(any(File.class))).thenReturn(resourceMock);
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
    verify(issuable, times(1)).addIssue(any(Issue.class));
  }

  @Test
  public void shouldNotSaveViolationIfErrorIsOutside() {
    Set<ValgrindError> valgrindErrors = new HashSet<ValgrindError>();
    valgrindErrors.add(mockValgrindError(false));
    sensor.saveErrors(project, context, valgrindErrors);
    verify(issuable, times(0)).addIssue(any(Issue.class));
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
