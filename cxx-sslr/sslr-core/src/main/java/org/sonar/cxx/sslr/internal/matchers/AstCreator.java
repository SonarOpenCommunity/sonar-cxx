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
package org.sonar.cxx.sslr.internal.matchers;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;
import org.sonar.cxx.sslr.internal.vm.TriviaExpression;
import org.sonar.cxx.sslr.parser.ParsingResult;

public final class AstCreator {

  private static final URI FAKE_URI;

  static {
    try {
      FAKE_URI = new URI("tests://unittest");
    } catch (URISyntaxException e) {
      // Can't happen
      throw new IllegalStateException(e);
    }
  }

  private final LocatedText input;
  private final Token.Builder tokenBuilder = Token.builder();
  private final List<Trivia> trivias = new ArrayList<>();

  public static AstNode create(ParsingResult parsingResult, LocatedText input) {
    var astNode = new AstCreator(input).visit(parsingResult.getParseTreeRoot());
    if (astNode == null) {
      throw new IllegalStateException("create ParsingResult: "
        + parsingResult.toString()
        + " LocatedText: " + input.toString());
    }
    // Unwrap AstNodeType for root node:
    astNode.hasToBeSkippedFromAst();
    return astNode;
  }

  private AstCreator(LocatedText input) {
    this.input = input;
  }

  private AstNode visit(ParseNode node) {
    if (node.getMatcher() instanceof MutableParsingRule) {
      return visitNonTerminal(node);
    } else {
      return visitTerminal(node);
    }
  }

  @CheckForNull
  private AstNode visitTerminal(ParseNode node) {
    if (node.getMatcher() instanceof TriviaExpression ruleMatcher) {
      switch (ruleMatcher.getTriviaKind()) {
        case SKIPPED_TEXT:
          return null;
        case COMMENT:
          updateTokenPositionAndValue(node);
          tokenBuilder.setTrivia(Collections.<Trivia>emptyList());
          tokenBuilder.setType(GenericTokenType.COMMENT);
          trivias.add(Trivia.createComment(tokenBuilder.build()));
          return null;
        default:
          throw new IllegalStateException("Unexpected trivia kind: " + ruleMatcher.getTriviaKind());
      }
    } else if (node.getMatcher() instanceof TokenExpression tokenExpression) {
      updateTokenPositionAndValue(node);
      var ruleMatcher = tokenExpression;
      tokenBuilder.setType(ruleMatcher.getTokenType());
      if (ruleMatcher.getTokenType() == GenericTokenType.COMMENT) {
        tokenBuilder.setTrivia(Collections.<Trivia>emptyList());
        trivias.add(Trivia.createComment(tokenBuilder.build()));
        return null;
      }
    } else {
      updateTokenPositionAndValue(node);
      tokenBuilder.setType(UNDEFINED_TOKEN_TYPE);
    }
    var token = tokenBuilder.setTrivia(trivias).build();
    trivias.clear();
    var astNode = new AstNode(token);
    astNode.setFromIndex(node.getStartIndex());
    astNode.setToIndex(node.getEndIndex());
    return astNode;
  }

  private void updateTokenPositionAndValue(ParseNode node) {
    var location = input.getLocation(node.getStartIndex());
    if (location == null) {
      tokenBuilder.setGeneratedCode(true);
      // Godin: line, column and uri has no value for generated code, but we should bypass checks in TokenBuilder
      tokenBuilder.setLine(1);
      tokenBuilder.setColumn(0);
      tokenBuilder.setURI(FAKE_URI);
    } else {
      tokenBuilder.setGeneratedCode(false);
      tokenBuilder.setLine(location.getLine());
      tokenBuilder.setColumn(location.getColumn() - 1);
      tokenBuilder.setURI(location.getFileURI() == null ? FAKE_URI : location.getFileURI());
      tokenBuilder.notCopyBook();
    }

    var value = getValue(node);
    tokenBuilder.setValueAndOriginalValue(value);
  }

  private AstNode visitNonTerminal(ParseNode node) {
    var ruleMatcher = (MutableParsingRule) node.getMatcher();
    List<AstNode> astNodes = new ArrayList<>();
    for (var child : node.getChildren()) {
      var astNode = visit(child);
      if (astNode != null) {
        if (astNode.hasToBeSkippedFromAst()) {
          astNodes.addAll(astNode.getChildren());
        } else {
          astNodes.add(astNode);
        }
      }
    }

    Token token = null;
    for (var child : astNodes) {
      if (child.getToken() != null) {
        token = child.getToken();
        break;
      }
    }

    var astNode = new AstNode(ruleMatcher, ruleMatcher.getName(), token);
    for (var child : astNodes) {
      astNode.addChild(child);
    }
    astNode.setFromIndex(node.getStartIndex());
    astNode.setToIndex(node.getEndIndex());
    return astNode;
  }

  private String getValue(ParseNode node) {
    var result = new StringBuilder();
    for (int i = node.getStartIndex(); i < Math.min(node.getEndIndex(), input.length()); i++) {
      result.append(input.charAt(i));
    }
    return result.toString();
  }

  // @VisibleForTesting
  static final TokenType UNDEFINED_TOKEN_TYPE = new TokenType() {
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
  };

}
