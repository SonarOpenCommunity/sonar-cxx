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

package org.sonar.plugins.cxx;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

public class CxxLineCounterTest {
  private CxxLineCounter sensor;
  private SensorContext context;

  @Before
  public void setUp() {
    sensor = new CxxLineCounter(TestUtils.mockCxxLanguage());
    context = mock(SensorContext.class);
  }

  @Test
  public void testLineCounting() {
    Project project = mockProject();
    sensor.analyse(project, context);

    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.LINES), eq(92.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.COMMENT_LINES), eq(9.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.COMMENT_BLANK_LINES), eq(5.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.COMMENTED_OUT_CODE_LINES), eq(8.0));
    verify(context).saveMeasure((Resource) anyObject(), eq(CoreMetrics.NCLOC), eq(58.0));
  }

  private Project mockProject() {
    Project project = TestUtils.mockProject();

    File sourceFile;
    File sourceDir;
    try{
      sourceFile = new File(getClass().getResource("/org/sonar/plugins/cxx/code_chunks.cc").toURI());
      sourceDir = new File(getClass().getResource("/").toURI());
    } catch(java.net.URISyntaxException e) {
      System.out.println("Got an exception while mockint project: " + e);
      return null;
    }

    List<File> sourceFiles = project.getFileSystem().getSourceFiles(TestUtils.mockCxxLanguage());
    sourceFiles.clear();
    sourceFiles.add(sourceFile);
    List<File> sourceDirs = project.getFileSystem().getSourceDirs();
    sourceDirs.clear();
    sourceDirs.add(sourceDir);

    return project;
  }
}
