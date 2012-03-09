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

package org.sonar.plugins.cxx.cppncss;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.any;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ProjectFileSystem;


public class CxxCppNcssSensorTest {
  private CxxCppNcssSensor sensor;
  private SensorContext context;
  private Project project;
  
  @Before
  public void setUp() throws java.net.URISyntaxException {
    project = mockProject();
    sensor = new CxxCppNcssSensor(project);
    context = mock(SensorContext.class);
    Resource resourceMock = mock(Resource.class);
    when(context.getResource((Resource)anyObject())).thenReturn(resourceMock);
  }
  
  @Test
  public void shouldReportCorrectViolations() {
    sensor.analyse(project, context);
    
    verify(context, times(5)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.FUNCTIONS), anyDouble());
    verify(context, times(5)).saveMeasure((Resource) anyObject(),
                                          eq(CoreMetrics.COMPLEXITY), anyDouble());
    verify(context, times(15)).saveMeasure((Resource) anyObject(), any(Measure.class));
  }
  
  private Project mockProject() throws java.net.URISyntaxException {
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    File basedir = new File(getClass().getResource("/org/sonar/plugins/cxx/").toURI());
    when(fileSystem.getBasedir()).thenReturn(basedir);
    
    Project project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(fileSystem);
    
    return project;
  }
}
