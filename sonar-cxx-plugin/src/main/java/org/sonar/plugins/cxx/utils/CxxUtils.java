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
package org.sonar.plugins.cxx.utils;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.resources.Project;


/**
 * Utility class holding various, well, utilities
 */
public final class CxxUtils {

  private CxxUtils() {
    // only static methods
  }

  /**
   * Default logger.
   */
  public static final Logger LOG = LoggerFactory.getLogger("CxxPlugin");

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
   * Normalize the given path to pass it to sonar. Return null if normalization has failed.
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
   * @return returns case sensitive full path
   */
  public static String getCaseSensitiveFileName(String filename, ModuleFileSystem fs) {
    File targetfile = new java.io.File(filename);
    String filePath = filename;
    if (targetfile.exists()) {
      filePath = normalizePath(filename);
    } else {
      // RATS, CppCheck and Vera++ provide names like './file.cpp' - add source folder for index check
      filePath = resolveFileName(filename, fs.sourceDirs());
    }
    if (filePath == null) {
      filePath = filename;
    }
    return filePath;
  }

  private static String resolveFileName(String file, List<java.io.File> searchDirs) {
    String targetfile = null;
    Iterator<java.io.File> iterator = searchDirs.iterator();
    while (iterator.hasNext()) {
      File sourcefile = new java.io.File(iterator.next().getPath() + java.io.File.separatorChar + file);
      if (sourcefile.exists()) {
        targetfile = normalizePath(sourcefile.getAbsolutePath());
        break;
      }
    }
   
    return targetfile;  
  }

  public static boolean isReactorProject(Project project) {
    return project.isRoot() && !project.getModules().isEmpty();
  }
}

