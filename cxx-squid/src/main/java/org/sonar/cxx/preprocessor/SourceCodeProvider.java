/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * The source code provider is responsible for locating source files
 * and getting their content.  A source file can be specified both as
 * an absolute and as a relative file sytem path.  In the latter case
 * the scanner searches a list of directories (known to him) for a
 * file with such a name.
 */
public class SourceCodeProvider {
  private List<File> absCodeLocations = new LinkedList<File>();
  private List<File> relCodeLocations = new LinkedList<File>();

  public void setCodeLocations(List<File> locations) {
    for (File location : locations) {
      if (location.isAbsolute()) {
        if (location.exists()) {
          // debug
          absCodeLocations.add(location);
        }
        else {
          // spruch
        }
      }
      else {
        // debug
        relCodeLocations.add(location);
      }
    }
  }

  public String getSourceCode(String filename, String cwd) {
    String code = null;
    File file = new File(filename);
    if (file.isAbsolute()) {
      code = getSourceCode(file);
    }
    else {
      // lookup in the current directory
      File abspath = new File(new File(cwd), file.getPath());
      code = getSourceCode(abspath);

      if (code == null) {

        // lookup in the relative code locations
        for (File folder : relCodeLocations) {
          abspath = FileUtils.getFile(cwd, folder.getPath(), filename);
          code = getSourceCode(abspath);
          if (code != null)
            break;
        }

        // lookup in the absolute code locations
        for (File folder : absCodeLocations) {
          abspath = FileUtils.getFile(folder, filename);
          code = getSourceCode(abspath);
          if (code != null)
            break;
        }
      }
    }

    return code;
  }

  private String getSourceCode(File file) {
    String code = null;
    if (file.exists()) {
      try {
        code = FileUtils.readFileToString(file);
      } catch (java.io.IOException e) {
        // spruch
      }
    }
    else {
      // spruch
    }

    return code;
  }

}
