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
 * derived from Sonar .NET Plugin :: Core
 * Copyright (C) 2010 Jose Chillan, Alexandre Victoor and SonarSource
 */
package org.sonar.plugins.cxx.compiler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.CxxPlugin;
import org.sonar.plugins.cxx.api.CxxException;
import org.sonar.plugins.cxx.api.microsoft.MicrosoftWindowsEnvironment;
import org.sonar.plugins.cxx.api.microsoft.ModelFactory;
import org.sonar.plugins.cxx.api.microsoft.SourceFile;
import org.sonar.plugins.cxx.api.microsoft.VisualStudioProject;
import org.sonar.plugins.cxx.api.microsoft.VisualStudioSolution;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * Project Builder created and executed once per build to override the project definition, based on the Visual Studio files found in the
 * sources.
 */
public class CxxVisualStudioProjectBuilder extends ProjectBuilder {
  
  private static final Logger LOG = LoggerFactory.getLogger(CxxVisualStudioProjectBuilder.class);

  protected Settings configuration;
  private MicrosoftWindowsEnvironment microsoftWindowsEnvironment;

  /**
   * Creates a new {@link VisualStudioProjectBuilder}
   * 
   * @param reactor
   *          the reactor
   * @param configuration
   *          the shared .NET configuration
   * @param microsoftWindowsEnvironment
   *          the shared Microsoft Windows Environment
   */
  public CxxVisualStudioProjectBuilder(ProjectReactor reactor, Settings configuration,
      MicrosoftWindowsEnvironment microsoftWindowsEnvironment) {
    super(reactor);
    this.configuration = configuration;
    LOG.info(configuration.toString());
    this.microsoftWindowsEnvironment = microsoftWindowsEnvironment;
  }

  @Override
  protected void build(ProjectReactor reactor) {
    if (configuration.getString("sonar.language").equals(CxxLanguage.KEY)) {
      LOG.debug("Executing VisualStudioProjectBuilder");
      ProjectDefinition root = reactor.getRoot();

      // First, read all the plugin configuration details related to MS Windows
      retrieveMicrosoftWindowsEnvironmentConfig();

      // Then create the Visual Studio Solution object from the ".sln" file
      createVisualStudioSolution(root.getBaseDir());

      // And finally create the Sonar projects definition
      createMultiProjectStructure(root);

      // lock the MicrosoftWindowsEnvironment object so that nobody can modify it afterwards
      microsoftWindowsEnvironment.lock();
    }
  }

  private void createMultiProjectStructure(ProjectDefinition root) {
    VisualStudioSolution currentSolution = microsoftWindowsEnvironment.getCurrentSolution();
    root.resetSourceDirs();
    LOG.debug("- Root Project: " + root.getName());
    LOG.debug("- workDir: " + root.getWorkDir());
    LOG.debug("- workDir (absolut path): " + root.getWorkDir().getAbsolutePath());
    LOG.debug("- BaseDir (absolut path): " + root.getBaseDir().getAbsolutePath());    
    String workDir = root.getWorkDir().getAbsolutePath().substring(root.getBaseDir().getAbsolutePath().length() + 1);
    microsoftWindowsEnvironment.setWorkingDirectory(workDir);

    boolean safeMode = "safe".equalsIgnoreCase(configuration.getString(CxxPlugin.VS_KEY_GENERATION_STRATEGY_KEY));
    LOG.debug("- use Safe mode for Multi-Project key generation: " + safeMode);
    for (VisualStudioProject vsProject : currentSolution.getProjects()) {
      final String projectKey;
      if (safeMode) {
        projectKey = root.getKey() + ":" + StringUtils.deleteWhitespace(vsProject.getName());
      } else {
        projectKey = StringUtils.substringBefore(root.getKey(), ":") + ":" + StringUtils.deleteWhitespace(vsProject.getName());
      }
//      LOG.debug(" - projectKey = " + projectKey);

      if (projectKey.equals(root.getKey())) {
        throw new SonarException("The solution and one of its projects have the same key ('" + projectKey
          + "'). Please set a unique 'sonar.projectKey' for the solution.");
      }

      Properties subprojectProperties = (Properties) root.getProperties().clone();
//      overrideSonarLanguageProperty(vsProject, subprojectProperties);

      ProjectDefinition subProject = ProjectDefinition.create().setProperties(subprojectProperties)
          .setBaseDir(vsProject.getDirectory()).setWorkDir(new File(vsProject.getDirectory(), workDir)).setKey(projectKey)
          .setVersion(root.getVersion()).setName(vsProject.getName());

      if (vsProject.isTest()) {
        subProject.setTestDirs(".");
        for (SourceFile sourceFile : vsProject.getSourceFiles()) {
          subProject.addTestFiles(sourceFile.getFile());
        }
      } else {
        subProject.setSourceDirs(".");
        for (SourceFile sourceFile : vsProject.getSourceFiles()) {
          subProject.addSourceFiles(sourceFile.getFile());
        }
      }

      LOG.debug("  - Adding Sub Project => " + subProject.getName());
      root.addSubProject(subProject);
    }
  }

//  protected void overrideSonarLanguageProperty(VisualStudioProject vsProject, Properties subprojectProperties) {
//    Collection<SourceFile> sourceFiles = vsProject.getSourceFiles();
//    if (!sourceFiles.isEmpty()) {
//      for (SourceFile sourceFile : sourceFiles) {
//        String key = DotNetLanguages.getLanguageKeyFromFileExtension(StringUtils.substringAfterLast(sourceFile.getName(), "."));
//        if (key != null) {
//          subprojectProperties.setProperty("sonar.language", key);
//          return;
//        }
//      }
//    }
//  }

