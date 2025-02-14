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
package com.sonar.cxx.sslr.impl; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import java.io.File;
import java.util.List;
import org.sonar.cxx.sslr.internal.matchers.LexerfulAstCreator;
import org.sonar.cxx.sslr.internal.vm.Machine;
import org.sonar.cxx.sslr.internal.vm.MutableGrammarCompiler;
import org.sonar.cxx.sslr.parser.ParserAdapter;

/**
 * To create a new instance of this class use <code>{@link Parser#builder(Grammar)}</code>.
 *
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.</p>
 */
public class Parser<G extends Grammar> {

  private RuleDefinition rootRule;
  private final Lexer lexer;
  private final G grammar;

  /**
   * @since 1.16
   */
  protected Parser(G grammar) {
    this.grammar = grammar;
    lexer = null;
  }

  private Parser(Builder<G> builder) {
    this.lexer = builder.lexer;
    this.grammar = builder.grammar;
    this.rootRule = (RuleDefinition) this.grammar.getRootRule();
  }

  public AstNode parse(File file) {
    try {
      lexer.lex(file);
    } catch (LexerException e) {
      throw new RecognitionException(e);
    }
    return parse(lexer.getTokens());
  }

  public AstNode parse(String source) {
    try {
      lexer.lex(source);
    } catch (LexerException e) {
      throw new RecognitionException(e);
    }
    return parse(lexer.getTokens());
  }

  public AstNode parse(List<Token> tokens) {
    // TODO can be compiled only once
    var g = MutableGrammarCompiler.compile(rootRule);
    return LexerfulAstCreator.create(Machine.parse(tokens, g), tokens);
  }

  public G getGrammar() {
    return grammar;
  }

  public RuleDefinition getRootRule() {
    return rootRule;
  }

  public void setRootRule(Rule rootRule) {
    this.rootRule = (RuleDefinition) rootRule;
  }

  public static <G extends Grammar> Builder<G> builder(G grammar) {
    return new Builder<>(grammar);
  }

  public static <G extends Grammar> Builder<G> builder(Parser<G> parser) {
    return new Builder<>(parser);
  }

  public static final class Builder<G extends Grammar> {

    private Parser<G> baseParser;
    private Lexer lexer;
    private final G grammar;

    private Builder(G grammar) {
      this.grammar = grammar;
    }

    private Builder(Parser<G> parser) {
      this.baseParser = parser;
      this.lexer = parser.lexer;
      this.grammar = parser.grammar;
    }

    public Parser<G> build() {
      if (baseParser instanceof ParserAdapter) {
        return baseParser;
      }
      return new Parser<>(this);
    }

    public Builder<G> withLexer(Lexer lexer) {
      this.lexer = lexer;
      return this;
    }

  }

}
