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

import static org.sonar.cxx.api.CxxTokenType.STRING;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.sonar.sslr.api.Token;

public final class Macro {

  public final String name;
  public final List<Token> params;
  public final List<Token> body;
  public final boolean isVariadic;

  public Macro(String name, @Nullable List<Token> params, @Nullable List<Token> body, boolean variadic) {
    this.name = name;
    if (params == null) {
      this.params = null;
    } else {
      this.params = new ArrayList<>(params);
    }
    if (body == null) {
      this.body = null;
    } else {
      this.body = new ArrayList<>(body);
    }
    this.isVariadic = variadic;
  }

  /**
   * Constructor for standard (predefined) macros
   *
   * @param name
   * @param body
   */
  private Macro(String name, String body) {
    this.name = name;
    this.params = null;
    this.body = Collections.singletonList(Token.builder()
        .setLine(1)
        .setColumn(0)
        .setURI(URI.create(""))
        .setValueAndOriginalValue(body)
        .setType(STRING)
        .build());
    this.isVariadic = false;
  }

  @Override
  public String toString() {
    String paramsStr = "";
    if (params != null) {
      final String joinedParams = params.stream().map(Token::getValue).collect(Collectors.joining(", "));
      paramsStr = "(" + joinedParams + (isVariadic ? "..." : "") + ")";
    }
    String bodyStr = "";
    if (body != null) {
      bodyStr = body.stream().map(Token::getValue).collect(Collectors.joining(" "));
    }
    return name + paramsStr + " -> '" + bodyStr + "'";
  }

  public boolean checkArgumentsCount(int count) {
    return isVariadic ? count >= params.size() - 1 : count == params.size();
  }

  private static void add(Map<String, Macro> map, String name, String body) {
    map.put(name, new Macro(name, body));
  }

  /**
   * This is a collection of standard macros according to
   * http://gcc.gnu.org/onlinedocs/cpp/Standard-Predefined-Macros.html
   */
  private static final Map<String, Macro> STANDARD_MACROS_IMPL = new HashMap<>();
  static {

    add(STANDARD_MACROS_IMPL, "__FILE__", "\"file\"");
    add(STANDARD_MACROS_IMPL, "__LINE__", "1");
    // indicates 'date unknown'. should suffice
    add(STANDARD_MACROS_IMPL, "__DATE__", "\"??? ?? ????\"");
    // indicates 'time unknown'. should suffice
    add(STANDARD_MACROS_IMPL, "__TIME__", "\"??:??:??\"");
    add(STANDARD_MACROS_IMPL, "__STDC__", "1");
    add(STANDARD_MACROS_IMPL, "__STDC_HOSTED__", "1");
    add(STANDARD_MACROS_IMPL, "__cplusplus", "201103L");
    // __has_include support (C++17)
    add(STANDARD_MACROS_IMPL, "__has_include", "1");
  }

  /**
   * Smaller set of defines as rest is provides by compilation unit settings
   */
  private static final Map<String, Macro> UNIT_MACROS_IMPL = new HashMap<>();
  static {
    add(UNIT_MACROS_IMPL, "__FILE__", "\"file\"");
    add(UNIT_MACROS_IMPL, "__LINE__", "1");
    add(UNIT_MACROS_IMPL, "__DATE__", "\"??? ?? ????\"");
    add(UNIT_MACROS_IMPL, "__TIME__", "\"??:??:??\"");
  }

  /**
   * Macros to replace C++ keywords when parsing C files
   */
  private static Map<String, Macro> COMPATIBILITY_MACROS_IMPL = new HashMap<>();
  static {
    // This is a collection of macros used to let C code be parsed by C++ parser
    add(COMPATIBILITY_MACROS_IMPL, "alignas", "__alignas");
    add(COMPATIBILITY_MACROS_IMPL, "alignof", "__alignof");
    add(COMPATIBILITY_MACROS_IMPL, "catch", "__catch");
    add(COMPATIBILITY_MACROS_IMPL, "class", "__class");
    add(COMPATIBILITY_MACROS_IMPL, "constexpr", "__constexpr");
    add(COMPATIBILITY_MACROS_IMPL, "const_cast", "__const_cast");
    add(COMPATIBILITY_MACROS_IMPL, "decltype", "__decltype");
    add(COMPATIBILITY_MACROS_IMPL, "delete", "__delete");
    add(COMPATIBILITY_MACROS_IMPL, "dynamic_cast", "__dynamic_cast");
    add(COMPATIBILITY_MACROS_IMPL, "explicit", "__explicit");
    add(COMPATIBILITY_MACROS_IMPL, "export", "__export");
    add(COMPATIBILITY_MACROS_IMPL, "final", "__final");
    add(COMPATIBILITY_MACROS_IMPL, "friend", "__friend");
    add(COMPATIBILITY_MACROS_IMPL, "mutable", "__mutable");
    add(COMPATIBILITY_MACROS_IMPL, "namespace", "__namespace");
    add(COMPATIBILITY_MACROS_IMPL, "new", "__new");
    add(COMPATIBILITY_MACROS_IMPL, "noexcept", "__noexcept");
    add(COMPATIBILITY_MACROS_IMPL, "nullptr", "__nullptr");
    add(COMPATIBILITY_MACROS_IMPL, "operator", "__operator");
    add(COMPATIBILITY_MACROS_IMPL, "override", "__override");
    add(COMPATIBILITY_MACROS_IMPL, "private", "__private");
    add(COMPATIBILITY_MACROS_IMPL, "protected", "__protected");
    add(COMPATIBILITY_MACROS_IMPL, "public", "__public");
    add(COMPATIBILITY_MACROS_IMPL, "reinterpret_cast", "__reinterpret_cast");
    add(COMPATIBILITY_MACROS_IMPL, "static_assert", "__static_assert");
    add(COMPATIBILITY_MACROS_IMPL, "static_cast", "__static_cast");
    add(COMPATIBILITY_MACROS_IMPL, "thread_local", "__thread_local");
    add(COMPATIBILITY_MACROS_IMPL, "throw", "__throw");
    add(COMPATIBILITY_MACROS_IMPL, "try", "__try");
    add(COMPATIBILITY_MACROS_IMPL, "typeid", "__typeid");
    add(COMPATIBILITY_MACROS_IMPL, "typename", "__typename");
    add(COMPATIBILITY_MACROS_IMPL, "using", "__using");
    add(COMPATIBILITY_MACROS_IMPL, "template", "__template");
    add(COMPATIBILITY_MACROS_IMPL, "virtual", "__virtual");
  }

  public static final Map<String, Macro> STANDARD_MACROS = Collections.unmodifiableMap(STANDARD_MACROS_IMPL);
  public static final Map<String, Macro> UNIT_MACROS = Collections.unmodifiableMap(UNIT_MACROS_IMPL);
  public static final Map<String, Macro> COMPATIBILITY_MACROS = Collections.unmodifiableMap(COMPATIBILITY_MACROS_IMPL);

}
