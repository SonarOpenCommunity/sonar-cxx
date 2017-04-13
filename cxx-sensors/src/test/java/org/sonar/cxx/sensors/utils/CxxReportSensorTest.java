/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import org.sonar.cxx.sensors.utils.CxxReportSensor;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import org.sonar.api.config.Settings;
import org.sonar.cxx.CxxLanguage;

public class CxxReportSensorTest {

  private final String VALID_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  private final String VALID_REPORT_PATH_LIST = "cppcheck-reports/*V1.xml, cppcheck-reports/*V2.xml";
  private final String INVALID_REPORT_PATH = "something";
  private final String REPORT_PATH_PROPERTY_KEY = "cxx.reportPath";
 
  private class CxxReportSensorImpl extends CxxReportSensor {

    public CxxReportSensorImpl(CxxLanguage language, FileSystem fs) {
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
    protected String reportPathKey() {
      return "test.report";
    }

    @Override
    protected String getSensorKey() {
      return "testSensor";
    }
  };

  private CxxReportSensor sensor;
  private File baseDir;
  private static FileSystem fs;

  @Before
  public void init() {
    fs = TestUtils.mockFileSystem();
    CxxLanguage language = TestUtils.mockCxxLanguage();
    sensor = new CxxReportSensorImpl(language, fs);
    try {
      baseDir = new File(getClass().getResource("/org/sonar/cxx/sensors/reports-project/").toURI());
    } catch (java.net.URISyntaxException e) {
      System.out.println(e);
    }
  }

  @Test
  public void shouldntThrowWhenInstantiating() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    new CxxReportSensorImpl(language, fs);
  }

  @Test
  public void getReports_shouldFindNothingIfNoKey() {    
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(REPORT_PATH_PROPERTY_KEY)).thenReturn(new String[] { INVALID_REPORT_PATH });
    List<File> reports = CxxReportSensor.getReports(language, baseDir, "");
    assertNotFound(reports);
  }

  @Test
  public void getReports_shouldFindNothingIfNoPath() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(REPORT_PATH_PROPERTY_KEY)).thenReturn(new String[] { "" });    
    List<File> reports = CxxReportSensor.getReports(language, baseDir, REPORT_PATH_PROPERTY_KEY);
    assertNotFound(reports);
  }

  @Test
  public void getReports_shouldFindNothingIfInvalidPath() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(REPORT_PATH_PROPERTY_KEY)).thenReturn(new String[] { INVALID_REPORT_PATH });    
    List<File> reports = CxxReportSensor.getReports(language, baseDir, REPORT_PATH_PROPERTY_KEY);
    assertNotFound(reports);
  }

  @Test
  public void getReports_shouldFindSomething() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(REPORT_PATH_PROPERTY_KEY)).thenReturn(new String[] { VALID_REPORT_PATH });        
    List<File> reports = CxxReportSensor.getReports(language, baseDir, REPORT_PATH_PROPERTY_KEY);
    assertFound(reports);
    assert (reports.size() == 6);
  }

  @Test
  public void getReports_shouldFindSomethingList() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(REPORT_PATH_PROPERTY_KEY)).thenReturn(new String[] { "cppcheck-reports/*V1.xml", "cppcheck-reports/*V2.xml" });            
    List<File> reports = CxxReportSensor.getReports(language, baseDir, REPORT_PATH_PROPERTY_KEY);
    assertFound(reports);
    assert (reports.size() == 5);
  }

  @Test
  public void savesACorrectLineLevelViolation() {
    // assert(sensor.saveViolation(??, ??, rulerepokey, "existingfile",
    //                             "1", "existingruleid", "somemessage"))
  }

  @Test
  public void savesACorrectFileLevelViolation() {
    //TDB
  }

  @Test
  public void savesACorrectProjectLevelViolation() {
    //TDB
  }

  ///// negative testcases for saveViolation ////////////
  @Test
  public void savesOnProjectLevelIfFilenameIsEmpty() {
    //TDB
  }

  @Test
  public void doesNotSaveIfLineNumberCannotBeParsed() {
    //TDB
  }

  @Test
  public void doesNotSaveIfRuleCannotBeFound() {
    //TDB
  }

  @Test
  public void doesNotSaveIfResourceCannotBeFoundInSonar() {
    //TDB
  }

  private void assertFound(List<File> reports) {
    assert (reports != null);
    assert (reports.get(0).exists());
    assert (reports.get(0).isAbsolute());
  }

  private void assertNotFound(List<File> reports) {
    assert (reports != null);
  }

}
