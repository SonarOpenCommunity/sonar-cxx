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
/*
 * derived from Sonar .NET Plugin
 * Authors :: Jose Chillan, Alexandre Victoor and SonarSource
 */
package org.sonar.plugins.cxx.api.microsoft;

import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.utils.SonarException;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.plugins.cxx.compiler.CxxVisualStudioProjectBuilder;

import java.io.File;
import java.util.Map;

/**
 * Class used to share information, between .NET plugins, about Windows and Visual Studio elements, such as:
 * <ul>
 * <li>the environment settings (.NET SDK directory for instance),</li>
 * <li>the current Visual Studio solution that is being analyzed.</li>
 * </ul>
 */
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class MicrosoftWindowsEnvironment implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(MicrosoftWindowsEnvironment.class);
  
  private CxxConfiguration configuration;
  private boolean locked;
  // static configuration elements that are fed at the beginning of an analysis and that do not change afterwards
//  private String dotnetVersion;
//  private File WindowsSdkDirectory;
//  private String silverlightVersion;
//  private File silverlightDirectory;
  private VisualStudioSolution currentSolution;
  private Map<String, VisualStudioProject> projectsByName;
  private String workDir;
  // dynamic elements that will change during analysis
  private boolean testExecutionDone = false;

  public MicrosoftWindowsEnvironment() {
    this(null);
  }

  public MicrosoftWindowsEnvironment(CxxConfiguration configuration) {
    this.configuration = configuration;
    projectsByName = Maps.newHashMap();
  }

  /**
   * After invoking this method, the {@link MicrosoftWindowsEnvironment} class won't be able to index files anymore: if
   * {@link #indexFile(SourceFile, File)} is called, a {@link IllegalStateException} will be thrown.
   */
  public void lock() {
    this.locked = true;
  }

  private void checkIfLocked() {
    if (locked) {
      throw new SonarException("Cannot override attributes that have already been assigned on MicrosoftWindowsEnvironment class.");
    }
  }

  /**
   * Returns the {@link VisualStudioProject} that is under analysis and which name is the given project name.
   * 
   * @return the current Visual Studio project
   */
  public VisualStudioProject getCurrentProject(String projectName) {
    LOG.debug("getCurrentProject : "+ projectName);
    LOG.debug("projectsByName size : {} " + projectsByName.size());
    LOG.debug("projectsByName " + projectsByName.get(projectName));
    return projectsByName.get(projectName);
  }

  /**
   * <b>Must not be used.</b>
   * 
   * @param currentSolution
   *          the currentSolution to set
   */
  public void setCurrentSolution(VisualStudioSolution currentSolution) {
    checkIfLocked();
    this.currentSolution = currentSolution;
    for (VisualStudioProject vsProject : currentSolution.getProjects()) {
      projectsByName.put(vsProject.getName(), vsProject);
    }
    if (configuration != null) {
//      String sonarBranch = configuration.getString("sonar.branch");
      String sonarBranch = "";
      if (!StringUtils.isEmpty(sonarBranch)) {
        // we also reference the projects with the name that Sonar gives when 'sonar.branch' is used
        for (VisualStudioProject vsProject : currentSolution.getProjects()) {
          projectsByName.put(vsProject.getName() + " " + sonarBranch, vsProject);
        }
      }
    }
  }

  /**
   * Returns the {@link VisualStudioSolution} that is under analysis
   * 
   * @return the current Visual Studio solution
   */
  public VisualStudioSolution getCurrentSolution() {
    return currentSolution;
  }

  /**
   * Tells whether tests have already been executed or not.
   * 
   * @return true if tests have already been executed.
   */
  public boolean isTestExecutionDone() {
    return testExecutionDone;
  }

  /**
   * Call this method once the tests have been executed and their reports generated.
   */
  public void setTestExecutionDone() {
    this.testExecutionDone = true;
  }

  /**
   * Returns the working directory that must be used during the Sonar analysis. For example, it is "target/sonar" if Maven runner is used,
   * or ".sonar" if the Java runner is used.
   * 
   * @return the working directory
   */
  public String getWorkingDirectory() {
    return workDir;
  }

  /**
   * Sets the working directory used during the Sonar analysis.
   * 
   * @param workDir
   *          the working directory
   */
  public void setWorkingDirectory(String workDir) {
    this.workDir = workDir;
  }

}
