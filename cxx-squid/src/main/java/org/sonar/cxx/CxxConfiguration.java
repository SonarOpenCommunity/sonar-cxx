/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.sonar.squidbridge.api.SquidConfiguration;

public class CxxConfiguration extends SquidConfiguration {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("CxxConfiguration");
  
  private boolean ignoreHeaderComments = false;
  private final Set<String> uniqueIncludes = new HashSet<String>();
  private final Set<String> uniqueDefines = new HashSet<String>();
  private List<String> forceIncludeFiles = new ArrayList<String>();
  private List<String> headerFileSuffixes = new ArrayList<String>();
  private String baseDir;
  private boolean errorRecoveryEnabled = true;
  private List<String> cFilesPatterns = new ArrayList<String>();

  public CxxConfiguration() {
  }

  public CxxConfiguration(Charset charset) {
    super(charset);
  }

  public void setIgnoreHeaderComments(boolean ignoreHeaderComments) {
    this.ignoreHeaderComments = ignoreHeaderComments;
  }

  public boolean getIgnoreHeaderComments() {
    return ignoreHeaderComments;
  }

  public void setDefines(List<String> defines) {
    for(String define : defines) {
      if (!uniqueDefines.contains(define)) {
        uniqueDefines.add(define);
      }
    }
  }

  public void setDefines(String[] defines) {
    if (defines != null) {
      setDefines(Arrays.asList(defines));
    }
  }

  public List<String> getDefines() {
    return new ArrayList<String>(uniqueDefines);
  }

  public void setIncludeDirectories(List<String> includeDirectories) {
    for(String include : includeDirectories) {
      if (!uniqueIncludes.contains(include)) {
        uniqueIncludes.add(include);
      }
    }
  }

  public void setIncludeDirectories(String[] includeDirectories) {
    if (includeDirectories != null) {
      setIncludeDirectories(Arrays.asList(includeDirectories));
    }
  }

  public List<String> getIncludeDirectories() {
    return new ArrayList<String>(uniqueIncludes);
  }

  public void setForceIncludeFiles(List<String> forceIncludeFiles) {
    this.forceIncludeFiles = forceIncludeFiles;
  }

  public void setForceIncludeFiles(String[] forceIncludeFiles) {
    if (forceIncludeFiles != null) {
      setForceIncludeFiles(Arrays.asList(forceIncludeFiles));
    }
  }

