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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A dot net project extracted from a solution
 *
 * @author Fabrice BELLINGARD
 * @author Jose CHILLAN Apr 16, 2009
 */
public class VisualStudioProject {

  private static final Logger LOG = LoggerFactory.getLogger(VisualStudioProject.class);

  private String name;
  private File projectFile;
  private ArtifactType type;
//  private String projectName;
//  private String realProjectName;
  //private String assemblyVersion;
  //private String realAssemblyName;
  //private String rootNamespace;
  private UUID projectGuid;
  /** Output directory specified from maven */
  private String forcedOutputDir;
  private Map<BuildConfiguration, File> buildConfOutputDirMap;
  private File directory;
  //private boolean silverlightProject;
  private Map<File, SourceFile> sourceFileMap;

  private boolean unitTest;

  private boolean integTest;

  /**
   * Builds a {@link VisualStudioProject} ...
   *
   * @param name
   * @param projectFile
   */
  public VisualStudioProject() {
    super();
  }

  /**
   * Gets the relative path of a file contained in the project. <br>
   * For example, if the visual studio project is C:/MySolution/MyProject/MyProject.vcxproj and the file is
   * C:/MySolution/MyProject/Dummy/Foo.cpp, then the result is Dummy/Foo.cpp
   *
   * @param file
   *          the file whose relative path is to be computed
   * @return the relative path, or <code>null</code> if the file is not in the project sub-directories
   */
  public String getRelativePath(File file) {
    File canonicalDirectory;
    try {
      canonicalDirectory = directory.getCanonicalFile();

      File canonicalFile = file.getCanonicalFile();

      String filePath = canonicalFile.getPath();
      String directoryPath = canonicalDirectory.getPath();
      if (!filePath.startsWith(directoryPath)) {
        // The file is not in the directory
        return null;
      }
      return StringUtils.removeStart(StringUtils.removeStart(filePath, directoryPath), "\\");
    } catch (IOException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("io exception with file " + file, e);
      }
      return null;
    }
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
   * Returns the projectFile.
   *
   * @return The projectFile to return.
   */
  public File getProjectFile() {
    return this.projectFile;
  }

  /**
   * Returns the type.
   *
   * @return The type to return.
   */
  public ArtifactType getType() {
    return this.type;
  }

  public UUID getProjectGuid() {
    return projectGuid;
  }

  /**
   * Provides the location of the generated artifact(s) of this project according to the build configuration(s) used.
   *
   * @param buildConfiguration
   *          Visual Studio build configuration used to generate the project
   * @param buildPlatform
   *          Platform used to build the project. Typical values are "Any CPU", "x86" and "x64"
   * @return
   */
  public File getArtifactDirectory(String buildConfiguration, String buildPlatform) {
    File artifactDirectory = null;
    if (StringUtils.isNotEmpty(forcedOutputDir)) {
      // first trying to use forcedOutputDir as a relative path
      File interimDirectory = new File(directory, forcedOutputDir);

      if (!interimDirectory.exists()) {
        // path specified "forcedOutputDir" should be absolute,
        // not relative to the project root directory
        interimDirectory = new File(forcedOutputDir);
      }

      artifactDirectory = interimDirectory;

    } else {
      artifactDirectory = buildConfOutputDirMap.get(new BuildConfiguration(buildConfiguration, buildPlatform));
      if (artifactDirectory == null) {
        // just take the first one found...
        artifactDirectory = buildConfOutputDirMap.values().iterator().next();
        LOG.warn("Configuration(s) {} not found for platform {} in " + projectFile, buildConfiguration, buildPlatform);
        LOG.warn("Fallback to directory {} for project {}", artifactDirectory, name);
      } else {
        LOG.debug("Using directory " + artifactDirectory + " for project " + name + " with buildconfiguration " + buildConfiguration);
      }
    }
    return artifactDirectory;
  }

  /**
   * Gets the generated assembly according to the build configurations
   *
   * @param buildConfiguration
   *          Visual Studio build configuration used to generate the project
   * @return
   */
  public File getArtifact(String buildConfiguration, String buildPlatform) {
    File artifactDirectory = getArtifactDirectory(buildConfiguration, buildPlatform);
    return new File(artifactDirectory, getArtifactName());
  }

  /**
   * Gets the generated assemblies according to the build configurations. There is zero or one single assembly generated except for web
   * assemblies.
   *
   * @param buildConfiguration
   *          Visual Studio build configuration used to generate the project
   * @return a Set of the generated assembly files. If no files found, the set will be empty.
   */
//  public Set<File> getGeneratedAssemblies(String buildConfiguration, String platform) {
//    Set<File> result = Sets.newHashSet();
//    File assembly = getArtifact(buildConfiguration, platform);
//    if (assembly.exists()) {
//      result.add(assembly);
//    } else {
//      LOG.info("Skipping the non generated assembly of project : {}", name);
//    }
//    return result;
//  }

  /**
   * Gets the name of the artifact.
   *
   * @return
   */
  public String getArtifactName() {
    return name + "." + getExtension();
  }

  /**
   * Returns the directory.
   *
   * @return The directory to return.
   */
  public File getDirectory() {
    return this.directory;
  }

