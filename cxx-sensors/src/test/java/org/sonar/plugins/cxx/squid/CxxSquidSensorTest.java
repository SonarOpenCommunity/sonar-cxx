/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Files;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.issue.NoSonarFilter;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetrics;
import org.sonar.cxx.sensors.squid.CxxSquidSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import org.sonar.cxx.visitors.CxxFunctionComplexityVisitor;
import org.sonar.cxx.visitors.CxxFunctionSizeVisitor;

public class CxxSquidSensorTest {

  private CxxSquidSensor sensor;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    ActiveRules rules = mock(ActiveRules.class);
    var checkFactory = new CheckFactory(rules);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);

    sensor = new CxxSquidSensor(
      settings.asConfig(),
      fileLinesContextFactory,
      checkFactory,
      new NoSonarFilter(),
      null);
  }

  @Test
  public void testCollectingSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/codechunks-project");
    DefaultInputFile inputFile0 = buildTestInputFile(baseDir, "code_chunks.cc");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile0);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.NCLOC).value()).isEqualTo(54);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(50);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(7);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.CLASSES).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.COMPLEXITY).value()).isEqualTo(19);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.COGNITIVE_COMPLEXITY).value()).isEqualTo(8);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.COMMENT_LINES).value()).isEqualTo(15);
    softly.assertAll();
  }

  @Test
  public void testComplexitySquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/complexity-project");
    SensorContextTester context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxFunctionComplexityVisitor.FUNCTION_COMPLEXITY_THRESHOLD_KEY, 3);
    settings.setProperty(CxxFunctionSizeVisitor.FUNCTION_SIZE_THRESHOLD_KEY, 3);
    context.setSettings(settings);

    DefaultInputFile inputFile = buildTestInputFile(baseDir, "complexity.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(22);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(2);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.COMPLEXITY).value()).isEqualTo(38);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.COGNITIVE_COMPLEXITY).value()).isEqualTo(16);

    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.COMPLEX_FUNCTIONS).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.COMPLEX_FUNCTIONS_PERC)).isNull(); // see DensityMeasureComputer
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.COMPLEX_FUNCTIONS_LOC).value()).isEqualTo(6);
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.COMPLEX_FUNCTIONS_LOC_PERC)).isNull(); // see DensityMeasureComputer

    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.LOC_IN_FUNCTIONS).value()).isEqualTo(59);

    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.BIG_FUNCTIONS).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.BIG_FUNCTIONS_PERC)).isNull(); // see DensityMeasureComputer
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.BIG_FUNCTIONS_LOC).value()).isEqualTo(44);
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.BIG_FUNCTIONS_LOC_PERC)).isNull(); // see DensityMeasureComputer
    softly.assertAll();
  }

  @Test
  public void testDocumentationSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/documentation-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "documentation0.hh");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.PUBLIC_API_KEY).value()).isEqualTo(8);
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY).value()).isEqualTo(2);
    softly.assertThat(context.measure(inputFile.key(), CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY)).isNull(); // see DensityMeasureComputer

    final String moduleKey = context.project().key();
    softly.assertThat(context.measure(moduleKey, CxxMetrics.PUBLIC_API_KEY)).isNull(); // see AggregateMeasureComputer
    softly.assertThat(context.measure(moduleKey, CxxMetrics.PUBLIC_UNDOCUMENTED_API_KEY)).isNull(); // see AggregateMeasureComputer
    softly.assertThat(context.measure(moduleKey, CxxMetrics.PUBLIC_DOCUMENTED_API_DENSITY_KEY)).isNull(); // see AggregateMeasureComputer
    softly.assertAll();
  }

  @Test
  public void testReplacingOfExtenalMacros() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/external-macro-project");
    SensorContextTester context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.DEFINES_KEY, "MACRO class A{};");
    context.setSettings(settings);

    DefaultInputFile inputFile = buildTestInputFile(baseDir, "test.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(1);
    softly.assertAll();
  }

  @Test
  public void testFindingIncludedFiles() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/include-directories-project");
    SensorContextTester context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY, "include");
    context.setSettings(settings);

    DefaultInputFile inputFile = buildTestInputFile(baseDir, "src/main.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(0);
    softly.assertAll();

  }

  @Test
  public void testForceIncludedFiles() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/force-include-project");
    SensorContextTester context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY, "include");
    settings.setProperty(CxxSquidSensor.FORCE_INCLUDE_FILES_KEY, "force1.hh,subfolder/force2.hh");
    context.setSettings(settings);

    DefaultInputFile inputFile = buildTestInputFile(baseDir, "src/src1.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    // These checks actually check the force include feature, since only if it works the metric values will be like follows
    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(2);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(0);
    softly.assertAll();
  }

  @Test
  public void testBehaviourOnCircularIncludes() throws IOException {
    // especially: when two files, both belonging to the set of
    // files to analyse, include each other, the preprocessor guards have to be disabled
    // and both have to be counted in terms of metrics
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/circular-includes-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "test1.hh");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
  }

  private DefaultInputFile buildTestInputFile(File baseDir, String fileName) throws IOException {
    var target = new File(baseDir, fileName);
    String content = Files.contentOf(target, StandardCharsets.UTF_8);
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(StandardCharsets.UTF_8).setLanguage(CxxLanguage.KEY)
      .setType(InputFile.Type.MAIN).build();
    return inputFile;
  }

}
