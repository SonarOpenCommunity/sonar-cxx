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
package com.sonar.cxx.sslr.test.minic; // cxx: in use

import static com.sonar.cxx.sslr.api.GenericTokenType.EOF;
import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.api.Grammar;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.BREAK;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.CONTINUE;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.ELSE;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.IF;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.INT;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.RETURN;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.STRUCT;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.VOID;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Keywords.WHILE;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Literals.INTEGER;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.ADD;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.BRACE_L;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.BRACE_R;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.COMMA;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.DEC;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.DIV;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.EQ;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.EQEQ;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.GT;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.GTE;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.INC;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.LT;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.LTE;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.MUL;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.NE;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.PAREN_L;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.PAREN_R;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.SEMICOLON;
import static com.sonar.cxx.sslr.test.minic.MiniCLexer.Punctuators.SUB;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder;

public enum MiniCGrammar implements GrammarRuleKey {

  BIN_TYPE,
  BIN_FUNCTION_DEFINITION,
  BIN_PARAMETER,
  BIN_VARIABLE_DEFINITION,
  BIN_FUNCTION_REFERENCE,
  BIN_VARIABLE_REFERENCE,
  COMPILATION_UNIT,
  DEFINITION,
  STRUCT_DEFINITION,
  STRUCT_MEMBER,
  FUNCTION_DEFINITION,
  VARIABLE_DEFINITION,
  PARAMETERS_LIST,
  PARAMETER_DECLARATION,
  COMPOUND_STATEMENT,
  VARIABLE_INITIALIZER,
  ARGUMENT_EXPRESSION_LIST,
  STATEMENT,
  EXPRESSION_STATEMENT,
  RETURN_STATEMENT,
  CONTINUE_STATEMENT,
  BREAK_STATEMENT,
  IF_STATEMENT,
  WHILE_STATEMENT,
  CONDITION_CLAUSE,
  ELSE_CLAUSE,
  NO_COMPLEXITY_STATEMENT,
  EXPRESSION,
  ASSIGNMENT_EXPRESSION,
  RELATIONAL_EXPRESSION,
  RELATIONAL_OPERATOR,
  ADDITIVE_EXPRESSION,
  ADDITIVE_OPERATOR,
  MULTIPLICATIVE_EXPRESSION,
  MULTIPLICATIVE_OPERATOR,
  UNARY_EXPRESSION,
  UNARY_OPERATOR,
  POSTFIX_EXPRESSION,
  POSTFIX_OPERATOR,
  PRIMARY_EXPRESSION;