  /**
   * Sets the root directory of the project. For a regular project, this is where is located the vcxproj file.
   *
   * @param directory
   *          The directory to set.
   */
  void setDirectory(File directory) {
    try {
      this.directory = directory.getCanonicalFile();
    } catch (IOException e) {
      LOG.warn("Invalid project directory : " + directory);
    }
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
   * Sets the projectFile.
   *
   * @param projectFile
   *          The projectFile to set.
   */
  void setProjectFile(File projectFile) {
    this.projectFile = projectFile;
  }

  /**
   * Sets the projectGuid.
   *
   * @param projectGuid
   *          The projectGuid to set.
   */
  void setProjectGuid(UUID projectGuid) {
    this.projectGuid = projectGuid;
  }

  /**
   * Sets the type.
   *
   * @param type
   *          The type to set.
   */
  void setType(ArtifactType type) {
    this.type = type;
  }

  /**
   * @return true if the project contains tests (unit or integ).
   */
  public boolean isTest() {
    return unitTest || integTest;
  }

  public boolean isUnitTest() {
    return this.unitTest;
  }

  public boolean isIntegTest() {
    return this.integTest;
  }

  void setUnitTest(boolean test) {
    this.unitTest = test;
  }

  void setIntegTest(boolean test) {
    this.integTest = test;
  }

  /**
   * Gets the artifact extension (static library, dynamic load library, executable, ActiveX )
   *
   * @return the extension
   */
  public String getExtension() {
    String result = null;
    switch (type) {
      case EXE:
        result = "exe";
        break;
      case DLL:
        result = "dll";
        break;
      case LIB:
        result = "lib";
        break;
      case OCX:
        result = "ocx";
        break;
      default:
        result = null;
    }
    return result;
  }

  /**
   * Gets all the files contained in the project
   *
   * @return
   */
  public Collection<SourceFile> getSourceFiles() {
    if (sourceFileMap == null) {
      initializeSourceFileMap();
    }
    return sourceFileMap.values();
  }

  @SuppressWarnings("unchecked")
  private void initializeSourceFileMap() {
    Map<File, SourceFile> allFiles = new LinkedHashMap<File, SourceFile>();
    if (projectFile != null) {
      LOG.debug(projectFile.toString());
      List<String> filesPath = ModelFactory.getFilesPath(projectFile);
      LOG.debug("file paths: " + filesPath.size());
      for (String filePath : filesPath) {
        try {
          // We build the file and retrieves its canonical path
          File file = new File(directory, filePath).getCanonicalFile();
          String fileName = file.getName();
          String folder = StringUtils.replace(StringUtils.removeEnd(StringUtils.removeEnd(filePath, fileName), "\\"), "\\", "/");
          SourceFile sourceFile = new SourceFile(this, file, folder, fileName);
          allFiles.put(file, sourceFile);
        } catch (IOException e) {
          LOG.error("Bad file :" + filePath, e);
        }
      }

    } else {
      // For web projects, we take all the C# & VB files
      Collection<File> csharpFiles = FileUtils.listFiles(directory, new String[] {"cs", "vb"}, true);
      for (File file : csharpFiles) {
        SourceFile sourceFile = new SourceFile(this, file, file.getParent(), file.getName());
        allFiles.put(file, sourceFile);
      }
    }
    this.sourceFileMap = allFiles;
  }

  Map<File, SourceFile> getSourceFileMap() {
    if (sourceFileMap == null) {
      initializeSourceFileMap();
    }
    return sourceFileMap;
  }

  /**
   * Gets the source representation of a given file.
   *
   * @param file
   *          the file to retrieve
   * @return the associated source file, or <code>null</code> if the file is not included in the assembly.
   */
  public SourceFile getFile(File file) {
    File currentFile;
    try {
      currentFile = file.getCanonicalFile();
    } catch (IOException e) {
      // File not found
      if (LOG.isDebugEnabled()) {
        LOG.debug("file not found " + file, e);
      }
      return null;
    }
    // We ensure the source files are loaded
    getSourceFiles();
    return sourceFileMap.get(currentFile);
  }

  /**
   * Test if this project is a parent directory of the given file.
   *
   * @param file
   *          the file to check
   * @return <code>true</code> if the file is under this project
   */
  public boolean isParentDirectoryOf(File file) {
    return ModelFactory.isSubDirectory(directory, file);
  }

  /**
   * Checks if the project contains a given source file.
   *
   * @param file
   *          the file to check
   * @return <code>true</code> if the project contains the file
   */
  public boolean contains(File file) {
    if (file == null || !file.exists()) {
      return false;
    }
    try {
      File currentFile = file.getCanonicalFile();
      // We ensure the source files are loaded
      getSourceFiles();
      return sourceFileMap.containsKey(currentFile);
    } catch (IOException e) {
      LOG.debug("file error", e);
    }

    return false;
  }

  public boolean isWebProject() {
    return projectFile == null;
  }


  void setBuildConfOutputDirMap(Map<BuildConfiguration, File> buildConfOutputDirMap) {
    this.buildConfOutputDirMap = buildConfOutputDirMap;
  }

  void setForcedOutputDir(String forcedOutputDir) {
    this.forcedOutputDir = forcedOutputDir;
  }

  @Override
  public String toString() {
    return "Project(name=" + name + ", type=" + type + ", directory=" + directory + ", file=" + projectFile + ")";
  }

}
