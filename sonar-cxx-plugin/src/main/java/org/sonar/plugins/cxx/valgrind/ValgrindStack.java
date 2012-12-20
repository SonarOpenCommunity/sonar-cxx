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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;


/** Represents a call stack, consists basically of a list of frames */
class ValgrindStack {
  private List<ValgrindFrame> frames = new ArrayList<ValgrindFrame>();
  
  /**
   * Adds a stack frame to this call stack
   * @param frame The frame to add
   */
  public void addFrame(ValgrindFrame frame) { frames.add(frame); }
    
  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    for (ValgrindFrame frame: frames) {
      res.append(frame);
      res.append("\n");
    }
    return res.toString();
  }

  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    for(ValgrindFrame frame: frames) {
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
  public ValgrindFrame getLastOwnFrame(String basedir) {
    for(ValgrindFrame frame: frames){
      if (isInside(frame.getDir(), basedir)){
        return frame;
      }
    }
    return null;
  }
  
  private boolean isInside(String path, String folder) {
    return "".equals(path) ? false : path.startsWith(folder);
  }
}
  
