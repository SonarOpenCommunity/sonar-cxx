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

import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.internal.matchers.Matcher;

import java.util.Map;

public class CompiledGrammar {

  private final Map<GrammarRuleKey, CompilableGrammarRule> rules;
  private final Instruction[] instructions;
  private final GrammarRuleKey rootRuleKey;
  private final int rootRuleOffset;

  public CompiledGrammar(Instruction[] instructions, Map<GrammarRuleKey, CompilableGrammarRule> rules, GrammarRuleKey rootRuleKey, int rootRuleOffset) {
    this.instructions = instructions;
    this.rules = rules;
    this.rootRuleKey = rootRuleKey;
    this.rootRuleOffset = rootRuleOffset;
  }

  public Instruction[] getInstructions() {
    return instructions;
  }

  public Matcher getMatcher(GrammarRuleKey ruleKey) {
    return rules.get(ruleKey);
  }

  public GrammarRuleKey getRootRuleKey() {
    return rootRuleKey;
  }

  public int getRootRuleOffset() {
    return rootRuleOffset;
  }

}
