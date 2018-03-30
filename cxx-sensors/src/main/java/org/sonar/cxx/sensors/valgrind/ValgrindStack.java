/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.valgrind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a call stack, consists basically of a list of frames
 */
class ValgrindStack {

  private final List<ValgrindFrame> frames = new ArrayList<>();

  /**
   * Adds a stack frame to this call stack
   *
   * @param frame The frame to add
   */
  public void addFrame(ValgrindFrame frame) {
    frames.add(frame);
  }

  public List<ValgrindFrame> getFrames() {
    return Collections.unmodifiableList(frames);
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    for (ValgrindFrame frame : frames) {
      res.append(frame);
      res.append('\n');
    }
    return res.toString();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(frames)
        .toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ValgrindStack other = (ValgrindStack) obj;
    return new EqualsBuilder()
        .append(frames, other.frames)
        .isEquals();
  }

  /**
   * Returns the last frame (counted from the bottom of the stack) of a function
   * which is in 'our' code
   *
   * @param basedir
   * @return ValgrindFrame frame or null
   */
  @Nullable
  public ValgrindFrame getLastOwnFrame(String basedir) {
    String workdir = FilenameUtils.normalize(basedir);
    for (ValgrindFrame frame : frames) {
      if (isInside(frame.getDir(), workdir)) {
        return frame;
      }
    }
    return null;
  }

  private static boolean isInside(String path, String folder) {
    return (path == null || "".equals(path)) ? false : path.startsWith(folder);
  }

}
