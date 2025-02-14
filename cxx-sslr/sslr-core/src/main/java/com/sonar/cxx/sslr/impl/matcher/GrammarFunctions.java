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
package com.sonar.cxx.sslr.impl.matcher;

import com.sonar.cxx.sslr.api.TokenType;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeClassExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenValueExpression;

/**
 * @deprecated in 1.19, use {@link org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder} instead.
 */
@Deprecated(since = "1.19")
final class GrammarFunctions {

  private GrammarFunctions() {
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
    if (e instanceof String string) {
      expression = new TokenValueExpression(string);
    } else if (e instanceof TokenType tokenType) {
      expression = new TokenTypeExpression(tokenType);
    } else if (e instanceof RuleDefinition ruleDefinition) {
      expression = ruleDefinition;
    } else if (e instanceof Class aClass) {
      expression = new TokenTypeClassExpression(aClass);
    } else if (e instanceof ParsingExpression parsingExpression) {
      expression = parsingExpression;
    } else {
      throw new IllegalArgumentException(
        "The matcher object can't be anything else than a Rule, Matcher, String, TokenType or Class. Object = " + e);
    }
    return expression;
  }

  private static void checkSize(@Nullable Object[] e) {
    if (e == null || e.length == 0) {
      throw new IllegalArgumentException("You must define at least one matcher.");
    }
  }

}
