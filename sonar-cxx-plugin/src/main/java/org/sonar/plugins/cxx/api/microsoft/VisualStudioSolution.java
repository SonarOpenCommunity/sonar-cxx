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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.Project;

/**
 * A visual studio solution model.
 * 
 * @author Fabrice BELLINGARD
 * @author Jose CHILLAN Apr 16, 2009
 */
public class VisualStudioSolution {

  private static final Logger LOG = LoggerFactory.getLogger(VisualStudioSolution.class);

  private File solutionFile;
  private File solutionDir;
  private String name;
  private List<VisualStudioProject> projects;
  private List<BuildConfiguration> buildConfigurations;

  public VisualStudioSolution(File solutionFile, List<VisualStudioProject> projects) {
    this.solutionFile = solutionFile;
    this.solutionDir = solutionFile.getParentFile();
    this.projects = projects;
    initializeFileAssociations();
  }

  /**
   * Clean-up file/project associations in order to avoid having the same file in several projects.
   */
  private void initializeFileAssociations() {
    Set<File> csFiles = new HashSet<File>();
    for (VisualStudioProject project : projects) {
      Set<File> projectFiles = project.getSourceFileMap().keySet();
      Set<File> projectFilesToRemove = new HashSet<File>();
      for (File file : projectFiles) {
        if (getProjectByLocation(file) == null) {
          projectFilesToRemove.add(file);
        }
      }
      // remove files not present in the project directory
      projectFiles.removeAll(projectFilesToRemove);

      // remove files present in other projects
      projectFiles.removeAll(csFiles);

      csFiles.addAll(projectFiles);
    }
  }

  /**
   * Gets the project a c++ file belongs to.
   * 
   * @param file
   * @return the project contains the file, or <code>null</code> if none is matching
   */
  public VisualStudioProject getProject(File file) {
    for (VisualStudioProject project : projects) {
      if (project.contains(file)) {
        return project;
      }
    }
    return null;
  }

  /**
   * Gets the project from current sonarQube project.
   * 
   * @param current sonarQube project
   * @return the related project , or <code>null</code> if none is matching
   */
  public VisualStudioProject getProjectFromSonarProject(Project sonarProject) {
    String currentProjectName = sonarProject.getName();
    String branch = sonarProject.getBranch();
    for (VisualStudioProject project : projects) {
      final String vsProjectName;
      if (StringUtils.isEmpty(branch)) {
        vsProjectName = project.getName();
      } else {
        vsProjectName = project.getName() + " " + branch;
      }
      if (currentProjectName.equals(vsProjectName)) {
        return project;
      }
    }
    return null;
  }

  /**
   * Gets the project whose base directory contains the file/directory.
   * 
   * @param file
   *          the file to look for
   * @return the associated project, or <code>null</code> if none is matching
   */
  public final VisualStudioProject getProjectByLocation(File file) {
    String canonicalPath;
    try {
      canonicalPath = file.getCanonicalPath();
      for (VisualStudioProject project : projects) {
        File directory = project.getDirectory();
        String projectFolderPath = directory.getPath();
        if (canonicalPath.startsWith(projectFolderPath) && project.isParentDirectoryOf(file)) {
          return project;
        }
      }
    } catch (IOException e) {
      LOG.debug("getProjectByLocation i/o exception", e);
    }

    return null;
  }

  /**
   * Returns the solutionFile.
   * 
   * @return The solutionFile to return.
   */
  public File getSolutionFile() {
    return this.solutionFile;
  }

  /**
   * Returns the solutionDir.
   * 
   * @return The solutionDir to return.
   */
  public File getSolutionDir() {
    return this.solutionDir;
  }

  /**
   * Gets a project by its  name.
   * 
   * @param project name
   *         
   * @return the project, or <code>null</code> if not found
   */
  public VisualStudioProject getProject(String name) {
    VisualStudioProject result = null;
    for (VisualStudioProject project : projects) {
      LOG.debug("act project: "+ project.getName());
      if (name.equalsIgnoreCase(project.getName())) {
        result = project;
        break;
      }
    }
    return result;
  }

  /**
   * Gets a project by its UUID.
   * 
   * @param UUID
   *         
   * @return the project, or <code>null</code> if not found
   */
  public VisualStudioProject getProject(UUID projectGuid) {
    for (VisualStudioProject p : projects) {
      if (p.getProjectGuid().equals(projectGuid)) {
        return p;
      }
    }

    return null;
  }

  /**
   * Returns the projects.
   * 
   * @return The projects to return.
   */
  public List<VisualStudioProject> getProjects() {
    return this.projects;
  }

  /**
   * Returns the unit test projects.
   * 
   * @return The projects to return.
   */
  public List<VisualStudioProject> getUnitTestProjects() {
    List<VisualStudioProject> result = new ArrayList<VisualStudioProject>();
    for (VisualStudioProject visualStudioProject : projects) {
      if (visualStudioProject.isUnitTest()) {
        result.add(visualStudioProject);
      }
    }
    return result;
  }

  /**
   * Returns the integ test projects.
   * 
   * @return The projects to return.
   */
  public List<VisualStudioProject> getIntegTestProjects() {
    List<VisualStudioProject> result = new ArrayList<VisualStudioProject>();
    for (VisualStudioProject visualStudioProject : projects) {
      if (visualStudioProject.isIntegTest()) {
        result.add(visualStudioProject);
      }
    }
    return result;
  }

  /**
   * Returns the name.
   * 
   * @return The name to return.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          The name to set.
   */
  void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the build configurations.
   * 
   * @return the list of build configurations.
   */
  public List<BuildConfiguration> getBuildConfigurations() {
    return buildConfigurations;
  }

  /**
   * Set the build configurations.
   */
  void setBuildConfigurations(List<BuildConfiguration> buildConfigurations) {
    this.buildConfigurations = buildConfigurations;
  }

  @Override
  public String toString() {
    return "Solution(path=" + solutionFile + ")";
  }

}
