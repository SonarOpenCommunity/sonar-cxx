/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.prejobs;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.cxx.sensors.utils.CxxReportSensor;
import org.sonar.cxx.sensors.utils.TestUtils;

public class XlstSensorTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private FileSystem fs;
  private final MapSettings settings = new MapSettings();

  @BeforeEach
  public void setUp() {
    fs = TestUtils.mockFileSystem(TestUtils.loadResource("/org/sonar/cxx"));

    settings.setProperty(CxxReportSensor.ERROR_RECOVERY_KEY, true);
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "2" + XlstSensor.STYLESHEET_KEY, "");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "2" + XlstSensor.INPUT_KEY, "");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "2" + XlstSensor.OUTPUT_KEY, "");
  }

  @Test
  public void noLoggingIfNotUsed() {
    var context = SensorContextTester.create(fs.baseDir());

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    assertThat(logTester.logs(LoggerLevel.ERROR).isEmpty()).isTrue();
    assertThat(logTester.logs(LoggerLevel.WARN).isEmpty()).isTrue();
    assertThat(logTester.logs(LoggerLevel.INFO).isEmpty()).isTrue();
  }

  @Test
  public void shouldReportNothingWhenNoReportFound() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, "notexistingpath");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "2" + XlstSensor.STYLESHEET_KEY, "notexistingpath");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.INPUT_KEY, "notexistingpath");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.OUTPUT_KEY, "notexistingpath");
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    var reportAfter = new File("notexistingpath");
    assertThat(reportAfter.exists() && reportAfter.isFile()).
      withFailMessage("The output file does exist!").isFalse();
  }

  @Test
  public void shouldNotCreateMessage() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, "something");
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void shouldCreateMissingStylesheetMessage() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, "");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.OUTPUT_KEY, "outputs");
    settings.setProperty("outputs", "outputs");
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    List<String> log = logTester.logs(LoggerLevel.ERROR);
    assertThat(log).contains("XLST: 'sonar.cxx.xslt.1.stylesheet' value is not defined.");
  }

  @Test
  public void shouldCreateEmptyInputsMessage() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, "something");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.INPUT_KEY, "");
    settings.setProperty("something", "something");
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    List<String> log = logTester.logs(LoggerLevel.ERROR);
    assertThat(log).contains("XLST: 'sonar.cxx.xslt.1.inputs' value is not defined.");
  }

  @Test
  public void shouldCreateEmptyOutputsMessage() {
    var context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, "something");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.INPUT_KEY, "something");
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.OUTPUT_KEY, "");
    settings.setProperty("something", "something");
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    List<String> log = logTester.logs(LoggerLevel.ERROR);
    assertThat(log).contains("XLST: 'sonar.cxx.xslt.1.outputs' value is not defined.");
  }

  @Test
  public void shouldTransformReportExternalXlst()
    throws java.io.IOException, javax.xml.transform.TransformerException {
    var context = SensorContextTester.create(fs.baseDir());
    var stylesheetFile = "prejobs" + File.separator + "xslt-stylesheet.xslt";
    var inputFile = "prejobs" + File.separator + "xslt-input.xml";
    var outputFile = "xslt-output.xml";
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, stylesheetFile);
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.INPUT_KEY, inputFile);
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.OUTPUT_KEY, outputFile);
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    var reportBefore = new File(fs.baseDir() + File.separator + inputFile);
    var reportAfter = new File(fs.baseDir() + File.separator + "prejobs" + File.separator + "xslt-output.xml");
    assertThat(reportAfter.exists() && reportAfter.isFile())
      .withFailMessage("The output file does not exist!").isTrue();
    assertThat(!FileUtils.contentEquals(reportBefore, reportAfter))
      .withFailMessage("The input and output file is equal!").isTrue();
  }

  @Test
  public void shouldTransformReportInternalXlst()
    throws java.io.IOException, javax.xml.transform.TransformerException {
    var context = SensorContextTester.create(fs.baseDir());
    var stylesheetFile = "cppunit-1.x-to-junit-1.0.xsl";
    var inputFile = "prejobs" + File.separator + "cppunit-report.xml";
    var outputFile = "_*.after_xslt";
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.STYLESHEET_KEY, stylesheetFile);
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.INPUT_KEY, inputFile);
    settings.setProperty(XlstSensor.OTHER_XSLT_KEY + "1" + XlstSensor.OUTPUT_KEY, outputFile);
    context.setSettings(settings);

    var sensor = new XlstSensor();
    logTester.clear();
    sensor.execute(context);

    var reportBefore = new File(fs.baseDir() + File.separator + inputFile);
    var reportAfter = new File(fs.baseDir() + File.separator + "prejobs" + File.separator + "_cppunit-report.after_xslt");
    assertThat(reportAfter.exists() && reportAfter.isFile())
      .withFailMessage("The output file does not exist!").isTrue();
    assertThat(!FileUtils.contentEquals(reportBefore, reportAfter))
      .withFailMessage("The input and output file is equal!").isTrue();
  }

}
