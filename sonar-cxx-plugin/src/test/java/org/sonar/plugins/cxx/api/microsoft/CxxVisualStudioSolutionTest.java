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
package org.sonar.plugins.cxx.api.microsoft;

import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.test.TestUtils;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CxxVisualStudioSolutionTest {

  private static final String SOLUTION_PATH = "/org/sonar/plugins/cxx/solution/Example/Example.sln";

  @Test
  public void testGetProjectFile() throws Exception {
    VisualStudioSolution solution = ModelFactory.getSolution(TestUtils.getResource(SOLUTION_PATH));
    File sourceFile = TestUtils.getResource("/org/sonar/plugins/cxx/solution/Example/Example.Core/Money.cpp");
    VisualStudioProject project = solution.getProject(sourceFile);
    assertEquals("Example.Core", project.getName());
  }

//  @Test
//  public void testGetProjectWithFileOutside() throws Exception {
//    VisualStudioSolution solution = ModelFactory.getSolution(TestUtils.getResource(SOLUTION_PATH));
//    File sourceFile = TestUtils.getResource("/solution/LinkTestSolution/src/AssemblyInfo.cs");
//    VisualStudioProject project = solution.getProject(sourceFile);
//    assertNull(project);
//  }

  @Test
  public void testGetProjectWithFakeFile() throws Exception {
    VisualStudioSolution solution = ModelFactory.getSolution(TestUtils.getResource(SOLUTION_PATH));
    File sourceFile = TestUtils.getResource("/org/sonar/plugins/cxx/solution/Example/Example.Core/FooBar.cpp");
    VisualStudioProject project = solution.getProject(sourceFile);
    assertNull(project);
  }

//  @Test
//  public void testGetUnitTestProjects() throws Exception {
//    VisualStudioSolution solution = ModelFactory.getSolution(TestUtils.getResource(SOLUTION_PATH));
//    List<VisualStudioProject> testProjects = solution.getUnitTestProjects();
//    assertEquals(1, testProjects.size());
//  }

  @Test
  public void testGetProjectFromSonarProject() throws Exception {
    VisualStudioSolution solution = ModelFactory.getSolution(TestUtils.getResource(SOLUTION_PATH));
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Example.Application");
    VisualStudioProject vsProject = solution.getProjectFromSonarProject(project);
    assertEquals("Example.Application", vsProject.getName());
  }

  @Test
  public void testGetProjectFromSonarProjectWithBranch() throws Exception {
    VisualStudioSolution solution = ModelFactory.getSolution(TestUtils.getResource(SOLUTION_PATH));
    Project project = mock(Project.class);
    when(project.getName()).thenReturn("Example.Application MyBranch");
    when(project.getBranch()).thenReturn("MyBranch");
    VisualStudioProject vsProject = solution.getProjectFromSonarProject(project);
    assertEquals("Example.Application", vsProject.getName());
  }
}
