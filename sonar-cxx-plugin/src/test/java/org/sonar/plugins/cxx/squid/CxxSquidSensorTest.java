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

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.TestUtils;

public class CxxSquidSensorTest {
  private CxxSquidSensor sensor;
  private SensorContext context;
  private Settings settings;
  private DefaultFileSystem fs;
  private Project project;
  private ProjectReactor reactor;
  
  @Before
  public void setUp() {
    settings = new Settings();
    context = mock(SensorContext.class);
  }

  @Test
  public void testCollectingSquidMetrics() {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/codechunks-project");
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "code_chunks.cc", Type.MAIN));
    
    sensor.analyse(project, context);

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(92.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(54.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(50.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(7.0));

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.COMPLEXITY), eq(19.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.COMMENT_LINES), eq(15.0));
  }

  @Test
  public void testReplacingOfExtenalMacros() {
    settings.setProperty(CxxPlugin.DEFINES_KEY, "MACRO class A{};");
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/external-macro-project");    
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "test.cc", Type.MAIN));
    
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
    settings.setProperty(CxxPlugin.INCLUDE_DIRECTORIES_KEY, "include");
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/include-directories-project");    
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "src/main.cc", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/bar.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/HEADER1.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/HEADER2.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/include1.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/include2.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/include3.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/widget.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/subfolder/include4.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include_snd/include_snd_1.hh", Type.MAIN));
//    fs.add(TestUtils.CxxInputFile(baseDir, "include_snd/subfolder/include_snd_subfolder_1.hh", Type.MAIN));
    
    sensor.analyse(project, context);

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(29.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(9.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(9.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
  }

@Test
  public void testForceIncludedFiles() {
    settings.setProperty(CxxPlugin.INCLUDE_DIRECTORIES_KEY, "include"); // todo: not sure if it should work without this (=> using baseDir)?
    settings.setProperty(CxxPlugin.FORCE_INCLUDE_FILES_KEY, "force1.hh,subfolder/force2.hh");
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/force-include-project");  
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "src/src1.cc", Type.MAIN));
    fs.add(TestUtils.CxxInputFile(baseDir, "src/src2.cc", Type.MAIN));  
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/force1.hh", Type.MAIN));  
//    fs.add(TestUtils.CxxInputFile(baseDir, "include/subfolder/force2.hh", Type.MAIN)); 

    sensor.analyse(project, context);

    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
//    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(1.0));
//    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(1.0));
//    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(2.0));
//    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(1.0));
//    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
  }

  @Test
  public void testBehaviourOnCircularIncludes() {
    // especially: when two files, both belonging to the set of
    // files to analyse, include each other, the preprocessor guards have to be disabled
    // and both have to be counted in terms of metrics
	File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/circular-includes-project");  
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "test1.hh", Type.MAIN));
    fs.add(TestUtils.CxxInputFile(baseDir, "test2.hh", Type.MAIN));  
    
    sensor.analyse(project, context);

    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(1.0));
  }

  @Test
  public void testCircularFileDependency() {
	File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/circular-includes-project");  
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "test1.hh", Type.MAIN));
    fs.add(TestUtils.CxxInputFile(baseDir, "test2.hh", Type.MAIN)); 
    
    sensor.analyse(project, context);

    verify(context).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_CYCLES), eq(1.0));
    verify(context).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_FEEDBACK_EDGES), eq(1.0));
    verify(context).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_TANGLES), eq(1.0));
    verify(context).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_EDGES_WEIGHT), eq(2.0));

    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_CYCLES), eq(0.0));
    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_FEEDBACK_EDGES), eq(0.0));
    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_TANGLES), eq(0.0));
    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_EDGES_WEIGHT), eq(0.0));
  }

  @Test
  public void testCircularPackageDependency() {
	File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/circular-packages-project");  
    setUpSensor(baseDir);
    fs.add(TestUtils.CxxInputFile(baseDir, "Package1/test1.hh", Type.MAIN));
    fs.add(TestUtils.CxxInputFile(baseDir, "Package2/test2.hh", Type.MAIN)); 
    fs.add(TestUtils.CxxInputFile(baseDir, "Package2/test3.hh", Type.MAIN)); 
    fs.add(TestUtils.CxxInputFile(baseDir, "Package2/test4.hh", Type.MAIN)); 
    
    sensor.analyse(project, context);

    verify(context, times(2)).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_CYCLES), eq(0.0));
    verify(context, times(2)).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_FEEDBACK_EDGES), eq(0.0));
    verify(context, times(2)).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_TANGLES), eq(0.0));
    verify(context, times(2)).saveMeasure((Directory) anyObject(), eq(CoreMetrics.FILE_EDGES_WEIGHT), anyDouble()); //0 for package1, 1 for package2

    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_CYCLES), eq(1.0));
    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_FEEDBACK_EDGES), eq(1.0));
    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_TANGLES), eq(1.0));
    verify(context).saveMeasure((Project) anyObject(), eq(CoreMetrics.PACKAGE_EDGES_WEIGHT), eq(3.0));
  }

  private void setUpSensor(File baseDir){
    project = TestUtils.mockProject(baseDir);
    fs = TestUtils.mockFileSystem(baseDir);
    ActiveRules rules = mock(ActiveRules.class);
    CheckFactory checkFactory = new CheckFactory(rules);
    reactor = TestUtils.mockReactor();
    sensor = new CxxSquidSensor(mock(ResourcePerspectives.class),
                                settings, fs, reactor, checkFactory, rules);
  }
}
