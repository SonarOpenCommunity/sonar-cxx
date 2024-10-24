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
package com.sonar.cxx.sslr.api; // cxx: in use

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Trivia {

  public enum TriviaKind {
    COMMENT,
    PREPROCESSOR,
    SKIPPED_TEXT
  }

  private static final String TRIVIA_KIND = "TRIVIA kind=";

  private final TriviaKind kind;
  private final List<Token> tokens;
  private final PreprocessingDirective preprocessingDirective;

  private Trivia(TriviaKind kind, Token... tokens) {
    this(kind, null, tokens);
  }

  private Trivia(TriviaKind kind, @Nullable PreprocessingDirective preprocessingDirective, Token... tokens) {
    this.kind = kind;
    this.preprocessingDirective = preprocessingDirective;
    this.tokens = Arrays.asList(tokens);
    if (this.tokens.isEmpty()) {
      throw new IllegalArgumentException(
        "the trivia must have at least one associated token to be able to call getToken()");
    }
  }

  public Token getToken() {
    return tokens.get(0);
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public boolean isComment() {
    return kind == TriviaKind.COMMENT;
  }

  public boolean isPreprocessor() {
    return kind == TriviaKind.PREPROCESSOR;
  }

  public boolean isSkippedText() {
    return kind == TriviaKind.SKIPPED_TEXT;
  }

  public boolean hasPreprocessingDirective() {
    return preprocessingDirective != null;
  }

  public PreprocessingDirective getPreprocessingDirective() {
    return preprocessingDirective;
  }

  @Override
  public String toString() {
    if (tokens.isEmpty()) {
      return TRIVIA_KIND + kind;
    } else if (tokens.size() == 1) {
      var token = tokens.get(0);
      return TRIVIA_KIND + kind + " line=" + token.getLine() + " type=" + token.getType() + " value=" + token
        .getOriginalValue();
    } else {
      var sb = new StringBuilder();
      for (var token : tokens) {
        sb.append(token.getOriginalValue());
        sb.append(' ');
      }

      return TRIVIA_KIND + kind + " value = " + sb.toString();
    }
  }

  public static Trivia createComment(Token commentToken) {
    return new Trivia(TriviaKind.COMMENT, commentToken);
  }

  public static Trivia createSkippedText(@Nonnull List<Token> tokens) {
    Objects.requireNonNull(tokens, "tokens cannot be null");

    return createSkippedText(tokens.toArray(Token[]::new));
  }

  public static Trivia createSkippedText(Token... tokens) {
    return new Trivia(TriviaKind.SKIPPED_TEXT, tokens);
  }

  public static Trivia createPreprocessingToken(Token preprocessingToken) {
    return new Trivia(TriviaKind.PREPROCESSOR, preprocessingToken);
  }

  public static Trivia createPreprocessingDirective(PreprocessingDirective preprocessingDirective) {
    return new Trivia(TriviaKind.PREPROCESSOR, preprocessingDirective);
  }

  public static Trivia createPreprocessingDirective(AstNode ast, Grammar grammar) {
    return createPreprocessingDirective(PreprocessingDirective.create(ast, grammar));
  }

}
