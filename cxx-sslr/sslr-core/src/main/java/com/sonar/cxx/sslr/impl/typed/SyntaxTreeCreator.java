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
package com.sonar.cxx.sslr.impl.typed;

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia;
import com.sonar.cxx.sslr.api.typed.Input;
import com.sonar.cxx.sslr.api.typed.NodeBuilder;
import com.sonar.cxx.sslr.api.typed.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.matchers.ParseNode;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;
import org.sonar.cxx.sslr.internal.vm.TriviaExpression;

public class SyntaxTreeCreator<T> {

  private final Object treeFactory;
  private final GrammarBuilderInterceptor mapping;
  private final NodeBuilder nodeBuilder;

  private final Token.Builder tokenBuilder = Token.builder();
  private final List<Trivia> trivias = new ArrayList<>();

  private Input input;

  public SyntaxTreeCreator(Object treeFactory, GrammarBuilderInterceptor mapping, NodeBuilder nodeBuilder) {
    this.treeFactory = treeFactory;
    this.mapping = mapping;
    this.nodeBuilder = nodeBuilder;
  }

  public T create(ParseNode node, Input input) {
    this.input = input;
    this.trivias.clear();
    return (T) visit(node);
  }

  private Object visit(ParseNode node) {
    if (node.getMatcher() instanceof MutableParsingRule) {
      return visitNonTerminal(node);
    } else {
      return visitTerminal(node);
    }
  }

  private Object visitNonTerminal(ParseNode node) {
    var rule = (MutableParsingRule) node.getMatcher();
    var ruleKey = rule.getRuleKey();
    var method = mapping.actionForRuleKey(ruleKey);

    Object result;

    if (mapping.hasMethodForRuleKey(ruleKey)) {

      // TODO Drop useless intermediate nodes
      if (node.getChildren().size() != 1) {
        throw new IllegalStateException();
      }
      result = visit(node.getChildren().get(0));

    } else if (mapping.isOptionalRule(ruleKey)) {

      if (node.getChildren().size() > 1) {
        throw new IllegalStateException();
      }
      if (node.getChildren().isEmpty()) {
        result = Optional.absent();
      } else {
        result = Optional.of(visit(node.getChildren().get(0)));
      }

    } else {
      List<Object> convertedChildren = new ArrayList<>();
      for (var child : node.getChildren()) {
        convertedChildren.add(visit(child));
      }
      if (mapping.isOneOrMoreRule(ruleKey)) {
        result = convertedChildren;
      } else if (mapping.isZeroOrMoreRule(ruleKey)) {
        result = convertedChildren.isEmpty() ? Optional.absent() : Optional.of(convertedChildren);
      } else if (method == null) {
        result = nodeBuilder.createNonTerminal(ruleKey, rule, convertedChildren, node.getStartIndex(), node
                                               .getEndIndex());
      } else {
        result = ReflectionUtils.invokeMethod(method, treeFactory, convertedChildren.toArray(new Object[0]));
      }
    }
    return result;
  }

  @CheckForNull
  private Object visitTerminal(ParseNode node) {
    TokenType type = null;
    if (node.getMatcher() instanceof TriviaExpression) {
      var ruleMatcher = (TriviaExpression) node.getMatcher();
      switch (ruleMatcher.getTriviaKind()) {
        case SKIPPED_TEXT:
          return null;
        case COMMENT:
          addComment(node);
          return null;
        default:
          throw new IllegalStateException("Unexpected trivia kind: " + ruleMatcher.getTriviaKind());
      }
    } else if (node.getMatcher() instanceof TokenExpression) {
      var ruleMatcher = (TokenExpression) node.getMatcher();
      type = ruleMatcher.getTokenType();
      if (GenericTokenType.COMMENT.equals(ruleMatcher.getTokenType())) {
        addComment(node);
        return null;
      }
    }
    var result = nodeBuilder.createTerminal(input, node.getStartIndex(), node.getEndIndex(), trivias, type);
    trivias.clear();
    return result;
  }

  private void addComment(ParseNode node) {
    tokenBuilder.setGeneratedCode(false);
    var lineAndColumn = input.lineAndColumnAt(node.getStartIndex());
    tokenBuilder.setLine(lineAndColumn[0]);
    tokenBuilder.setColumn(lineAndColumn[1] - 1);
    tokenBuilder.setURI(input.uri());
    var value = input.substring(node.getStartIndex(), node.getEndIndex());
    tokenBuilder.setValueAndOriginalValue(value);
    tokenBuilder.setTrivia(Collections.<Trivia>emptyList());
    tokenBuilder.setType(GenericTokenType.COMMENT);
    trivias.add(Trivia.createComment(tokenBuilder.build()));
  }

}
