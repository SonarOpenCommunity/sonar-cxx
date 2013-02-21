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
package org.sonar.plugins.cxx.squid;

import org.sonar.api.config.Settings;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.plugins.cxx.CxxPlugin;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

public class CxxSquidSensorTest {
  private CxxSquidSensor sensor;
  private SensorContext context;
  private Settings settings;
  
  @Before
  public void setUp() {
    settings = Settings.createForComponent(new CxxPlugin());
    sensor = new CxxSquidSensor(settings);
    context = mock(SensorContext.class);
  }

  @Test
  public void testLineCounting() {
    Project project = mockProject();
    sensor.analyse(project, context);

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(92.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(54.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(50.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(7.0));

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.COMPLEXITY), eq(19.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.COMMENT_BLANK_LINES), eq(7.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.COMMENT_LINES), eq(15.0));
  }

  private Project mockProject() {
    Project project = TestUtils.mockProject();

    File sourceDir;
    InputFile inputFile = null;
    try{
      sourceDir = new File(getClass().getResource("/").toURI());
      inputFile = mock(InputFile.class);
      when(inputFile.getFile())
        .thenReturn(new File(getClass().getResource("/org/sonar/plugins/cxx/code_chunks.cc").toURI()));
    } catch(java.net.URISyntaxException e) {
      System.out.println("Got an exception while mocking project: " + e);
      return null;
    }

    List<InputFile> mainFiles = project.getFileSystem().mainFiles(CxxLanguage.KEY);
    mainFiles.clear();
    mainFiles.add(inputFile);
    List<File> sourceDirs = project.getFileSystem().getSourceDirs();
    sourceDirs.clear();
    sourceDirs.add(sourceDir);

    return project;
  }
  
  @Test
  public void testReplacingOfExtenalMacros() {
    settings.appendProperty(CxxPlugin.DEFINES_KEY, "MACRO class A{};");
    
    List<File> sourceDirs = new ArrayList<File>();
    List<File> testDirs = new ArrayList<File>();
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/squid/external_macro");
    sourceDirs.add(baseDir);
    Project project = TestUtils.mockProject(baseDir, sourceDirs, testDirs);
    
    sensor.analyse(project, context);

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(2.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(1.0));
  }

  
  @Test
  public void testFindingIncludedFiles() {
    settings.appendProperty(CxxPlugin.INCLUDE_DIRECTORIES_KEY, "include");
    
    List<File> sourceDirs = new ArrayList<File>();
    List<File> testDirs = new ArrayList<File>();      
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/squid/include_directories");
    sourceDirs.add(new File(baseDir, "src"));
    Project project = TestUtils.mockProject(baseDir, sourceDirs, testDirs);
    
    sensor.analyse(project, context);

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(16.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(5.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(5.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
  }
  
  @Test
  public void testBehaviourOnCircularIncludes() {
    // especially: when two files, both belonging to the set of
    // files to analyse, include each other, the preprocessor guards have to be disabled 
    // and both have to be counted in terms of metrics
    
    List<File> sourceDirs = new ArrayList<File>();
    List<File> testDirs = new ArrayList<File>();      
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/squid/circular_includes");
    sourceDirs.add(baseDir);
    Project project = TestUtils.mockProject(baseDir, sourceDirs, testDirs);
    
    sensor.analyse(project, context);

    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(1.0));
  }

}
