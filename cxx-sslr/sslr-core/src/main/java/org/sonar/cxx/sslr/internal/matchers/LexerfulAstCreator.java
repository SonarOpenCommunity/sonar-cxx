/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package org.sonar.cxx.sslr.internal.matchers; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeExpression;

public final class LexerfulAstCreator {

  private final List<Token> tokens;

  private LexerfulAstCreator(List<Token> tokens) {
    this.tokens = tokens;
  }

  public static AstNode create(ParseNode node, List<Token> tokens) {
    var astNode = new LexerfulAstCreator(tokens).visit(node);
    if (astNode == null) {
      throw new IllegalStateException("create ParseNode: "
        + node.toString()
        + " Tokens: " + tokens.toString());
    }
    // Unwrap AstNodeType for root node:
    astNode.hasToBeSkippedFromAst();

    return astNode;
  }

  private AstNode visit(ParseNode node) {
    if (node.getMatcher() instanceof RuleDefinition) {
      return visitNonTerminal(node);
    } else {
      return visitTerminal(node);
    }
  }

  private AstNode visitNonTerminal(ParseNode node) {
    List<AstNode> astNodes = new ArrayList<>();
    for (var child : node.getChildren()) {
      var astNode = visit(child);
      if (astNode == null) {
        // skip
      } else if (astNode.hasToBeSkippedFromAst()) {
        astNodes.addAll(astNode.getChildren());
      } else {
        astNodes.add(astNode);
      }
    }

    var ruleMatcher = (RuleDefinition) node.getMatcher();

    var token = node.getStartIndex() < tokens.size() ? tokens.get(node.getStartIndex()) : null;
    var astNode = new AstNode(ruleMatcher, ruleMatcher.getName(), token);
    for (var child : astNodes) {
      astNode.addChild(child);
    }
    astNode.setFromIndex(node.getStartIndex());
    astNode.setToIndex(node.getEndIndex());

    return astNode;
  }

  @CheckForNull
  private AstNode visitTerminal(ParseNode node) {
    var token = tokens.get(node.getStartIndex());
    // For compatibility with SSLR < 1.19, TokenType should be checked only for TokenTypeExpression:
    if ((node.getMatcher() instanceof TokenTypeExpression) && token.getType().hasToBeSkippedFromAst(null)) {
      return null;
    }
    var astNode = new AstNode(token);
    astNode.setFromIndex(node.getStartIndex());
    astNode.setToIndex(node.getEndIndex());
    return astNode;
  }

}
