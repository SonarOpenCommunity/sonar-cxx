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
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cxx.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class CxxFileSensorImpl extends CxxFileSensor
{

  List<InputFile> files = new ArrayList<InputFile>();

  @Override
  protected void parseFile(InputFile file, Project project, SensorContext context) {
    files.add(file);
  }

  public List<InputFile> getParsedFilesList() {
    return files;
  }

}

public class CxxFileSensorTest {

  private static final int PARSED_FILE_COUNT = 3;

  private CxxFileSensorImpl sensor;
  private Project project;

  @Before
  public void setup() {
    sensor = new CxxFileSensorImpl();
    project = TestUtils.mockProject();
  }

  @Test
  public void shouldExecuteOnProjectTest() {
    assertTrue(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void analyseTest() {
    sensor.analyse(project, null);
    assertEquals(PARSED_FILE_COUNT, sensor.getParsedFilesList().size());
  }

}
