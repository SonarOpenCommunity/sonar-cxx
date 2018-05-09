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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents an error found by valgrind. It always has an id, a descriptive
 * text and a stack trace.
 */
class ValgrindError {

  private final String kind;
  private final String text;
  private final List<ValgrindStack> stacks;

  /**
   * Constructs a ValgrindError out of the given attributes
   *
   * @param kind The kind of error, plays the role of an id
   * @param text Description of the error
   * @param stacks One or more associated call stacks
   */
  public ValgrindError(String kind, String text, List<ValgrindStack> stacks) {
    this.kind = kind;
    this.text = text;
    this.stacks = Collections.unmodifiableList(stacks);
  }


  /**
   * For debug prints only; SonarQube cannot deal with long/multi-line error
   * messages. Formats like Markdown or HTML are not supported.
   *
   * For the sake of readability each ValgrindStack will be saved as a separate NewIssue.
   *
   * See <a href=
   * "http://javadocs.sonarsource.org/7.0/apidocs/org/sonar/api/batch/sensor/issue/NewIssueLocation.html#message-java.lang.String-">NewIssueLocation::message()</a>
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ValgrindError [kind=").append(kind).append(", text=").append(text).append(", stacks=[");
    for (ValgrindStack stack : stacks) {
      sb.append(" ValgrindStack=[").append(stack).append("] ");
    }
    sb.append("] ]");
    return sb.toString();
  }


  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ValgrindError other = (ValgrindError) obj;
    return new EqualsBuilder()
        .append(kind, other.kind)
        .append(text, other.text)
        .append(stacks, other.stacks)
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(kind)
        .append(text)
        .append(stacks)
        .toHashCode();
  }

  String getKind() {
    return this.kind;
  }

  public String getText() {
    return text;
  }

  public List<ValgrindStack> getStacks() {
    return stacks;
  }

}
