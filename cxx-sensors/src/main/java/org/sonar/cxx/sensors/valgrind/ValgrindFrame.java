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

import java.io.File;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a stack frame. Overwrites equality. Has a string serialization
 * that resembles the valgrind output in textual mode.
 */
class ValgrindFrame {

  private String ip;
  private String obj;
  private String fn;
  private String dir;
  private String file;
  private String line;

  /**
   * Constructs a stack frame with given attributes. Its perfectly valid if some
   * of them are empty or don't carry meaningful information.
   */
  public ValgrindFrame(@Nullable String ip, @Nullable String obj, @Nullable String fn, @Nullable String dir,
    @Nullable String file, @Nullable String line) {
    this.ip = (ip != null) ? ip : "???";
    this.obj = (obj != null) ? obj : "";
    this.fn = (fn != null) ? fn : "???";
    this.dir = (dir != null) ? FilenameUtils.normalize(dir) : "";
    this.file = (file != null) ? file : "";
    this.line = (line != null) ? line : "";
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder().append(ip).append(": ").append(fn);
    if (isLocationKnown()) {
      builder.append(" (")
        .append("".equals(file) ? ("in " + obj) : (file + getLineStr()))
        .append(')');
    }

    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null)
      return false;
    if (getClass() != o.getClass())
      return false;
    ValgrindFrame other = (ValgrindFrame) o;
    return new EqualsBuilder()
        .append(ip, other.ip)
        .append(obj, other.obj)
        .append(fn, other.fn)
        .append(dir, other.dir)
        .append(file, other.file)
        .append(line, other.line)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(ip)
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

  public boolean isLocationKnown() {
    return !("".equals(file) && "".equals(obj));
  }

  private String getLineStr() {
    return "".equals(line) ? "" : ":" + line;
  }
}
