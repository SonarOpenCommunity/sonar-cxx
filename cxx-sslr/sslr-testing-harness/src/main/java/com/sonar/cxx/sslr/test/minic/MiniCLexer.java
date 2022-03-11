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
package com.sonar.cxx.sslr.test.minic;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.channel.BlackHoleChannel;
import com.sonar.cxx.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.cxx.sslr.impl.channel.PunctuatorChannel;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.regexp;

public final class MiniCLexer {

  private MiniCLexer() {
  }

  public static enum Literals implements TokenType {

    INTEGER;

    @Override
    public String getName() {
      return name();
    }

    @Override
    public String getValue() {
      return name();
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

  }

  public static enum Punctuators implements TokenType {

    PAREN_L("("), PAREN_R(")"),
    BRACE_L("{"), BRACE_R("}"),
    EQ("="), COMMA(","), SEMICOLON(";"),
    ADD("+"), SUB("-"), MUL("*"), DIV("/"),
    EQEQ("=="), NE("!="), LT("<"), LTE("<="), GT(">"), GTE(">="),
    INC("++"), DEC("--"),
    HASH("#");

    private final String value;

    private Punctuators(String value) {
      this.value = value;
    }

    @Override
    public String getName() {
      return name();
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

  }

  public static enum Keywords implements TokenType {

    STRUCT("struct"),
    INT("int"), VOID("void"),
    RETURN("return"), IF("if"), ELSE("else"), WHILE("while"),
    CONTINUE("continue"), BREAK("break");

    private final String value;

    private Keywords(String value) {
      this.value = value;
    }

    @Override
    public String getName() {
      return name();
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }

    public static String[] keywordValues() {
      var keywordsEnum = Keywords.values();
      var keywords = new String[keywordsEnum.length];
      for (int i = 0; i < keywords.length; i++) {
        keywords[i] = keywordsEnum[i].getValue();
      }
      return keywords;
    }

  }

  public static Lexer create() {
    return Lexer.builder()
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(new IdentifierAndKeywordChannel("[a-zA-Z]([a-zA-Z0-9_]*[a-zA-Z0-9])?+", true, Keywords.values()))
      .withChannel(regexp(Literals.INTEGER, "[0-9]+"))
      .withChannel(commentRegexp("(?s)/\\*.*?\\*/"))
      .withChannel(new PunctuatorChannel(Punctuators.values()))
      .withChannel(new BlackHoleChannel("[ \t\r\n]+"))
      .build();
  }

}
