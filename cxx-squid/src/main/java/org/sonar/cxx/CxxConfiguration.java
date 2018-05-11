/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.squidbridge.api.SquidConfiguration;

public class CxxConfiguration extends SquidConfiguration {

  private static final Logger LOG = Loggers.get(CxxConfiguration.class);
  public static final String OVERALLINCLUDEKEY = "CxxOverallInclude";
  public static final String OVERALLDEFINEKEY = "CxxOverallDefine";

  private boolean ignoreHeaderComments;
  private final Map<String, List<String>> uniqueIncludes = new HashMap<>();
  private final Map<String, Set<String>> uniqueDefines = new HashMap<>();
  private List<String> forceIncludeFiles = new ArrayList<>();
  private List<String> headerFileSuffixes = new ArrayList<>();
  private String baseDir;
  private boolean errorRecoveryEnabled = true;
  private List<String> cFilesPatterns = new ArrayList<>();
  private boolean missingIncludeWarningsEnabled = true;
  private String jsonCompilationDatabaseFile;
  private CxxCompilationUnitSettings globalCompilationUnitSettings;
  private final Map<String, CxxCompilationUnitSettings> compilationUnitSettings = new HashMap<>();

  private final CxxVCppBuildLogParser cxxVCppParser;

  public CxxConfiguration() {
    uniqueIncludes.put(OVERALLINCLUDEKEY, new ArrayList<>());
    uniqueDefines.put(OVERALLDEFINEKEY, new HashSet<>());
    cxxVCppParser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
  }

  public CxxConfiguration(Charset encoding) {
    super(encoding);
    uniqueIncludes.put(OVERALLINCLUDEKEY, new ArrayList<>());
    uniqueDefines.put(OVERALLDEFINEKEY, new HashSet<>());
    cxxVCppParser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
  }

  public CxxConfiguration(FileSystem fs) {
    super(fs.encoding());
    uniqueIncludes.put(OVERALLINCLUDEKEY, new ArrayList<>());
    uniqueDefines.put(OVERALLDEFINEKEY, new HashSet<>());
    cxxVCppParser = new CxxVCppBuildLogParser(uniqueIncludes, uniqueDefines);
  }

  public void setIgnoreHeaderComments(boolean ignoreHeaderComments) {
    this.ignoreHeaderComments = ignoreHeaderComments;
  }

  public boolean getIgnoreHeaderComments() {
    return ignoreHeaderComments;
  }

  public void setDefines(@Nullable String[] defines) {
    if (defines == null) {
      return;
    }

    Set<String> overallDefs = uniqueDefines.get(OVERALLDEFINEKEY);
    for (String define : defines) {
      if (!overallDefs.contains(define)) {
        overallDefs.add(define);
      }
    }
  }

  public void addOverallDefine(String define) {
    Set<String> overallDefs = uniqueDefines.get(OVERALLDEFINEKEY);
    if (!overallDefs.contains(define)) {
      overallDefs.add(define);
    }
  }

  public List<String> getDefines() {
    Set<String> allDefines = new HashSet<>();

    for (Set<String> elemSet : uniqueDefines.values()) {
      for (String value : elemSet) {
        if (!allDefines.contains(value)) {
          allDefines.add(value);
        }
      }
    }

    return new ArrayList<>(allDefines);
  }

  public void setIncludeDirectories(List<String> includeDirectories) {
    List<String> overallIncludes = uniqueIncludes.get(OVERALLINCLUDEKEY);
    for (String include : includeDirectories) {
      if (!overallIncludes.contains(include)) {
        LOG.debug("setIncludeDirectories() adding dir '{}'", include);
        overallIncludes.add(include);
      }
    }
  }

  public void addOverallIncludeDirectory(String includeDirectory) {
    List<String> overallIncludes = uniqueIncludes.get(OVERALLINCLUDEKEY);
    if (!overallIncludes.contains(includeDirectory)) {
      LOG.debug("setIncludeDirectories() adding dir '{}'", includeDirectory);
      overallIncludes.add(includeDirectory);
    }
  }

  public void setIncludeDirectories(@Nullable String[] includeDirectories) {
    if (includeDirectories != null) {
      setIncludeDirectories(Arrays.asList(includeDirectories));
    }
  }

  public List<String> getIncludeDirectories() {
    List<String> allIncludes = new ArrayList<>();

    for (List<String> elemList : uniqueIncludes.values()) {
      for (String value : elemList) {
        if (!allIncludes.contains(value)) {
          allIncludes.add(value);
        }
      }
    }

    return allIncludes;
  }

