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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeSkippingPolicy;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.impl.ast.AlwaysSkipFromAst;
import com.sonar.cxx.sslr.impl.ast.NeverSkipFromAst;
import com.sonar.cxx.sslr.impl.ast.SkipFromAstIfOnlyOneChild;
import com.sonar.cxx.sslr.impl.matcher.GrammarFunctions.Standard;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.cxx.sslr.internal.vm.CompilationHandler;
import org.sonar.cxx.sslr.internal.vm.Instruction;
import org.sonar.cxx.sslr.internal.vm.MemoParsingExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.RuleRefExpression;

/**
 * <p>This class is not intended to be instantiated or subclassed by clients.</p>
 */
public class RuleDefinition implements Rule, AstNodeSkippingPolicy, GrammarRuleKey, CompilableGrammarRule, MemoParsingExpression {

  private final GrammarRuleKey ruleKey;
  private final String name;
  private ParsingExpression expression;
  private AstNodeType astNodeSkippingPolicy = NeverSkipFromAst.INSTANCE;
  private boolean memoize = false;

  public RuleDefinition(String name) {
    this.ruleKey = this;
    this.name = name;
  }

  public RuleDefinition(GrammarRuleKey ruleKey) {
    this.ruleKey = ruleKey;
    this.name = ruleKey.toString();
  }

  public String getName() {
    return name;
  }

  @Override
  public RuleDefinition is(Object... e) {
    throwExceptionIfRuleAlreadyDefined("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
    throwExceptionIfEmptyListOfMatchers(e);
    setExpression(GrammarFunctions.convertToSingleExpression(e));
    return this;
  }

  @Override
  public RuleDefinition override(Object... e) {
    throwExceptionIfEmptyListOfMatchers(e);
    setExpression(GrammarFunctions.convertToSingleExpression(e));
    return this;
  }

  @Override
  public void mock() {
    setExpression((ParsingExpression) Standard.firstOf(getName(), getName().toUpperCase()));
  }

  @Override
  public void skip() {
    astNodeSkippingPolicy = AlwaysSkipFromAst.INSTANCE;
  }

  @Override
  public void skipIfOneChild() {
    astNodeSkippingPolicy = SkipFromAstIfOnlyOneChild.INSTANCE;
  }

  private void throwExceptionIfRuleAlreadyDefined(String exceptionMessage) {
    if (getExpression() != null) {
      throw new IllegalStateException(exceptionMessage);
    }
  }

  private void throwExceptionIfEmptyListOfMatchers(Object[] matchers) {
    if (matchers.length == 0) {
      throw new IllegalStateException("The rule '" + ruleKey + "' should at least contains one matcher.");
    }
  }

  @Override
  public boolean hasToBeSkippedFromAst(AstNode node) {
    if (AstNodeSkippingPolicy.class.isAssignableFrom(astNodeSkippingPolicy.getClass())) {
      return ((AstNodeSkippingPolicy) astNodeSkippingPolicy).hasToBeSkippedFromAst(node);
    }
    return false;
  }

  /**
   * @since 1.18
   */
  public AstNodeType getRealAstNodeType() {
    return ruleKey;
  }

  @Override
  public GrammarRuleKey getRuleKey() {
    return ruleKey;
  }

  @Override
  public ParsingExpression getExpression() {
    return expression;
  }

  @Override
  public void setExpression(ParsingExpression expression) {
    this.expression = expression;
  }

  @Override
  public Instruction[] compile(CompilationHandler compiler) {
    return compiler.compile(new RuleRefExpression(getRuleKey()));
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean shouldMemoize() {
    return memoize;
  }

  public void enableMemoization() {
    memoize = true;
  }

}
