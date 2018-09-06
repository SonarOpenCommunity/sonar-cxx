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
package org.sonar.cxx.visitors;

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.api.SourceCode;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;

public class CxxCognitiveComplexityVisitor<G extends Grammar> extends MultiLocatitionSquidCheck<G> {

  private static final AstNodeType[] DESCENDANT_TYPES = new AstNodeType[] {
      CxxGrammarImpl.handler,
      CxxGrammarImpl.iterationStatement,
      CxxGrammarImpl.lambdaExpression,
      CxxGrammarImpl.logicalAndExpression,
      CxxGrammarImpl.logicalOrExpression,
      CxxGrammarImpl.selectionStatement,
      CxxKeyword.ELSE,
      CxxKeyword.GOTO,
      CxxPunctuator.QUEST,
      IDENTIFIER };

  private static final AstNodeType[] INCREMENT_TYPES = new AstNodeType[] {
      CxxGrammarImpl.handler,
      CxxGrammarImpl.iterationStatement,
      CxxGrammarImpl.logicalAndExpression,
      CxxGrammarImpl.logicalOrExpression,
      CxxGrammarImpl.selectionStatement,
      CxxKeyword.ELSE,
      CxxKeyword.GOTO,
      CxxPunctuator.QUEST };

  private static final AstNodeType[] NESTING_LEVEL_TYPES = new AstNodeType[] {
      CxxGrammarImpl.handler,
      CxxGrammarImpl.iterationStatement,
      CxxGrammarImpl.lambdaExpression,
      CxxGrammarImpl.selectionStatement,
      CxxPunctuator.QUEST };

  private static final AstNodeType[] NESTING_INCREMENTS_TYPES = new AstNodeType[] {
      CxxGrammarImpl.handler,
      CxxGrammarImpl.iterationStatement,
      CxxGrammarImpl.selectionStatement,
      CxxPunctuator.QUEST };

  private Deque<CxxComplexityScope> complexityScopes;

  private static final Set<AstNodeType> SUBSCRIPTION_NODES = new HashSet<>();
  static {
    SUBSCRIPTION_NODES.add(CxxGrammarImpl.functionDefinition);
    SUBSCRIPTION_NODES.addAll(Arrays.asList(DESCENDANT_TYPES));
    SUBSCRIPTION_NODES.addAll(Arrays.asList(INCREMENT_TYPES));
    SUBSCRIPTION_NODES.addAll(Arrays.asList(NESTING_LEVEL_TYPES));
    SUBSCRIPTION_NODES.addAll(Arrays.asList(NESTING_INCREMENTS_TYPES));
  }

  protected void analyzeComplexity(CxxComplexityScope scope) {
    SourceCode code = getContext().peekSourceCode();
    code.setMeasure(CxxMetric.COGNITIVE_COMPLEXITY, scope.getComplexity());
  }

  @Override
  public void init() {
    for (AstNodeType astNodeType : SUBSCRIPTION_NODES) {
      subscribeTo(astNodeType);
    }
    complexityScopes = new LinkedList<>();
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CxxGrammarImpl.functionDefinition)) {
      complexityScopes.addFirst(new CxxComplexityScope(node.getTokenLine()));
    }

    if (complexityScopes.isEmpty() || isElseIf(node)) {
      return;
    }

    if (node.is(NESTING_INCREMENTS_TYPES)) {
      complexityScopes.getFirst().addComplexitySourceWithNesting(node);
    } else if (node.is(INCREMENT_TYPES)) {
      complexityScopes.getFirst().addComplexitySource(node);
    }

    if (node.is(NESTING_LEVEL_TYPES)) {
      for (CxxComplexityScope scope : complexityScopes) {
        scope.increaseNesting();
      }
    }
  }

  @Override
  public void leaveNode(AstNode node) {
    if (node.is(CxxGrammarImpl.functionDefinition)) {
      analyzeComplexity(complexityScopes.removeFirst());
    }

    if (complexityScopes.isEmpty() || isElseIf(node)) {
      return;
    }

    if (node.is(NESTING_LEVEL_TYPES)) {
      for (CxxComplexityScope scope : complexityScopes) {
        scope.decreaseNesting();
      }
    }
  }

  private static boolean isElseIf(AstNode node) {
    return node.is(CxxGrammarImpl.selectionStatement) && node.getToken().getType().equals(CxxKeyword.IF)
        && node.getParent().getPreviousAstNode().getType().equals(CxxKeyword.ELSE);
  }
}
