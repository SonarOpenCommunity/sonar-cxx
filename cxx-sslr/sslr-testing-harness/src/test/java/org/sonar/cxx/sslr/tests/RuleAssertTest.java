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
package org.sonar.cxx.sslr.tests;

import com.sonar.cxx.sslr.api.Rule;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.internal.grammar.MutableParsingRule;

class RuleAssertTest {

  private Rule rule;

  @BeforeEach
  public void setUp() {
    rule = new MutableParsingRule("ruleName").is("foo");
  }

  @Test
  void ok() {
    new RuleAssert(rule)
      .matches("foo")
      .notMatches("bar");
  }

  @Test
  void testMatchesFailure() {
    var thrown = catchThrowableOfType(ParsingResultComparisonFailure.class,
      () -> new RuleAssert(rule).matches("bar")
    );
    assertThat(thrown).hasMessageContaining("Rule 'ruleName' should match:\nbar");
  }

  @Test
  void testNotMatchesFailure() {
    var thrown = catchThrowableOfType(AssertionError.class,
      () -> new RuleAssert(rule).notMatches("foo")
    );
    assertThat(thrown).hasMessage("Rule 'ruleName' should not match:\nfoo");
  }

  @Test
  void shouldNotAcceptNull() {
    var thrown = catchThrowableOfType(AssertionError.class,
      () -> new RuleAssert((Rule) null).matches("")
    );
    assertThat(thrown).hasMessageContaining("Expecting actual not to be null");
  }

  @Test
  void notMatchesShouldNotAcceptPrefixMatch() {
    new RuleAssert(rule)
      .notMatches("foo bar");
  }

  @Test
  void matchesPrefixOk() {
    new RuleAssert(rule)
      .matchesPrefix("foo", " bar");
  }

  @Test
  void matchesPrefixFullMistmatch() {
    var thrown = catchThrowableOfType(ParsingResultComparisonFailure.class,
      () -> new RuleAssert(rule).matchesPrefix("bar", " baz")
    );
    assertThat(thrown).hasMessageContaining("Rule 'ruleName' should match:\nbar\nwhen followed by:\n baz");
  }

  @Test
  void matchesPrefixWrongPrefix() {
    var thrown = catchThrowableOfType(ParsingResultComparisonFailure.class,
      () -> new RuleAssert(rule).matchesPrefix("foo bar", " baz")
    );
    assertThat(thrown).hasMessage("Rule 'ruleName' should match:\nfoo bar\nwhen followed by:\n baz\nbut matched:\nfoo");
  }

}