  public List<String> getForceIncludeFiles() {
    return forceIncludeFiles;
  }

  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
  }

  public String getBaseDir() {
    return baseDir;
  }

  public void setErrorRecoveryEnabled(boolean errorRecoveryEnabled){
    this.errorRecoveryEnabled = errorRecoveryEnabled;
  }

  public boolean getErrorRecoveryEnabled(){
    return this.errorRecoveryEnabled;
  }

  public List<String> getCFilesPatterns() {
    return cFilesPatterns;
  }

  public void setCFilesPatterns(String[] cFilesPatterns) {
    if (this.cFilesPatterns != null) {
      this.cFilesPatterns = Arrays.asList(cFilesPatterns);
    }
  }

  public void setHeaderFileSuffixes(List<String> headerFileSuffixes) {
      this.headerFileSuffixes = headerFileSuffixes;
  }

  public void setHeaderFileSuffixes(String[] headerFileSuffixes) {
    if (headerFileSuffixes != null) {
      setHeaderFileSuffixes(Arrays.asList(headerFileSuffixes));
    }
  }

  public List<String> getHeaderFileSuffixes() {
    return this.headerFileSuffixes;
  }


  public void setCompilationPropertiesWithBuildLog(List<File> reports, String fileFormat, String charsetName) {
    
    if(reports == null) {
      return;
    }

    for(File buildLog : reports) {
      if (buildLog.exists()) {
        LOG.debug("Parse build log  file '{}'", buildLog.getAbsolutePath());
        if (fileFormat.equals("Visual C++")) {
          parseVCppLog(buildLog, charsetName);
        }

        LOG.debug("Parse build log OK: includes: '{}' defines: '{}'", uniqueIncludes.size(), uniqueDefines.size());
      } else {
        LOG.error("Compilation log not found: '{}'", buildLog.getAbsolutePath());
      }    
    }
  }

  private void parseVCppLog(File buildLog, String charsetName) {

      try {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(buildLog), charsetName));
        String line;
        String currentProjectPath = "";
        while ((line = br.readLine()) != null) {
          if (line.startsWith("  INCLUDE=")) { // handle environment includes 
            String[] includes = line.split("=")[1].split(";");
            for(String include : includes) {
              if (!uniqueIncludes.contains(include)) {
                uniqueIncludes.add(include);
              }
            }
          }
          
          // get base path of project to make 
          // Target "ClCompile" in file "C:\Program Files (x86)\MSBuild\Microsoft.Cpp\v4.0\V120\Microsoft.CppCommon.targets" from project "D:\Development\SonarQube\cxx\sonar-cxx\integration-tests\testdata\googletest_bullseye_vs_project\PathHandling.Test\PathHandling.Test.vcxproj" (target "_ClCompile" depends on it):
          if (line.startsWith("Target \"ClCompile\" in file")) {
            currentProjectPath = line.split("\" from project \"")[1].split("\\s+")[0].replace("\"", "");              
          }
          if (line.contains("C:\\Program Files (x86)\\Microsoft Visual Studio 10.0\\VC\\bin\\CL.exe") || 
                  line.contains("C:\\Program Files\\Microsoft Visual Studio 10.0\\VC\\bin\\CL.exe")) {
            parseVCppCompilerCLLine(line, currentProjectPath);
          }
          if (line.contains("C:\\Program Files (x86)\\Microsoft Visual Studio 11.0\\VC\\bin\\CL.exe") || 
                  line.contains("C:\\Program Files\\Microsoft Visual Studio 11.0\\VC\\bin\\CL.exe")) {
            parseVCppCompilerCLLine(line, currentProjectPath);       
          }
          if (line.contains("C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\bin\\CL.exe") || 
                  line.contains("C:\\Program Files\\Microsoft Visual Studio 12.0\\VC\\bin\\CL.exe")) {
            parseVCppCompilerCLLine(line, currentProjectPath);        
          }
          if (line.contains("C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\bin\\CL.exe") || 
                  line.contains("C:\\Program Files\\Microsoft Visual Studio 14.0\\VC\\bin\\CL.exe")) {
            parseVCppCompilerCLLine(line, currentProjectPath);        
          }  
        }
        br.close();
      } catch (IOException ex) {
        LOG.error("Cannot parse build log", ex);
      }
  }

  private void parseVCppCompilerCLLine(String line, String projectPath) {
    File file = new File(projectPath);
    String project = file.getParent();

    for (String includeElem : getMatches(Pattern.compile("/I\"(.*?)\""), line)) {
      ParseInclude(includeElem, project);
    }
    
    for (String includeElem : getMatches(Pattern.compile("/I([^\\s\"]+) "), line)) {
      ParseInclude(includeElem, project);
    }
    
    for (String macroElem : getMatches(Pattern.compile("[/-]D\\s([^\\s]+)"), line)) {
      ParseMacro(macroElem);
    }
  }

  private List<String> getMatches(Pattern pattern, String text) {
    List<String> matches = new ArrayList<String>();
    Matcher m = pattern.matcher(text);
    while(m.find()) { matches.add(m.group(1)); }
    return matches;
    }
  
  
  private void ParseInclude(String element, String project) {    
    try {
      File includeRoot = new File(element.replace("\"", ""));
      String includePath = "";
      if (!includeRoot.isAbsolute()) {
          includeRoot = new File(project, includeRoot.getPath());
          includePath = includeRoot.getCanonicalPath();
      } else {
        includePath = includeRoot.getCanonicalPath();
      }
      if (!uniqueIncludes.contains(includePath)) {
        uniqueIncludes.add(includePath);
      }
    } catch (java.io.IOException io) {
      LOG.error("Cannot parse include path using element '{}' : '{}'", element, io.getMessage());
    }
  }

  private void ParseMacro(String macroElem) {
    macroElem.replace("=", " ");
    if (!uniqueDefines.contains(macroElem)) {
      uniqueDefines.add(macroElem);
  }
}
}