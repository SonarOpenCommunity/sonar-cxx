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
package org.sonar.cxx.sensors.other;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxOtherXsltTest {

  @Rule
  public LogTester logTester = new LogTester();

  private FileSystem fs;
  private final MapSettings settings = new MapSettings();

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();

    settings.setProperty("sonar.cxx.errorRecoveryEnabled", true);
    settings.setProperty("sonar.cxx.other.xslt.2.stylesheet", "");
    settings.setProperty("sonar.cxx.other.xslt.2.inputs", "");
    settings.setProperty("sonar.cxx.other.xslt.2.outputs", "");
  }

  @Test
  public void noLoggingIfNotUsed() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.transformFiles(fs.baseDir(), context);

    Assert.assertTrue(logTester.logs(LoggerLevel.ERROR).isEmpty());
    Assert.assertTrue(logTester.logs(LoggerLevel.WARN).isEmpty());
    Assert.assertTrue(logTester.logs(LoggerLevel.INFO).isEmpty());
  }

  @Test
  public void shouldReportNothing() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.execute(context);

    List<String> log = logTester.logs(LoggerLevel.ERROR);
    assertThat(log).isEmpty();
  }

  @Test
  public void shouldReportNothingWhenNoReportFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "notexistingpath");
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.STYLESHEET_KEY, "notexistingpath");
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "2" + CxxOtherSensor.STYLESHEET_KEY, "notexistingpath");
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.INPUT_KEY, "notexistingpath");
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.OUTPUT_KEY, "notexistingpath");
    context.setSettings(settings);

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.execute(context);

    File reportAfter = new File("notexistingpath");
    Assert.assertFalse("The output file does exist!", reportAfter.exists() && reportAfter.isFile());
  }

  @Test
  public void shouldNotCreateMessage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty("sonar.cxx.other.xslt.1.stylesheet", "something");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(0);
  }

  @Test
  public void shouldCreateMissingStylesheetMessage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty("sonar.cxx.other.xslt.1.stylesheet", "");
    settings.setProperty("sonar.cxx.other.xslt.1.outputs", "outputs");
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-reports/externalrules-with-duplicates.xml");
    settings.setProperty("outputs", "outputs");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.execute(context);

    List<String> log = logTester.logs(LoggerLevel.ERROR);
    assertThat(log).contains("XLST: sonar.cxx.other.xslt.1.stylesheet value is not defined.");
  }

  @Test
  public void shouldCreateEmptyInputsMessage() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty("sonar.cxx.other.xslt.1.stylesheet", "something");
    settings.setProperty("sonar.cxx.other.xslt.1.inputs", "");
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "something");
    settings.setProperty("something", "something");
    context.setSettings(settings);

    context.fileSystem().add(TestInputFileBuilder.create("ProjectKey", "sources/utils/code_chunks.cpp")
      .setLanguage("cpp").initMetadata("asd\nasdas\nasda\n").build());

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.execute(context);

    List<String> log = logTester.logs(LoggerLevel.ERROR);
    assertThat(log).contains("XLST: sonar.cxx.other.xslt.1.inputs value is not defined.");
  }

  @Test
  public void transformReport_shouldTransformReport()
    throws java.io.IOException, javax.xml.transform.TransformerException {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    String stylesheetFile = "externalrules-reports" + File.separator + "externalrules-xslt-stylesheet.xslt";
    String inputFile = "externalrules-reports" + File.separator + "externalrules-xslt-input.xml";
    String outputFile = "externalrules-reports" + File.separator + "externalrules-xslt-output.xml";
    settings.setProperty(CxxOtherSensor.REPORT_PATH_KEY, "externalrules-xslt-output.xml");
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.STYLESHEET_KEY, stylesheetFile);
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.INPUT_KEY, inputFile);
    settings.setProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.OUTPUT_KEY, outputFile);
    context.setSettings(settings);

    CxxOtherSensor sensor = new CxxOtherSensor(settings.asConfig());
    logTester.clear();
    sensor.transformFiles(fs.baseDir(), context);

    File reportBefore = new File(fs.baseDir() + "/" + inputFile);
    File reportAfter = new File(fs.baseDir() + "/" + outputFile);
    Assert.assertTrue("The output file does not exist!", reportAfter.exists() && reportAfter.isFile());
    Assert.assertTrue("The input and output file is equal!", !FileUtils.contentEquals(reportBefore, reportAfter));
  }

}
