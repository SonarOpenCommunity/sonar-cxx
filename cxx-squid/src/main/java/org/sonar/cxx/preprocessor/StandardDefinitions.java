/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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

import java.util.Map;
import java.util.HashMap;

public final class StandardDefinitions {

  private StandardDefinitions() {
  }

  public static Map<String, String> macros() {
    // This is a collection of standard macros according to
    // http://gcc.gnu.org/onlinedocs/cpp/Standard-Predefined-Macros.html

    return new HashMap<String, String>() {{
        put("__FILE__", "\"file\""); // for now
        put("__LINE__", "1"); // that should hopefully suffice
        put("__DATE__", "\"??? ?? ????\""); // indicates 'date unknown'. should suffice
        put("__TIME__", "\"??:??:??\""); // indicates 'time unknown'. should suffice
        put("__STDC__", "1");
        put("__STDC_HOSTED__", "1");
        put("__cplusplus", "201103L");
      }};
  }
  
  public static Map<String, String> compatibilityMacros() {
    // This is a collection of macros used to let C code be parsed by C++ parser
    return new HashMap<String, String>() {{
        put("alignas", "__alignas");
        put("alignof", "__alignof");
        put("catch", "__catch");
        put("class", "__class");
        put("constexpr", "__constexpr");
        put("const_cast", "__const_cast");
        put("decltype", "__decltype");
        put("delete", "__delete");
        put("dynamic_cast", "__dynamic_cast");
        put("explicit", "__explicit");
        put("export", "__export");
        put("final", "__final");
        put("friend", "__friend");
        put("mutable", "__mutable");
        put("namespace", "__namespace");
        put("new", "__new");
        put("noexcept", "__noexcept");
        put("nullptr", "__nullptr");
        put("operator", "__operator");
        put("override", "__override");
        put("private", "__private");
        put("protected", "__protected");
        put("public", "__public");
        put("reinterpret_cast", "__reinterpret_cast");
        put("static_assert", "__static_assert");
        put("static_cast", "__static_cast");
        put("thread_local", "__thread_local");
        put("throw", "__throw");
        put("try", "__try");
        put("typeid", "__typeid");
        put("typename", "__typename");
        put("using", "__using");
        put("template", "__template");
        put("virtual", "__virtual");
      }};
  }
  
}
