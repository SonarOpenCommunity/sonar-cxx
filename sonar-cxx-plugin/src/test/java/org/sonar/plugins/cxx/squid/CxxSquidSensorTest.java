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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CxxSquidSensorTest {
  private CxxSquidSensor sensor;
  private SensorContext context;
  private Settings settings;
  private ModuleFileSystem fs;
  private List<File> emptyList;
  private Project project;

  @Before
  public void setUp() {
    emptyList = new ArrayList<File>();
    settings = new Settings();
    context = mock(SensorContext.class);
  }

  @Test
  public void testCollectingSquidMetrics() {
    setUpSensor(TestUtils.loadResource("codechunks-project"), null);

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
    setUpSensor(TestUtils.loadResource("external-macro-project"), null);

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
    setUpSensor(TestUtils.loadResource("include-directories-project"), "src");

    sensor.analyse(project, context);

    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(27.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(8.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(0.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(8.0));
    verify(context).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
  }

@Test
  public void testForceIncludedFiles() {
    settings.setProperty(CxxPlugin.INCLUDE_DIRECTORIES_KEY, "include"); // todo: not sure if it should work without this (=> using baseDir)?
    settings.setProperty(CxxPlugin.FORCE_INCLUDE_FILES_KEY, "force1.hh,subfolder/force2.hh");
    setUpSensor(TestUtils.loadResource("force-include-project"), "src");

    sensor.analyse(project, context);

    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FILES), eq(1.0));
    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.LINES), eq(1.0));
    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(1.0));
    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.STATEMENTS), eq(2.0));
    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.FUNCTIONS), eq(1.0));
    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.CLASSES), eq(0.0));
  }

  @Test
  public void testBehaviourOnCircularIncludes() {
    // especially: when two files, both belonging to the set of
    // files to analyse, include each other, the preprocessor guards have to be disabled
    // and both have to be counted in terms of metrics
    setUpSensor(TestUtils.loadResource("circular-includes-project"), null);
    sensor.analyse(project, context);

    verify(context, times(2)).saveMeasure((org.sonar.api.resources.File) anyObject(), eq(CoreMetrics.NCLOC), eq(1.0));
  }

  private void setUpSensor(File baseDir, String sourceDir){
    List<File> sourceDirs = new ArrayList<File>();
    sourceDirs.add(sourceDir == null ? baseDir : new File(baseDir, sourceDir));
    project = TestUtils.mockProject(baseDir, sourceDirs, emptyList);
    fs = TestUtils.mockFileSystem(baseDir, sourceDirs, emptyList);
    sensor = new CxxSquidSensor(mock(RulesProfile.class), settings, fs);
  }
}
