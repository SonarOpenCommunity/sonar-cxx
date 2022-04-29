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
package org.sonar.cxx.sslr.grammar; // cxx: in use

import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.sonar.cxx.sslr.internal.grammar.MutableGrammar;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
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
 * A builder for creating <a href="http://en.wikipedia.org/wiki/Parsing_expression_grammar">Parsing Expression
 * Grammars</a> for lexerful parsing.
 * {@link com.sonar.cxx.sslr.impl.Lexer} is required for parsers of such grammars.
 * <p>
 * Objects of following types can be used as an atomic parsing expressions:
 * <ul>
 * <li>GrammarRuleKey</li>
 * <li>TokenType</li>
 * <li>String</li>
 * </ul>
 *
 * @since 1.18
 * @see LexerlessGrammarBuilder
 */
public class LexerfulGrammarBuilder extends GrammarBuilder {

  private final Map<GrammarRuleKey, RuleDefinition> definitions = new HashMap<>();
  private GrammarRuleKey rootRuleKey;

  public static LexerfulGrammarBuilder create() {
    return new LexerfulGrammarBuilder();
  }

  private LexerfulGrammarBuilder() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GrammarRuleBuilder rule(GrammarRuleKey ruleKey) {
    var rule = definitions.get(ruleKey);
    if (rule == null) {
      rule = new RuleDefinition(ruleKey);
      definitions.put(ruleKey, rule);
    }
    return new RuleBuilder(this, rule);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRootRule(GrammarRuleKey ruleKey) {
    rule(ruleKey);
    rootRuleKey = ruleKey;
  }

  /**
   * Constructs grammar.
   *
   * @throws GrammarException if some of rules were used, but not defined
   * @return grammar
   * @see #buildWithMemoizationOfMatchesForAllRules()
   */
  public Grammar build() {
    for (var rule : definitions.values()) {
      if (rule.getExpression() == null) {
        throw new GrammarException("The rule '" + rule.getRuleKey() + "' hasn't been defined.");
      }
    }
    return new MutableGrammar(definitions, rootRuleKey);
  }

  /**
   * Constructs grammar with memoization of matches for all rules.
   *
   * @throws GrammarException if some of rules were used, but not defined
   * @return grammar
   * @see #build()
   */
  public Grammar buildWithMemoizationOfMatchesForAllRules() {
    for (var rule : definitions.values()) {
      rule.enableMemoization();
    }
    return build();
  }

  /**
   * Creates parsing expression - "adjacent".
   * During execution of this expression parser will execute sub-expression only if there is no space between next and
   * previous tokens.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public Object adjacent(Object e) {
    return new SequenceExpression(AdjacentExpression.INSTANCE, convertToExpression(e));
  }

  /**
   * Creates parsing expression - "any token but not".
   * Equivalent of expression {@code sequence(nextNot(e), anyToken())}
   * Do not overuse this method.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public Object anyTokenButNot(Object e) {
    return new SequenceExpression(new NextNotExpression(convertToExpression(e)), AnyTokenExpression.INSTANCE);
  }

  /**
   * Creates parsing expression - "is one of them".
   * During execution of this expression parser will consume following token only if its type belongs to the provided
   * list.
   * Equivalent of expression {@code firstOf(t1, rest)}.
   * Do not overuse this method.
   *
   * @param t1 first type of token
   * @param rest rest of types
   */
  public Object isOneOfThem(TokenType t1, TokenType... rest) {
    var types = new TokenType[1 + rest.length];
    types[0] = t1;
    System.arraycopy(rest, 0, types, 1, rest.length);
    return new TokenTypesExpression(types);
  }

  /**
   * Creates parsing expression - "bridge".
   * Equivalent of:
   * <pre>
   *   rule(bridge).is(
   *     from,
   *     zeroOrMore(firstOf(
   *       sequence(nextNot(firstOf(from, to)), anyToken()),
   *       bridge
   *     )),
   *     to
   *   ).skip()
   * </pre>
   * Do not overuse this expression.
   */
  public Object bridge(TokenType from, TokenType to) {
    return new TokensBridgeExpression(from, to);
  }

  /**
   * @deprecated in 1.19, use {@link #anyToken()} instead.
   */
  @Deprecated(since = "1.19")
  public Object everything() {
    return AnyTokenExpression.INSTANCE;
  }

  /**
   * Creates parsing expression - "any token".
   * During execution of this expression parser will unconditionally consume following token.
   * This expression fails, if end of input reached.
   */
  public Object anyToken() {
    return AnyTokenExpression.INSTANCE;
  }

  /**
   * Creates parsing expression - "till new line".
   * During execution of this expression parser will consume all following tokens, which are on the current line.
   * This expression always succeeds.
   * Do not overuse this expression.
   */
  public Object tillNewLine() {
    return TillNewLineExpression.INSTANCE;
  }

  /**
   * Creates parsing expression - "till".
   * Equivalent of expression {@code sequence(zeroOrMore(nextNot(e), anyToken()), e)}.
   * Do not overuse this method.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public Object till(Object e) {
    // TODO repeated expression
    var expression = convertToExpression(e);
    return new SequenceExpression(
      new ZeroOrMoreExpression(
        new SequenceExpression(
          new NextNotExpression(expression),
          AnyTokenExpression.INSTANCE)),
      expression);
  }

  /**
   * Creates parsing expression - "exclusive till".
   * Equivalent of expression {@code zeroOrMore(nextNot(e), anyToken())}.
   * Do not overuse this method.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   */
  public Object exclusiveTill(Object e) {
    return new ZeroOrMoreExpression(
      new SequenceExpression(
        new NextNotExpression(convertToExpression(e)),
        AnyTokenExpression.INSTANCE));
  }

  /**
   * Creates parsing expression - "exclusive till".
   * Equivalent of expression {@code zeroOrMore(nextNot(firstOf(e, rest)), anyToken())}.
   * Do not overuse this method.
   *
   * @param e1 first sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   */
  public Object exclusiveTill(Object e1, Object... rest) {
    return exclusiveTill(new FirstOfExpression(convertToExpressions(e1, rest)));
  }

  @Override
  protected ParsingExpression convertToExpression(@Nonnull Object e) {
    Objects.requireNonNull(e, "Parsing expression can't be null");
    final ParsingExpression result;
    if (e instanceof ParsingExpression) {
      result = (ParsingExpression) e;
    } else if (e instanceof GrammarRuleKey) {
      var ruleKey = (GrammarRuleKey) e;
      rule(ruleKey);
      result = definitions.get(ruleKey);
    } else if (e instanceof TokenType) {
      result = new TokenTypeExpression((TokenType) e);
    } else if (e instanceof String) {
      result = new TokenValueExpression((String) e);
    } else if (e instanceof Class) {
      result = new TokenTypeClassExpression((Class) e);
    } else {
      throw new IllegalArgumentException("Incorrect type of parsing expression: " + e.getClass().toString());
    }
    return result;
  }

}
