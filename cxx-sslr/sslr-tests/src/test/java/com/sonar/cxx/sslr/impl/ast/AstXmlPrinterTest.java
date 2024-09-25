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
package com.sonar.cxx.sslr.impl.ast;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AstXmlPrinterTest {

  @Test
  void testPrintRuleAstNode() {
    var token = mock(Token.class);
    when(token.getType()).thenReturn(new WordTokenType());
    when(token.getValue()).thenReturn("word");
    when(token.getOriginalValue()).thenReturn("word");
    when(token.getLine()).thenReturn(34);
    when(token.getColumn()).thenReturn(12);

    var root = new AstNode(new RuleDefinition("expr"), "expr", token);

    assertThat(AstXmlPrinter.print(root)).isEqualTo("<expr tokenValue=\"word\" tokenLine=\"34\" tokenColumn=\"12\"/>");
  }

  @Test
  void testPrintWordAstNode() {
    var token = mock(Token.class);
    when(token.getType()).thenReturn(new WordTokenType());
    when(token.getValue()).thenReturn("myword");
    when(token.getLine()).thenReturn(1);
    when(token.getColumn()).thenReturn(1);
    var root = new AstNode(token);
    assertThat(AstXmlPrinter.print(root)).isEqualTo("<WORD tokenValue=\"myword\" tokenLine=\"1\" tokenColumn=\"1\"/>");
  }

  @Test
  void testPrintFullAstNode() {
    var astNode = new AstNode(new RuleDefinition("expr"), "expr", null);

    var tokenX = mock(Token.class);
    when(tokenX.getType()).thenReturn(new WordTokenType());
    when(tokenX.getValue()).thenReturn("x");
    when(tokenX.getLine()).thenReturn(1);
    when(tokenX.getColumn()).thenReturn(1);
    astNode.addChild(new AstNode(tokenX));

    var tokenEq = mock(Token.class);
    when(tokenEq.getType()).thenReturn(new WordTokenType());
    when(tokenEq.getValue()).thenReturn("=");
    when(tokenEq.getLine()).thenReturn(1);
    when(tokenEq.getColumn()).thenReturn(1);
    astNode.addChild(new AstNode(tokenEq));

    var token4 = mock(Token.class);
    when(token4.getType()).thenReturn(new WordTokenType());
    when(token4.getValue()).thenReturn("4");
    when(token4.getLine()).thenReturn(1);
    when(token4.getColumn()).thenReturn(1);
    astNode.addChild(new AstNode(token4));

    var tokenWord = mock(Token.class);
    when(tokenWord.getType()).thenReturn(new WordTokenType());
    when(tokenWord.getValue()).thenReturn("WORD");
    when(tokenWord.getLine()).thenReturn(1);
    when(tokenWord.getColumn()).thenReturn(1);
    astNode.addChild(new AstNode(tokenWord));

    var expectedResult = new StringBuilder()
      .append("<expr>\n")
      .append("  <WORD tokenValue=\"x\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
      .append("  <WORD tokenValue=\"=\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
      .append("  <WORD tokenValue=\"4\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
      .append("  <WORD tokenValue=\"WORD\" tokenLine=\"1\" tokenColumn=\"1\"/>\n")
      .append("</expr>")
      .toString();
    assertThat(AstXmlPrinter.print(astNode)).isEqualTo(expectedResult);
  }

  private static class WordTokenType implements TokenType {

    @Override
    public String getName() {
      return "WORD";
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

    @Override
    public String getValue() {
      return "WORDS";
    }

  }

}
