/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.utils;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.TestUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CxxReportSensorTest {
  private final String VALID_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  private final String INVALID_REPORT_PATH = "something";
  private final String REPORT_PATH_PROPERTY_KEY = "cxx.reportPath";

  private class CxxReportSensorImpl extends CxxReportSensor {
    public CxxReportSensorImpl(Settings settings, ModuleFileSystem fs){
      super(settings, fs);
    }
    
    @Override
    public void analyse(Project p, SensorContext sc) {
    }
  };

  private CxxReportSensor sensor;
  private File baseDir;
  private Settings settings;
  private ModuleFileSystem fs;

  @Before
  public void init() {
    settings = new Settings();
    fs = TestUtils.mockFileSystem();
    sensor = new CxxReportSensorImpl(settings, fs);
    try {
      baseDir = new File(getClass().getResource("/org/sonar/plugins/cxx/reports-project/").toURI());
    } catch (java.net.URISyntaxException e) {
      System.out.println(e);
    }
  }

  @Test
  public void shouldntThrowWhenInstantiating() {
    new CxxReportSensorImpl(settings, fs);
  }

  @Test
  public void shouldAllwaysExecute() {
    // which means: only on cxx projects
    CxxReportSensor sensor = new CxxReportSensorImpl(settings, fs);
    Project cxxProject = mockProjectWithSomeFiles(CxxLanguage.KEY);
    Project foreignProject = mockProjectWithLanguageKey("whatever");
    assert (sensor.shouldExecuteOnProject(cxxProject));
  }

  @Test
  public void getReports_shouldFindSomethingIfThere() {
    List<File> reports = sensor.getReports(settings, baseDir.getPath(),
        "", VALID_REPORT_PATH);
    assertFound(reports);
  }

  @Test
  public void getReports_shouldFindNothingIfNotThere() {
    List<File> reports = sensor.getReports(new Settings(), baseDir.getPath(),
        "", INVALID_REPORT_PATH);
    assertNotFound(reports);
  }

  @Test
  public void getReports_shouldUseConfigurationWithHigherPriority() {
    // we'll detect this condition by passing something not existing as config property
    // and something existing as default. The result is 'found nothing' because the
    // config has been used
    settings.setProperty(REPORT_PATH_PROPERTY_KEY, INVALID_REPORT_PATH);

    List<File> reports = sensor.getReports(settings, baseDir.getPath(),
        REPORT_PATH_PROPERTY_KEY, VALID_REPORT_PATH);
    assertNotFound(reports);
  }

  @Test
  public void getReports_shouldFallbackToDefaultIfNothingConfigured() {
    List<File> reports = sensor.getReports(settings, baseDir.getPath(),
        REPORT_PATH_PROPERTY_KEY, VALID_REPORT_PATH);
    assertFound(reports);
  }

  private void assertFound(List<File> reports) {
    assert (reports != null);
    assert (reports.get(0).exists());
    assert (reports.get(0).isAbsolute());
  }

  private void assertNotFound(List<File> reports) {
    assert (reports != null);
  }

  private static Project mockProjectWithLanguageKey(String languageKey) {
    Project project = TestUtils.mockProject();
    when(project.getLanguageKey()).thenReturn(languageKey);
    return project;
  }

  private static Project mockProjectWithSomeFiles(String languageKey) {
        Project project = TestUtils.mockProject();
        List<InputFile> listofFiles = new LinkedList<InputFile>();
        InputFile inputFile = mock(InputFile.class);
        listofFiles.add(0, inputFile);
        when(project.getLanguageKey()).thenReturn(languageKey);
        when(project.getFileSystem().mainFiles(languageKey)).thenReturn(listofFiles);
        return project;
  }
}
