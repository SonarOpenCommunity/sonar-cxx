/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.utils;

import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.utils.PathUtils;

/**
 * Each issues in SonarQube might have multiple locations; Encapsulate its properties in this structure
 */
public class CxxReportLocation {

  private final String file;
  private final String line;
  private final String column;
  private final String info;

  public CxxReportLocation(@Nullable String file, @Nullable String line, @Nullable String column, String info) {
    super();

    // Normalize file using separators in UNIX format, removing double and single dot path steps. This is to avoid
    // duplicates in the issue containers because they are using the file as key. PathUtils.sanitize uses
    // FilenameUtils.normalize internally, relative paths starting with a double dot will cause that path segment
    // and the one before to be removed. If the double dot has no parent path segment to work with, null is returned.
    // null would mean 'project issue' which is wrong in this context. To avoid this we extract the filename to
    // generate at least a meningful error message (#2747).
    var normalized = PathUtils.sanitize(file);
    if (normalized == null && (file != null && !file.isBlank())) {

      // use FilenameUtils.getName because this works on Windows and Linux also if
      // report is generated on the one and consumed on the other
      normalized = FilenameUtils.getName(file);
    }

    this.file = normalized;
    this.line = line;
    this.column = column;
    this.info = info;
  }

  public String getFile() {
    return file;
  }

  public String getLine() {
    return line;
  }

  public String getColumn() {
    return column;
  }

  public String getInfo() {
    return info;
  }

  @Override
  public String toString() {
    return "CxxReportLocation ["
             + "file=" + file
             + ", line=" + line
             + ", column=" + column
             + ", info=" + info
             + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(file, line, column, info);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    var other = (CxxReportLocation) obj;
    return Objects.equals(line, other.line)
             && Objects.equals(column, other.column)
             && Objects.equals(file, other.file)
             && Objects.equals(info, other.info);
  }

}
