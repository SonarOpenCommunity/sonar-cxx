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
package org.sonar.cxx.toolkit;

import java.util.Collections;
import java.util.List;
import org.sonar.api.config.Configuration;
import org.sonar.cxx.CxxLanguage;

/**
 *
 * @author jocs
 */
public class CppLanguage extends CxxLanguage {

  public CppLanguage(Configuration config) {
    super("c++", "cxx", config);
  }

  @Override
  public String[] getFileSuffixes() {
    return new String[]{"cpp", "hpp", "h", "hxx", "cxx"};
  }

  @Override
  public String[] getSourceFileSuffixes() {
    return new String[]{"cpp", "cxx"};
  }

  @Override
  public String[] getHeaderFileSuffixes() {
    return new String[]{"hpp", "h", "hxx"};
  }

  @Override
  public List<Class> getChecks() {
    return Collections.emptyList();
  }

  @Override
  public String getRepositoryKey() {
    return "";
  }
}
