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

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents an error found by valgrind. It always has an id, a descriptive
 * text and a stack trace.
 */
class ValgrindError {

  private final String kind;
  private final String text;
  private final ValgrindStack stack;

  /**
   * Constructs a ValgrindError out of the given attributes
   *
   * @param kind The kind of error, plays the role of an id
   * @param text Description of the error
   * @param stack The associated call stack
   */
  public ValgrindError(String kind, String text, ValgrindStack stack) {
    this.kind = kind;
    this.text = text;
    this.stack = stack;
  }

  @Override
  public String toString() {
    return text + "\n\n" + stack;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ValgrindError other = (ValgrindError) o;
    return hashCode() == other.hashCode();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(kind)
      .append(stack)
      .toHashCode();
  }

  String getKind() {
    return this.kind;
  }

  /**
   * @see ValgrindStack#getLastFrame
   */
  public ValgrindFrame getLastOwnFrame(String basedir) {
    return stack.getLastOwnFrame(basedir);
  }
}
