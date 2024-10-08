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
package org.sonar.cxx.sslr.parser;

import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import java.lang.reflect.Constructor;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.internal.vm.EndOfInputExpression;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
import org.sonar.cxx.sslr.internal.vm.NothingExpression;
import org.sonar.cxx.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.OptionalExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.PatternExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.StringExpression;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;
import org.sonar.cxx.sslr.internal.vm.TriviaExpression;
import org.sonar.cxx.sslr.internal.vm.ZeroOrMoreExpression;

class GrammarOperatorsTest {

  @Test
  void test() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);

    assertThat(GrammarOperators.sequence(e1)).isSameAs(e1);
    assertThat(GrammarOperators.sequence(e1, e2)).isInstanceOf(SequenceExpression.class);
    assertThat(GrammarOperators.sequence("foo")).isInstanceOf(StringExpression.class);
    assertThat(GrammarOperators.sequence('f')).isInstanceOf(StringExpression.class);

    assertThat(GrammarOperators.firstOf(e1)).isSameAs(e1);
    assertThat(GrammarOperators.firstOf(e1, e2)).isInstanceOf(FirstOfExpression.class);

    assertThat(GrammarOperators.optional(e1)).isInstanceOf(OptionalExpression.class);

    assertThat(GrammarOperators.oneOrMore(e1)).isInstanceOf(OneOrMoreExpression.class);

    assertThat(GrammarOperators.zeroOrMore(e1)).isInstanceOf(ZeroOrMoreExpression.class);

    assertThat(GrammarOperators.next(e1)).isInstanceOf(NextExpression.class);

    assertThat(GrammarOperators.nextNot(e1)).isInstanceOf(NextNotExpression.class);

    assertThat(GrammarOperators.regexp("foo")).isInstanceOf(PatternExpression.class);

    assertThat(GrammarOperators.endOfInput()).isInstanceOf(EndOfInputExpression.class);

    assertThat(GrammarOperators.nothing()).isInstanceOf(NothingExpression.class);
  }

  @Test
  void test_token() {
    var tokenType = mock(TokenType.class);
    var e = mock(ParsingExpression.class);
    var result = GrammarOperators.token(tokenType, e);
    assertThat(result).isInstanceOf(TokenExpression.class);
    assertThat(((TokenExpression) result).getTokenType()).isSameAs(tokenType);
  }

  @Test
  void test_commentTrivia() {
    var e = mock(ParsingExpression.class);
    var result = GrammarOperators.commentTrivia(e);
    assertThat(result).isInstanceOf(TriviaExpression.class);
    assertThat(((TriviaExpression) result).getTriviaKind()).isEqualTo(TriviaKind.COMMENT);
  }

  @Test
  void test_skippedTrivia() {
    var e = mock(ParsingExpression.class);
    var result = GrammarOperators.skippedTrivia(e);
    assertThat(result).isInstanceOf(TriviaExpression.class);
    assertThat(((TriviaExpression) result).getTriviaKind()).isEqualTo(TriviaKind.SKIPPED_TEXT);
  }

  @Test
  void illegal_argument() {
    var thrown = catchThrowableOfType(IllegalArgumentException.class,
      () -> GrammarOperators.sequence(new Object())
    );
    assertThat(thrown).hasMessage("Incorrect type of parsing expression: class java.lang.Object");
  }

  @Test
  void private_constructor() throws Exception {
    Constructor constructor = GrammarOperators.class.getDeclaredConstructor();
    assertThat(constructor.canAccess(null)).isFalse();
    constructor.setAccessible(true);
    constructor.newInstance();
  }

}