  private void retrieveMicrosoftWindowsEnvironmentConfig() {
    // .NET version
//    String dotnetVersion = configuration.getString(DotNetConstants.DOTNET_VERSION_KEY);
//    microsoftWindowsEnvironment.setDotnetVersion(dotnetVersion);
    // .NET SDK folder
//    File dotnetSdkDirectory = new File(configuration.getString(DotNetConstants.getDotnetSdkDirKey(dotnetVersion)));
//    if (!dotnetSdkDirectory.isDirectory()) {
//      LOG.warn("/!\\ The following .NET SDK directory does not exist, please check your plugin configuration: "
//        + dotnetSdkDirectory.getPath());
//    }
//    microsoftWindowsEnvironment.setDotnetSdkDirectory(dotnetSdkDirectory);
//
//    // Silverlight version
//    String silverlightVersion = configuration.getString(DotNetConstants.SILVERLIGHT_VERSION_KEY);
//    microsoftWindowsEnvironment.setSilverlightVersion(silverlightVersion);
//    // Silverlight folder
//    File silverlightDirectory = new File(configuration.getString(DotNetConstants.getSilverlightDirKey(silverlightVersion)));
//    if (!silverlightDirectory.isDirectory()) {
//      LOG.warn("/!\\ The following silverlight SDK directory does not exist, please check your plugin configuration: "
//        + silverlightDirectory.getPath());
//    }
//    microsoftWindowsEnvironment.setSilverlightDirectory(silverlightDirectory);
  }

  private void createVisualStudioSolution(File baseDir) {
    File slnFile = findSlnFile(baseDir);
    if (slnFile == null) {
      throw new SonarException("No valid '.sln' file could be found. Please read the previous log messages to know more.");
    }
    LOG.info("The following 'sln' file has been found and will be used: " + slnFile.getAbsolutePath());

    try {
      ModelFactory.setTestProjectNamePattern(configuration.getString(CxxPlugin.VS_TEST_PROJECT_PATTERN_KEY));
      ModelFactory.setIntegTestProjectNamePattern(configuration.getString(CxxPlugin.VS_IT_PROJECT_PATTERN_KEY));
      VisualStudioSolution solution = ModelFactory.getSolution(slnFile);
      microsoftWindowsEnvironment.setCurrentSolution(solution);
    } catch (IOException e) {
      throw new SonarException("Error occured while reading Visual Studio files.", e);
    } catch (CxxException e) {
      throw new SonarException("Error occured while reading Visual Studio files.", e);
    }
  }

  private File findSlnFile(File baseDir) {
    String slnFilePath = configuration.getString(CxxPlugin.VS_SOLUTION_FILE_KEY);
    final File slnFile;
    if (StringUtils.isEmpty(slnFilePath)) {
      LOG.info("No '.sln' file found or specified: trying to find one...");
      slnFile = searchForSlnFile(baseDir);
    } else {
      final File confSlnFile = new File(baseDir, slnFilePath);
      if (confSlnFile.isFile()) {
        slnFile = confSlnFile;
      } else {
        slnFile = null;
        LOG.warn("The specified '.sln' path does not point to an existing file: " + confSlnFile.getAbsolutePath());
      }
    }
    return slnFile;
  }

  private File searchForSlnFile(File baseDir) {
    File slnFile = null;
    @SuppressWarnings("unchecked")
    Collection<File> foundSlnFiles = FileUtils.listFiles(baseDir, new String[] {"sln"}, false);
    if (foundSlnFiles.isEmpty()) {
      LOG.warn("No '.sln' file specified, and none found at the root of the project: " + baseDir.getAbsolutePath());
    } else if (foundSlnFiles.size() > 1) {
      LOG.warn("More than one '.sln' file found at the root of the project: please tell which one to use via the configuration ("
        + CxxPlugin.VS_SOLUTION_FILE_KEY + ").");
    } else {
      slnFile = foundSlnFiles.iterator().next();
    }
    return slnFile;
  }

}
