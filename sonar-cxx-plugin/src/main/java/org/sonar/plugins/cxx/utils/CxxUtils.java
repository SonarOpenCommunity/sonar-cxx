/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.DirectoryScanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.Project;

/**
 * Utility class holding various, well, utilities
 */
public final class CxxUtils {

  /**
   * Default logger.
   */
  public static final Logger LOG = LoggerFactory.getLogger("CxxPlugin");

  private CxxUtils() {
    // only static methods
  }

  /**
   * @param file
   * @return Returns file path of provided file, or "null" if file == null
   */
  public static String fileToAbsolutePath(File file) {
    if (file == null) {
      return "null";
    }
    return file.getAbsolutePath();
  }

  /**
   * Normalize the given path to pass it to sonar. Return null if normalization
   * has failed.
   */
  public static String normalizePath(String filename) {
    try {
      return new File(filename).getCanonicalPath();
    } catch (java.io.IOException e) {
      LOG.error("path normalizing of '{}' failed: '{}'", filename, e.toString());
      return null;
    }
  }

  /**
   * Creates a scanner for a string path and a baseDir
   * If base dir is outside report than sets the basedir to local path
   * @param rootDirPath
   * @param reportPath
   * @return 
   */  
  public static CxxSearchPathData GetDirectoryScannerForReport(String rootDirPath, String reportPath) {
    File singleFile = new File(reportPath);
    CxxSearchPathData scanner = new CxxSearchPathData();
    
    CxxUtils.LOG.debug("Unprocessed root directory '{}'", rootDirPath);
    CxxUtils.LOG.debug("Unprocessed report file '{}'", reportPath);
    
    if (singleFile.isAbsolute()) {
      String delimeter = Pattern.quote(System.getProperty("file.separator"));
      String reportNormalize = FilenameUtils.normalize(reportPath);
      String[] elementsOfPath = reportNormalize.split(delimeter);
      String pattern = elementsOfPath[elementsOfPath.length - 1];
      String root = reportNormalize.replace(pattern, "");
      if (root.endsWith(File.separator)) {
        scanner.setBaseDir(root.substring(0, root.length()-1));
      } else {
        scanner.setBaseDir(root);
      }
      
      scanner.setPattern(pattern);
    } else {
      if (reportPath.startsWith("**")) {
        scanner.setBaseDir(FilenameUtils.normalize(rootDirPath));
        scanner.setPattern(reportPath);
        scanner.setRecursive();
      } else {
        File file1 = new File(rootDirPath);
        File file2 = new File(file1, reportPath);    
        scanner.setBaseDir(FilenameUtils.normalize(file2.getParent()));
        scanner.setPattern(file2.getName());
      }
    }

    CxxUtils.LOG.debug("Processed root directory '{}'", scanner.getBaseDir());
    CxxUtils.LOG.debug("Processed report file '{}'", scanner.getPattern());
    
    return scanner;
  }
  
  public static void GetReportForBaseDirAndPattern(String baseDirPath, String reportPath, List<File> reports) {    
    try {
      CxxSearchPathData scanner = GetDirectoryScannerForReport(baseDirPath, reportPath);
      Collection<Path> reportPaths = CxxFileFinder.FindFiles(scanner.getBaseDir(), scanner.getPattern(), scanner.isRecursive());
      
      for (Path path : reportPaths) {
        CxxUtils.LOG.debug("add report '{}'", path.toAbsolutePath().toString());
        reports.add(new File(path.toAbsolutePath().toString()));
      }
    } catch (IOException ex) {
      CxxUtils.LOG.warn("Cannot find a report for '{}={}'", baseDirPath, reportPath);
      CxxUtils.LOG.warn("Exception '{}'", ex.getMessage());
    }
  }
  
  /**
   * @return returns case sensitive full path
   */
  public static String normalizePathFull(String filename, String baseDir) {
    File targetfile = new java.io.File(filename);
    String filePath;
    if (targetfile.isAbsolute()) {
      filePath = normalizePath(filename);
    } else {
      // RATS, CppCheck and Vera++ provide names like './file.cpp' - add source folder for index check
      filePath = normalizePath(baseDir + File.separator + filename);
    }
    return filePath;
  }

  public static boolean isReactorProject(Project project) {
    return project.isRoot() && !project.getModules().isEmpty();
  }
}

