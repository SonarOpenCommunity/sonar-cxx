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
package org.sonar.cxx.sslr.internal.matchers;

import com.sonar.cxx.sslr.impl.ast.AstXmlPrinter;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.parser.ParseErrorFormatter;
import org.sonar.cxx.sslr.parser.ParseRunner;

class ExpressionGrammarTest {

  private ExpressionGrammar grammar;

  @BeforeEach
  public void setUp() {
    grammar = new ExpressionGrammar();
  }

  @Test
  void match() {
    var inputString = "20 * ( 2 + 2 ) - var";
    var input = inputString.toCharArray();
    var parseRunner = new ParseRunner(grammar.root);
    var result = parseRunner.parse(input);
    assertThat(result.isMatched()).isTrue();
    ParseTreePrinter.print(result.getParseTreeRoot(), input);
    assertThat(ParseTreePrinter.leafsToString(result.getParseTreeRoot(), input)).as("full-fidelity").isEqualTo(
      inputString);
  }

  @Test
  void mismatch() {
    var inputString = "term +";
    var input = inputString.toCharArray();
    var parseRunner = new ParseRunner(grammar.root);
    var result = parseRunner.parse(input);
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.print(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isEqualTo(6);
  }

  @Test
  void prefix_match() {
    var inputString = "term +";
    var input = inputString.toCharArray();
    var parseRunner = new ParseRunner(grammar.expression);
    var result = parseRunner.parse(input);
    assertThat(result.isMatched()).isTrue();
  }

  @Test
  void should_mock() {
    var inputString = "term plus term";
    var input = inputString.toCharArray();
    grammar.term.mock();
    grammar.plus.mock();
    var parseRunner = new ParseRunner(grammar.root);
    var result = parseRunner.parse(input);
    assertThat(result.isMatched()).isTrue();
    ParseTreePrinter.print(result.getParseTreeRoot(), input);
    assertThat(ParseTreePrinter.leafsToString(result.getParseTreeRoot(), input)).as("full-fidelity").isEqualTo(
      inputString);
  }

  @Test
  void should_create_ast() throws Exception {
    var inputString = "20 * 2 + 2 - var";
    var grammar = new ExpressionGrammar();
    var input = inputString.toCharArray();
    var parseRunner = new ParseRunner(grammar.root);
    var result = parseRunner.parse(input);

    var astNode = AstCreator.create(result, new LocatedText(null, inputString.toCharArray()));
    System.out.println(astNode.getTokens());
    System.out.println(AstXmlPrinter.print(astNode));

    assertThat(astNode.getTokens()).hasSize(7);

    var firstToken = astNode.getToken();
    assertThat(firstToken.getLine()).isEqualTo(1);
    assertThat(firstToken.getColumn()).isZero();
    assertThat(firstToken.getValue()).isEqualTo("20");
    assertThat(firstToken.getOriginalValue()).isEqualTo("20");

    var tokenWithTrivia = astNode.getFirstDescendant(grammar.mul).getToken();
    assertThat(tokenWithTrivia).isNotNull();
    assertThat(tokenWithTrivia.getLine()).isEqualTo(1);
    assertThat(tokenWithTrivia.getColumn()).isEqualTo(3);
    assertThat(tokenWithTrivia.getTrivia()).hasSize(1);
    assertThat(tokenWithTrivia.getValue()).isEqualTo("*");
    assertThat(tokenWithTrivia.getOriginalValue()).isEqualTo("*");
  }

}
