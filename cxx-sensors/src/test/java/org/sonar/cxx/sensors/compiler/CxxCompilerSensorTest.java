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
package org.sonar.cxx.sensors.compiler;

import java.io.File;
import java.nio.charset.StandardCharsets;
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
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxCompilerSensorTest {

  @Rule
  public LogTester logTester = new LogTester();

  private DefaultFileSystem fs;
  SensorContextTester context;
  CxxCompilerSensorMock sensor;

  @Before
  public void setUp() {
    fs = TestUtils.mockFileSystem();
    context = SensorContextTester.create(fs.baseDir());
    sensor = new CxxCompilerSensorMock();
  }

  @Test
  public void testFileNotFound() throws XMLStreamException {
    var report = new File("");
    sensor.setRegex("*");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("FileNotFoundException")).isTrue();
  }

  @Test
  public void testRegexEmpty() throws XMLStreamException {
    var report = new File("");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("empty custom regular expression")).isTrue();
  }

  @Test
  public void testRegexInvalid() throws XMLStreamException {
    var report = new File(fs.baseDir(), "compiler-reports/VC-report.vclog");
    sensor.setRegex("*");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("PatternSyntaxException")).isTrue();
  }

  @Test
  public void testRegexNamedGroupMissing() throws XMLStreamException {
    var report = new File(fs.baseDir(), "compiler-reports/VC-report.vclog");
    sensor.setRegex(".*");
    sensor.testProcessReport(context, report);
    String log = logTester.logs().toString();
    assertThat(log.contains("No group with name")).isTrue();
  }

  private class CxxCompilerSensorMock extends CxxCompilerSensor {

    private String regex = "";

    @Override
    public void describe(SensorDescriptor descriptor) {
    }

    public void testProcessReport(final SensorContext context, File report) throws XMLStreamException {
      processReport(context, report);
    }

    public void setRegex(String regex) {
      this.regex = regex;
    }

    @Override
    protected String getCompilerKey() {
      return "XXX";
    }

    @Override
    protected String getCharset(final SensorContext context) {
      return StandardCharsets.UTF_8.name();
    }

    @Override
    protected String getRegex(final SensorContext context) {
      return regex;
    }

    @Override
    protected String getReportPathKey() {
      return "cxx.reportPath";
    }

    @Override
    protected String getRuleRepositoryKey() {
      return "cxx.XXX";
    }

  }

}
