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
package org.sonar.cxx.sslr.internal.vm; // cxx: in use

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;

public class MutableGrammarCompiler extends CompilationHandler {

  private final Queue<CompilableGrammarRule> compilationQueue = new ArrayDeque<>();
  private final Map<GrammarRuleKey, CompilableGrammarRule> matchers = new HashMap<>();
  private final Map<GrammarRuleKey, Integer> offsets = new HashMap<>();

  public static CompiledGrammar compile(CompilableGrammarRule rule) {
    return new MutableGrammarCompiler().doCompile(rule);
  }

  private CompiledGrammar doCompile(CompilableGrammarRule start) {
    List<Instruction> instructions = new ArrayList<>();

    // Compile
    compilationQueue.add(start);
    matchers.put(start.getRuleKey(), start);

    while (!compilationQueue.isEmpty()) {
      var rule = compilationQueue.poll();
      var ruleKey = rule.getRuleKey();

      offsets.put(ruleKey, instructions.size());
      Instruction.addAll(instructions, compile(rule.getExpression()));
      instructions.add(Instruction.ret());
    }

    // Link
    var result = instructions.toArray(Instruction[]::new);
    for (int i = 0; i < result.length; i++) {
      var instruction = result[i];
      if (instruction instanceof RuleRefExpression expression) {
        var ruleKey = expression.getRuleKey();
        int offset = offsets.get(ruleKey);
        result[i] = Instruction.call(offset - i, matchers.get(ruleKey));
      }
    }

    return new CompiledGrammar(result, matchers, start.getRuleKey(), offsets.get(start.getRuleKey()));
  }

  @Override
  public Instruction[] compile(ParsingExpression expression) {
    if (expression instanceof CompilableGrammarRule rule) {
      if (!matchers.containsKey(rule.getRuleKey())) {
        compilationQueue.add(rule);
        matchers.put(rule.getRuleKey(), rule);
      }
      return rule.compile(this);
    } else {
      return expression.compile(this);
    }
  }

}
