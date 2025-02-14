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

import com.sonar.cxx.sslr.api.AstNode;
import java.util.List;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;

/**
 * @param <T>
 * @since 1.21
 */
public interface GrammarBuilder<T> {

  /**
   *
   * @param <U>
   * @return
   */
  <U> NonterminalBuilder<U> nonterminal();

  /**
   *
   * @param <U>
   * @param ruleKey
   * @return
   */
  <U> NonterminalBuilder<U> nonterminal(GrammarRuleKey ruleKey);

  /**
   * Creates parsing expression - "first of". During the execution of this expression parser execute sub-expressions in
   * order until one succeeds. This expressions succeeds if any sub-expression succeeds.
   * <p>
   * Be aware that in expression {@code firstOf("foo", sequence("foo", "bar"))} second sub-expression will never be
   * executed.
   *
   * @param <U>
   * @param methods
   * @return
   */
  <U> U firstOf(U... methods);

  /**
   * Creates parsing expression - "optional". During execution of this expression parser will execute sub-expression
   * once. This expression always succeeds, with an empty match if sub-expression fails.
   * <p>
   * Be aware that this expression is greedy, i.e. expression {@code sequence(optional("foo"), "foo")} will never
   * succeed.
   *
   * @param <U>
   * @param method
   * @return
   */
  <U> Optional<U> optional(U method);

  /**
   * Creates parsing expression - "one or more". During execution of this expression parser will repeatedly try
   * sub-expression until it fails. This expression succeeds only if sub-expression succeeds at least once.
   * <p>
   * Be aware that:
   * <ul>
   * <li>This expression is a greedy, i.e. expression {@code sequence(oneOrMore("foo"), "foo")} will never succeed.
   * <li>Sub-expression must not allow empty matches, i.e. for expression {@code oneOrMore(optional("foo"))} parser will
   * report infinite loop.
   * </ul>
   *
   * @param <U>
   * @param method
   * @return
   */
  <U> List<U> oneOrMore(U method);

  /**
   * Creates parsing expression - "zero or more". During execution of this expression parser will repeatedly try
   * sub-expression until it fails. This expression always succeeds, with an empty match if sub-expression fails.
   * <p>
   * Be aware that:
   * <ul>
   * <li>This expression is greedy, i.e. expression {@code sequence(zeroOrMore("foo"), "foo")} will never succeed.
   * <li>Sub-expression must not allow empty matches, i.e. for expression {@code zeroOrMore(optional("foo"))} parser
   * will report infinite loop.
   * </ul>
   *
   * @param <U>
   * @param method
   * @return
   */
  <U> Optional<List<U>> zeroOrMore(U method);

  /**
   *
   * @param ruleKey
   * @return
   */
  AstNode invokeRule(GrammarRuleKey ruleKey);

  /**
   *
   * @param ruleKey
   * @return
   */
  T token(GrammarRuleKey ruleKey);

}
