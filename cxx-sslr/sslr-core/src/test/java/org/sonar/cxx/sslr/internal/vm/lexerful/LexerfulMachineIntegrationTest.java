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
package org.sonar.cxx.sslr.internal.vm.lexerful;

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.sslr.internal.vm.CompilationHandler;
import org.sonar.cxx.sslr.internal.vm.Machine;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;

class LexerfulMachineIntegrationTest {

  private Token[] tokens;

  @Test
  void tokenType() {
    var instructions = new TokenTypeExpression(GenericTokenType.IDENTIFIER).compile(new CompilationHandler());
    assertThat(Machine.execute(instructions, token(GenericTokenType.IDENTIFIER))).isTrue();
    assertThat(Machine.execute(instructions, token(GenericTokenType.LITERAL))).isFalse();
  }

  @Test
  void tokenTypes() {
    var instructions = new TokenTypesExpression(GenericTokenType.IDENTIFIER, GenericTokenType.LITERAL)
      .compile(new CompilationHandler());
    tokens = new Token[]{token(GenericTokenType.IDENTIFIER)};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
    tokens = new Token[]{token(GenericTokenType.LITERAL)};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
    tokens = new Token[]{token(GenericTokenType.UNKNOWN_CHAR)};
    assertThat(Machine.execute(instructions, tokens)).isFalse();
  }

  @Test
  void tokenValue() {
    var instructions = new TokenValueExpression("foo").compile(new CompilationHandler());
    assertThat(Machine.execute(instructions, token("foo"))).isTrue();
    assertThat(Machine.execute(instructions, token("bar"))).isFalse();
  }

  @Test
  void anyToken() {
    var instructions = AnyTokenExpression.INSTANCE.compile(new CompilationHandler());
    assertThat(Machine.execute(instructions, token("foo"))).isTrue();
  }

  @Test
  void tokensBridge() {
    var instructions = new TokensBridgeExpression(GenericTokenType.IDENTIFIER, GenericTokenType.LITERAL)
      .compile(new CompilationHandler());
    tokens = new Token[]{token(GenericTokenType.IDENTIFIER), token(GenericTokenType.LITERAL)};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
    tokens = new Token[]{token(GenericTokenType.IDENTIFIER), token(GenericTokenType.IDENTIFIER), token(
                         GenericTokenType.LITERAL)};
    assertThat(Machine.execute(instructions, tokens)).isFalse();
    tokens = new Token[]{token(GenericTokenType.IDENTIFIER), token(GenericTokenType.IDENTIFIER), token(
                         GenericTokenType.LITERAL), token(GenericTokenType.LITERAL)};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
    tokens = new Token[]{token(GenericTokenType.IDENTIFIER), token(GenericTokenType.UNKNOWN_CHAR), token(
                         GenericTokenType.LITERAL)};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
  }

  @Test
  void tokenTypeClass() {
    var instructions = new TokenTypeClassExpression(GenericTokenType.class).compile(new CompilationHandler());
    tokens = new Token[]{token(GenericTokenType.IDENTIFIER)};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
  }

  @Test
  void adjacent() {
    var instructions = new SequenceExpression(
      new TokenValueExpression("foo"),
      AdjacentExpression.INSTANCE,
      new TokenValueExpression("bar")).compile(new CompilationHandler());
    tokens = new Token[]{token(1, 1, "foo"), token(1, 4, "bar")};
    assertThat(Machine.execute(instructions, tokens)).isTrue();
    tokens = new Token[]{token(1, 1, "foo"), token(1, 5, "bar")};
    assertThat(Machine.execute(instructions, tokens)).isFalse();
  }

  private static Token token(TokenType type) {
    return when(mock(Token.class).getType()).thenReturn(type).getMock();
  }

  private static Token token(String value) {
    return when(mock(Token.class).getValue()).thenReturn(value).getMock();
  }

  private static Token token(int line, int column, String value) {
    var token = mock(Token.class);
    when(token.getLine()).thenReturn(line);
    when(token.getColumn()).thenReturn(column);
    when(token.getValue()).thenReturn(value);
    return token;
  }

}
