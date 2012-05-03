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

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents an error found by valgrind. It always has an id,
 * a descriptive text and a stack trace.
 */
class ValgrindError {
  private String kind;
  private String text;
  private ValgrindStack stack;
  
  /**
   * Constructs a ValgrindError out of the given attributes
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
  public String toString() { return text + "\n\n" + stack; }
    
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

  String getKind() { return this.kind; }

  /**
   * @see ValgrindStack#getLastFrame
   */
  public ValgrindFrame getLastOwnFrame(String basedir) {
    return stack.getLastOwnFrame(basedir);
  }
}
  
