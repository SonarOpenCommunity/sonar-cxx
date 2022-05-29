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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import static org.sonar.cxx.parser.CxxTokenType.STRING;

final class PPMacro {

  public final String name;
  public final List<Token> params;
  public final List<Token> body;
  public final boolean isVariadic;

  private PPMacro(String name, @Nullable List<Token> params, @Nullable List<Token> body, boolean variadic) {
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
  private PPMacro(String name, String body) {
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

  static PPMacro create(AstNode defineLineAst) {
    var ast = defineLineAst.getFirstChild();
    var nameNode = ast.getFirstDescendant(PPGrammarImpl.ppToken);
    String macroName = nameNode.getTokenValue();

    var paramList = ast.getFirstDescendant(PPGrammarImpl.parameterList);
    List<Token> macroParams = paramList == null
                                ? "objectlikeMacroDefinition".equals(ast.getName()) ? null : new LinkedList<>()
                                : getChildrenIdentifierTokens(paramList);

    var vaargs = ast.getFirstDescendant(PPGrammarImpl.variadicparameter);
    if ((vaargs != null) && (macroParams != null)) {
      var identifier = vaargs.getFirstChild(GenericTokenType.IDENTIFIER);
      macroParams.add(identifier == null
                        ? PPGeneratedToken.build(vaargs.getToken(), GenericTokenType.IDENTIFIER, "__VA_ARGS__")
                        : identifier.getToken());
    }

    var replList = ast.getFirstDescendant(PPGrammarImpl.replacementList);
    List<Token> macroBody = replList == null
                              ? new LinkedList<>()
                              : replList.getTokens().subList(0, replList.getTokens().size() - 1);

    return new PPMacro(macroName, macroParams, macroBody, vaargs != null);
  }

  private static void add(Map<String, PPMacro> map, String name, String body) {
    map.put(name, new PPMacro(name, body));
  }

  private static List<Token> getChildrenIdentifierTokens(AstNode identListAst) {
    return identListAst.getChildren(GenericTokenType.IDENTIFIER)
      .stream()
      .map(AstNode::getToken)
      .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    StringBuilder ab = new StringBuilder(64);
    ab.append("{");
    ab.append(name);
    if (params != null) {
      ab.append("(");
      ab.append(params.stream().map(Token::getValue).collect(Collectors.joining(", ")));
      if (isVariadic) {
        ab.append("...");
      }
      ab.append(")");
    }
    if (body != null) {
      ab.append(":");
      ab.append(body.stream().map(Token::getValue).collect(Collectors.joining()));
    }
    ab.append("}");
    return ab.toString();
  }

  boolean checkArgumentsCount(int count) {
    if (params != null) {
      return isVariadic ? count >= params.size() - 1 : count == params.size();
    }
    return false;
  }

}
