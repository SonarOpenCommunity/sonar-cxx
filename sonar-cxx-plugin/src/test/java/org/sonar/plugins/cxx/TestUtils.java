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

import org.apache.commons.configuration.Configuration;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.bootstrap.ProjectReactor;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.CoreProperties;
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
   * @return  default mock project
   */
  public static Project mockProject() {
    return mockProject(loadResource("/org/sonar/plugins/cxx/reports-project"));
  }

  /**
   * Mock project
   * @param baseDir project base dir
   * @return  mocked project
   */
  public static Project mockProject(File baseDir) {
    List<Project> emptyProjectList = new ArrayList<Project>();
    
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBasedir()).thenReturn(baseDir);
    when(fileSystem.getSourceCharset()).thenReturn(Charset.defaultCharset());

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
    when(project.getModules()).thenReturn(emptyProjectList);
    return project;
  }

  
          
  public static ProjectReactor mockReactor(File baseDir,
                                                List<File> sourceDirs, List<File> testDirs) {
    ProjectReactor reactor = mock(ProjectReactor.class);
    ProjectDefinition projectDef = mock(ProjectDefinition.class);
    when(reactor.getRoot()).thenReturn(projectDef);
    when(projectDef.getBaseDir()).thenReturn(baseDir);

    return reactor;
  }

  public static DefaultFileSystem mockFileSystem(File baseDir) {
    DefaultFileSystem fs = new DefaultFileSystem();
    fs.setEncoding(Charset.forName("UTF-8"));
    fs.setBaseDir(baseDir);
    return fs;
  }

  public static DefaultFileSystem mockFileSystem() {
    File baseDir = loadResource("/org/sonar/plugins/cxx/reports-project");
    return mockFileSystem(baseDir);
  }
  
  public static ProjectReactor mockReactor() {
    File baseDir = loadResource("/org/sonar/plugins/cxx/reports-project");
    List<File> empty = new ArrayList<File>();
    return mockReactor(baseDir, empty, empty);
  }  

  public static CxxLanguage mockCxxLanguage() {
    return new CxxLanguage(new Settings());
  }

  public static DefaultInputFile CxxInputFile(File baseDir, String relpath,
      Type ftype) {
    return new DefaultInputFile(relpath)
        .setAbsolutePath(new File(baseDir, relpath).getAbsolutePath())
        .setLanguage(CxxLanguage.KEY).setType(ftype);
  }
}

