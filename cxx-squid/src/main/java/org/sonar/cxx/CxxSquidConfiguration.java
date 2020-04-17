/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.squidbridge.api.SquidConfiguration;

public class CxxSquidConfiguration extends SquidConfiguration {

  public static final String OVERALLINCLUDEKEY = "CxxOverallInclude";
  public static final String OVERALLDEFINEKEY = "CxxOverallDefine";
  private static final Logger LOG = Loggers.get(CxxSquidConfiguration.class);

  private boolean ignoreHeaderComments;
  private final Map<String, List<String>> uniqueIncludes = new HashMap<>();
  private final Map<String, Set<String>> uniqueDefines = new HashMap<>();
  private List<String> forceIncludeFiles = new ArrayList<>();
  private String baseDir = "";
  private boolean errorRecoveryEnabled = true;
  private String jsonCompilationDatabaseFile;
  private CxxCompilationUnitSettings globalCompilationUnitSettings;
  private final Map<String, CxxCompilationUnitSettings> compilationUnitSettings = new HashMap<>();
  private String[] publicApiFileSuffixes = new String[]{};
  private int functionComplexityThreshold = 10;
  private int functionSizeThreshold = 20;
  private boolean cpdIgnoreLiteral = false;
  private boolean cpdIgnoreIdentifier = false;

  private final CxxVCppBuildLogParser cxxVCppParser;

  public CxxSquidConfiguration() {
    this(Charset.defaultCharset());
  }

  public CxxSquidConfiguration(Charset encoding) {
    super(encoding);
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
    if (defines != null && defines.length > 0) {
      Set<String> overallDefs = uniqueDefines.get(OVERALLDEFINEKEY);
      overallDefs.addAll(Arrays.asList(defines));
    }
  }

  public List<String> getDefines() {
    var allDefines = new HashSet<String>();
    for (var elemSet : uniqueDefines.values()) {
      allDefines.addAll(elemSet);
    }
    return new ArrayList<>(allDefines);
  }

  public void addOverallDefine(String define) {
    Set<String> overallDefs = uniqueDefines.get(OVERALLDEFINEKEY);
    overallDefs.add(define);
  }

  public void setIncludeDirectories(List<String> includeDirectories) {
    List<String> overallIncludes = uniqueIncludes.get(OVERALLINCLUDEKEY);
    for (var include : includeDirectories) {
      if (!overallIncludes.contains(include)) {
        LOG.debug("setIncludeDirectories() adding dir '{}'", include);
        overallIncludes.add(include);
      }
    }
  }

  public List<Path> getIncludeDirectories() {
    var allIncludes = new HashSet<Path>();
    for (var elemList : uniqueIncludes.values()) {
      for (var elem : elemList) {
        allIncludes.add(Paths.get(elem));
      }
    }
    return new ArrayList<>(allIncludes);
  }

  public void addOverallIncludeDirectory(String includeDirectory) {
    List<String> overallIncludes = uniqueIncludes.get(OVERALLINCLUDEKEY);
    if (!overallIncludes.contains(includeDirectory)) {
      LOG.debug("setIncludeDirectories() adding dir '{}'", includeDirectory);
      overallIncludes.add(includeDirectory);
    }
  }

  public void setIncludeDirectories(@Nullable String[] includeDirectories) {
    if (includeDirectories != null && includeDirectories.length > 0) {
      setIncludeDirectories(Arrays.asList(includeDirectories));
    }
  }

  public void setForceIncludeFiles(List<String> forceIncludeFiles) {
    this.forceIncludeFiles = new ArrayList<>(forceIncludeFiles);
  }

  public List<String> getForceIncludeFiles() {
    return new ArrayList<>(forceIncludeFiles);
  }

  public void setForceIncludeFiles(@Nullable String[] forceIncludeFiles) {
    if (forceIncludeFiles != null && forceIncludeFiles.length > 0) {
      setForceIncludeFiles(Arrays.asList(forceIncludeFiles));
    }
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

  public Set<String> getCompilationUnitSourceFiles() {
    return Collections.unmodifiableSet(compilationUnitSettings.keySet());
  }

  public void setPublicApiFileSuffixes(String[] suffixes) {
    publicApiFileSuffixes = suffixes.clone();
  }

  public String[] getPublicApiFileSuffixes() {
    return publicApiFileSuffixes.clone();
  }

  public void setFunctionComplexityThreshold(int threshold) {
    functionComplexityThreshold = threshold;
  }

  public int getFunctionComplexityThreshold() {
    return functionComplexityThreshold;
  }

  public void setFunctionSizeThreshold(int threshold) {
    functionSizeThreshold = threshold;
  }

  public int getFunctionSizeThreshold() {
    return functionSizeThreshold;
  }

  public void setCpdIgnoreLiteral(boolean cpdIgnoreLiteral) {
    this.cpdIgnoreLiteral = cpdIgnoreLiteral;
  }

  public boolean getCpdIgnoreLiteral() {
    return this.cpdIgnoreLiteral;
  }

  public void setCpdIgnoreIdentifier(boolean cpdIgnoreIdentifier) {
    this.cpdIgnoreIdentifier = cpdIgnoreIdentifier;
  }

  public boolean getCpdIgnoreIdentifier() {
    return this.cpdIgnoreIdentifier;
  }

  public void setCompilationPropertiesWithBuildLog(@Nullable List<File> reports,
                                                   String fileFormat,
                                                   String charsetName) {

    if (reports == null || reports.isEmpty()) {
      return;
    }

    for (var buildLog : reports) {
      if (buildLog.exists()) {
        if ("Visual C++".equals(fileFormat)) {
          cxxVCppParser.parseVCppLog(buildLog, baseDir, charsetName);
          LOG.info("Parse build log '" + buildLog.getAbsolutePath()
                     + "' added includes: '" + getIncludeDirectories().size()
                     + "', added defines: '" + getDefines().size() + "'");
          if (LOG.isDebugEnabled()) {
            for (var allIncludes : uniqueIncludes.values()) {
              if (!allIncludes.isEmpty()) {
                LOG.debug("Includes folders ({})='{}'", allIncludes.size(), allIncludes);
              }
            }
            for (var allDefines : uniqueDefines.values()) {
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
