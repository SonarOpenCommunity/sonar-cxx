/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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

import java.nio.file.Paths;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Each issues in SonarQube might have multiple locations; Encapsulate its properties in this structure
 */
public class CxxReportLocation {

  private final String file;
  private final String line;
  private final String info;

  public CxxReportLocation(@Nullable String file, @Nullable String line, String info) {
    super();
    if (file != null) {
      this.file = Paths.get(file).normalize().toString();
    } else {
      this.file = null;
    }
    this.line = line;
    this.info = info;
  }

  public String getFile() {
    return file;
  }

  public String getLine() {
    return line;
  }

  public String getInfo() {
    return info;
  }

  @Override
  public String toString() {
    return "CxxReportLocation [file=" + file + ", line=" + line + ", info=" + info + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(file, info, line);
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
    CxxReportLocation other = (CxxReportLocation) obj;
    return Objects.equals(file, other.file) && Objects.equals(info, other.info) && Objects.equals(line, other.line);
  }

}
