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
package org.sonar.cxx.sslr.grammar;

import org.sonar.cxx.sslr.internal.vm.CompilableGrammarRule;
import org.sonar.cxx.sslr.internal.vm.FirstOfExpression;
import org.sonar.cxx.sslr.internal.vm.NextExpression;
import org.sonar.cxx.sslr.internal.vm.NextNotExpression;
import org.sonar.cxx.sslr.internal.vm.NothingExpression;
import org.sonar.cxx.sslr.internal.vm.OneOrMoreExpression;
import org.sonar.cxx.sslr.internal.vm.OptionalExpression;
import org.sonar.cxx.sslr.internal.vm.ParsingExpression;
import org.sonar.cxx.sslr.internal.vm.SequenceExpression;
import org.sonar.cxx.sslr.internal.vm.ZeroOrMoreExpression;

/**
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.</p>
 */
abstract class GrammarBuilder {

  /**
   * Allows to describe rule. Result of this method should be used only for execution of methods in it, i.e. you should
   * not save reference on it. No guarantee that this method always returns the same instance for the same key of rule.
   */
  public abstract GrammarRuleBuilder rule(GrammarRuleKey ruleKey);

  /**
   * Allows to specify that given rule should be root for grammar.
   */
  public abstract void setRootRule(GrammarRuleKey ruleKey);

  /**
   * Creates parsing expression - "sequence". During execution of this expression parser will sequentially execute all
   * sub-expressions. This expression succeeds only if all sub-expressions succeed.
   *
   * @param e1 first sub-expression
   * @param e2 second sub-expression
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   */
  public final Object sequence(Object e1, Object e2) {
    return new SequenceExpression(convertToExpression(e1), convertToExpression(e2));
  }

  /**
   * Creates parsing expression - "sequence". See {@link #sequence(Object, Object)} for more details.
   *
   * @param e1 first sub-expression
   * @param e2 second sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   */
  public final Object sequence(Object e1, Object e2, Object... rest) {
    return new SequenceExpression(convertToExpressions(e1, e2, rest));
  }

  /**
   * Creates parsing expression - "first of". During the execution of this expression parser execute sub-expressions in
   * order until one succeeds. This expressions succeeds if any sub-expression succeeds.
   * <p>
   * Be aware that in expression {@code firstOf("foo", sequence("foo", "bar"))} second sub-expression will never be
   * executed.
   *
   * @param e1 first sub-expression
   * @param e2 second sub-expression
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   */
  public final Object firstOf(Object e1, Object e2) {
    return new FirstOfExpression(convertToExpression(e1), convertToExpression(e2));
  }

  /**
   * Creates parsing expression - "first of". See {@link #firstOf(Object, Object)} for more details.
   *
   * @param e1 first sub-expression
   * @param e2 second sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   */
  public final Object firstOf(Object e1, Object e2, Object... rest) {
    return new FirstOfExpression(convertToExpressions(e1, e2, rest));
  }

  /**
   * Creates parsing expression - "optional". During execution of this expression parser will execute sub-expression
   * once. This expression always succeeds, with an empty match if sub-expression fails.
   * <p>
   * Be aware that this expression is greedy, i.e. expression {@code sequence(optional("foo"), "foo")} will never
   * succeed.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public final Object optional(Object e) {
    return new OptionalExpression(convertToExpression(e));
  }

  /**
   * Creates parsing expression - "optional". Convenience method equivalent to calling
   * {@code optional(sequence(e, rest))}.
   *
   * @param e1 first sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   * @see #optional(Object)
   * @see #sequence(Object, Object)
   */
  public final Object optional(Object e1, Object... rest) {
    return new OptionalExpression(new SequenceExpression(convertToExpressions(e1, rest)));
  }

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
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public final Object oneOrMore(Object e) {
    return new OneOrMoreExpression(convertToExpression(e));
  }

  /**
   * Creates parsing expression - "one or more". Convenience method equivalent to calling
   * {@code oneOrMore(sequence(e1, rest))}.
   *
   * @param e1 first sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   * @see #oneOrMore(Object)
   * @see #sequence(Object, Object)
   */
  public final Object oneOrMore(Object e1, Object... rest) {
    return new OneOrMoreExpression(new SequenceExpression(convertToExpressions(e1, rest)));
  }

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
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public final Object zeroOrMore(Object e) {
    return new ZeroOrMoreExpression(convertToExpression(e));
  }

