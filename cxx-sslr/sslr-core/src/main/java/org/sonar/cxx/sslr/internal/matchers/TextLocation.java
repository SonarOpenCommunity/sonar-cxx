/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.internal.matchers;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import javax.annotation.Nullable;

class TextLocation {

  private final File file;
  private final URI uri;
  private final int line;
  private final int column;

  public TextLocation(@Nullable File file, @Nullable URI uri, int line, int column) {
    this.file = file;
    this.uri = uri;
    this.line = line;
    this.column = column;
  }

  public File getFile() {
    return file;
  }

  /**
   * For internal use only.
   */
  public URI getFileURI() {
    return uri;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  @Override
  public int hashCode() {
    return Objects.hash(file, line, column);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (getClass() == obj.getClass()) {
      var other = (TextLocation) obj;
      return Objects.equals(this.file, other.file)
               && this.line == other.line
               && this.column == other.column;
    }
    return false;
  }

  @Override
  public String toString() {
    return "TextLocation{" + "file=" + file + ", line=" + line + ", column=" + column + '}';
  }

}
