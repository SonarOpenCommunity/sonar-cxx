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
package org.sonar.plugins.cxx.squid;

import com.sonar.sslr.api.Grammar;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.squid.CxxSquidSensor;
import org.sonar.cxx.sensors.squid.SquidSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.SourceFile;

public class CxxSquidSensorTest {

  private CxxSquidSensor sensor;
  private CxxLanguage language;

  @Before
  public void setUp() {
    language = TestUtils.mockCxxLanguage();
    ActiveRules rules = mock(ActiveRules.class);
    CheckFactory checkFactory = new CheckFactory(rules);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);
    when(language.getPluginProperty(CxxCoverageSensor.REPORT_PATH_KEY))
      .thenReturn("sonar.cxx." + CxxCoverageSensor.REPORT_PATH_KEY);

    sensor = new CxxSquidSensor(
      language,
      fileLinesContextFactory,
      checkFactory,
      null);
  }

  @Test
  public void testCollectingSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/codechunks-project");
    File target = new File(baseDir, "code_chunks.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
      .setType(InputFile.Type.MAIN).build();

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    Collection<Measure> measures = context.measures("ProjectKey:code_chunks.cc");

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
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/complexity-project");
    File target = new File(baseDir, "complexity.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setLanguage(language.getKey()).setType(InputFile.Type.MAIN).build();

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    Collection<Measure> measures = context.measures("ProjectKey:complexity.cc");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(22);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(2);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.COMPLEXITY).value()).isEqualTo(38);
  }

  @Test
  public void testReplacingOfExtenalMacros() throws UnsupportedEncodingException, IOException {
    when(this.language.getStringLinesOption(CxxSquidSensor.DEFINES_KEY)).thenReturn(new String[]{"MACRO class A{};"});
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/external-macro-project");
    File target = new File(baseDir, "test.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setLanguage(language.getKey()).setType(InputFile.Type.MAIN).build();

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    Collection<Measure> measures = context.measures("ProjectKey:test.cc");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(1);
  }

  @Test
  public void testFindingIncludedFiles() throws UnsupportedEncodingException, IOException {
    when(this.language.getStringArrayOption(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY)).thenReturn(new String[]{"include"});
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/include-directories-project");
    File target = new File(baseDir, "src/main.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setLanguage(language.getKey()).setType(InputFile.Type.MAIN).build();

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    Collection<Measure> measures = context.measures("ProjectKey:src/main.cc");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FILES).value()).isEqualTo(1);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(9);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.FUNCTIONS).value()).isEqualTo(9);
    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.CLASSES).value()).isEqualTo(0);

  }

  @Test
  public void testForceIncludedFiles() throws UnsupportedEncodingException, IOException {

    when(this.language.getStringArrayOption(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY)).thenReturn(new String[]{"include"});
    when(this.language.getStringArrayOption(CxxSquidSensor.FORCE_INCLUDE_FILES_KEY)).thenReturn(new String[]{"force1.hh", "subfolder/force2.hh"});

    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/force-include-project");
    File target = new File(baseDir, "src/src1.cc");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setLanguage(language.getKey()).setType(InputFile.Type.MAIN).build();

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    Collection<Measure> measures = context.measures("ProjectKey:src/src1.cc");

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
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/circular-includes-project");
    File target = new File(baseDir, "test1.hh");

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setLanguage(language.getKey()).setType(InputFile.Type.MAIN).build();

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    Collection<Measure> measures = context.measures("ProjectKey:test1.hh");

    assertThat(GetIntegerMeasureByKey(measures, CoreMetrics.NCLOC).value()).isEqualTo(1);
  }

  @Test
  public void testSquidSensors() throws IOException{
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/codechunks-project");
    File target = new File(baseDir, "code_chunks.cc");
    SensorContextTester context = SensorContextTester.create(baseDir);

    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
      .setType(InputFile.Type.MAIN).build();

    SquidAstVisitor<Grammar> mockVisitor = (SquidAstVisitor<Grammar>) mock(SquidAstVisitor.class);
    SquidSensor squidSensorMock = mock(SquidSensor.class);
    when(squidSensorMock.getVisitor()).thenReturn(mockVisitor);

    sensor.getSquidSensors().clear();
    sensor.getSquidSensors().add(squidSensorMock);

    context.fileSystem().add(inputFile);
    sensor.execute(context);

    verify(squidSensorMock, times(1)).getVisitor();
    verify(squidSensorMock, times(1)).publishMeasureForFile(eq(inputFile), any(SourceFile.class), eq(context));
    verify(squidSensorMock, times(1)).publishMeasureForProject(any(InputModule.class), eq(context));
  }



  private Measure GetIntegerMeasureByKey(Collection<Measure> measures, Metric<Integer> metric) {
    for (Measure measure : measures) {
      if (measure.metric().equals(metric)) {
        return measure;
      }
    }
    return null;
  }
}
