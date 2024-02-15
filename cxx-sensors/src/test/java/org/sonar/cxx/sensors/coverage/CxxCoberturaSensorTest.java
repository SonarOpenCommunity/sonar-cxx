/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.cxx.sensors.coverage.cobertura.CoberturaParser;
import org.sonar.cxx.sensors.coverage.cobertura.CxxCoverageCoberturaSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

class CxxCoberturaSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private DefaultFileSystem fs;
  private final Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  void testPathJoin() {
    var empty = Path.of("");
    String result;
    /*
     * path1    | path2    | result
     * ---------|----------|-------
     * empty    | empty    | empty
     * empty    | absolute | absolute path2
     * empty    | relative | relative path2
     * absolute | empty    | empty
     * relative | empty    | empty
     * absolute | absolute | absolute path2
     * absolute | relative | absolute path1 + relative path2
     * relative | absolute | absolute path2
     * relative | relative | relative path1 + relative path2
     */

    if (TestUtils.isWindows()) {

      // Windows
      var p1_abs1 = Path.of("c:\\test1");
      var p1_abs2 = Path.of("c:");
      var p1_abs3 = Path.of("c:\\");
      var p1_rel1 = Path.of("\\test1");
      var p2_abs1 = Path.of("c:\\test2\\report.txt");
      var p2_rel1 = Path.of("\\test2\\report.txt");
      var p2_rel2 = Path.of("test2\\report.txt");

      result = CoberturaParser.join(empty, empty);
      assertThat(result).isEmpty();

      result = CoberturaParser.join(empty, p2_abs1);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(empty, p2_rel1);
      assertThat(result).isEqualTo(".\\test2\\report.txt");

      result = CoberturaParser.join(p1_abs1, empty);
      assertThat(result).isEmpty();

      result = CoberturaParser.join(p1_rel1, empty);
      assertThat(result).isEmpty();

      result = CoberturaParser.join(p1_abs1, p2_abs1);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(p1_abs1, p2_rel1);
      assertThat(result).isEqualTo("c:\\test1\\test2\\report.txt");

      result = CoberturaParser.join(p1_rel1, p2_abs1);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(p1_rel1, p2_rel1);
      assertThat(result).isEqualTo(".\\test1\\test2\\report.txt");

      result = CoberturaParser.join(p1_abs2, p2_rel2);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(p1_abs3, p2_rel2);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");
    } else {

      // Linux
      var p1_abs1 = Path.of("/home/test1");
      var p1_rel1 = Path.of("test1");
      var p2_abs1 = Path.of("/home/test2/report.txt");
      var p2_rel1 = Path.of("test2/report.txt");

      result = CoberturaParser.join(empty, empty);
      assertThat(result).isEmpty();

      result = CoberturaParser.join(empty, p2_abs1);
      assertThat(result).isEqualTo("/home/test2/report.txt");

      result = CoberturaParser.join(empty, p2_rel1);
      assertThat(result).isEqualTo("./test2/report.txt");

      result = CoberturaParser.join(p1_abs1, empty);
      assertThat(result).isEmpty();

      result = CoberturaParser.join(p1_rel1, empty);
      assertThat(result).isEmpty();

      result = CoberturaParser.join(p1_abs1, p2_abs1);
      assertThat(result).isEqualTo("/home/test2/report.txt");

      result = CoberturaParser.join(p1_abs1, p2_rel1);
      assertThat(result).isEqualTo("/home/test1/test2/report.txt");

      result = CoberturaParser.join(p1_rel1, p2_abs1);
      assertThat(result).isEqualTo("/home/test2/report.txt");

      result = CoberturaParser.join(p1_rel1, p2_rel1);
      assertThat(result).isEqualTo("./test1/test2/report.txt");
    }

  }

  @Test
  void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY,
                         "coverage-reports/cobertura/coverage-result-cobertura.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isZero();
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isZero();
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isZero();
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
  }

  @Test
  void shouldReportCorrectCoverageSQ62() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY,
                         "coverage-reports/cobertura/coverage-result-cobertura.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isZero();
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
  }

  @Test
  void shouldReportNoCoverageSaved() {
    var context = SensorContextTester.create(fs.baseDir());
    final String reportPathsValue = "coverage-reports/cobertura/specific-cases/does-not-exist.xml";
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY, reportPathsValue);
    context.setSettings(settings);

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    List<String> log = logTester.logs();
    assertThat(log).contains(
      "Property 'sonar.cxx.cobertura.reportPaths': cannot find any files matching the Ant pattern(s) '"
        + PathUtils.sanitize(new File(fs.baseDir(), reportPathsValue).getAbsolutePath()) + "'");
  }

  @Test
  void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY,
                         "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");
    context.setSettings(settings);

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    assertThat(linesOfCodeByFile).isEmpty();
  }

  @Test
  void shouldReportNoCoverageWhenReportEmpty() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY,
                         "coverage-reports/cobertura/specific-cases/coverage-result-cobertura-empty.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isNull();
  }

  @Test
  void shouldReportNoCoverageWhenReportInvalid() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY,
                         "coverage-reports/cobertura/specific-cases/coverage-result-invalid.xml");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/application/main.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n\n\n\n\n\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/utils.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());
    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cxx").initMetadata("asd\nasdas\nasda\n").build());

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 1)).isNull();
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isNull();
  }

}
