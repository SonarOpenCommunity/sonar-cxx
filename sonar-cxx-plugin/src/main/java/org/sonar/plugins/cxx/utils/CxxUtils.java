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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class holding various, well, utilities
 */
public final class CxxUtils {
  private static boolean isCaseSensitive = true;
  private static boolean caseSensitiveTestDone = false;

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
   * @return returns true if file system on this operating system is case sensitive
   */
  public static boolean isSystemCaseSensitive() {
    if (!caseSensitiveTestDone) {
      try {
        caseSensitiveTestDone = true;
        File fileLowerCase = new java.io.File(System.getProperty("java.io.tmpdir") +
                                              java.io.File.separatorChar +
                                              "cxx.test.abc.txt");
        fileLowerCase.createNewFile();
        File fileUpperCase = new java.io.File(System.getProperty("java.io.tmpdir") +
                                              java.io.File.separatorChar +
                                              "CXX.TEST.ABC.TXT");
        isCaseSensitive = !fileUpperCase.exists();
        fileLowerCase.delete();
      } catch (java.io.IOException e) {
        CxxUtils.LOG.error("isSystemCaseSensitive failed '{}'", e.toString());
      }
    }

    return isCaseSensitive;
  }
  
  /**
   * @return returns case sensitive filename
   */
  public static String getCaseSensitiveFileName(String file, List<java.io.File> sourceDirs) {
    File targetfile = new java.io.File(file);
    if (targetfile.exists()) {
      file = getRealFileName(targetfile);
    } else {
      Iterator<java.io.File> iterator = sourceDirs.iterator();
      while (iterator.hasNext()) {
        targetfile = new java.io.File(iterator.next().getPath() + java.io.File.separatorChar + file);
        if (targetfile.exists()) {
          file = getRealFileName(targetfile);
          break;
        }
      }
    }
    return file;
  }

  /**
   * Get the case sensitive file name.
   */
  private static String getRealFileName(File filename) {
    try {
      return filename.getCanonicalFile().getAbsolutePath();
    } catch (java.io.IOException e) {
      CxxUtils.LOG.error("getRealFileName failed '{}'", e.toString());
    }
    return filename.getName();
  }
}
