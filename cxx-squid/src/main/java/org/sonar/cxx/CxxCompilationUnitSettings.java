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
package org.sonar.cxx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

/**
 * CxxCompilationUnitSettings
 */
public class CxxCompilationUnitSettings {

  private Map<String, String> defines = new ConcurrentHashMap<>();
  private List<String> includes = new ArrayList<>();

  public Map<String, String> getDefines() {
    return defines;
  }

  public void setDefines(@Nullable Map<String, String> defines) {
    if (defines != null) {
      this.defines = defines;
    }
  }

  public List<String> getIncludes() {
    return new ArrayList<>(includes);
  }

  public void setIncludes(@Nullable List<String> includes) {
    if (includes != null) {
      this.includes = new ArrayList<>(includes);
    }
  }
}
