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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The source code provider is responsible for locating source files
 * and getting their content. A source file can be specified both as
 * an absolute and as a relative file system path. In the latter case
 * the scanner searches a list of directories (known to him) for a
 * file with such a name.
 */
public class SourceCodeProvider {
  private List<File> includeRoots = new LinkedList<File>();
  public static final Logger LOG = LoggerFactory.getLogger("SourceCodeProvider");
  
  public void setIncludeRoots(List<String> includeRoots, String baseDir) {
    for (String tmp : includeRoots) {
      
      File includeRoot = new File(tmp);
      if (!includeRoot.isAbsolute()) {
        includeRoot = new File(baseDir, tmp);
      }
      
      if (includeRoot.exists()) {
        LOG.debug("storing include root: '{}'", includeRoot);
        this.includeRoots.add(includeRoot);
      }
      else {
        LOG.warn("the include root {} doesnt exist", includeRoot.getAbsolutePath());
      }
    }
  }

  public File getSourceCodeFile(String filename, String cwd) {
    File result = null;
    File file = new File(filename);
    if (file.isAbsolute()){
      if (file.exists()) {
        result = file;
      }
    }
    else {
      // lookup in the current directory
      File abspath = new File(new File(cwd), file.getPath());
      if(abspath.exists()){
        result = abspath;
      }
      else {
        // lookup relative to the stored include roots
        for (File folder : includeRoots) {
          abspath = new File(folder.getPath(), filename);
          if (abspath.exists()){
            result = abspath;
            break;
          }
        }
      }
    }
    
    if(result != null){
      try{
        result = result.getCanonicalFile();
      } catch(java.io.IOException io) {
        LOG.error("cannot get canonical form of: '{}'", result);
      }
    }
    
    return result;
  }
    
  public String getSourceCode(File file) {
    String code = null;
    if (file.exists()) {
      try {
        code = FileUtils.readFileToString(file);
      } catch (java.io.IOException e) {
        LOG.error("Cannot read contents of the file '{}'", file);
      }
    }
    else {
      //LOG.debug("Unsuccessful probing the path '{}'", file);
    }

    return code;
  }
}
