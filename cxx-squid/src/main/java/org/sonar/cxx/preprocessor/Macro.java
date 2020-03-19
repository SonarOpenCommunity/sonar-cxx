/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import com.sonar.sslr.api.Token;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import static org.sonar.cxx.api.CxxTokenType.STRING;

public final class Macro {

  public static final String CPLUSPLUS = "__cplusplus";

  /**
   * This is a collection of standard macros according to
   * http://gcc.gnu.org/onlinedocs/cpp/Standard-Predefined-Macros.html
   */
  public static final Map<String, Macro> STANDARD_MACROS = initStandardMacros();

  /**
   * Smaller set of defines as rest is provides by compilation unit settings
   */
  public static final Map<String, Macro> UNIT_MACROS = initUnitMacros();

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

  private static Map<String, Macro> initStandardMacros() {
    Map<String, Macro> map = new HashMap<>();
    add(map, "__FILE__", "\"file\"");
    add(map, "__LINE__", "1");
    // indicates 'date unknown'. should suffice
    add(map, "__DATE__", "\"??? ?? ????\"");
    // indicates 'time unknown'. should suffice
    add(map, "__TIME__", "\"??:??:??\"");
    add(map, "__STDC__", "1");
    add(map, "__STDC_HOSTED__", "1");
    add(map, CPLUSPLUS, "201103L");
    // __has_include support (C++17)
    add(map, "__has_include", "1");
    return Collections.unmodifiableMap(map);
  }

  private static Map<String, Macro> initUnitMacros() {
    Map<String, Macro> map = new HashMap<>();
    add(map, "__FILE__", "\"file\"");
    add(map, "__LINE__", "1");
    add(map, "__DATE__", "\"??? ?? ????\"");
    add(map, "__TIME__", "\"??:??:??\"");
    return Collections.unmodifiableMap(map);
  }

  private static void add(Map<String, Macro> map, String name, String body) {
    map.put(name, new Macro(name, body));
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

}
