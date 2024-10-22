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
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.ast.AstXmlPrinter;
import java.util.Arrays;
import java.util.Collections;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;
import org.sonar.cxx.sslr.parser.ParsingResult;

class AstCreatorTest {

  @Test
  void shouldCreateTokensAndTrivias() {
    var input = "foo bar".toCharArray();

    var tokenMatcher = mockTokenMatcher(GenericTokenType.IDENTIFIER);
    var triviaMatcher = mockTokenMatcher(GenericTokenType.COMMENT);
    var ruleMatcher = mockRuleMatcher("rule");
    var realAstNodeType = mock(AstNodeType.class);
    when(ruleMatcher.getRealAstNodeType()).thenReturn(realAstNodeType);

    var triviaNode = new ParseNode(0, 4, Collections.<ParseNode>emptyList(), triviaMatcher);
    var tokenNode = new ParseNode(4, 7, Collections.<ParseNode>emptyList(), tokenMatcher);
    var parseTreeRoot = new ParseNode(0, 7, Arrays.asList(triviaNode, tokenNode), ruleMatcher);

    InputBuffer inputBuffer = new ImmutableInputBuffer(input);
    var parsingResult = new ParsingResult(inputBuffer, true, parseTreeRoot, null);

    var astNode = AstCreator.create(parsingResult, new LocatedText(null, input));
    System.out.println(AstXmlPrinter.print(astNode));

    assertThat(astNode.getType()).isSameAs(realAstNodeType);
    assertThat(astNode.getName()).isEqualTo("rule");
    assertThat(astNode.getFromIndex()).isZero();
    assertThat(astNode.getToIndex()).isEqualTo(7);
    assertThat(astNode.hasChildren()).isTrue();

    assertThat(astNode.getTokens()).hasSize(1);
    var token = astNode.getTokens().get(0);
    assertThat(astNode.getToken()).isSameAs(token);
    assertThat(token.getValue()).isEqualTo("bar");
    assertThat(token.getOriginalValue()).isEqualTo("bar");
    assertThat(token.getLine()).isEqualTo(1);
    assertThat(token.getColumn()).isEqualTo(4);
    assertThat(token.getType()).isEqualTo(GenericTokenType.IDENTIFIER);

    assertThat(token.getTrivia()).hasSize(1);
    var trivia = token.getTrivia().get(0);
    var triviaToken = trivia.getToken();
    assertThat(triviaToken.getValue()).isEqualTo("foo ");
    assertThat(triviaToken.getOriginalValue()).isEqualTo("foo ");
    assertThat(triviaToken.getLine()).isEqualTo(1);
    assertThat(triviaToken.getColumn()).isZero();
    assertThat(triviaToken.getType()).isEqualTo(GenericTokenType.COMMENT);
  }

  @Test
  void shouldCreateTokensWithoutTokenMatcher() {
    var input = "foobar".toCharArray();

    var firstTerminal = new ParseNode(0, 3, Collections.<ParseNode>emptyList(), null);
    var secondTerminal = new ParseNode(3, 6, Collections.<ParseNode>emptyList(), null);
    var ruleMatcher = mockRuleMatcher("rule");
    var realAstNodeType = mock(AstNodeType.class);
    when(ruleMatcher.getRealAstNodeType()).thenReturn(realAstNodeType);
    var parseTreeRoot = new ParseNode(0, 6, Arrays.asList(firstTerminal, secondTerminal), ruleMatcher);

    InputBuffer inputBuffer = new ImmutableInputBuffer(input);
    var parsingResult = new ParsingResult(inputBuffer, true, parseTreeRoot, null);

    var astNode = AstCreator.create(parsingResult, new LocatedText(null, input));
    System.out.println(AstXmlPrinter.print(astNode));

    assertThat(astNode.getType()).isSameAs(realAstNodeType);
    assertThat(astNode.getName()).isEqualTo("rule");
    assertThat(astNode.getFromIndex()).isZero();
    assertThat(astNode.getToIndex()).isEqualTo(6);
    assertThat(astNode.hasChildren()).isTrue();

    assertThat(astNode.getTokens()).hasSize(2);
    var token = astNode.getTokens().get(0);
    assertThat(astNode.getToken()).isSameAs(token);
    assertThat(token.getValue()).isEqualTo("foo");
    assertThat(token.getOriginalValue()).isEqualTo("foo");
    assertThat(token.getLine()).isEqualTo(1);
    assertThat(token.getColumn()).isZero();
    assertThat(token.getType()).isSameAs(AstCreator.UNDEFINED_TOKEN_TYPE);
    assertThat(token.getType().getName()).isEqualTo("TOKEN");

    token = astNode.getTokens().get(1);
    assertThat(token.getValue()).isEqualTo("bar");
    assertThat(token.getOriginalValue()).isEqualTo("bar");
    assertThat(token.getLine()).isEqualTo(1);
    assertThat(token.getColumn()).isEqualTo(3);
    assertThat(token.getType()).isSameAs(AstCreator.UNDEFINED_TOKEN_TYPE);
  }

  @Test
  void shouldSkipNodes() {
    var input = "foo".toCharArray();

    var ruleMatcher1 = mockRuleMatcher("rule1");
    when(ruleMatcher1.hasToBeSkippedFromAst(Mockito.any(AstNode.class))).thenReturn(true);
    var ruleMatcher2 = mockRuleMatcher("rule2");
    var realAstNodeType = mock(AstNodeType.class);
    when(ruleMatcher2.getRealAstNodeType()).thenReturn(realAstNodeType);

    var node = new ParseNode(0, 3, Collections.<ParseNode>emptyList(), ruleMatcher1);
    var parseTreeRoot = new ParseNode(0, 3, Arrays.asList(node), ruleMatcher2);

    InputBuffer inputBuffer = new ImmutableInputBuffer(input);
    var parsingResult = new ParsingResult(inputBuffer, true, parseTreeRoot, null);

    var astNode = AstCreator.create(parsingResult, new LocatedText(null, input));
    System.out.println(AstXmlPrinter.print(astNode));

    assertThat(astNode.getType()).isSameAs(realAstNodeType);
    assertThat(astNode.getName()).isEqualTo("rule2");
    assertThat(astNode.getFromIndex()).isZero();
    assertThat(astNode.getToIndex()).isEqualTo(3);
    assertThat(astNode.hasChildren()).isFalse();
    assertThat(astNode.getToken()).isNull();
  }

  private static MutableParsingRule mockRuleMatcher(String name) {
    return when(mock(MutableParsingRule.class).getName()).thenReturn(name).getMock();
  }

  private static TokenExpression mockTokenMatcher(TokenType tokenType) {
    return when(mock(TokenExpression.class).getTokenType()).thenReturn(tokenType).getMock();
  }

}
