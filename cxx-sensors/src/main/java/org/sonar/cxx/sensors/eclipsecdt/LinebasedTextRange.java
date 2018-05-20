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

public class LinebasedTextRange implements Comparable<LinebasedTextRange> {
  private final LinebasedTextPointer start;
  private final LinebasedTextPointer end;

  public LinebasedTextRange(LinebasedTextPointer start, LinebasedTextPointer end) {
    this.start = start;
    this.end = end;
  }

  public LinebasedTextPointer start() {
    return start;
  }

  public LinebasedTextPointer end() {
    return end;
  }

  @Override
  public String toString() {
    return "LinebasedTextRange [start=" + start + ", end=" + end + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((end == null) ? 0 : end.hashCode());
    result = prime * result + ((start == null) ? 0 : start.hashCode());
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
    LinebasedTextRange other = (LinebasedTextRange) obj;
    if (end == null) {
      if (other.end != null)
        return false;
    } else if (!end.equals(other.end))
      return false;
    if (start == null) {
      if (other.start != null)
        return false;
    } else if (!start.equals(other.start))
      return false;
    return true;
  }

  @Override
  public int compareTo(LinebasedTextRange o) {
    int i = start().compareTo(o.start());
    if (i == 0) {
      return end().compareTo(o.end());
    }
    return i;
  }

}