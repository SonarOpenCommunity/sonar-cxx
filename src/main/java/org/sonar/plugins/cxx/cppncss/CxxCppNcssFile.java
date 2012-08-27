/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.cppncss;

import org.sonar.plugins.cxx.utils.CxxOsType;

public class CxxCppNcssFile {
  
  private int line = 0;
  private String wholeString = "";
  private String fileName = "";
  
  CxxCppNcssFile(String fullFileName, CxxOsType osType) {
    this.wholeString = fullFileName;    
    switch(osType)    
    {
      case WINDOWS:
        parseWindowsPath(fullFileName);
        break;
        
      case UNIX:
        parseUnixPath(fullFileName);
        break;
        
      default:
        throw new UnsupportedOperationException("Could not resolve CppNcss file name for your OS.");      
    }
  }
  
  public String getFileName() {    
    return fileName;
  }
  
  public String getWholeString() {
    return wholeString;
  }
  
  public int getLine() {
    return line;
  }
  
  private void parseWindowsPath(String fullPath) {
    String[] loc = fullPath.split(":");
    if(loc.length == 3) {
      this.fileName = loc[0] + ":" + loc[1];  
      this.line = Integer.valueOf(loc[2]);
    }
    else {
      this.fileName = loc[0];
      this.line = Integer.valueOf(loc[1]);
    }
  }
  
  private void parseUnixPath(String fullPath) {
    String[] loc = fullPath.split(":");
    this.fileName = loc[0];
    this.line = Integer.valueOf(loc[1]);
  }
  
}
