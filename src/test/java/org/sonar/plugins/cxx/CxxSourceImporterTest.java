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

package org.sonar.plugins.cxx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import static junit.framework.Assert.assertTrue;

public class CxxSourceImporterTest {
  @Test
  public void testSourceImporter() {
    SensorContext context = mock(SensorContext.class);           
    Project project = mockProject();
    Settings config = new Settings(new PropertyDefinitions(CxxPlugin.class));
    config.setProperty(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY, true);    
    CxxSourceImporter importer = new CxxSourceImporter(TestUtils.mockCxxLanguage(), project, config);
    assertTrue(importer.shouldExecuteOnProject(project));    
    importer.analyse(project, context);

    verify(context).saveSource((Resource) anyObject(), eq("<c++ source>\n"));
  }

  private Project mockProject() {    
    File sourceDir;
    try{
      sourceDir = new File(getClass().getResource("/org/sonar/plugins/cxx").toURI());
    } catch (java.net.URISyntaxException e) {
      System.out.println("Error while mocking project: " + e);
      return null;
    }
    
    List<File> srcDirs = new ArrayList<File>(); 
    srcDirs.add(sourceDir);            
    Project project = TestUtils.mockProject(sourceDir, srcDirs, new ArrayList<File>());
          
    return project;
  }
}
