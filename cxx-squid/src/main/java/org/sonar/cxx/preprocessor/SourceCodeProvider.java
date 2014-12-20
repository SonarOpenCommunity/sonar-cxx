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
package org.sonar.cxx.preprocessor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * The source code provider is responsible for locating source files
 * and getting their content. A source file can be specified both as
 * an absolute and as a relative file system path. In the latter case
 * the scanner searches a list of directories (known to him) for a
 * file with such a name.
 */
public class SourceCodeProvider {
  private List<File> includeRoots = new LinkedList<File>();
  //public static final Logger LOG = LoggerFactory.getLogger("SourceCodeProvider");

  public void setIncludeRoots(List<String> includeRoots, String baseDir) {
    for (String tmp : includeRoots) {

      File includeRoot = new File(tmp);
      if (!includeRoot.isAbsolute()) {
        includeRoot = new File(baseDir, tmp);
      }

      try {
        includeRoot = includeRoot.getCanonicalFile();
      } catch (java.io.IOException io) {
        //LOG.error("cannot get canonical form of: '{}'", includeRoot);
      }

      if (includeRoot.isDirectory()) {
        //LOG.debug("storing include root: '{}'", includeRoot);
        this.includeRoots.add(includeRoot);
      }
      else {
        //LOG.warn("the include root {} doesnt exist", includeRoot.getAbsolutePath());
      }
    }
  }

  public File getSourceCodeFile(String filename, String cwd, boolean quoted) {
    File result = null;
    File file = new File(filename);
    if (file.isAbsolute()) {
      if (file.isFile()) {
        result = file;
      }
    }
    else {
      // This seems to be an established convention:
      // The special behavior in the quoted case is to look up relative to the
      // current directory.
      if (quoted) {
        File abspath = new File(new File(cwd), file.getPath());
        if (abspath.isFile()) {
          result = abspath;
        }
      }

      // The standard behavior: lookup relative to to the include roots.
      // The quoted case falls back to this, if its special handling wasnt
      // successul (as forced by the Standard).
      if (result == null) {
        for (File folder : includeRoots) {
          File abspath = new File(folder.getPath(), filename);
          if (abspath.isFile()) {
            result = abspath;
            break;
          }
        }
      }
    }

    if (result != null) {
      try {
        result = result.getCanonicalFile();
      } catch (java.io.IOException io) {
        //LOG.error("cannot get canonical form of: '{}'", result);
      }
    }

    return result;
  }

  public String getSourceCode(File file) {
    String code = null;
    if (file.isFile()) {
      try {
        code = FileUtils.readFileToString(file);
      } catch (java.io.IOException e) {
        //LOG.error("Cannot read contents of the file '{}'", file);
      }
    }

    return code;
  }
}
