/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.squid;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Collection;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.plugins.cxx.CxxLanguage;

public class CxxSquidSensorTest {

  private CxxSquidSensor sensor;
  private Settings settings;
  
  @Before
  public void setUp() {
    settings = new Settings();
    ActiveRules rules = mock(ActiveRules.class);
    CheckFactory checkFactory = new CheckFactory(rules);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);
    sensor = new CxxSquidSensor(settings, fileLinesContextFactory, checkFactory, rules, null);    
  }

  @Test
  public void testCollectingSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/codechunks-project");
    
    SensorContextTester context = SensorContextTester.create(baseDir);    
    
    String fileName = "code_chunks.cc";
    String content = new String(Files.readAllBytes(new File(baseDir, fileName).toPath()), "UTF-8");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content).setLanguage(CxxLanguage.KEY).setType(InputFile.Type.MAIN));    
    sensor.execute(context);
    Collection<Measure> measures = context.measures("myProjectKey:code_chunks.cc");
            
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(54);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.STATEMENTS).value()).isEqualTo(50);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(7);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.COMPLEXITY).value()).isEqualTo(19);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.COMMENT_LINES).value()).isEqualTo(15);
  }

  @Test
  public void testComplexitySquidMetrics() throws UnsupportedEncodingException, IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/complexity-project");

    SensorContextTester context = SensorContextTester.create(baseDir);
    
    String fileName = "complexity.cc";
    String content = new String(Files.readAllBytes(new File(baseDir, fileName).toPath()), "UTF-8");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content).setLanguage(CxxLanguage.KEY).setType(InputFile.Type.MAIN));
           
    sensor.execute(context);
    Collection<Measure> measures = context.measures("myProjectKey:complexity.cc");
    
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(22);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(2);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.COMPLEXITY).value()).isEqualTo(38);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.COMPLEXITY_IN_CLASSES).value()).isEqualTo(10);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.COMPLEXITY_IN_FUNCTIONS).value()).isEqualTo(38);    
  }  
  
  @Test
  public void testReplacingOfExtenalMacros() throws UnsupportedEncodingException, IOException {
    settings.setProperty(CxxPlugin.DEFINES_KEY, "MACRO class A{};");
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/external-macro-project");

    
    SensorContextTester context = SensorContextTester.create(baseDir);
    String fileName = "test.cc";
    String content = new String(Files.readAllBytes(new File(baseDir, fileName).toPath()), "UTF-8");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content).setLanguage(CxxLanguage.KEY).setType(InputFile.Type.MAIN));
        
    sensor.execute(context);
    Collection<Measure> measures = context.measures("myProjectKey:test.cc");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(1);        
  }

  @Test
  public void testFindingIncludedFiles() throws UnsupportedEncodingException, IOException {
    settings.setProperty(CxxPlugin.INCLUDE_DIRECTORIES_KEY, "include");
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/include-directories-project");

    SensorContextTester context = SensorContextTester.create(baseDir);
    String fileName = "src/main.cc";
    String content = new String(Files.readAllBytes(new File(baseDir, fileName).toPath()), "UTF-8");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content).setLanguage(CxxLanguage.KEY).setType(InputFile.Type.MAIN));
       
    sensor.execute(context);
    Collection<Measure> measures = context.measures("myProjectKey:src/main.cc");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(9);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(9);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(0); 
    
  }

  @Test
  public void testForceIncludedFiles() throws UnsupportedEncodingException, IOException {
    settings.setProperty(CxxPlugin.INCLUDE_DIRECTORIES_KEY, "include");
    settings.setProperty(CxxPlugin.FORCE_INCLUDE_FILES_KEY, "force1.hh,subfolder/force2.hh");
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/force-include-project");

    
    SensorContextTester context = SensorContextTester.create(baseDir);
    String fileName = "src/src1.cc";
    String content = new String(Files.readAllBytes(new File(baseDir, fileName).toPath()), "UTF-8");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content).setLanguage(CxxLanguage.KEY).setType(InputFile.Type.MAIN));

    
    sensor.execute(context);
    Collection<Measure> measures = context.measures("myProjectKey:src/src1.cc");

    // These checks actually check the force include feature, since only if it works the metric values will be like follows    
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.STATEMENTS).value()).isEqualTo(2);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(0);     
  }

  @Test
  public void testBehaviourOnCircularIncludes() throws UnsupportedEncodingException, IOException {
    // especially: when two files, both belonging to the set of
    // files to analyse, include each other, the preprocessor guards have to be disabled
    // and both have to be counted in terms of metrics
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/circular-includes-project");
    
    SensorContextTester context = SensorContextTester.create(baseDir);
    
    String fileName = "test1.hh";
    String content = new String(Files.readAllBytes(new File(baseDir, fileName).toPath()), "UTF-8");
    context.fileSystem().add(new DefaultInputFile("myProjectKey", fileName).initMetadata(content).setLanguage(CxxLanguage.KEY).setType(InputFile.Type.MAIN));

    
    sensor.execute(context);
    Collection<Measure> measures = context.measures("myProjectKey:test1.hh");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(1);
  }


  private Measure GetIntegerMeasureByKey(Collection<Measure> measures, Metric<Integer> metric) {
    for (Measure measure: measures) {
      if (measure.metric().equals(metric)) {
        return measure;
      }      
    }
    
    return null;
  }
}
