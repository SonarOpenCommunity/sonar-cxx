/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.valgrind;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a stack frame. Overwrites equality. Has a string serialization
 * that resembles the valgrind output in textual mode.
 */
class ValgrindFrame {

  private String ip = "???";
  private String obj = "";
  private String fn = "???";
  private String dir = "";
  private String file = "";
  private String line = "";

  /**
   * Constucts a stack frame with given attributes. Its perfectly valid if some
   * of them are empty or dont carry meaningfull information.
   */
  public ValgrindFrame(String ip, String obj, String fn, String dir, String file, String line) {
    if (ip != null) {
      this.ip = ip;
    }
    if (obj != null) {
      this.obj = obj;
    }
    if (fn != null) {
      this.fn = fn;
    }
    if (dir != null) {
      this.dir = FilenameUtils.normalize(dir);
    }
    if (file != null) {
      this.file = file;
    }
    if (line != null) {
      this.line = line;
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder().append(ip).append(": ").append(fn);
    if (isLocationKnown()) {
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

  String getPath() {
    return new File(dir, file).getPath();
  }

  String getDir() {
    return dir;
  }

  String getLine() {
    return line;
  }

  private boolean isLocationKnown() {
    return !("".equals(file) && "".equals(obj));
  }

  private String getLineStr() {
    return "".equals(line) ? "" : ":" + line;
  }
}
