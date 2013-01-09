/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Rule;
import org.sonar.cxx.api.CxxTokenType;

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.anyToken;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.not;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.one2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or;
import static org.sonar.cxx.api.CppKeyword.DEFINE;
import static org.sonar.cxx.api.CppKeyword.INCLUDE;
import static org.sonar.cxx.api.CppKeyword.IFDEF;
import static org.sonar.cxx.api.CppKeyword.IFNDEF;
import static org.sonar.cxx.api.CppKeyword.IF;
import static org.sonar.cxx.api.CxxTokenType.WS;

/**
 * The rules are a subset of those found in the C++ Standard, A.14 "Preprocessor directives"
 */
public class CppGrammar extends Grammar {
  public Rule preprocessor_line;
  public Rule define_line;
  public Rule include_line;
  public Rule ifdef_line;
  public Rule ifndef_line;
  public Rule replacement_list;
  public Rule argument_list;
  public Rule pp_token;
  public Rule if_line;
  public Rule constant_expression;
  public Rule primary_expression;
  public Rule unary_expression;
  public Rule unary_operator;
  public Rule multiplicative_expression;
  public Rule additive_expression;
  public Rule shift_expression;
  public Rule relational_expression;
  public Rule equality_expression;
  public Rule and_expression;
  public Rule exclusive_or_expression;
  public Rule inclusive_or_expression;
  public Rule logical_and_expression;
  public Rule logical_or_expression;
  public Rule conditional_expression;
  public Rule expression;
  public Rule bool;
  public Rule literal;
  public Rule defined_expression;
  public Rule functionlike_macro;
  public Rule functionlike_macro_definition;
  public Rule objectlike_macro_definition;

  public CppGrammar() {
    preprocessor_line.is(
      or(
        define_line,
        include_line,
        ifdef_line,
        ifndef_line,
        if_line
        )
      );

    define_line.is(
      or(
        functionlike_macro_definition,
        objectlike_macro_definition
        )
      );

    functionlike_macro_definition.is(
      or(
        and(DEFINE, WS, pp_token, "(", opt(WS), opt(argument_list), opt(WS), ")", WS, replacement_list),
        and(DEFINE, WS, pp_token, "(", opt(WS), "...", opt(WS), ")", WS, replacement_list),
        and(DEFINE, WS, pp_token, "(", opt(WS), argument_list, opt(WS), ",", opt(WS), "...", opt(WS), ")", WS, replacement_list)
        )
      );

    objectlike_macro_definition.is(
      or(
        and(DEFINE, WS, pp_token, opt(and(WS, replacement_list)))
        )
      );

    replacement_list.is(
      o2n(
        or(
          "##",
          "#",
            pp_token
          )
        )
      );

    argument_list.is(IDENTIFIER, o2n(opt(WS), ",", opt(WS), IDENTIFIER));
    pp_token.is(anyToken());

    include_line.is(
      INCLUDE, WS,
      or(
        and("<", one2n(not(">"), opt(WS), pp_token), ">"),
        CxxTokenType.STRING
        )
      );

    ifdef_line.is(IFDEF, WS, IDENTIFIER);
    ifndef_line.is(IFNDEF, WS, IDENTIFIER);

    if_line.is(IF, WS, constant_expression);
    if_expressions();
  }

  void if_expressions(){
    constant_expression.is(conditional_expression);

    conditional_expression.is(
      or(
        and(logical_or_expression, opt(WS), "?", opt(WS), expression, opt(WS), ":", opt(WS), conditional_expression),
        logical_or_expression
        )
      ).skipIfOneChild();

    logical_or_expression.is(logical_and_expression, o2n(opt(WS), "||", opt(WS), logical_and_expression)).skipIfOneChild();

    logical_and_expression.is(inclusive_or_expression, o2n(opt(WS), "&&", opt(WS), inclusive_or_expression)).skipIfOneChild();

    inclusive_or_expression.is(exclusive_or_expression, o2n(opt(WS), "|", opt(WS), exclusive_or_expression)).skipIfOneChild();

    exclusive_or_expression.is(and_expression, o2n(opt(WS), "^", opt(WS), and_expression)).skipIfOneChild();

    and_expression.is(equality_expression, o2n(opt(WS), "&", opt(WS), equality_expression)).skipIfOneChild();

    equality_expression.is(relational_expression, o2n(opt(WS), or("==", "!="), opt(WS), relational_expression)).skipIfOneChild();

    relational_expression.is(shift_expression, o2n(opt(WS), or("<", ">", "<=", ">="), opt(WS), shift_expression)).skipIfOneChild();

    shift_expression.is(additive_expression, o2n(opt(WS), or("<<", ">>"), opt(WS), additive_expression)).skipIfOneChild();

    additive_expression.is(multiplicative_expression, o2n(opt(WS), or("+", "-"), opt(WS), multiplicative_expression)).skipIfOneChild();

    multiplicative_expression.is(unary_expression, o2n(opt(WS), or("*", "/", "%"), opt(WS), unary_expression)).skipIfOneChild();

    unary_expression.is(
      or(
        and(unary_operator, opt(WS), multiplicative_expression),
        primary_expression
        )
      ).skipIfOneChild();

    unary_operator.is(
      or("+", "-", "!", "~")
      );

    primary_expression.is(
      or(
        literal,
        and("(", opt(WS), expression, opt(WS), ")"),
        defined_expression,
        functionlike_macro,
        IDENTIFIER
        )
      ).skipIfOneChild();

    literal.is(
      or(
        CxxTokenType.CHARACTER,
        CxxTokenType.STRING,
        CxxTokenType.NUMBER,
        bool
        )
      );

    bool.is(
      or(
        "true",
        "false"
        )
      );

    expression.is(conditional_expression, o2n(opt(WS), ",", opt(WS), conditional_expression));

    defined_expression.is(
      "defined", WS,
      or(
        and("(", opt(WS), IDENTIFIER, opt(WS), ")"),
        IDENTIFIER
        )
      );

    functionlike_macro.is(IDENTIFIER, opt(WS), "(", opt(WS), argument_list, opt(WS), ")");
  }

  @Override
  public Rule getRootRule() {
    return preprocessor_line;
  }
}
