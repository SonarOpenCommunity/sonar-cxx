/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
package com.sonar.cxx.sslr.test.minic;

import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.impl.Lexer;
import static com.sonar.cxx.sslr.test.lexer.LexerConditions.*;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.*;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Literals.INTEGER;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.*;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

class MiniCLexerTest {

  Lexer lexer = MiniCLexer.create();

  @Test
  void lexIdentifiers() {
    AssertionsForClassTypes.assertThat(lexer.lex("abc")).has(hasToken("abc", IDENTIFIER));
    AssertionsForClassTypes.assertThat(lexer.lex("abc0")).has(hasToken("abc0", IDENTIFIER));
    AssertionsForClassTypes.assertThat(lexer.lex("abc_0")).has(hasToken("abc_0", IDENTIFIER));
    AssertionsForClassTypes.assertThat(lexer.lex("i")).has(hasToken("i", IDENTIFIER));
  }

  @Test
  void lexIntegers() {
    AssertionsForClassTypes.assertThat(lexer.lex("0")).has(hasToken("0", INTEGER));
    AssertionsForClassTypes.assertThat(lexer.lex("000")).has(hasToken("000", INTEGER));
    AssertionsForClassTypes.assertThat(lexer.lex("1234")).has(hasToken("1234", INTEGER));
  }

  @Test
  void lexKeywords() {
    AssertionsForClassTypes.assertThat(lexer.lex("int")).has(hasToken(INT));
    AssertionsForClassTypes.assertThat(lexer.lex("void")).has(hasToken(VOID));
    AssertionsForClassTypes.assertThat(lexer.lex("return")).has(hasToken(RETURN));
    AssertionsForClassTypes.assertThat(lexer.lex("if")).has(hasToken(IF));
    AssertionsForClassTypes.assertThat(lexer.lex("else")).has(hasToken(ELSE));
    AssertionsForClassTypes.assertThat(lexer.lex("while")).has(hasToken(WHILE));
    AssertionsForClassTypes.assertThat(lexer.lex("break")).has(hasToken(BREAK));
    AssertionsForClassTypes.assertThat(lexer.lex("continue")).has(hasToken(CONTINUE));
    AssertionsForClassTypes.assertThat(lexer.lex("struct")).has(hasToken(STRUCT));
  }

  @Test
  void lexComments() {
    AssertionsForClassTypes.assertThat(lexer.lex("/*test*/")).has(hasComment("/*test*/"));
    AssertionsForClassTypes.assertThat(lexer.lex("/*test*/*/")).has(hasComment("/*test*/"));
    AssertionsForClassTypes.assertThat(lexer.lex("/*test/* /**/")).has(hasComment("/*test/* /**/"));
    AssertionsForClassTypes.assertThat(lexer.lex("/*test1\ntest2\ntest3*/")).has(hasComment("/*test1\ntest2\ntest3*/"));
  }

  @Test
  void lexPunctuators() {
    AssertionsForClassTypes.assertThat(lexer.lex("(")).has(hasToken(PAREN_L));
    AssertionsForClassTypes.assertThat(lexer.lex(")")).has(hasToken(PAREN_R));
    AssertionsForClassTypes.assertThat(lexer.lex("{")).has(hasToken(BRACE_L));
    AssertionsForClassTypes.assertThat(lexer.lex("}")).has(hasToken(BRACE_R));
    AssertionsForClassTypes.assertThat(lexer.lex("=")).has(hasToken(EQ));
    AssertionsForClassTypes.assertThat(lexer.lex(",")).has(hasToken(COMMA));
    AssertionsForClassTypes.assertThat(lexer.lex(";")).has(hasToken(SEMICOLON));
    AssertionsForClassTypes.assertThat(lexer.lex("+")).has(hasToken(ADD));
    AssertionsForClassTypes.assertThat(lexer.lex("-")).has(hasToken(SUB));
    AssertionsForClassTypes.assertThat(lexer.lex("*")).has(hasToken(MUL));
    AssertionsForClassTypes.assertThat(lexer.lex("/")).has(hasToken(DIV));
    AssertionsForClassTypes.assertThat(lexer.lex("<")).has(hasToken(LT));
    AssertionsForClassTypes.assertThat(lexer.lex("<=")).has(hasToken(LTE));
    AssertionsForClassTypes.assertThat(lexer.lex(">")).has(hasToken(GT));
    AssertionsForClassTypes.assertThat(lexer.lex(">=")).has(hasToken(GTE));
    AssertionsForClassTypes.assertThat(lexer.lex("==")).has(hasToken(EQEQ));
    AssertionsForClassTypes.assertThat(lexer.lex("!=")).has(hasToken(NE));
    AssertionsForClassTypes.assertThat(lexer.lex("++")).has(hasToken(INC));
    AssertionsForClassTypes.assertThat(lexer.lex("--")).has(hasToken(DEC));
  }

}
