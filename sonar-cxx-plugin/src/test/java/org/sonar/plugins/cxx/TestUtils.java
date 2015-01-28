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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;

public class TestUtils {

  public static Issuable mockIssuable() {
    Issue issue = mock(Issue.class);
    Issuable.IssueBuilder issueBuilder = mock(Issuable.IssueBuilder.class);
    when(issueBuilder.build()).thenReturn(issue);
    when(issueBuilder.ruleKey((RuleKey)anyObject())).thenReturn(issueBuilder);
    when(issueBuilder.line((Integer)anyObject())).thenReturn(issueBuilder);
    when(issueBuilder.message((String)anyObject())).thenReturn(issueBuilder);
    Issuable issuable = mock(Issuable.class);
    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);
    return issuable;
  }

  public static ResourcePerspectives mockPerspectives(Issuable issuable) {
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);
    when(perspectives.as((Class) anyObject(), (Resource) anyObject())).thenReturn(issuable);
    return perspectives;
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
   * Creates a default project mock
   */
  public static Project mockProject() {
    return mockProject(loadResource("/org/sonar/plugins/cxx/reports-project"));
  }

  /**
   * Creates a project mock given its root directory
   * @param baseDir project root directory
   * @return mocked project
   */
  public static Project mockProject(File baseDir) {
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBasedir()).thenReturn(baseDir);
    Project project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(fileSystem);
    return project;
  }

  public static ProjectReactor mockReactor(File baseDir, List<File> sourceDirs,
      List<File> testDirs) {
    ProjectReactor reactor = mock(ProjectReactor.class);
    ProjectDefinition projectDef = mock(ProjectDefinition.class);
    when(reactor.getRoot()).thenReturn(projectDef);
    when(projectDef.getBaseDir()).thenReturn(baseDir);

    return reactor;
  }
  
  public static ProjectReactor mockReactor() {
    File baseDir = loadResource("/org/sonar/plugins/cxx/reports-project");
    List<File> empty = new ArrayList<File>();
    return mockReactor(baseDir, empty, empty);
  }  
  
  /**
   * Mocks the filesystem given the root directory of the project
   * @param baseDir project root directory
   * @return mocked filesystem
   */
  public static DefaultFileSystem mockFileSystem(File baseDir) {
    return mockFileSystem(baseDir, Arrays.asList(new File(".")), null);
  }

  /**
   * Mocks the filesystem given the root directory and lists of source
   * and tests directories. The latter are given just as in sonar-project.properties
   * @param baseDir    project root directory
   * @param sourceDirs List of source directories, relative to baseDir.
   * @param testDirs   List of test directories, relative to baseDir.
   * @return mocked filesystem
   */
  public static DefaultFileSystem mockFileSystem(File baseDir,
                                                 List<File> sourceDirs,
                                                 List<File> testDirs) {
    DefaultFileSystem fs = new DefaultFileSystem();
    fs.setEncoding(Charset.forName("UTF-8"));
    fs.setBaseDir(baseDir);
    scanDirs(fs, baseDir, sourceDirs, Type.MAIN);
    scanDirs(fs, baseDir, testDirs, Type.TEST);
    return fs;
  }

  /**
   * Returns the default filesystem mock
   */
  public static DefaultFileSystem mockFileSystem() {
    return mockFileSystem(loadResource("/org/sonar/plugins/cxx/reports-project"));
  }

  public static CxxLanguage mockCxxLanguage() {
    return new CxxLanguage(new Settings());
  }

  private static void scanDirs(DefaultFileSystem fs, File baseDir, List<File> dirs, Type ftype) {
    if (dirs == null){
      return;
    }

//    List<InputFile> result = new ArrayList<InputFile>();
    String[] suffixes = mockCxxLanguage().getFileSuffixes();
    String[] includes = new String[suffixes.length];
    for (int i = 0; i < includes.length; ++i) {
      includes[i] = "**/*" + suffixes[i];
    }

    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setIncludes(includes);
    String relpath;
    for (File dir : dirs) {
      scanner.setBasedir(new File(baseDir, dir.getPath()));
      scanner.scan();
      for (String path : scanner.getIncludedFiles()) {
        relpath = new File(dir, path).getPath();
        fs.add(new DefaultInputFile(relpath)
               .setAbsolutePath(new File(baseDir, relpath).getAbsolutePath())
               .setLanguage(CxxLanguage.KEY)
               .setType(ftype));
      }
    }
  }
}

