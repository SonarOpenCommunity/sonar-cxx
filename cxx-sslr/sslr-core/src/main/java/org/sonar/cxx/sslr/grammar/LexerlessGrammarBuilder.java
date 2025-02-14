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
package org.sonar.cxx.sslr.grammar; // cxx: in use

import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.sonar.cxx.sslr.internal.grammar.MutableGrammar;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;
import org.sonar.cxx.sslr.internal.vm.EndOfInputExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.PatternExpression;
import org.sonar.cxx.sslr.internal.vm.StringExpression;
import org.sonar.cxx.sslr.internal.vm.TokenExpression;
import org.sonar.cxx.sslr.internal.vm.TriviaExpression;
import org.sonar.cxx.sslr.parser.LexerlessGrammar;

/**
 * A builder for creating <a href="http://en.wikipedia.org/wiki/Parsing_expression_grammar">Parsing Expression
 * Grammars</a> for lexerless parsing.
 * <p>
 * Objects of following types can be used as an atomic parsing expressions:
 * <ul>
 * <li>GrammarRuleKey</li>
 * <li>String</li>
 * <li>Character</li>
 * </ul>
 *
 * @since 1.18
 * @see LexerfulGrammarBuilder
 */
public final class LexerlessGrammarBuilder extends GrammarBuilder {

  private final Map<GrammarRuleKey, MutableParsingRule> definitions = new HashMap<>();
  private GrammarRuleKey rootRuleKey;

  private LexerlessGrammarBuilder() {
  }

  public static LexerlessGrammarBuilder create() {
    return new LexerlessGrammarBuilder();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GrammarRuleBuilder rule(GrammarRuleKey ruleKey) {
    var rule = definitions.computeIfAbsent(ruleKey, MutableParsingRule::new);
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
   */
  public LexerlessGrammar build() {
    for (var rule : definitions.values()) {
      if (rule.getExpression() == null) {
        throw new GrammarException("The rule '" + rule.getRuleKey() + "' hasn't been defined.");
      }
    }
    return new MutableGrammar(definitions, rootRuleKey);
  }

  /**
   * Creates parsing expression based on regular expression.
   *
   * @param regexp regular expression
   * @throws java.util.regex.PatternSyntaxException if the expression's syntax is invalid
   */
  public Object regexp(String regexp) {
    return new PatternExpression(regexp);
  }

  /**
   * Creates parsing expression - "end of input". This expression succeeds only if parser reached end of input.
   */
  public Object endOfInput() {
    return EndOfInputExpression.INSTANCE;
  }

  /**
   * Creates parsing expression - "token".
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public Object token(TokenType tokenType, Object e) {
    return new TokenExpression(tokenType, convertToExpression(e));
  }

  /**
   * Creates parsing expression - "comment trivia".
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public Object commentTrivia(Object e) {
    return new TriviaExpression(TriviaKind.COMMENT, convertToExpression(e));
  }

  /**
   * Creates parsing expression - "skipped trivia".
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public Object skippedTrivia(Object e) {
    return new TriviaExpression(TriviaKind.SKIPPED_TEXT, convertToExpression(e));
  }

  @Override
  protected ParsingExpression convertToExpression(@Nonnull Object e) {
    Objects.requireNonNull(e, "Parsing expression can't be null");
    ParsingExpression result;
    if (e instanceof ParsingExpression parsingExpression) {
      result = parsingExpression;
    } else if (e instanceof GrammarRuleKey ruleKey) {
      rule(ruleKey);
      result = definitions.get(ruleKey);
    } else if (e instanceof String string) {
      result = new StringExpression(string);
    } else if (e instanceof Character character) {
      result = new StringExpression(character.toString());
    } else {
      throw new IllegalArgumentException("Incorrect type of parsing expression: " + e.getClass().toString());
    }
    return result;
  }

}