  public static Grammar create() {
    var b = LexerfulGrammarBuilder.create();

    // Bins
    b.rule(BIN_TYPE).is(b.firstOf(
      INT,
      VOID));

    b.rule(BIN_PARAMETER).is(IDENTIFIER);

    b.rule(BIN_FUNCTION_DEFINITION).is(IDENTIFIER);

    b.rule(BIN_VARIABLE_DEFINITION).is(IDENTIFIER);

    b.rule(BIN_FUNCTION_REFERENCE).is(IDENTIFIER);

    b.rule(BIN_VARIABLE_REFERENCE).is(IDENTIFIER);

    // Miscellaneous
    b.rule(COMPILATION_UNIT).is(b.zeroOrMore(DEFINITION), EOF);

    b.rule(DEFINITION).is(b.firstOf(
      STRUCT_DEFINITION,
      FUNCTION_DEFINITION,
      VARIABLE_DEFINITION));

    b.rule(STRUCT_DEFINITION).is(STRUCT, IDENTIFIER, BRACE_L, b.oneOrMore(STRUCT_MEMBER, SEMICOLON), BRACE_R);

    b.rule(STRUCT_MEMBER).is(BIN_TYPE, IDENTIFIER);

    b.rule(FUNCTION_DEFINITION).is(BIN_TYPE, BIN_FUNCTION_DEFINITION, PAREN_L, b.optional(PARAMETERS_LIST), PAREN_R,
                                   COMPOUND_STATEMENT);

    b.rule(VARIABLE_DEFINITION).is(BIN_TYPE, BIN_VARIABLE_DEFINITION, b.optional(VARIABLE_INITIALIZER), SEMICOLON);

    b.rule(PARAMETERS_LIST).is(PARAMETER_DECLARATION, b.zeroOrMore(COMMA, PARAMETER_DECLARATION));

    b.rule(PARAMETER_DECLARATION).is(BIN_TYPE, BIN_PARAMETER);

    b.rule(COMPOUND_STATEMENT).is(BRACE_L, b.zeroOrMore(VARIABLE_DEFINITION), b.zeroOrMore(STATEMENT), BRACE_R);

    b.rule(VARIABLE_INITIALIZER).is(EQ, EXPRESSION);

    b.rule(ARGUMENT_EXPRESSION_LIST).is(EXPRESSION, b.zeroOrMore(COMMA, EXPRESSION));

    // Statements
    b.rule(STATEMENT).is(b.firstOf(
      EXPRESSION_STATEMENT,
      COMPOUND_STATEMENT,
      RETURN_STATEMENT,
      CONTINUE_STATEMENT,
      BREAK_STATEMENT,
      IF_STATEMENT,
      WHILE_STATEMENT,
      NO_COMPLEXITY_STATEMENT));

    b.rule(EXPRESSION_STATEMENT).is(EXPRESSION, SEMICOLON);

    b.rule(RETURN_STATEMENT).is(RETURN, EXPRESSION, SEMICOLON);

    b.rule(CONTINUE_STATEMENT).is(CONTINUE, SEMICOLON);

    b.rule(BREAK_STATEMENT).is(BREAK, SEMICOLON);

    b.rule(IF_STATEMENT).is(IF, CONDITION_CLAUSE, STATEMENT, b.optional(ELSE_CLAUSE));

    b.rule(WHILE_STATEMENT).is(WHILE, CONDITION_CLAUSE, STATEMENT);

    b.rule(CONDITION_CLAUSE).is(PAREN_L, EXPRESSION, PAREN_R);

    b.rule(ELSE_CLAUSE).is(ELSE, STATEMENT);

    b.rule(NO_COMPLEXITY_STATEMENT).is("nocomplexity", STATEMENT);

    // Expressions
    b.rule(EXPRESSION).is(ASSIGNMENT_EXPRESSION);

    b.rule(ASSIGNMENT_EXPRESSION).is(RELATIONAL_EXPRESSION, b.optional(EQ, RELATIONAL_EXPRESSION)).skipIfOneChild();

    b.rule(RELATIONAL_EXPRESSION).is(ADDITIVE_EXPRESSION, b.optional(RELATIONAL_OPERATOR, RELATIONAL_EXPRESSION))
      .skipIfOneChild();

    b.rule(RELATIONAL_OPERATOR).is(b.firstOf(
      EQEQ,
      NE,
      LT,
      LTE,
      GT,
      GTE));

    b.rule(ADDITIVE_EXPRESSION).is(MULTIPLICATIVE_EXPRESSION, b.optional(ADDITIVE_OPERATOR, ADDITIVE_EXPRESSION))
      .skipIfOneChild();

    b.rule(ADDITIVE_OPERATOR).is(b.firstOf(
      ADD,
      SUB));

    b.rule(MULTIPLICATIVE_EXPRESSION).is(UNARY_EXPRESSION, b
                                         .optional(MULTIPLICATIVE_OPERATOR, MULTIPLICATIVE_EXPRESSION)).skipIfOneChild();

    b.rule(MULTIPLICATIVE_OPERATOR).is(b.firstOf(
      MUL,
      DIV));

    b.rule(UNARY_EXPRESSION).is(b.firstOf(
      b.sequence(UNARY_OPERATOR, PRIMARY_EXPRESSION),
      POSTFIX_EXPRESSION)).skipIfOneChild();

    b.rule(UNARY_OPERATOR).is(b.firstOf(
      INC,
      DEC));

    b.rule(POSTFIX_EXPRESSION).is(b.firstOf(
      b.sequence(PRIMARY_EXPRESSION, POSTFIX_OPERATOR),
      b.sequence(BIN_FUNCTION_REFERENCE, PAREN_L, b.optional(ARGUMENT_EXPRESSION_LIST), PAREN_R),
      PRIMARY_EXPRESSION)).skipIfOneChild();

    b.rule(POSTFIX_OPERATOR).is(b.firstOf(
      INC,
      DEC));

    b.rule(PRIMARY_EXPRESSION).is(b.firstOf(
      INTEGER,
      BIN_VARIABLE_REFERENCE,
      b.sequence(PAREN_L, EXPRESSION, PAREN_R)));

    b.setRootRule(COMPILATION_UNIT);

    return b.build();
  }

}
