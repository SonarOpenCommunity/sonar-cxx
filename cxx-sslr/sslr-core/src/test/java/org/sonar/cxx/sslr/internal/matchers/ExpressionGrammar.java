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
package org.sonar.cxx.sslr.internal.matchers;

import com.sonar.cxx.sslr.api.Rule;
import org.sonar.cxx.sslr.parser.LexerlessGrammar;

import static org.sonar.cxx.sslr.parser.GrammarOperators.commentTrivia;
import static org.sonar.cxx.sslr.parser.GrammarOperators.endOfInput;
import static org.sonar.cxx.sslr.parser.GrammarOperators.firstOf;
import static org.sonar.cxx.sslr.parser.GrammarOperators.regexp;
import static org.sonar.cxx.sslr.parser.GrammarOperators.zeroOrMore;

public class ExpressionGrammar extends LexerlessGrammar {

  Rule whitespace;
  Rule endOfInput;

  Rule plus;
  Rule minus;
  Rule div;
  Rule mul;
  Rule number;
  Rule variable;
  Rule lpar;
  Rule rpar;

  Rule root;
  Rule expression;
  Rule term;
  Rule factor;
  Rule parens;

  public ExpressionGrammar() {
    whitespace.is(commentTrivia(regexp("\\s*+"))).skip();

    plus.is('+', whitespace);
    minus.is('-', whitespace);
    div.is('/', whitespace);
    mul.is('*', whitespace);
    number.is(regexp("[0-9]++"), whitespace);
    variable.is(regexp("\\p{javaJavaIdentifierStart}++\\p{javaJavaIdentifierPart}*+"), whitespace);
    lpar.is('(', whitespace);
    rpar.is(')', whitespace);
    endOfInput.is(endOfInput());

    // If in part of grammar below we will replace
    // plus, minus, div, mul, lpar and rpar by punctuators '+', '-', '/', '*', '(' and ')' respectively,
    // number by GenericTokenType.CONSTANT, variable by GenericTokenType.IDENTIFIER
    // and remove space
    // then it will look exactly as it was with lexer:
    root.is(whitespace, expression, endOfInput);
    expression.is(term, zeroOrMore(firstOf(plus, minus), term));
    term.is(factor, zeroOrMore(firstOf(div, mul), factor));
    factor.is(firstOf(number, parens, variable));
    parens.is(lpar, expression, rpar);
  }

  @Override
  public Rule getRootRule() {
    return root;
  }

}
