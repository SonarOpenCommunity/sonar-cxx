/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

/**
 * Predefined macros
 *
 * The macro names of this class are predefined in every translation unit.
 */
class PPPredefinedMacros {

  static String[] predefinedMacros = {
    "__FILE__ \"file\"",
    "__LINE__ 1",
    // indicates 'date unknown'. should suffice
    "__DATE__ \"??? ?? ????\"",
    // indicates 'time unknown'. should suffice
    "__TIME__ \"??:??:??\"",
    "__STDC__ 1",
    "__STDC_HOSTED__ 1",
    // set C++14 as default
    "__cplusplus 201402L",
    // __has_include support (C++17)
    "__has_include 1"
  };

  static String[] predefinedMacroValues() {
    return predefinedMacros.clone();
  }

}