  /**
   * Creates parsing expression - "zero or more". Convenience method equivalent to calling
   * {@code zeroOrMore(sequence(e1, rest))}.
   *
   * @param e1 sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   * @see #zeroOrMore(Object)
   * @see #sequence(Object, Object)
   */
  public final Object zeroOrMore(Object e1, Object... rest) {
    return new ZeroOrMoreExpression(new SequenceExpression(convertToExpressions(e1, rest)));
  }

  /**
   * Creates parsing expression - "next". During execution of this expression parser will execute sub-expression once.
   * This expression succeeds only if sub-expression succeeds, but never consumes any input.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public final Object next(Object e) {
    return new NextExpression(convertToExpression(e));
  }

  /**
   * Creates parsing expression - "next". Convenience method equivalent to calling {@code next(sequence(e1, rest))}.
   *
   * @param e1 first sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   * @see #next(Object)
   * @see #sequence(Object, Object)
   */
  public final Object next(Object e1, Object... rest) {
    return new NextExpression(new SequenceExpression(convertToExpressions(e1, rest)));
  }

  /**
   * Creates parsing expression - "next not". During execution of this expression parser will execute sub-expression
   * once. This expression succeeds only if sub-expression fails.
   *
   * @param e sub-expression
   * @throws IllegalArgumentException if given argument is not a parsing expression
   */
  public final Object nextNot(Object e) {
    return new NextNotExpression(convertToExpression(e));
  }

  /**
   * Creates parsing expression - "next not". Convenience method equivalent to calling
   * {@code nextNot(sequence(e1, rest))}.
   *
   * @param e1 sub-expression
   * @param rest rest of sub-expressions
   * @throws IllegalArgumentException if any of given arguments is not a parsing expression
   * @see #nextNot(Object)
   * @see #sequence(Object, Object)
   */
  public final Object nextNot(Object e1, Object... rest) {
    return new NextNotExpression(new SequenceExpression(convertToExpressions(e1, rest)));
  }

  /**
   * Creates parsing expression - "nothing". This expression always fails.
   */
  public static final Object nothing() {
    return NothingExpression.INSTANCE;
  }

  protected abstract ParsingExpression convertToExpression(Object e);

  ParsingExpression[] convertToExpressions(Object e1, Object[] rest) {
    var result = new ParsingExpression[1 + rest.length];
    result[0] = convertToExpression(e1);
    for (int i = 0; i < rest.length; i++) {
      result[1 + i] = convertToExpression(rest[i]);
    }
    return result;
  }

  private ParsingExpression[] convertToExpressions(Object e1, Object e2, Object[] rest) {
    var result = new ParsingExpression[2 + rest.length];
    result[0] = convertToExpression(e1);
    result[1] = convertToExpression(e2);
    for (int i = 0; i < rest.length; i++) {
      result[2 + i] = convertToExpression(rest[i]);
    }
    return result;
  }

  /**
   * Adapts {@link CompilableGrammarRule} to be used as {@link GrammarRuleBuilder}.
   */
  static class RuleBuilder implements GrammarRuleBuilder {

    private final GrammarBuilder b;
    private final CompilableGrammarRule delegate;

    public RuleBuilder(GrammarBuilder b, CompilableGrammarRule delegate) {
      this.b = b;
      this.delegate = delegate;
    }

    @Override
    public GrammarRuleBuilder is(Object e) {
      if (delegate.getExpression() != null) {
        throw new GrammarException("The rule '" + delegate.getRuleKey()
          + "' has already been defined somewhere in the grammar.");
      }
      delegate.setExpression(b.convertToExpression(e));
      return this;
    }

    @Override
    public GrammarRuleBuilder is(Object e, Object... rest) {
      return is(new SequenceExpression(b.convertToExpressions(e, rest)));
    }

    @Override
    public GrammarRuleBuilder override(Object e) {
      delegate.setExpression(b.convertToExpression(e));
      return this;
    }

    @Override
    public GrammarRuleBuilder override(Object e, Object... rest) {
      return override(new SequenceExpression(b.convertToExpressions(e, rest)));
    }

    @Override
    public void skip() {
      delegate.skip();
    }

    @Override
    public void skipIfOneChild() {
      delegate.skipIfOneChild();
    }

  }

}
