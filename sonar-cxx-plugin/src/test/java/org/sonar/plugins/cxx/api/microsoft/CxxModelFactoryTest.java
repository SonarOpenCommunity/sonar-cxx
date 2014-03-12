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

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Tests for visual studio utilities.
 * 
 * @author Fabrice BELLINGARD
 * @author Jose CHILLAN Sep 1, 2009
 */
public class CxxModelFactoryTest {

  private static final String PROJECT_CORE_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/Example/Example.Core/Example.Core.vcxproj";
  private static final String SAMPLE_FILE_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/Example/Example.Core/Money.cpp";
  private static final String SOLUTION_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/Example/Example.sln";
//  private static final String MESSY_SOLUTION_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/MessyTestSolution/MessyTestSolution.sln";
//  private static final String INVALID_SOLUTION_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/InvalidSolution/InvalidSolution.sln";
  private static final String SOLUTION_WITH_DUP_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/DuplicatesExample/Example.sln";
  private static final String SOLUTION_WITH_CUSTOM_BUILD_PATH = "target/test-classes/org/sonar/plugins/cxx/solution/CustomBuild/CustomBuild.sln";
  
  @Test
  public void testReadFiles() {
    File file = new File(PROJECT_CORE_PATH);
    List<String> files = ModelFactory.getFilesPath(file);
    assertEquals("Bad number of files extracted", 7, files.size());
  }

  @Test
  public void testSolutionWithCustomBuild() throws Exception {
    File file = new File(SOLUTION_WITH_CUSTOM_BUILD_PATH);
    VisualStudioSolution solution = ModelFactory.getSolution(file);
    List<BuildConfiguration> buildConfigurations = solution.getBuildConfigurations();
    assertEquals(9, buildConfigurations.size());
    assertTrue(buildConfigurations.contains(new BuildConfiguration("Debug")));
    assertTrue(buildConfigurations.contains(new BuildConfiguration("Release")));
    assertTrue(buildConfigurations.contains(new BuildConfiguration("CustomCompil")));

    assertEquals(1, solution.getProjects().size());
//    VisualStudioProject project = solution.getProjects().get(0);

//    assertTrue(project.getArtifact("CustomCompil", "Win32").getAbsolutePath().contains("CustomCompil"));
//    assertTrue(project.getArtifact("CustomCompil", "Win32").getAbsolutePath().endsWith(project.getName() + ".dll"));

  }

  @Test
  public void testReadSolution() throws Exception {
    File file = new File(SOLUTION_PATH);
    VisualStudioSolution solution = ModelFactory.getSolution(file);
    VisualStudioProject project = solution.getProject("Example.Core.Tests");
    Collection<SourceFile> files = project.getSourceFiles();
    for (SourceFile sourceFile : files) {
      assertThat(sourceFile.toString()).startsWith("Source(");
      assertThat(sourceFile.toString()).endsWith(")");
    }
    assertEquals("Bad number of files extracted", 4, files.size());
  }

  @Test
  public void testProjecFiles() throws Exception {
    File file = new File(PROJECT_CORE_PATH);
    VisualStudioProject project = ModelFactory.getProject(file);
    assertNotNull("Could not retrieve a project ", project);
    Collection<SourceFile> sourceFiles = project.getSourceFiles();
    assertEquals("Bad number of files extracted", 7, sourceFiles.size());
  }

  @Test
  public void testProjectFolder() throws Exception {
    File projectFile = new File(PROJECT_CORE_PATH);
    VisualStudioProject project = ModelFactory.getProject(projectFile);
    File sourceFile = new File(SAMPLE_FILE_PATH);
    String relativePath = project.getRelativePath(sourceFile);
    assertThat("Invalid relative path", relativePath, containsString("Money.cpp"));
  }

  @Test
  public void integTestPatterns() {
    VisualStudioProject testProject = new VisualStudioProject();
    testProject.setName("MyProjectTest");
    VisualStudioProject secondTestProject = new VisualStudioProject();
    secondTestProject.setName("MyProject.IT");
    VisualStudioProject project = new VisualStudioProject();
    project.setName("MyProject");

    String unitPatterns = "*Test";
    String integPatterns = "*.IT";

    ModelFactory.assessTestProject(project, unitPatterns, integPatterns);
    ModelFactory.assessTestProject(testProject, unitPatterns, integPatterns);
    ModelFactory.assessTestProject(secondTestProject, unitPatterns, integPatterns);
    assertFalse(project.isTest());
    assertTrue(testProject.isTest());
    assertTrue(secondTestProject.isTest());
    assertTrue(testProject.isUnitTest());
    assertFalse(secondTestProject.isUnitTest());
    assertFalse(testProject.isIntegTest());
    assertTrue(secondTestProject.isIntegTest());
  }

  @Test
  public void testSolutionWithAssemblyNameDuplicates() throws Exception {
    File file = new File(SOLUTION_WITH_DUP_PATH);
    VisualStudioSolution solution = ModelFactory.getSolution(file);
    List<VisualStudioProject> projects = solution.getProjects();
    assertEquals(2, projects.size());
    assertFalse(projects.get(0).getName().equals(projects.get(1).getName()));
  }

}
