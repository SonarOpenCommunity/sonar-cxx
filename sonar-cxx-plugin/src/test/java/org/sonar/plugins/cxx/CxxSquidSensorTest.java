/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.cpd.internal.TokensLine;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.internal.DefaultNoSonarFilter;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.cxx.CxxMetrics;

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

    sensor = new CxxSquidSensor(fileLinesContextFactory, checkFactory, new DefaultNoSonarFilter(), null);
  }

  @Test
  public void testCollectingSquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/codechunks-project");
    var inputFile0 = TestUtils.buildInputFile(baseDir, "code_chunks.cc");

    var context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile0);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.NCLOC).value()).isEqualTo(54);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(50);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(7);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.CLASSES).value()).isZero();
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.COMPLEXITY).value()).isEqualTo(19);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.COGNITIVE_COMPLEXITY).value()).isEqualTo(8);
    softly.assertThat(context.measure(inputFile0.key(), CoreMetrics.COMMENT_LINES).value()).isEqualTo(15);
    softly.assertAll();
  }

  @Test
  public void testCpdTokens() throws Exception {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx");
    var context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.CPD_IGNORE_IDENTIFIERS_KEY, true);
    settings.setProperty(CxxSquidSensor.CPD_IGNORE_LITERALS_KEY, true);
    context.setSettings(settings);

    var inputFile = TestUtils.buildInputFile(baseDir, "cpd.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    List<TokensLine> cpdTokenLines = context.cpdTokens("ProjectKey:" + inputFile.file().getName());
    assertThat(cpdTokenLines).hasSize(75);

    // ld &= 0xFF;
    var firstTokensLine = cpdTokenLines.get(2);
    assertThat(firstTokensLine.getValue()).isEqualTo("_I&=_N;");
    assertThat(firstTokensLine.getStartLine()).isEqualTo(4);
    assertThat(firstTokensLine.getStartUnit()).isEqualTo(10);
    assertThat(firstTokensLine.getEndLine()).isEqualTo(4);
    assertThat(firstTokensLine.getEndUnit()).isEqualTo(13);

    // if (xosfile_read_stamped_no_path(fn, &ob, 1, 1, 1, 1, 1)) return 1;
    var secondTokensLine = cpdTokenLines.get(48);
    assertThat(secondTokensLine.getValue()).isEqualTo("if(_I(_I,&_I,_N,_N,_N,_N,_N))return_N;");
    assertThat(secondTokensLine.getStartLine()).isEqualTo(60);
    assertThat(secondTokensLine.getStartUnit()).isEqualTo(283);
    assertThat(secondTokensLine.getEndLine()).isEqualTo(60);
    assertThat(secondTokensLine.getEndUnit()).isEqualTo(305);

    // case 3: return "three";
    var thirdTokensLine = cpdTokenLines.get(71);
    assertThat(thirdTokensLine.getValue()).isEqualTo("case_N:return_S;");
    assertThat(thirdTokensLine.getStartLine()).isEqualTo(86);
    assertThat(thirdTokensLine.getStartUnit()).isEqualTo(381);
    assertThat(thirdTokensLine.getEndLine()).isEqualTo(86);
    assertThat(thirdTokensLine.getEndUnit()).isEqualTo(386);
  }

  @Test
  public void testComplexitySquidMetrics() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/complexity-project");
    var context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.FUNCTION_COMPLEXITY_THRESHOLD_KEY, 3);
    settings.setProperty(CxxSquidSensor.FUNCTION_SIZE_THRESHOLD_KEY, 3);
    context.setSettings(settings);

    var inputFile = TestUtils.buildInputFile(baseDir, "complexity.cc");
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
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/documentation-project");
    var inputFile = TestUtils.buildInputFile(baseDir, "documentation0.hh");

    var context = SensorContextTester.create(baseDir);
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
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/external-macro-project");
    var context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.DEFINES_KEY, "MACRO class A{};");
    context.setSettings(settings);

    var inputFile = TestUtils.buildInputFile(baseDir, "test.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isZero();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isZero();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isEqualTo(1);
    softly.assertAll();
  }

  @Test
  public void testFindingIncludedFiles() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/include-directories-project");
    var context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY, "include");
    context.setSettings(settings);

    var inputFile = TestUtils.buildInputFile(baseDir, "src/main.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isZero();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(9);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isZero();
    softly.assertAll();

  }

  @Test
  public void testForceIncludedFiles() throws IOException {
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/force-include-project");
    var context = SensorContextTester.create(baseDir);
    settings.setProperty(CxxSquidSensor.INCLUDE_DIRECTORIES_KEY, "include");
    settings.setProperty(CxxSquidSensor.FORCE_INCLUDES_KEY, "force1.hh,subfolder/force2.hh");
    context.setSettings(settings);

    var inputFile = TestUtils.buildInputFile(baseDir, "src/src1.cc");
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    // These checks actually check the force include feature, since only if it works the metric values will be like follows
    var softly = new SoftAssertions();
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.STATEMENTS).value()).isEqualTo(2);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.FUNCTIONS).value()).isEqualTo(1);
    softly.assertThat(context.measure(inputFile.key(), CoreMetrics.CLASSES).value()).isZero();
    softly.assertAll();
  }

  @Test
  public void testBehaviourOnCircularIncludes() throws IOException {
    // especially: when two files, both belonging to the set of
    // files to analyse, include each other, the preprocessor guards have to be disabled
    // and both have to be counted in terms of metrics
    File baseDir = TestUtils.loadResource("/org/sonar/plugins/cxx/circular-includes-project");
    var inputFile = TestUtils.buildInputFile(baseDir, "test1.hh");

    var context = SensorContextTester.create(baseDir);
    context.fileSystem().add(inputFile);
    sensor.execute(context);

    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(1);
  }

}
