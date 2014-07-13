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

import org.apache.commons.configuration.Configuration;
import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.scan.filesystem.FileQuery;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
  public static RuleFinder mockRuleFinder() {
    Rule ruleMock = Rule.create("", "", "");
    RuleFinder ruleFinder = mock(RuleFinder.class);
    when(ruleFinder.findByKey((String) anyObject(),
        (String) anyObject())).thenReturn(ruleMock);
    when(ruleFinder.find((RuleQuery) anyObject())).thenReturn(ruleMock);
    return ruleFinder;
  }

  public static File loadResource(String resourceName) {
    URL resource = TestUtils.class.getResource(resourceName);
    File resourceAsFile = null;
    try {
      resourceAsFile = new File(resource.toURI());
    } catch (URISyntaxException e) {
      System.out.println("Cannot load resource: " + resourceName);
    }

    return resourceAsFile;
  }

  /**
   * @return  default mock project
   */
  public static Project mockProject() {
    List<File> empty = new ArrayList<File>();
    return mockProject(loadResource("/org/sonar/plugins/cxx/reports-project"), empty, empty);
  }

  /**
   * Mock project
   * @param baseDir project base dir
   * @param sourceFiles project source files
   * @return  mocked project
   */
  public static Project mockProject(File baseDir, List<File> sourceDirs, List<File> testDirs) {
    List<File> mainSourceFiles = scanForSourceFiles(sourceDirs);
    List<File> testSourceFiles = scanForSourceFiles(testDirs);

    List<InputFile> mainFiles = fromSourceFiles(mainSourceFiles);
    List<InputFile> testFiles = fromSourceFiles(testSourceFiles);

    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBasedir()).thenReturn(baseDir);
    when(fileSystem.getSourceCharset()).thenReturn(Charset.defaultCharset());
    when(fileSystem.getSourceFiles(mockCxxLanguage())).thenReturn(mainSourceFiles);
    when(fileSystem.getTestFiles(mockCxxLanguage())).thenReturn(testSourceFiles);
    when(fileSystem.mainFiles(CxxLanguage.KEY)).thenReturn(mainFiles);
    when(fileSystem.testFiles(CxxLanguage.KEY)).thenReturn(testFiles);
    when(fileSystem.getSourceDirs()).thenReturn(sourceDirs);
    when(fileSystem.getTestDirs()).thenReturn(testDirs);

    Project project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(fileSystem);
    CxxLanguage lang = mockCxxLanguage();
    when(project.getLanguage()).thenReturn(lang);
    when(project.getLanguageKey()).thenReturn(lang.getKey());
    // only for testing, Configuration is deprecated
    Configuration configuration = mock(Configuration.class);
    when(configuration.getBoolean(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY,
        CoreProperties.CORE_IMPORT_SOURCES_DEFAULT_VALUE)).thenReturn(true);
    when(project.getConfiguration()).thenReturn(configuration);

    return project;
  }

  public static ModuleFileSystem mockFileSystem(File baseDir,
                                                List<File> sourceDirs, List<File> testDirs) {
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.sourceCharset()).thenReturn(Charset.forName("UTF-8"));
    when(fs.baseDir()).thenReturn(baseDir);
    when(fs.sourceDirs()).thenReturn(sourceDirs);
    when(fs.testDirs()).thenReturn(testDirs);

    List<File> mainSourceFiles = scanForSourceFiles(sourceDirs);
    List<File> testSourceFiles = scanForSourceFiles(testDirs);

    when(fs.files(any(FileQuery.class))).thenReturn(mainSourceFiles);

    return fs;
  }

  public static ModuleFileSystem mockFileSystem() {
    File baseDir = loadResource("/org/sonar/plugins/cxx/reports-project");
    List<File> empty = new ArrayList<File>();
    return mockFileSystem(baseDir, empty, empty);
  }

  private static List<InputFile> fromSourceFiles(List<File> sourceFiles) {
    List<InputFile> result = new ArrayList<InputFile>();
    for (File file : sourceFiles) {
      InputFile inputFile = mock(InputFile.class);
      when(inputFile.getFile()).thenReturn(new File(file, ""));
      result.add(inputFile);
    }
    return result;
  }

  public static CxxLanguage mockCxxLanguage() {
    return new CxxLanguage(new Settings());
  }

  private static List<File> scanForSourceFiles(List<File> sourceDirs) {
    List<File> result = new ArrayList<File>();
    String[] suffixes = mockCxxLanguage().getFileSuffixes();
    String[] includes = new String[suffixes.length];
    for (int i = 0; i < includes.length; ++i) {
      includes[i] = "**/*" + suffixes[i];
    }

    DirectoryScanner scanner = new DirectoryScanner();
    for (File baseDir : sourceDirs) {
      scanner.setBasedir(baseDir);
      scanner.setIncludes(includes);
      scanner.scan();
      for (String relPath : scanner.getIncludedFiles()) {
        File f = new File(baseDir, relPath);
        result.add(f);
      }
    }

    return result;
  }
}
