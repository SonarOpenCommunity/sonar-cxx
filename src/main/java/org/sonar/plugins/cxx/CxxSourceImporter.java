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
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SupportedEnvironment;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

@SupportedEnvironment({ "maven" })
public final class CxxSourceImporter extends AbstractSourceImporter {

  private static final String GROUP_ID = "org.codehaus.mojo";
  private static final String ARTIFACT_ID = "cxx-maven-plugin";

  private Project project = null;
  private MavenProject mavenProject = null;

  public CxxSourceImporter() {
    super(CxxLanguage.INSTANCE);
    logger.info("CxxSourceImporter()");
  }

  public CxxSourceImporter(Project p) {
    super(CxxLanguage.INSTANCE);
    logger.info("Maven project seems not to be available");
    project = p;
    mavenProject = project.getPom();
    if (null != mavenProject) {
      logger.info("Maven project as been found using deprecated sonar API");
    }
    addCxxSourceDir();
  }

  public CxxSourceImporter(Project p, MavenProject mp) {
    super(CxxLanguage.INSTANCE);
    logger.info("Maven project is available");
    project = p;
    mavenProject = mp;
    addCxxSourceDir();
  }

  private static Logger logger = LoggerFactory.getLogger(CxxSourceImporter.class);

  protected void addCxxSourceDir() {
    // add my import sources
    MavenPlugin plugin = MavenPlugin.getPlugin(mavenProject, getMAVEN_PLUGIN_GROUP_ID(), getMAVEN_PLUGIN_ARTIFACT_ID());
    String sourceDirs[] = null;
    if (plugin != null) {
      logger.info("Found {} {} configuration", getMAVEN_PLUGIN_GROUP_ID(), getMAVEN_PLUGIN_ARTIFACT_ID());
      sourceDirs = plugin.getParameters("/sourceDirs/param");
      for (String aPath : sourceDirs) {
        // mavenProject.addCompileSourceRoot(aPath);
        project.getFileSystem().addSourceDir(new File(aPath));
      }
      sourceDirs = plugin.getParameters("/sourceDirs/sourceDir");
      for (String aPath : sourceDirs) {
        // mavenProject.addCompileSourceRoot(aPath);
        project.getFileSystem().addSourceDir(new File(aPath));
      }
    } else {
      logger.info("{} {} configuration not found", getMAVEN_PLUGIN_GROUP_ID(), getMAVEN_PLUGIN_ARTIFACT_ID());
    }
  }

  protected String getMAVEN_PLUGIN_ARTIFACT_ID() {
    return ARTIFACT_ID;
  }

  protected String getMAVEN_PLUGIN_GROUP_ID() {
    return GROUP_ID;
  }

  protected Resource<CxxDir> createResource(File file, List<File> sourceDirs, boolean unitTest) {
    // project has been setted in analyse()
    return CxxFile.fromFileName(project, file.getAbsolutePath(), false);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
