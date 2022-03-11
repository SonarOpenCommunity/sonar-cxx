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
package com.sonar.cxx.sslr.impl.matcher;

import com.sonar.cxx.sslr.api.TokenType;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
import org.sonar.cxx.sslr.internal.vm.NothingExpression;
import org.sonar.cxx.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.OptionalExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.ZeroOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.AnyTokenExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TillNewLineExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeClassExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypesExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenValueExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokensBridgeExpression;

public class GrammarFunctionsTest {

  @Test
  public void test() {
    var e1 = mock(ParsingExpression.class);
    var e2 = mock(ParsingExpression.class);

    var rule = mock(RuleDefinition.class);
    assertThat(GrammarFunctions.Standard.and(rule)).isSameAs(rule);
    assertThat(GrammarFunctions.Standard.and(e1)).isSameAs(e1);
    assertThat(GrammarFunctions.Standard.and(e1, e2)).isInstanceOf(SequenceExpression.class);
    assertThat(GrammarFunctions.Standard.and("foo")).isInstanceOf(TokenValueExpression.class);
    assertThat(GrammarFunctions.Standard.and(mock(TokenType.class))).isInstanceOf(TokenTypeExpression.class);
    assertThat(GrammarFunctions.Standard.and(Object.class)).isInstanceOf(TokenTypeClassExpression.class);

    assertThat(GrammarFunctions.Standard.firstOf(e1)).isSameAs(e1);
    assertThat(GrammarFunctions.Standard.firstOf(e1, e2)).isInstanceOf(FirstOfExpression.class);

    assertThat(GrammarFunctions.Standard.or(e1)).isSameAs(e1);
    assertThat(GrammarFunctions.Standard.or(e1, e2)).isInstanceOf(FirstOfExpression.class);

    assertThat(GrammarFunctions.Standard.opt(e1)).isInstanceOf(OptionalExpression.class);

    assertThat(GrammarFunctions.Standard.o2n(e1)).isInstanceOf(ZeroOrMoreExpression.class);

    assertThat(GrammarFunctions.Standard.one2n(e1)).isInstanceOf(OneOrMoreExpression.class);

    assertThat(GrammarFunctions.Predicate.next(e1)).isInstanceOf(NextExpression.class);

    assertThat(GrammarFunctions.Predicate.not(e1)).isInstanceOf(NextNotExpression.class);

    assertThat(GrammarFunctions.Advanced.isTrue()).as("singleton").isSameAs(AnyTokenExpression.INSTANCE);

    assertThat(GrammarFunctions.Advanced.isFalse()).as("singleton").isSameAs(NothingExpression.INSTANCE);

    assertThat(GrammarFunctions.Advanced.tillNewLine()).as("singleton").isSameAs(TillNewLineExpression.INSTANCE);

    assertThat(GrammarFunctions.Advanced.bridge(mock(TokenType.class), mock(TokenType.class))).isInstanceOf(
      TokensBridgeExpression.class);

    assertThat(GrammarFunctions.Advanced.isOneOfThem(mock(TokenType.class), mock(TokenType.class))).isInstanceOf(
      TokenTypesExpression.class);

    assertThat(GrammarFunctions.Advanced.adjacent(e1).toString()).isEqualTo("Sequence[Adjacent, " + e1 + "]");

    assertThat(GrammarFunctions.Advanced.anyTokenButNot(e1).toString()).isEqualTo("Sequence[NextNot[" + e1
                                                                                    + "], AnyToken]");

    assertThat(GrammarFunctions.Advanced.till(e1).toString()).isEqualTo("Sequence[ZeroOrMore[Sequence[NextNot[" + e1
                                                                          + "], AnyToken]], " + e1 + "]");

    assertThat(GrammarFunctions.Advanced.exclusiveTill(e1).toString()).isEqualTo("ZeroOrMore[Sequence[NextNot[" + e1
                                                                                   + "], AnyToken]]");
    assertThat(GrammarFunctions.Advanced.exclusiveTill(e1, e2).toString()).isEqualTo(
      "ZeroOrMore[Sequence[NextNot[FirstOf[" + e1 + ", " + e2 + "]], AnyToken]]");
  }

  @Test
  public void firstOf_requires_at_least_one_argument() {
    var thrown = catchThrowableOfType(GrammarFunctions.Standard::firstOf,
                                  IllegalArgumentException.class);
    assertThat(thrown).hasMessage("You must define at least one matcher.");
  }

  @Test
  public void and_requires_at_least_one_argument() {
    var thrown = catchThrowableOfType(GrammarFunctions.Standard::and,
                                  IllegalArgumentException.class);
    assertThat(thrown).hasMessage("You must define at least one matcher.");
  }

  @Test
  public void isOneOfThem_requires_at_least_one_argument() {
    var thrown = catchThrowableOfType(GrammarFunctions.Advanced::isOneOfThem,
                                  IllegalArgumentException.class);
    assertThat(thrown).hasMessage("You must define at least one matcher.");
  }

  @Test
  public void test_incorrect_type_of_parsing_expression() {
    var thrown = catchThrowableOfType(() -> GrammarFunctions.Standard.and(new Object()),
                                  IllegalArgumentException.class);
    assertThat(thrown).hasMessageStartingWith(
      "The matcher object can't be anything else than a Rule, Matcher, String, TokenType or Class. Object = java.lang.Object@");
  }

  @Test
  public void private_constructors() throws Exception {
    assertThat(hasPrivateConstructor(GrammarFunctions.class)).isTrue();
    assertThat(hasPrivateConstructor(GrammarFunctions.Standard.class)).isTrue();
    assertThat(hasPrivateConstructor(GrammarFunctions.Predicate.class)).isTrue();
    assertThat(hasPrivateConstructor(GrammarFunctions.Advanced.class)).isTrue();
  }

  private static final boolean hasPrivateConstructor(Class cls) throws Exception {
    var constructor = cls.getDeclaredConstructor();
    var result = !constructor.isAccessible();
    constructor.setAccessible(true);
    constructor.newInstance();
    return result;
  }

}