  public void setForceIncludeFiles(List<String> forceIncludeFiles) {
    this.forceIncludeFiles = new ArrayList<>(forceIncludeFiles);
  }

  public void setForceIncludeFiles(@Nullable String[] forceIncludeFiles) {
    if (forceIncludeFiles != null) {
      setForceIncludeFiles(Arrays.asList(forceIncludeFiles));
    }
  }

  public List<String> getForceIncludeFiles() {
    return new ArrayList<>(forceIncludeFiles);
  }

  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
  }

  public String getBaseDir() {
    return baseDir;
  }

  public void setErrorRecoveryEnabled(boolean errorRecoveryEnabled) {
    this.errorRecoveryEnabled = errorRecoveryEnabled;
  }

  public boolean getErrorRecoveryEnabled() {
    return this.errorRecoveryEnabled;
  }

  public List<String> getCFilesPatterns() {
    return new ArrayList<>(cFilesPatterns);
  }

  public void setCFilesPatterns(@Nullable String[] cFilesPatterns) {
    if (cFilesPatterns != null) {
      this.cFilesPatterns = Arrays.asList(cFilesPatterns);
    }
  }

  public void setHeaderFileSuffixes(List<String> headerFileSuffixes) {
    this.headerFileSuffixes = new ArrayList<>(headerFileSuffixes);
  }

  public void setHeaderFileSuffixes(@Nullable String[] headerFileSuffixes) {
    if (headerFileSuffixes != null) {
      setHeaderFileSuffixes(Arrays.asList(headerFileSuffixes));
    }
  }

  public List<String> getHeaderFileSuffixes() {
    return new ArrayList<>(this.headerFileSuffixes);
  }

  public void setMissingIncludeWarningsEnabled(boolean enabled) {
    this.missingIncludeWarningsEnabled = enabled;
  }

  public boolean getMissingIncludeWarningsEnabled() {
    return this.missingIncludeWarningsEnabled;
  }

  public String getJsonCompilationDatabaseFile() {
    return jsonCompilationDatabaseFile;
  }

  public void setJsonCompilationDatabaseFile(String jsonCompilationDatabaseFile) {
    this.jsonCompilationDatabaseFile = jsonCompilationDatabaseFile;
  }

  public CxxCompilationUnitSettings getGlobalCompilationUnitSettings() {
    return globalCompilationUnitSettings;
  }

  public void setGlobalCompilationUnitSettings(CxxCompilationUnitSettings globalCompilationUnitSettings) {
    this.globalCompilationUnitSettings = globalCompilationUnitSettings;
  }

  public CxxCompilationUnitSettings getCompilationUnitSettings(String filename) {
    return compilationUnitSettings.get(filename);
  }

  public void addCompilationUnitSettings(String filename, CxxCompilationUnitSettings settings) {
    compilationUnitSettings.put(filename, settings);
  }

  public List<File> getCompilationUnitSourceFiles() {
    List<File> files = new ArrayList<>();

    for (String item : compilationUnitSettings.keySet()) {
      files.add(new File(item));
    }

    return files;
  }

  public void setCompilationPropertiesWithBuildLog(@Nullable List<File> reports,
    String fileFormat,
    String charsetName) {

    if (reports == null || reports.isEmpty()) {
      return;
    }

    for (File buildLog : reports) {
      if (buildLog.exists()) {
        if ("Visual C++".equals(fileFormat)) {
          cxxVCppParser.parseVCppLog(buildLog, baseDir, charsetName);
          LOG.info("Parse build log '" + buildLog.getAbsolutePath()
            + "' added includes: '" + uniqueIncludes.size()
            + "', added defines: '" + uniqueDefines.size() + "'");
          if (LOG.isDebugEnabled()) {
            for (List<String> allIncludes : uniqueIncludes.values()) {
              if (!allIncludes.isEmpty()) {
                LOG.debug("Includes folders ({})='{}'", allIncludes.size(), allIncludes);
              }
            }
            for (Set<String> allDefines : uniqueDefines.values()) {
              if (!allDefines.isEmpty()) {
                LOG.debug("Defines ({})='{}'", allDefines.size(), allDefines);
              }
            }
          }
        }
      } else {
        LOG.error("Compilation log file not found: '{}'", buildLog.getAbsolutePath());
      }
    }
  }

  public Charset getEncoding() {
    return super.getCharset();
  }
}
