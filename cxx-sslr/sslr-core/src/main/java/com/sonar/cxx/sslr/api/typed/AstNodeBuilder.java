/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.api.typed;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia;
import java.util.List;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;

/**
 * @since 1.21
 */
public class AstNodeBuilder implements NodeBuilder {

  private static final TokenType UNDEFINED_TOKEN_TYPE = new UndefinedTokenType();

  @Override
  public AstNode createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex,
                                   int endIndex) {
    Token token = null;

    for (var child : children) {
      if (child instanceof AstNode && ((AstNode) child).hasToken()) {
        token = ((AstNode) child).getToken();
        break;
      }
    }
    var astNode = new AstNode(rule, ruleKey.toString(), token);
    for (var child : children) {
      astNode.addChild((AstNode) child);
    }

    astNode.setFromIndex(startIndex);
    astNode.setToIndex(endIndex);

    return astNode;
  }

  @Override
  public AstNode createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    var lineAndColumn = input.lineAndColumnAt(startIndex);
    var token = Token.builder()
      .setType(type == null ? UNDEFINED_TOKEN_TYPE : type)
      .setLine(lineAndColumn[0])
      .setColumn(lineAndColumn[1] - 1)
      .setValueAndOriginalValue(input.substring(startIndex, endIndex))
      .setURI(input.uri())
      .setGeneratedCode(false)
      .setTrivia(trivias)
      .build();
    var astNode = new AstNode(token);
    astNode.setFromIndex(startIndex);
    astNode.setToIndex(endIndex);
    return astNode;
  }

  private static final class UndefinedTokenType implements TokenType {

    @Override
    public String getName() {
      return "TOKEN";
    }

    @Override
    public String getValue() {
      return getName();
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

    @Override
    public String toString() {
      return UndefinedTokenType.class.getSimpleName();
    }
  }

}
