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
package com.sonar.cxx.sslr.api.typed;

import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.api.Trivia;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;

/**
 * Parse tree or concrete syntax tree is a tree structure built from the input string during parsing. It represent the
 * structure of the input string. Each node in the parse tree is either a terminal or non-terminal. Terminals are the
 * leafs of the tree while the inner nodes are non-terminals.
 *
 * @since 1.21
 */
public interface NodeBuilder {

  /**
   * Create a non terminal object.
   *
   * @param ruleKey key of the rule
   * @param rule grammar rule
   * @param children list of children
   * @param startIndex first index in the line
   * @param endIndex last index in the line
   * @return non terminal object
   *
   * @see GrammarRuleKey
   * @see Rule
   */
  Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex);

  /**
   * Create a terminal object.
   *
   * @param input sequence of characters
   * @param startIndex first index in the line
   * @param endIndex last index in the line
   * @param trivias list of trivias
   * @param type type of the token
   * @return terminal object
   *
   * @see Trivia
   * @see TokenType
   */
  Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, @Nullable TokenType type);

}
