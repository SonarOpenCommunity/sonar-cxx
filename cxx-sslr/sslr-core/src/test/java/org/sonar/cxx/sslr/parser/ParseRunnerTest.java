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
package org.sonar.cxx.sslr.parser;

import com.sonar.cxx.sslr.api.GenericTokenType;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;

class ParseRunnerTest {

  @Test
  void should_not_accept_null() {
    var thrown = catchThrowableOfType(
      () -> new ParseRunner(null),
      NullPointerException.class);
    assertThat(thrown).isExactlyInstanceOf(NullPointerException.class);
  }

  @Test
  void should_report_error_at_rule_level() {
    var rule = new MutableParsingRule("rule").is("foo", "bar");
    var runner = new ParseRunner(rule);
    var result = runner.parse("foo".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isEqualTo(3);
  }

  @Test
  void should_report_error_at_end_of_input() {
    var endOfInput = new MutableParsingRule("endOfInput").is(GrammarOperators.endOfInput());
    var rule = new MutableParsingRule("rule").is("foo", endOfInput);
    var runner = new ParseRunner(rule);
    var result = runner.parse("foo bar".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isEqualTo(3);
  }

  @Test
  void should_not_report_error_inside_of_predicate_not() {
    var subRule = new MutableParsingRule("subRule").is("foo");
    var rule = new MutableParsingRule("rule").is(GrammarOperators.nextNot(subRule), "bar");
    var runner = new ParseRunner(rule);
    var result = runner.parse("baz".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isZero();
  }

  @Test
  void should_report_error_at_correct_index() {
    var rule = new MutableParsingRule("rule").is(GrammarOperators.nextNot("foo"));
    var runner = new ParseRunner(rule);
    var result = runner.parse("foo".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isZero();
  }

  @Test
  void should_report_error_inside_of_predicate_next() {
    var subRule = new MutableParsingRule("subRule").is("foo");
    var rule = new MutableParsingRule("rule").is(GrammarOperators.next(subRule), "bar");
    var runner = new ParseRunner(rule);
    var result = runner.parse("baz".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isZero();
  }

  @Test
  void should_not_report_error_inside_of_token() {
    var subRule = new MutableParsingRule("subRule").is("foo");
    var rule = new MutableParsingRule("rule").is(GrammarOperators.token(GenericTokenType.IDENTIFIER, subRule), "bar");
    var runner = new ParseRunner(rule);
    var result = runner.parse("baz".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isZero();
  }

  @Test
  void should_not_report_error_inside_of_trivia() {
    var subRule = new MutableParsingRule("subRule").is("foo");
    var rule = new MutableParsingRule("rule").is(GrammarOperators.skippedTrivia(subRule), "bar");
    var runner = new ParseRunner(rule);
    var result = runner.parse("baz".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isZero();
  }

  @Test
  void should_report_error_at_several_paths() {
    var subRule1 = new MutableParsingRule("subRule1").is("foo");
    var subRule2 = new MutableParsingRule("subRule2").is("bar");
    var rule = new MutableParsingRule("rule").is(GrammarOperators.firstOf(subRule1, subRule2));
    var runner = new ParseRunner(rule);
    var result = runner.parse("baz".toCharArray());
    assertThat(result.isMatched()).isFalse();
    var parseError = result.getParseError();
    System.out.println(new ParseErrorFormatter().format(parseError));
    assertThat(parseError.getErrorIndex()).isZero();
  }

}
