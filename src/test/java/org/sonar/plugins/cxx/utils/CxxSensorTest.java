/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx.utils;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.resources.Project;
import org.sonar.api.batch.SensorContext;
import org.junit.Test;
import org.junit.Before;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.TestUtils;

public class CxxSensorTest {
  private final String VALID_REPORT_PATH = "cppcheck-reports/cppcheck-result-*.xml";
  private final String INVALID_REPORT_PATH = "something";
  private final String REPORT_PATH_PROPERTY_KEY = "cxx.reportPath";

  private class CxxSensorImpl extends CxxSensor {
    public void analyse(Project p, SensorContext sc){}
  };

  private CxxSensor sensor;
  private File baseDir;

  @Before
  public void init() {
    sensor = new CxxSensorImpl();
    try{
      baseDir = new File(getClass().getResource("/org/sonar/plugins/cxx/").toURI());
    }
    catch(java.net.URISyntaxException e){
      System.out.println(e);
      return;
    }
  }

  @Test
  public void shouldntThrowWhenInstantiating() {
    new CxxSensorImpl();
  }

  @Test
  public void shouldExecuteOnlyWhenNecessary() {
    // which means: only on cxx projects
    CxxSensor sensor = new CxxSensorImpl();
    Project cxxProject = mockProjectWithLanguageKey(CxxLanguage.KEY);
    Project foreignProject = mockProjectWithLanguageKey("whatever");
    assert(sensor.shouldExecuteOnProject(cxxProject));
    assert(!sensor.shouldExecuteOnProject(foreignProject));
  }

  @Test
  public void getReports_shouldFindSomethingIfThere(){
    List<File> reports = sensor.getReports(mock(Configuration.class), baseDir.getPath(),
                                           "", VALID_REPORT_PATH);
    assertFound(reports);
  }

  @Test
  public void getReports_shouldFindNothingIfNotThere(){
    List<File> reports = sensor.getReports(mock(Configuration.class), baseDir.getPath(),
                                           "", INVALID_REPORT_PATH);
    assertNotFound(reports);
  }

  @Test
  public void getReports_shouldUseConfigurationWithHigherPriority(){
    // we'll detect this condition by passing something not existing as config property
    // and something existing as default. The result is 'found nothing' because the
    // config has been used

    Configuration config = mock(Configuration.class);
    when(config.getString(REPORT_PATH_PROPERTY_KEY)).thenReturn(INVALID_REPORT_PATH);
    
    List<File> reports = sensor.getReports(config, baseDir.getPath(),
                                           REPORT_PATH_PROPERTY_KEY, VALID_REPORT_PATH);
    assertFound(reports);
  }

  @Test
  public void getReports_shouldFallbackToDefaultIfNothingConfigured(){
    Configuration config = mock(Configuration.class);
    List<File> reports = sensor.getReports(config, baseDir.getPath(),
                                           REPORT_PATH_PROPERTY_KEY, VALID_REPORT_PATH);
    assertFound(reports);
  }

  private void assertFound(List<File> reports){
    assert(reports != null);
    assert(reports.size() == 1);
    assert(reports.get(0).exists());
    assert(reports.get(0).isAbsolute());
  }
  
  private void assertNotFound(List<File> reports){
    assert(reports != null);
  }

  private static Project mockProjectWithLanguageKey(String languageKey){
    Project project = TestUtils.mockProject();
    when(project.getLanguageKey()).thenReturn(languageKey);
    return project;
  }
}
