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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Rule;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

public class TestUtils{
  public static RuleFinder mockRuleFinder(){
    Rule ruleMock = Rule.create("", "", "");
    RuleFinder ruleFinder = mock(RuleFinder.class);
    when(ruleFinder.findByKey((String) anyObject(),
                              (String) anyObject())).thenReturn(ruleMock);
    return ruleFinder;
  }

  public static Project mockProject() {
    File reportsBasedir;
    File sourcesRootdir;
    
    try{
      reportsBasedir = new File(TestUtils.class.getResource("/org/sonar/plugins/cxx/").toURI());
      sourcesRootdir = new File(new File(TestUtils.class.getResource("/").toURI()),
                                "../../src/sample/SampleProject");
    }
    catch(java.net.URISyntaxException e){
      System.out.println("Get exception mocking project: " + e);
      return null;
    }
    
    List<File> sourceFiles = new ArrayList<File>();
    sourceFiles.add(new File(sourcesRootdir, "sources/application/main.cpp"));
    sourceFiles.add(new File(sourcesRootdir, "sources/tests/SAMPLE-test.cpp"));
    sourceFiles.add(new File(sourcesRootdir, "sources/tests/SAMPLE-test.h"));
    sourceFiles.add(new File(sourcesRootdir, "sources/tests/main.cpp"));
    sourceFiles.add(new File(sourcesRootdir, "sources/utils/code_chunks.cpp"));
    sourceFiles.add(new File(sourcesRootdir, "sources/utils/utils.cpp"));
    
    List<File> sourceDirs = new ArrayList<File>();
    sourceDirs.add(new File(sourcesRootdir, "sources"));
    
    ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getBasedir()).thenReturn(reportsBasedir);
    when(fileSystem.getSourceCharset()).thenReturn(Charset.defaultCharset());
    when(fileSystem.getSourceFiles(mockCxxLanguage())).thenReturn(sourceFiles);
    when(fileSystem.getSourceDirs()).thenReturn(sourceDirs);
    
    Project project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(fileSystem);
    CxxLanguage lang = mockCxxLanguage();
    when(project.getLanguage()).thenReturn(lang);
    
    return project;
  }
  
  public static CxxLanguage mockCxxLanguage(){
    return new CxxLanguage(mock(Configuration.class)); 
  }
}
