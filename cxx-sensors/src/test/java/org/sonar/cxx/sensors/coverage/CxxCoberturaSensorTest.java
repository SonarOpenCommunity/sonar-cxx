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
package org.sonar.cxx.sensors.coverage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.log.LogTester;
import org.sonar.cxx.sensors.coverage.cobertura.CoberturaParser;
import org.sonar.cxx.sensors.coverage.cobertura.CxxCoverageCoberturaSensor;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCoberturaSensorTest {

  @org.junit.Rule
  public LogTester logTester = new LogTester();

  private DefaultFileSystem fs;
  private final Map<InputFile, Set<Integer>> linesOfCodeByFile = new HashMap<>();
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
  }

  @Test
  public void testPathJoin() {
    Path empty = Paths.get("");
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
      Path abs1 = Paths.get("c:\\test1");
      Path rel1 = Paths.get("\\test1");
      Path abs2 = Paths.get("c:\\test2\\report.txt");
      Path rel2 = Paths.get("\\test2\\report.txt");

      result = CoberturaParser.join(empty, empty);
      assertThat(result).isEqualTo("");

      result = CoberturaParser.join(empty, abs2);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(empty, rel2);
      assertThat(result).isEqualTo(".\\test2\\report.txt");

      result = CoberturaParser.join(abs1, empty);
      assertThat(result).isEqualTo("");

      result = CoberturaParser.join(rel1, empty);
      assertThat(result).isEqualTo("");

      result = CoberturaParser.join(abs1, abs2);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(abs1, rel2);
      assertThat(result).isEqualTo("c:\\test1\\test2\\report.txt");

      result = CoberturaParser.join(rel1, abs2);
      assertThat(result).isEqualTo("c:\\test2\\report.txt");

      result = CoberturaParser.join(rel1, rel2);
      assertThat(result).isEqualTo(".\\test1\\test2\\report.txt");
    } else {

      // Linux
      Path abs1 = Paths.get("/home/test1");
      Path rel1 = Paths.get("test1");
      Path abs2 = Paths.get("/home/test2/report.txt");
      Path rel2 = Paths.get("test2/report.txt");

      result = CoberturaParser.join(empty, empty);
      assertThat(result).isEqualTo("");

      result = CoberturaParser.join(empty, abs2);
      assertThat(result).isEqualTo("/home/test2/report.txt");

      result = CoberturaParser.join(empty, rel2);
      assertThat(result).isEqualTo("./test2/report.txt");

      result = CoberturaParser.join(abs1, empty);
      assertThat(result).isEqualTo("");

      result = CoberturaParser.join(rel1, empty);
      assertThat(result).isEqualTo("");

      result = CoberturaParser.join(abs1, abs2);
      assertThat(result).isEqualTo("/home/test2/report.txt");

      result = CoberturaParser.join(abs1, rel2);
      assertThat(result).isEqualTo("/home/test1/test2/report.txt");

      result = CoberturaParser.join(rel1, abs2);
      assertThat(result).isEqualTo("/home/test2/report.txt");

      result = CoberturaParser.join(rel1, rel2);
      assertThat(result).isEqualTo("./test1/test2/report.txt");
    }

  }

  @Test
  public void shouldReportCorrectCoverageForAllTypesOfCoverage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
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
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 1)).isEqualTo(1);
    assertThat(context.lineHits("ProjectKey:sources/utils/code_chunks.cpp", 3)).isEqualTo(4);
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
  }

  @Test
  public void shouldReportCorrectCoverageSQ62() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
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
    assertThat(context.lineHits("ProjectKey:sources/utils/utils.cpp", 2)).isEqualTo(0);
    assertThat(context.lineHits("ProjectKey:sources/application/main.cpp", 8)).isEqualTo(8);
  }

  @Test
  public void shouldReportNoCoverageSaved() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
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
  public void shouldNotCrashWhenProcessingReportsContainingBigNumberOfHits() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxCoverageCoberturaSensor.REPORT_PATH_KEY,
                         "coverage-reports/cobertura/specific-cases/cobertura-bignumberofhits.xml");
    context.setSettings(settings);

    var sensor = new CxxCoverageCoberturaSensor();
    sensor.execute(context);

    assertThat(linesOfCodeByFile.isEmpty()).isTrue();
  }

  @Test
  public void shouldReportNoCoverageWhenReportEmpty() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
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
  public void shouldReportNoCoverageWhenReportInvalid() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
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
