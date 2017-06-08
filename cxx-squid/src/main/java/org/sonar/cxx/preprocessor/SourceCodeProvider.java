/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * The source code provider is responsible for locating source files and getting
 * their content. A source file can be specified both as an absolute and as a
 * relative file system path. In the latter case the scanner searches a list of
 * directories (known to him) for a file with such a name.
 */
public class SourceCodeProvider {

  private final List<File> includeRoots = new LinkedList<>();
  private final List<String> extensionSuffixes = new LinkedList<>();
  public static final Logger LOG = Loggers.get(SourceCodeProvider.class);

  public void setHeadearSuffixes(List<String> extensionSuffixesConf) {
    extensionSuffixes.addAll(extensionSuffixesConf);
  }
  
  public void setIncludeRoots(List<String> includeRoots, String baseDir) {
    for (String tmp : includeRoots) {

      File includeRoot = new File(tmp);
      if (!includeRoot.isAbsolute()) {
        includeRoot = new File(baseDir, tmp);
      }

      try {
        includeRoot = includeRoot.getCanonicalFile();
      } catch (java.io.IOException io) {
        LOG.error("cannot get canonical form of: '{}'", includeRoot);
      }

      if (includeRoot.isDirectory()) {
        LOG.debug("storing include root: '{}'", includeRoot);
        this.includeRoots.add(includeRoot);
      } else {
        LOG.warn("the include root '{}' doesn't exist", includeRoot.getAbsolutePath());
      }
    }
  }

  public File getSourceCodeFile(String filename, String cwd, boolean quoted) {        
    File returnFile = getSourceFile(filename, cwd, quoted);
    
    // handles files without extension or if it has extension but not found
    if (returnFile != null || filename.contains(".")) {
      return returnFile;
    } 
    
    // tries to find files with defined extensions
    for (String extension : extensionSuffixes) {
      String filenameWithExtension = filename + extension;
      returnFile = getSourceFile(filenameWithExtension, cwd, quoted);
      if (returnFile != null) {
        return returnFile;
      }      
    }
    
    return null;
  }
  
  private File getSourceFile(String filename, String cwd, boolean quoted) {
    File result = null;
    File file = new File(filename);
    if (file.isAbsolute()) {
      if (file.isFile()) {
        result = file;
      }
    } else {
      // This seems to be an established convention:
      // The special behavior in the quoted case is to look up relative to the
      // current directory.
      if (quoted) {
        File abspath = new File(new File(cwd), file.getPath());
        if (abspath.isFile()) {
          result = abspath;
        }
        else {
          // fall back to use include paths instead of local folder
          result = null;
        }
      }

      // The standard behavior: lookup relative to to the include roots.
      // The quoted case falls back to this, if its special handling wasn't
      // successful (as forced by the Standard).
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
        LOG.error("cannot get canonical form of: '{}'", result);
      }
    }

    return result;
  }

  public String getSourceCode(File file, Charset charset)
    throws IOException 
  {
    byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    return new String(encoded, charset);
  }
}
