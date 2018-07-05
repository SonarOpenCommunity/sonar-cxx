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
package org.sonar.cxx.sensors.utils;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.CxxMetricsFactory;

public class CxxReportSensorTest {

  private final String VALID_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  private final String VALID_REPORT_PATH_LIST = "cppcheck-reports/*V1.xml, cppcheck-reports/*V2.xml";
  private final String INVALID_REPORT_PATH = "something";
  private final String REPORT_PATH_PROPERTY_KEY = "cxx.reportPath";

  private File baseDir;
  private MapSettings settings = new MapSettings();

  private class CxxReportSensorImpl extends CxxReportSensor {

    public CxxReportSensorImpl(CxxLanguage language, MapSettings settings) {
      super(language);
    }

    @Override
    public void execute(SensorContext sc) {
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
      descriptor.onlyOnLanguage("c++").name("CxxReportSensorTest");
    }

    @Override
    public String getReportPathKey() {
      return "test.report";
    }

    @Override
    protected String getSensorKey() {
      return "testSensor";
    }

    @Override
    protected Optional<CxxMetricsFactory.Key> getMetricKey() {
      return Optional.empty();
    }
  };

  @Before
  public void init() {
    TestUtils.mockFileSystem();
    try {
      baseDir = new File(getClass().getResource("/org/sonar/cxx/sensors/reports-project/").toURI());
    } catch (java.net.URISyntaxException e) {
      System.out.println(e);
    }
  }

  @Test
  public void shouldntThrowWhenInstantiating() {
    CxxLanguage language = TestUtils.mockCxxLanguage();

    CxxReportSensor sensor = new CxxReportSensorImpl(language, settings);
    assertThat(sensor).isNotNull();
  }

  @Test
  public void getReports_shouldFindNothingIfNoKey() {
    TestUtils.mockCxxLanguage();
    settings.setProperty(REPORT_PATH_PROPERTY_KEY, INVALID_REPORT_PATH);

    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), baseDir, "");
    assertThat(reports).isNotNull();
  }

  @Test
  public void getReports_shouldFindNothingIfNoPath() {
    TestUtils.mockCxxLanguage();
    settings.setProperty(REPORT_PATH_PROPERTY_KEY, "");
    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), baseDir, REPORT_PATH_PROPERTY_KEY);
    assertThat(reports).isNotNull();
  }

  @Test
  public void getReports_shouldFindNothingIfInvalidPath() {
    TestUtils.mockCxxLanguage();
    settings.setProperty(REPORT_PATH_PROPERTY_KEY, INVALID_REPORT_PATH);
    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), baseDir, REPORT_PATH_PROPERTY_KEY);
    assertThat(reports).isNotNull();
  }

  @Test
  public void getReports_shouldFindSomething() {
    TestUtils.mockCxxLanguage();
    settings.setProperty(REPORT_PATH_PROPERTY_KEY, VALID_REPORT_PATH);
    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), baseDir, REPORT_PATH_PROPERTY_KEY);
    assertThat(reports).isNotNull();
    assertThat(reports.get(0).exists()).isTrue();
    assertThat(reports.get(0).isAbsolute()).isTrue();
    assertThat(reports.size() == 6).isTrue();
  }

  @Test
  public void getReports_shouldFindSomethingList() {
    TestUtils.mockCxxLanguage();
    settings.setProperty(REPORT_PATH_PROPERTY_KEY, VALID_REPORT_PATH_LIST);
    List<File> reports = CxxReportSensor.getReports(settings.asConfig(), baseDir, REPORT_PATH_PROPERTY_KEY);
    assertThat(reports).isNotNull();
    assertThat(reports.get(0).exists()).isTrue();
    assertThat(reports.get(0).isAbsolute()).isTrue();
    assertThat(reports.size() == 5).isTrue();
  }

}
