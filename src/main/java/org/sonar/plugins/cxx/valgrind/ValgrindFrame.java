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
package org.sonar.plugins.cxx.valgrind;

import java.io.File;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a stack frame. Overwrites equality. Has a string serialization that
 * resembles the valgrind output in textual mode.
 */
class ValgrindFrame{
  private String ip = "???";
  private String obj = "";
  private String fn = "???";
  private String dir = "";
  private String file = "";
  private int line = -1;

  /**
   * Constucts a stack frame with given attributes. Its perfectly valid if some of them
   * are empty or dont carry meaningfull information.
   */
  public ValgrindFrame(String ip, String obj, String fn, String dir, String file, int line){
    if (ip != null)   this.ip = ip;
    if (obj != null)  this.obj = obj;
    if (fn != null)   this.fn = fn;
    if (dir != null)  this.dir = dir;
    if (file != null) this.file = file;
    this.line = line;
  }
    
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder().append(ip).append(": ").append(fn);
    if(isLocationKnown()){
      builder.append(" (")
        .append("".equals(file) ? ("in " + obj) : (file + getLineStr()))
        .append(")");
    }
      
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValgrindFrame other = (ValgrindFrame) o;
    return hashCode() == other.hashCode();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(obj)
      .append(fn)
      .append(dir)
      .append(file)
      .append(line)
      .toHashCode();
  }
    
  String getPath() { return new File(dir, file).getPath(); }

  String getDir() { return dir; }

  int getLine() { return line; }
  
  private boolean isLocationKnown() { return !("".equals(file) && "".equals(obj)); }
  
  private String getLineStr() { return line == -1 ? "" : ":"+Integer.toString(line); }
}
