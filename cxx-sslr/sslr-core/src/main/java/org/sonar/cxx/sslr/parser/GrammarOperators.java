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
import java.util.Objects;
import javax.annotation.Nonnull;
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

/**
 * @since 1.16
 * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder} instead.
 */
public final class GrammarOperators {

  private GrammarOperators() {
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#sequence(Object, Object)}
   * instead.
   */
  public static Object sequence(Object... e) {
    return convertToSingleExpression(e);
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#firstOf(Object, Object)}
   * instead.
   */
  public static Object firstOf(@Nonnull Object... e) {
    Objects.requireNonNull(e);

    if (e.length == 1) {
      return convertToExpression(e[0]);
    }
    return new FirstOfExpression(convertToExpressions(e));
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#optional(Object)} instead.
   */
  public static Object optional(Object... e) {
    return new OptionalExpression(convertToSingleExpression(e));
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#oneOrMore(Object)} instead.
   */
  public static Object oneOrMore(Object... e) {
    return new OneOrMoreExpression(convertToSingleExpression(e));
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#zeroOrMore(Object)} instead.
   */
  public static Object zeroOrMore(Object... e) {
    return new ZeroOrMoreExpression(convertToSingleExpression(e));
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#next(Object)} instead.
   */
  public static Object next(Object... e) {
    return new NextExpression(convertToSingleExpression(e));
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#nextNot(Object)} instead.
   */
  public static Object nextNot(Object... e) {
    return new NextNotExpression(convertToSingleExpression(e));
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#regexp(String)} instead.
   */
  public static Object regexp(String regexp) {
    return new PatternExpression(regexp);
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#endOfInput()} instead.
   */
  public static Object endOfInput() {
    return EndOfInputExpression.INSTANCE;
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#nothing()} instead.
   */
  public static Object nothing() {
    return NothingExpression.INSTANCE;
  }

  /**
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#token(TokenType, Object)}
   * instead.
   */
  public static Object token(TokenType tokenType, Object e) {
    return new TokenExpression(tokenType, convertToExpression(e));
  }

  /**
   * @since 1.17
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#commentTrivia(Object)} instead.
   */
  public static Object commentTrivia(Object e) {
    return new TriviaExpression(TriviaKind.COMMENT, convertToExpression(e));
  }

  /**
   * @since 1.17
   * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder#skippedTrivia(Object)} instead.
   */
  public static Object skippedTrivia(Object e) {
    return new TriviaExpression(TriviaKind.SKIPPED_TEXT, convertToExpression(e));
  }

  private static ParsingExpression convertToSingleExpression(@Nonnull Object... elements) {
    Objects.requireNonNull(elements);

    if (elements.length == 1) {
      return convertToExpression(elements[0]);
    }
    return new SequenceExpression(convertToExpressions(elements));
  }

  private static ParsingExpression[] convertToExpressions(@Nonnull Object... elements) {
    Objects.requireNonNull(elements);
    if (elements.length <= 0) {
      throw new IllegalArgumentException();
    }

    var matchers = new ParsingExpression[elements.length];
    for (int i = 0; i < matchers.length; i++) {
      matchers[i] = convertToExpression(elements[i]);
    }
    return matchers;
  }

  private static ParsingExpression convertToExpression(@Nonnull Object e) {
    Objects.requireNonNull(e);

    if (e instanceof ParsingExpression parsingExpression) {
      return parsingExpression;
    } else if (e instanceof String string) {
      return new StringExpression(string);
    } else if (e instanceof Character character) {
      return new StringExpression(character.toString());
    } else {
      throw new IllegalArgumentException("Incorrect type of parsing expression: " + e.getClass().toString());
    }
  }

}
