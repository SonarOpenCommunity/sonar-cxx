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
package org.sonar.cxx.sensors.other;

import java.io.File;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxOtherXsltTest {

  private FileSystem fs;
  private CxxLanguage language;
  private MapSettings settings = new MapSettings();

  ;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    when(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY)).thenReturn("sonar.cxx." + CxxOtherSensor.REPORT_PATH_KEY);
    when(language.getPluginProperty("other.xslt.1.stylesheet")).thenReturn("sonar.cxx.other.xslt.1.stylesheet");
    when(language.getPluginProperty("other.xslt.1.inputs")).thenReturn("sonar.cxx.other.xslt.1.inputs");
    when(language.getPluginProperty("other.xslt.1.outputs")).thenReturn("sonar.cxx.other.xslt.1.outputs");

  }

  @Test
  public void shouldReportNothingWhenNoReportFound() {
    SensorContextTester context = SensorContextTester.create(fs.baseDir());

    when(language.getStringOption(CxxOtherSensor.REPORT_PATH_KEY)).thenReturn(Optional.of("notexistingpath"));
    when(language.getStringOption(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.STYLESHEET_KEY)).thenReturn(Optional.of("notexistingpath"));
    when(language.getStringOption(CxxOtherSensor.OTHER_XSLT_KEY + "2" + CxxOtherSensor.STYLESHEET_KEY)).thenReturn(Optional.of("notexistingpath"));
    when(language.getStringArrayOption(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.INPUT_KEY)).thenReturn(new String[]{"notexistingpath"});
    when(language.getStringArrayOption(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.OUTPUT_KEY)).thenReturn(new String[]{"notexistingpath"});
    CxxOtherSensor sensor = new CxxOtherSensor(language);

    sensor.execute(context);

    File reportAfter = new File("notexistingpath");
    Assert.assertFalse("The output file does exist!", reportAfter.exists() && reportAfter.isFile());
  }

  @Test
  public void transformReport_shouldTransformReport()
    throws java.io.IOException, javax.xml.transform.TransformerException {
    System.out.print("Starting transformReport_shouldTransformReport");
    String stylesheetFile = "externalrules-reports" + File.separator + "externalrules-xslt-stylesheet.xslt";
    String inputFile = "externalrules-reports" + File.separator + "externalrules-xslt-input.xml";
    String outputFile = "externalrules-reports" + File.separator + "externalrules-xslt-output.xml";

//    when(language.getStringOption(CxxOtherSensor.REPORT_PATH_KEY)).thenReturn("externalrules-xslt-output.xml");
//    when(language.getStringOption(language.getPluginProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.STYLESHEET_KEY))).thenReturn(stylesheetFile);
//    when(language.getStringArrayOption(language.getPluginProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.INPUT_KEY))).thenReturn(new String[] {inputFile});
//    when(language.getStringArrayOption(language.getPluginProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.OUTPUT_KEY))).thenReturn(new String[] {outputFile});
    SensorContextTester context = SensorContextTester.create(fs.baseDir());
    settings.setProperty(language.getPluginProperty(CxxOtherSensor.REPORT_PATH_KEY), "externalrules-xslt-output.xml");
    settings.setProperty(language.getPluginProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.STYLESHEET_KEY), stylesheetFile);
    settings.setProperty(language.getPluginProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.INPUT_KEY), inputFile);
    settings.setProperty(language.getPluginProperty(CxxOtherSensor.OTHER_XSLT_KEY + "1" + CxxOtherSensor.OUTPUT_KEY), outputFile);

    context.setSettings(settings);

    CxxOtherSensor sensor = new CxxOtherSensor(language);
    sensor.transformFiles(fs.baseDir(), context);

    File reportBefore = new File(fs.baseDir() + "/" + inputFile);
    File reportAfter = new File(fs.baseDir() + "/" + outputFile);
    Assert.assertTrue("The output file does not exist!", reportAfter.exists() && reportAfter.isFile());
    Assert.assertTrue("The input and output file is equal!", !FileUtils.contentEquals(reportBefore, reportAfter));
  }
}
