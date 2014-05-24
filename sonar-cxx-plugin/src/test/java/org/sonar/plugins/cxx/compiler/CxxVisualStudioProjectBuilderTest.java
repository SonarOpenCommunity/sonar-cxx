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
package org.sonar.plugins.cxx.compiler;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.api.microsoft.VisualStudioProject;
import org.sonar.plugins.cxx.api.microsoft.VisualStudioSolution;
import org.sonar.test.TestUtils;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class CxxVisualStudioProjectBuilderTest {

//  private static File fakeSdkDir;
  private ProjectReactor reactor;
  private ProjectDefinition root;
  private File solutionBaseDir;
  private CxxVisualStudioProjectBuilder projectBuilder;
  private Settings settings;

  @Before
  public void initBuilder() {
    settings = Settings.createForComponent(new CxxPlugin());
    settings.setProperty("sonar.language", "c++");
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_ENABLED, "true");
    solutionBaseDir = TestUtils.getResource("/org/sonar/plugins/cxx/solution/Example");
    root = ProjectDefinition.create().setBaseDir(solutionBaseDir).setWorkDir(new File(solutionBaseDir, "WORK-DIR"));
    root.setVersion("1.0");
    root.setKey("groupId:artifactId");
    reactor = new ProjectReactor(root);
    projectBuilder = new CxxVisualStudioProjectBuilder(reactor, settings);
  }

  @Test 
  public void testCxxProject() {
    settings.setProperty("sonar.language", "foo");
    projectBuilder.build(reactor);
    assertThat(reactor.getRoot().getSubProjects()).isEmpty();
  }

  @Test(expected = SonarException.class)
  public void testNonExistingSlnFile() throws Exception {
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_FILE_KEY, "NonExistingFile.sln");
    projectBuilder.build(reactor);
  }

  @Test
  public void testCorrectlyConfiguredProject() throws Exception {
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_FILE_KEY, "Example.sln");
    projectBuilder.build(reactor);
    // check that the solution is built
    VisualStudioSolution solution = projectBuilder.getCurrentSolution();
    assertNotNull(solution);
    assertThat(solution.getProjects().size(), is(3));
    assertThat(projectBuilder.getCurrentProject("Example.Application").getSourceFiles().size(), is(4));
    assertThat(projectBuilder.getCurrentProject("Example.Core").getSourceFiles().size(), is(7));
//     check the multi-module definition is correct
    assertThat(reactor.getRoot().getSubProjects().size(), is(3));
    assertThat(reactor.getRoot().getSourceFiles().size(), is(0));
    ProjectDefinition subProject = reactor.getRoot().getSubProjects().get(0);
    VisualStudioProject vsProject = projectBuilder.getCurrentProject("Example.Application");
    assertThat(subProject.getName(), is("Example.Application"));
    assertThat(subProject.getKey(), is("groupId:Example.Application"));
    assertThat(subProject.getVersion(), is("1.0"));
    assertThat(subProject.getBaseDir(), is(vsProject.getDirectory()));
    assertThat(subProject.getWorkDir(), is(new File(vsProject.getDirectory(), "WORK-DIR")));
    assertThat(subProject.getSourceDirs().iterator().next(), notNullValue());
    assertTrue(subProject.getTestDirs().isEmpty());
    ProjectDefinition testSubProject = reactor.getRoot().getSubProjects().get(2);
    assertThat(testSubProject.getName(), is("Example.Core.Tests"));
//    assertThat(testSubProject.getTestDirs().iterator().next(), notNullValue());
//    assertTrue(testSubProject.getSourceDirs().isEmpty());
  }

  @Test
  public void testCorrectlyConfiguredProjectInSafeMode() throws Exception {
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_FILE_KEY, "Example.sln");
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_KEY_GENERATION_STRATEGY_KEY, "safe");
    projectBuilder.build(reactor);
    // check that the configuration is OK
//    assertThat(projectBuilder.getWorkingDirectory(), is("WORK-DIR"));
    // check that the solution is built
    VisualStudioSolution solution = projectBuilder.getCurrentSolution();
    assertNotNull(solution);
    assertThat(solution.getProjects().size(), is(3));
    assertThat(projectBuilder.getCurrentProject("Example.Application").getSourceFiles().size(), is(4));
    assertThat(projectBuilder.getCurrentProject("Example.Core").getSourceFiles().size(), is(7));
    // check the multi-module definition is correct
    assertThat(reactor.getRoot().getSubProjects().size(), is(3));
    assertThat(reactor.getRoot().getSourceFiles().size(), is(0));
    ProjectDefinition subProject = reactor.getRoot().getSubProjects().get(0);
    VisualStudioProject vsProject = projectBuilder.getCurrentProject("Example.Application");
    assertThat(subProject.getName(), is("Example.Application"));
    assertThat(subProject.getKey(), is("groupId:artifactId:Example.Application"));
    assertThat(subProject.getVersion(), is("1.0"));
    assertThat(subProject.getBaseDir(), is(vsProject.getDirectory()));
    assertThat(subProject.getWorkDir(), is(new File(vsProject.getDirectory(), "WORK-DIR")));
    assertThat(subProject.getSourceDirs().iterator().next(), notNullValue());
    assertTrue(subProject.getTestDirs().isEmpty());
    ProjectDefinition testSubProject = reactor.getRoot().getSubProjects().get(2);
    assertThat(testSubProject.getName(), is("Example.Core.Tests"));
//    assertThat(testSubProject.getTestDirs().iterator().next(), notNullValue());
//    assertTrue(testSubProject.getSourceDirs().isEmpty());
  }

  @Test
  public void testNoSpecifiedSlnFileButOneFound() throws Exception {
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_FILE_KEY, "");
    projectBuilder.build(reactor);
    VisualStudioSolution solution = projectBuilder.getCurrentSolution();
    assertNotNull(solution);
    assertThat(solution.getProjects().size(), is(3));
  }

  @Test(expected = SonarException.class)
  public void testNoSpecifiedSlnFileButNoneFound() throws Exception {
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_FILE_KEY, "");
    root.setBaseDir(TestUtils.getResource("/org/sonar/plugins/cxx/solution"));
    projectBuilder.build(reactor);
  }

  @Test(expected = SonarException.class)
  public void testNoSpecifiedSlnFileButTooManyFound() throws Exception {
    settings.setProperty(CxxVisualStudioProjectBuilder.VS_SOLUTION_FILE_KEY, "");
    root.setBaseDir(TestUtils.getResource("/org/sonar/plugins/cxx/solution/FakeSolutionWithTwoSlnFiles"));
    projectBuilder.build(reactor);
  }

}
