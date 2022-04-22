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

import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.TokenType;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
import org.sonar.cxx.sslr.internal.vm.NothingExpression;
import org.sonar.cxx.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.OptionalExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.ZeroOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.AdjacentExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.AnyTokenExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TillNewLineExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeClassExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypesExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenValueExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokensBridgeExpression;

/**
 * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder} instead.
 */
@Deprecated(since = "1.19")
public final class GrammarFunctions {

  private GrammarFunctions() {
  }

  public static final class Standard {

    private Standard() {
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#zeroOrMore(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher o2n(Object... e) {
      return new ZeroOrMoreExpression(convertToSingleExpression(e));
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#oneOrMore(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher one2n(Object... e) {
      return new OneOrMoreExpression(convertToSingleExpression(e));
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#optional(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher opt(Object... e) {
      return new OptionalExpression(convertToSingleExpression(e));
    }

    /**
     * @deprecated in 1.16, use {@link GrammarFunctions.Standard#firstOf(Object...)} instead
     */
    @Deprecated(since = "1.16")
    public static Matcher or(Object... e) {
      return firstOf(e);
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#firstOf(Object, Object)}
     * instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher firstOf(Object... e) {
      checkSize(e);
      if (e.length == 1) {
        return convertToExpression(e[0]);
      } else {
        return new FirstOfExpression(convertToExpressions(e));
      }
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#sequence(Object, Object)}
     * instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher and(Object... e) {
      return convertToSingleExpression(e);
    }

  }

  public static final class Predicate {

    private Predicate() {
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#nextNot(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher not(Object e) {
      return new NextNotExpression(convertToExpression(e));
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#next(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher next(Object... e) {
      return new NextExpression(convertToSingleExpression(e));
    }

  }

  public static final class Advanced {

    private Advanced() {
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#adjacent(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher adjacent(Object e) {
      return new SequenceExpression(AdjacentExpression.INSTANCE, convertToExpression(e));
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#anyTokenButNot(Object)}
     * instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher anyTokenButNot(Object e) {
      return new SequenceExpression(new NextNotExpression(convertToExpression(e)), AnyTokenExpression.INSTANCE);
    }

    /**
     * @deprecated in 1.19, use
     * {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#isOneOfThem(TokenType, TokenType...)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher isOneOfThem(TokenType... types) {
      checkSize(types);
      return new TokenTypesExpression(types);
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#bridge(TokenType, TokenType)}
     * instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher bridge(TokenType from, TokenType to) {
      return new TokensBridgeExpression(from, to);
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#anyToken()} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher isTrue() {
      return AnyTokenExpression.INSTANCE;
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#nothing()} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher isFalse() {
      return NothingExpression.INSTANCE;
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#anyToken()} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher anyToken() {
      return AnyTokenExpression.INSTANCE;
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#tillNewLine()} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher tillNewLine() {
      return TillNewLineExpression.INSTANCE;
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#till(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher till(Object e) {
      var expression = convertToExpression(e);
      return new SequenceExpression(
        new ZeroOrMoreExpression(
          new SequenceExpression(
            new NextNotExpression(expression),
            AnyTokenExpression.INSTANCE)),
        expression);
    }

    /**
     * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#exclusiveTill(Object)} instead.
     */
    @Deprecated(since = "1.19")
    public static Matcher exclusiveTill(Object... e) {
      var expressions = convertToExpressions(e);
      var subExpression = expressions.length == 1 ? expressions[0] : new FirstOfExpression(expressions);
      return new ZeroOrMoreExpression(
        new SequenceExpression(
          new NextNotExpression(
            subExpression),
          AnyTokenExpression.INSTANCE));
    }

  }

  /**
   * @since 1.14
   * @deprecated in 1.19, use
   * {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder#buildWithMemoizationOfMatchesForAllRules()} instead.
   */
  @Deprecated(since = "1.19")
  public static void enableMemoizationOfMatchesForAllRules(Grammar grammar) {
    for (var ruleField : Grammar.getAllRuleFields(grammar.getClass())) {
      var ruleName = ruleField.getName();
      RuleDefinition rule;
      try {
        rule = (RuleDefinition) ruleField.get(grammar);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Unable to enable memoization for rule '" + ruleName + "'", e);
      }
      rule.enableMemoization();
    }
  }

  static ParsingExpression convertToSingleExpression(Object[] e) {
    checkSize(e);
    if (e.length == 1) {
      return convertToExpression(e[0]);
    } else {
      return new SequenceExpression(convertToExpressions(e));
    }
  }

  private static ParsingExpression[] convertToExpressions(Object[] e) {
    checkSize(e);
    var matchers = new ParsingExpression[e.length];
    for (int i = 0; i < matchers.length; i++) {
      matchers[i] = convertToExpression(e[i]);
    }
    return matchers;
  }

  private static ParsingExpression convertToExpression(Object e) {
    ParsingExpression expression;
    if (e instanceof String) {
      expression = new TokenValueExpression((String) e);
    } else if (e instanceof TokenType) {
      var tokenType = (TokenType) e;
      expression = new TokenTypeExpression(tokenType);
    } else if (e instanceof RuleDefinition) {
      expression = (RuleDefinition) e;
    } else if (e instanceof Class) {
      expression = new TokenTypeClassExpression((Class) e);
    } else if (e instanceof ParsingExpression) {
      expression = (ParsingExpression) e;
    } else {
      throw new IllegalArgumentException(
        "The matcher object can't be anything else than a Rule, Matcher, String, TokenType or Class. Object = " + e);
    }
    return expression;
  }

  private static void checkSize(Object[] e) {
    if (e == null || e.length == 0) {
      throw new IllegalArgumentException("You must define at least one matcher.");
    }
  }

}
