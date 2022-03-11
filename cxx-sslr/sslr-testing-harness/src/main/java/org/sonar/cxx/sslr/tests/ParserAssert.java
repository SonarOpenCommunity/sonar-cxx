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
package org.sonar.cxx.sslr.tests; // cxx: in use

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.impl.Parser;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import org.assertj.core.api.AbstractAssert;
import org.sonar.cxx.sslr.internal.vm.EndOfInputExpression;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.lexerful.TokenTypeExpression;
import org.sonar.cxx.sslr.tests.RuleAssert.EndOfInput;
import org.sonar.cxx.sslr.tests.RuleAssert.WithEndOfInput;

/**
 * To create a new instance of this class invoke <code>{@link Assertions#assertThat(Parser)}</code>.
 *
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.</p>
 *
 * @since 1.16
 */
public class ParserAssert extends AbstractAssert<ParserAssert, Parser> {

  public ParserAssert(Parser actual) {
    super(actual, ParserAssert.class);
  }

  private Parser createParserWithEofMatcher() {
    var rule = actual.getRootRule();
    var endOfInput = new RuleDefinition(new EndOfInput())
      .is(new FirstOfExpression(EndOfInputExpression.INSTANCE, new TokenTypeExpression(GenericTokenType.EOF)));
    var withEndOfInput = new RuleDefinition(new WithEndOfInput(actual.getRootRule().getRuleKey()))
      .is(rule, endOfInput);

    var parser = Parser.builder(actual).build();
    parser.setRootRule(withEndOfInput);

    return parser;
  }

  /**
   * Verifies that the actual <code>{@link Parser}</code> fully matches a given input.
   *
   * @return this assertion object.
   */
  public ParserAssert matches(String input) {
    isNotNull();
    hasRootRule();
    var parser = createParserWithEofMatcher();
    var expected = "Rule '" + getRuleName() + "' should match:\n" + input;
    try {
      parser.parse(input);
    } catch (RecognitionException e) {
      var actual = e.getMessage();
      throw new ParsingResultComparisonFailure(expected, actual);
    }
    return this;
  }

  /**
   * Verifies that the actual <code>{@link Parser}</code> not matches a given input.
   *
   * @return this assertion object.
   */
  public ParserAssert notMatches(String input) {
    isNotNull();
    hasRootRule();
    var parser = createParserWithEofMatcher();
    try {
      parser.parse(input);
    } catch (RecognitionException e) {
      // expected
      return this;
    }
    throw new AssertionError("Rule '" + getRuleName() + "' should not match:\n" + input);
  }

  private void hasRootRule() {
    Assertions.assertThat(actual.getRootRule())
      .overridingErrorMessage("Root rule of the parser should not be null")
      .isNotNull();
  }

  private String getRuleName() {
    return actual.getRootRule().getName();
  }

}
