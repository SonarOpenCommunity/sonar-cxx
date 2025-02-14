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
package com.sonar.cxx.sslr.impl.typed;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.typed.GrammarBuilder;
import com.sonar.cxx.sslr.api.typed.NonterminalBuilder;
import com.sonar.cxx.sslr.api.typed.Optional;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;

public class GrammarBuilderInterceptor<T> implements MethodInterceptor, GrammarBuilder<T>, NonterminalBuilder {

  private final LexerlessGrammarBuilder b;

  private final Set<GrammarRuleKey> mappedRuleKeys = new HashSet<>();
  private final Map<Method, GrammarRuleKey> methodToRuleKey = new HashMap<>();

  private final Map<GrammarRuleKey, Method> actions = new HashMap<>();
  private final Set<GrammarRuleKey> optionals = new HashSet<>();
  private final Set<GrammarRuleKey> oneOrMores = new HashSet<>();
  private final Set<GrammarRuleKey> zeroOrMores = new HashSet<>();

  private Method buildingMethod = null;
  private GrammarRuleKey ruleKey = null;
  private final Deque<ParsingExpression> expressionStack = new ArrayDeque<>();

  public GrammarBuilderInterceptor(LexerlessGrammarBuilder b) {
    this.b = b;
  }

  @Override
  public boolean intercept(Method method) {
    if (buildingMethod != null) {
      push(new DelayedRuleInvocationExpression(b, this, method));
      return true;
    }
    buildingMethod = method;
    return false;
  }

  @Override
  public <U> NonterminalBuilder<U> nonterminal() {
    return nonterminal(new DummyGrammarRuleKey(this.buildingMethod));
  }

  @Override
  public <U> NonterminalBuilder<U> nonterminal(GrammarRuleKey ruleKey) {
    this.ruleKey = ruleKey;
    this.mappedRuleKeys.add(ruleKey);
    this.methodToRuleKey.put(this.buildingMethod, this.ruleKey);
    return this;
  }

  @Override
  @CheckForNull
  public Object is(Object method) {
    if (expressionStack.size() != 1) {
      throw new IllegalStateException("Unexpected stack size: " + expressionStack.size());
    }

    var expression = pop();
    b.rule(ruleKey).is(expression);

    this.buildingMethod = null;
    this.ruleKey = null;

    return null;
  }

  @Override
  @CheckForNull
  public <U> U firstOf(U... methods) {
    ParsingExpression expression = new FirstOfExpression(pop(methods.length));
    expressionStack.push(expression);
    return null;
  }

  @Override
  @CheckForNull
  public <U> Optional<U> optional(U method) {
    var expression = pop();
    GrammarRuleKey grammarRuleKey = new DummyGrammarRuleKey("optional", expression);
    optionals.add(grammarRuleKey);
    b.rule(grammarRuleKey).is(b.optional(expression));
    invokeRule(grammarRuleKey);
    return null;
  }

  @Override
  @CheckForNull
  public <U> List<U> oneOrMore(U method) {
    var expression = pop();
    GrammarRuleKey grammarRuleKey = new DummyGrammarRuleKey("oneOrMore", expression);
    oneOrMores.add(grammarRuleKey);
    b.rule(grammarRuleKey).is(b.oneOrMore(expression));
    invokeRule(grammarRuleKey);
    return null;
  }

  @Override
  @CheckForNull
  public <U> Optional<List<U>> zeroOrMore(U method) {
    var expression = pop();
    GrammarRuleKey grammarRuleKey = new DummyGrammarRuleKey("zeroOrMore", expression);
    zeroOrMores.add(grammarRuleKey);
    b.rule(grammarRuleKey).is(b.zeroOrMore(expression));
    invokeRule(grammarRuleKey);
    return null;
  }

  @Override
  @CheckForNull
  public AstNode invokeRule(GrammarRuleKey grammarRuleKey) {
    pushDelayed(grammarRuleKey);
    return null;
  }

  @Override
  @CheckForNull
  public T token(GrammarRuleKey grammarRuleKey) {
    pushDelayed(grammarRuleKey);
    return null;
  }

  private void pushDelayed(GrammarRuleKey grammarRuleKey) {
    push(new DelayedRuleInvocationExpression(b, grammarRuleKey));
  }

  public void addAction(Method method, int stackElements) {
    method.setAccessible(true);
    GrammarRuleKey grammarRuleKey = new DummyGrammarRuleKey(method);
    actions.put(grammarRuleKey, method);
    var expression = stackElements == 1 ? pop() : new SequenceExpression(pop(stackElements));
    b.rule(grammarRuleKey).is(expression);
    invokeRule(grammarRuleKey);
  }

  private ParsingExpression[] pop(int n) {
    var result = new ParsingExpression[n];
    for (int i = n - 1; i >= 0; i--) {
      result[i] = pop();
    }
    return result;
  }

  private ParsingExpression pop() {
    return expressionStack.pop();
  }

  private void push(ParsingExpression expression) {
    expressionStack.push(expression);
  }

  @Nullable
  public Method actionForRuleKey(Object ruleKey) {
    return actions.get(ruleKey);
  }

  @Nullable
  public GrammarRuleKey ruleKeyForMethod(Method method) {
    return methodToRuleKey.get(method);
  }

  public boolean hasMethodForRuleKey(Object ruleKey) {
    return mappedRuleKeys.contains(ruleKey);
  }

  public boolean isOptionalRule(Object ruleKey) {
    return optionals.contains(ruleKey);
  }

  public boolean isOneOrMoreRule(Object ruleKey) {
    return oneOrMores.contains(ruleKey);
  }

  public boolean isZeroOrMoreRule(Object ruleKey) {
    return zeroOrMores.contains(ruleKey);
  }

  private static class DummyGrammarRuleKey implements GrammarRuleKey {

    private final Method method;
    private final String operator;
    private final ParsingExpression expression;

    public DummyGrammarRuleKey(Method method) {
      this.method = method;
      this.operator = null;
      this.expression = null;
    }

    public DummyGrammarRuleKey(String operator, ParsingExpression expression) {
      this.method = null;
      this.operator = operator;
      this.expression = expression;
    }

    @Override
    public String toString() {
      if (operator != null) {
        return operator + "(" + expression + ")";
      }

      var sb = new StringBuilder();
      sb.append("f.");
      sb.append(method.getName());
      sb.append('(');

      Class[] parameterTypes = method.getParameterTypes();
      for (int i = 0; i < parameterTypes.length - 1; i++) {
        sb.append(parameterTypes[i].getSimpleName());
        sb.append(", ");
      }
      if (parameterTypes.length > 0) {
        sb.append(parameterTypes[parameterTypes.length - 1].getSimpleName());
      }

      sb.append(')');

      return sb.toString();
    }

  }

}
