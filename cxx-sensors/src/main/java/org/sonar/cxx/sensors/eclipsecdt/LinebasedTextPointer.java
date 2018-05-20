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
package org.sonar.cxx.sensors.eclipsecdt;

public class LinebasedTextPointer implements Comparable<LinebasedTextPointer> {
  private final int line;
  private final int lineOffset;

  public LinebasedTextPointer(int line, int lineOffset) {
    this.line = line;
    this.lineOffset = lineOffset;
  }

  public int line() {
    return line;
  }

  public int lineOffset() {
    return lineOffset;
  }

  @Override
  public String toString() {
    return "LinebasedTextPointer [line=" + line + ", lineOffset=" + lineOffset + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + line;
    result = prime * result + lineOffset;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LinebasedTextPointer other = (LinebasedTextPointer) obj;
    if (line != other.line)
      return false;
    if (lineOffset != other.lineOffset)
      return false;
    return true;
  }

  @Override
  public int compareTo(LinebasedTextPointer o) {
    if (this.line == o.line()) {
      return Integer.compare(this.lineOffset, o.lineOffset());
    }
    return Integer.compare(this.line, o.line());
  }
}
