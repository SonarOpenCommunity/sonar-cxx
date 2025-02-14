/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
final class PPPredefinedMacros {

  private static final String[] predefinedMacros = {
    //
    // C++
    //
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
    //
    // __has_include support (C++17)
    //
    "__has_include __has_include", // define __has_include as macro, for e.g. #if __has_include
    "__has_include_next __has_include_next", // define __has_include as macro, for e.g. #if __has_include
    //
    // source: https://clang.llvm.org/docs/LanguageExtensions.html
    //
    "__has_builtin(x) 0",
    "__has_feature(x) 0",
    "__has_extension(x) 0",
    "__has_cpp_attribute(x) 0",
    "__has_c_attribute(x) 0",
    "__has_attribute(x) 0",
    "__has_declspec_attribute(x) 0",
    "__is_identifier(x) 1",
    "__has_warning(x) 0"
  };

  private PPPredefinedMacros() {

  }

  static String[] predefinedMacroValues() {
    return predefinedMacros.clone();
  }

}
