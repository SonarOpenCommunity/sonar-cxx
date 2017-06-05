/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.cxx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CxxCompilationUnitSettings
 */
public class CxxCompilationUnitSettings {
  private Map<String, String> defines = null;
  private List<String> includes = null;

  public Map<String, String> getDefines() {
    return defines;
  }

  public void setDefines(Map<String, String> defines) {
    this.defines = defines;
  }


  public List<String> getIncludes() {
    return new ArrayList<>(includes);
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }
}
