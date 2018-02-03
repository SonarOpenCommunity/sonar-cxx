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
package org.sonar.cxx.preprocessor;

import java.util.HashMap;
import java.util.Map;

/**
 * StandardDefinitions
 *
 */
public final class StandardDefinitions {

  private StandardDefinitions() {
  }

  /**
   * macros
   *
   * @return <String, String>
   */
  public static Map<String, String> macros() {
    // This is a collection of standard macros according to
    // http://gcc.gnu.org/onlinedocs/cpp/Standard-Predefined-Macros.html
    Map<String, String> source = new HashMap<>();
    source.put("__FILE__", "\"file\"");
    source.put("__LINE__", "1");
    // indicates 'date unknown'. should suffice
    source.put("__DATE__", "\"??? ?? ????\"");
    // indicates 'time unknown'. should suffice
    source.put("__TIME__", "\"??:??:??\"");
    source.put("__STDC__", "1");
    source.put("__STDC_HOSTED__", "1");
    source.put("__cplusplus", "201103L");
    // __has_include support (C++17)
    source.put("__has_include", "1");
    return source;
  }

  /**
   * compatibilityMacros
   *
   * @return <String, String>
   */
  public static Map<String, String> compatibilityMacros() {
    // This is a collection of macros used to let C code be parsed by C++ parser
    Map<String, String> source = new HashMap<>();
    source.put("alignas", "__alignas");
    source.put("alignof", "__alignof");
    source.put("catch", "__catch");
    source.put("class", "__class");
    source.put("constexpr", "__constexpr");
    source.put("const_cast", "__const_cast");
    source.put("decltype", "__decltype");
    source.put("delete", "__delete");
    source.put("dynamic_cast", "__dynamic_cast");
    source.put("explicit", "__explicit");
    source.put("export", "__export");
    source.put("final", "__final");
    source.put("friend", "__friend");
    source.put("mutable", "__mutable");
    source.put("namespace", "__namespace");
    source.put("new", "__new");
    source.put("noexcept", "__noexcept");
    source.put("nullptr", "__nullptr");
    source.put("operator", "__operator");
    source.put("override", "__override");
    source.put("private", "__private");
    source.put("protected", "__protected");
    source.put("public", "__public");
    source.put("reinterpret_cast", "__reinterpret_cast");
    source.put("static_assert", "__static_assert");
    source.put("static_cast", "__static_cast");
    source.put("thread_local", "__thread_local");
    source.put("throw", "__throw");
    source.put("try", "__try");
    source.put("typeid", "__typeid");
    source.put("typename", "__typename");
    source.put("using", "__using");
    source.put("template", "__template");
    source.put("virtual", "__virtual");
    return source;
  }
}
