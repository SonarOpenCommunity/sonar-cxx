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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import javax.xml.stream.XMLStreamException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.LogTester;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCompilerSensorTest {

  private DefaultFileSystem fs;
  private CxxLanguage language;
  SensorContextTester context;
  CxxCompilerSensorMock sensor;

  @Rule
  public LogTester logTester = new LogTester();

  private class CxxCompilerSensorMock extends CxxCompilerSensor {

    private String regex = "";

    public CxxCompilerSensorMock(CxxLanguage language) {
      super(language, "cxx.reportPath", "cxx.XXX");
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
    }

    @Override
    protected String getCompilerKey() {
      return "XXX";
    }

    @Override
    protected String getCharset(final SensorContext context) {
      return "UTF-8";
    }

    @Override
    protected String getRegex(final SensorContext context) {
      return regex;
    }

    @Override
    protected CxxMetricsFactory.Key getMetricKey() {
      return CxxMetricsFactory.Key.OTHER_SENSOR_ISSUES_KEY;
    }

    public void testProcessReport(final SensorContext context, File report) throws XMLStreamException {
      processReport(context, report);
    }

    public void setRegex(String regex) {
      this.regex = regex;
    }
  }

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    language = TestUtils.mockCxxLanguage();
    context = SensorContextTester.create(fs.baseDir());
    sensor = new CxxCompilerSensorMock(language);
  }

  @Test
  public void testFileNotFound() throws XMLStreamException {
    File report = new File("");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("FileNotFoundException")).isTrue();
  }

  @Test
  public void testRegexInvalid() throws XMLStreamException {
    File report = new File(fs.baseDir(), "compiler-reports/VC-report.vclog");
    sensor.setRegex("*");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("PatternSyntaxException")).isTrue();
  }

  @Test
  public void testRegexNamedGroupMissing() throws XMLStreamException {
    File report = new File(fs.baseDir(), "compiler-reports/VC-report.vclog");
    sensor.setRegex(".*");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("No group with name")).isTrue();
  }

}
