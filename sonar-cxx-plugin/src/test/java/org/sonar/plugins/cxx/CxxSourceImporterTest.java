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
package org.sonar.plugins.cxx;

import org.junit.Test;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CxxSourceImporterTest {
  @Test
  public void testSourceImporter() {
    SensorContext context = mock(SensorContext.class);
    Project project = mockProject();
    Settings config = new Settings(new PropertyDefinitions(CxxPlugin.class));
    config.setProperty(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY, true);
    CxxSourceImporter importer = new CxxSourceImporter(TestUtils.mockCxxLanguage());
    importer.shouldExecuteOnProject(project); // thats necessary: it gets the importer
                                              // into desired shape. Bad.

    importer.analyse(project, context);

    verify(context).saveSource((Resource<?>) anyObject(), eq("<c++ source>"));
  }

  private Project mockProject() {
    File sourceDir;
    try {
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
