/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import static com.sonar.cxx.sslr.api.GenericTokenType.*;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PPGeneratedTokenTest {

  @Test
  void testNewGeneratedTokenFromExistingToken() {
    Token token = TokenUtils.tokenBuilder(IDENTIFIER, "Token", 1, 2);
    Token result = PPGeneratedToken.build(token);
    assertThat(result.isGeneratedCode()).isTrue();
  }

  @Test
  void testNewGeneratedTokenWithNewValueAndType() {
    Token token = TokenUtils.tokenBuilder(IDENTIFIER, "Token", 1, 2);
    Token result = PPGeneratedToken.build(token, CONSTANT, "3");

    assertThat(result.isGeneratedCode()).isTrue();
    assertThat(result.getLine()).isEqualTo(1);
    assertThat(result.getColumn()).isEqualTo(2);
    assertThat(result.getType()).isEqualTo(CONSTANT);
    assertThat(result.getValue()).isEqualTo("3");
  }

  @Test
  void testNewGeneratedTokenWithNewPosition() throws URISyntaxException {
    Token token = TokenUtils.tokenBuilder(IDENTIFIER, "Token", 1, 2);
    var uri = new URI("tests://sample");
    Token result = PPGeneratedToken.build(token, uri, 3, 4);

    assertThat(result.isGeneratedCode()).isTrue();
    assertThat(result.getLine()).isEqualTo(3);
    assertThat(result.getColumn()).isEqualTo(4);
    assertThat(result.getType()).isEqualTo(IDENTIFIER);
    assertThat(result.getValue()).isEqualTo("Token");
    assertThat(result.getURI()).isEqualTo(uri);
  }

  @Test
  void testNewGeneratedToken() {
    Token result = PPGeneratedToken.build(CONSTANT, "3", 1, 2);

    assertThat(result.isGeneratedCode()).isTrue();
    assertThat(result.getLine()).isEqualTo(1);
    assertThat(result.getColumn()).isEqualTo(2);
    assertThat(result.getType()).isEqualTo(CONSTANT);
    assertThat(result.getValue()).isEqualTo("3");
  }

  @Test
  void testMarkAllAsGenerated() {
    List<Token> tokens = PPLexer.create().lex("A B");
    List<Token> result = PPGeneratedToken.markAllAsGenerated(tokens);

    assertThat(result)
      .hasSize(3)
      .matches(t -> t.get(0).isGeneratedCode())
      .matches(t -> t.get(1).isGeneratedCode())
      .matches(t -> t.get(2).isGeneratedCode());
  }

}
