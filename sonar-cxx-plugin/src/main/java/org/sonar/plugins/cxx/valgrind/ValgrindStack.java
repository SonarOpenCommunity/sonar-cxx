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
package org.sonar.plugins.cxx.valgrind;

import java.io.File;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sonar.plugins.cxx.utils.CxxUtils;

/** Represents a call stack, consists basically of a list of frames */
class ValgrindStack {
  private List<ValgrindFrame> frames = new ArrayList<ValgrindFrame>();

  /**
   * Adds a stack frame to this call stack
   * @param frame The frame to add
   */
  public void addFrame(ValgrindFrame frame) {
    frames.add(frame);
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    for (ValgrindFrame frame : frames) {
      res.append(frame);
      res.append("\n");
    }
    return res.toString();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    for (ValgrindFrame frame : frames) {
      builder.append(frame);
    }
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValgrindStack other = (ValgrindStack) o;
    return hashCode() == other.hashCode();
  }

  /**
   * Returns the last frame (counted from the bottom of the stack) of
   * a function which is in 'our' code
   */
  public List<ValgrindFrame> getLastOwnFrame(File baseDir, Map<String,List<String>> functionLookupTable) {
    
    List<ValgrindFrame> framesOut  = new ArrayList<ValgrindFrame>();
    for (ValgrindFrame frame : frames) {
      String[] functionSignatureElems = frame.getFunction().split("\\(")[0].split("::");
      String functionSignature = functionSignatureElems.length == 2 ?
          functionSignatureElems[1] : functionSignatureElems[0];
      
      List<String> paths = functionLookupTable.get(functionSignature);
      
      if(paths != null) {
                
        for (String path : paths) {
          File fileInSonar = new File(path);
          File filePathInSystem = new File(frame.getDir(), frame.getFile());
          
          if (filePathInSystem.getPath().contains(fileInSonar.getPath().replace(baseDir.getPath() + File.separator, ""))) {
            String dirInSonar = path.replace(frame.getFile(), "");
            dirInSonar = dirInSonar.substring(0, dirInSonar.length()-1);
            framesOut.add(new ValgrindFrame(frame.getIp(), 
                frame.getObj(),
                frame.getFunction(),
                dirInSonar,
                frame.getFile(),
                frame.getLine()));
          }
        }               
      }
    }
    return framesOut;
  }
}
