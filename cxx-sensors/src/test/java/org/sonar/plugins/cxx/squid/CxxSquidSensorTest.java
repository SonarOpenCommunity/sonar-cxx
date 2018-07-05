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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
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
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.coverage.CxxCoverageSensor;
import org.sonar.cxx.sensors.squid.CxxSquidSensor;
import org.sonar.cxx.sensors.utils.TestUtils;
import org.sonar.cxx.visitors.CxxFunctionComplexityVisitor;
import org.sonar.cxx.visitors.CxxFunctionSizeVisitor;

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

  private DefaultInputFile buildTestInputFile(File baseDir, String fileName) throws IOException
  {
    File target = new File(baseDir, fileName);
    String content = new String(Files.readAllBytes(target.toPath()), "UTF-8");
    DefaultInputFile inputFile = TestInputFileBuilder.create("ProjectKey", baseDir, target).setContents(content)
      .setCharset(Charset.forName("UTF-8")).setLanguage(language.getKey())
      .setType(InputFile.Type.MAIN).build();
    return inputFile;
  }

  @Test
  public void testCollectingSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/codechunks-project");
    DefaultInputFile inputFile0 = buildTestInputFile(baseDir, "code_chunks.cc");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile0);
    sensor.execute(context);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.FILES).value()).isEqualTo(1);
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
    when(this.language.getIntegerOption(same(CxxFunctionComplexityVisitor.FUNCTION_COMPLEXITY_THRESHOLD_KEY))).thenReturn(Optional.of(3));
    when(this.language.getIntegerOption(same(CxxFunctionSizeVisitor.FUNCTION_SIZE_THRESHOLD_KEY))).thenReturn(Optional.of(3));

    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/complexity-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "complexity.cc");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FILES).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(22);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(2);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.COMPLEXITY).value()).isEqualTo(38);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.COGNITIVE_COMPLEXITY).value()).isEqualTo(16);

    final Metric<Integer> COMPLEX_FUNCTIONS = language.<Integer>getMetric(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_KEY);
    final Metric<Double> COMPLEX_FUNCTIONS_PERC = language.<Double>getMetric(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_PERC_KEY);
    final Metric<Integer> COMPLEX_FUNCTIONS_LOC = language.<Integer>getMetric(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_KEY);
    final Metric<Double> COMPLEX_FUNCTIONS_LOC_PERC = language.<Double>getMetric(CxxMetricsFactory.Key.COMPLEX_FUNCTIONS_LOC_PERC_KEY);

    final Metric<Integer> LOC_IN_FUNCTIONS = language.<Integer>getMetric(CxxMetricsFactory.Key.LOC_IN_FUNCTIONS_KEY);
    final Metric<Integer> BIG_FUNCTIONS = language.<Integer>getMetric(CxxMetricsFactory.Key.BIG_FUNCTIONS_KEY);
    final Metric<Double> BIG_FUNCTIONS_PERC = language.<Double>getMetric(CxxMetricsFactory.Key.BIG_FUNCTIONS_PERC_KEY);
    final Metric<Integer> BIG_FUNCTIONS_LOC = language.<Integer>getMetric(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_KEY);
    final Metric<Double> BIG_FUNCTIONS_LOC_PERC = language.<Double>getMetric(CxxMetricsFactory.Key.BIG_FUNCTIONS_LOC_PERC_KEY);

    softly.assertThat(context.measure(inputFile.key(), COMPLEX_FUNCTIONS).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), COMPLEX_FUNCTIONS_PERC)).isNull(); // see DensityMeasureComputer
    softly.assertThat(context.measure(inputFile.key(), COMPLEX_FUNCTIONS_LOC).value()).isEqualTo(6);
    softly.assertThat(context.measure(inputFile.key(), COMPLEX_FUNCTIONS_LOC_PERC)).isNull(); // see DensityMeasureComputer

    softly.assertThat(context.measure(inputFile.key(), LOC_IN_FUNCTIONS).value()).isEqualTo(59);

    softly.assertThat(context.measure(inputFile.key(), BIG_FUNCTIONS).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), BIG_FUNCTIONS_PERC)).isNull(); // see DensityMeasureComputer
    softly.assertThat(context.measure(inputFile.key(), BIG_FUNCTIONS_LOC).value()).isEqualTo(44);
    softly.assertThat(context.measure(inputFile.key(), BIG_FUNCTIONS_LOC_PERC)).isNull(); // see DensityMeasureComputer
    softly.assertAll();
  }

  @Test
  public void testDocumentationSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/documentation-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "documentation0.hh");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    final Metric<Integer> API = language.<Integer>getMetric(CxxMetricsFactory.Key.PUBLIC_API_KEY);
    final Metric<Integer> UNDOCUMENTED_API = language
        .<Integer>getMetric(CxxMetricsFactory.Key.PUBLIC_UNDOCUMENTED_API_KEY);
    final Metric<Double> DOCUMENTED_API_DENSITY = language
        .<Double>getMetric(CxxMetricsFactory.Key.PUBLIC_DOCUMENTED_API_DENSITY_KEY);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), API).value()).isEqualTo(8);
    softly.assertThat(context.measure(inputFile.key(), UNDOCUMENTED_API).value()).isEqualTo(2);
    softly.assertThat(context.measure(inputFile.key(), DOCUMENTED_API_DENSITY)).isNull(); // see DensityMeasureComputer

    final String moduleKey = context.module().key();
    softly.assertThat(context.measure(moduleKey, API)).isNull(); // see AggregateMeasureComputer
    softly.assertThat(context.measure(moduleKey, UNDOCUMENTED_API)).isNull(); // see AggregateMeasureComputer
    softly.assertThat(context.measure(moduleKey, DOCUMENTED_API_DENSITY)).isNull(); // see AggregateMeasureComputer
    softly.assertAll();
  }

  @Test
  public void testReplacingOfExtenalMacros() throws IOException {
    when(this.language.getStringLinesOption(CxxSquidSensor.DEFINES_KEY)).thenReturn(new String[]{"MACRO class A{};"});
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/external-macro-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "test.cc");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FILES).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(1);
    softly.assertAll();
  }

  @Test
  public void testFindingIncludedFiles() throws IOException {
    when(this.language.getStringArrayOption(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY)).thenReturn(new String[]{"include"});
    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/include-directories-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "src/main.cc");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FILES).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(0);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(0);
    softly.assertAll();

  }

  @Test
  public void testForceIncludedFiles() throws IOException {

    when(this.language.getStringArrayOption(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY)).thenReturn(new String[]{"include"});
    when(this.language.getStringArrayOption(CxxSquidSensor.FORCE_INCLUDE_FILES_KEY)).thenReturn(new String[]{"force1.hh", "subfolder/force2.hh"});

    File baseDir = TestUtils.loadResource("/org/sonar/cxx/sensors/force-include-project");
    DefaultInputFile inputFile = buildTestInputFile(baseDir, "src/src1.cc");

    SensorContextTester context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    // These checks actually check the force include feature, since only if it works the metric values will be like follows
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FILES).value()).isEqualTo(1);
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
}
