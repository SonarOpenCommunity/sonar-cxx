/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import static org.sonar.cxx.parser.CxxTokenType.STRING;

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
