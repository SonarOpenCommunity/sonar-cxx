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
package org.sonar.cxx.sslr.internal.grammar; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeSkippingPolicy;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.impl.ast.AlwaysSkipFromAst;
import com.sonar.cxx.sslr.impl.ast.NeverSkipFromAst;
import com.sonar.cxx.sslr.impl.ast.SkipFromAstIfOnlyOneChild;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.cxx.sslr.internal.vm.CompilationHandler;
import org.sonar.cxx.sslr.internal.vm.Instruction;
import org.sonar.cxx.sslr.internal.vm.MemoParsingExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.RuleRefExpression;
import org.sonar.cxx.sslr.parser.GrammarOperators;

public class MutableParsingRule implements CompilableGrammarRule, Matcher, Rule, AstNodeSkippingPolicy,
  MemoParsingExpression, GrammarRuleKey {

  private final GrammarRuleKey ruleKey;
  private final String name;
  private ParsingExpression expression;
  private AstNodeSkippingPolicy astNodeSkippingPolicy = NeverSkipFromAst.INSTANCE;

  public MutableParsingRule(String name) {
    this.ruleKey = this;
    this.name = name;
  }

  public MutableParsingRule(GrammarRuleKey ruleKey) {
    this.ruleKey = ruleKey;
    this.name = ruleKey.toString();
  }

  public String getName() {
    return name;
  }

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
  public Rule is(Object... e) {
    if (expression != null) {
      throw new GrammarException("The rule '" + ruleKey + "' has already been defined somewhere in the grammar.");
    }
    setExpression((ParsingExpression) GrammarOperators.sequence(e));
    return this;
  }

  @Override
  public Rule override(Object... e) {
    setExpression((ParsingExpression) GrammarOperators.sequence(e));
    return this;
  }

  @Override
  public void setExpression(ParsingExpression expression) {
    this.expression = expression;
  }

  @Override
  public void skip() {
    astNodeSkippingPolicy = AlwaysSkipFromAst.INSTANCE;
  }

  @Override
  public void skipIfOneChild() {
    astNodeSkippingPolicy = SkipFromAstIfOnlyOneChild.INSTANCE;
  }

  @Override
  public boolean hasToBeSkippedFromAst(@Nullable AstNode node) {
    return astNodeSkippingPolicy.hasToBeSkippedFromAst(node);
  }

  @Override
  public Instruction[] compile(CompilationHandler compiler) {
    return compiler.compile(new RuleRefExpression(ruleKey));
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean shouldMemoize() {
    return true;
  }

}
