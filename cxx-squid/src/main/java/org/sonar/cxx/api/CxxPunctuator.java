/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.api;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;

/**
 * C++ Standard, as of 2010-03-26, Section 2.13 "Operators and punctuators"
 */
public enum CxxPunctuator implements TokenType {

  // Basic arithmetic operators
  PLUS("+"),
  MINUS("-"),
  MUL("*"),
  DIV("/"),
  MODULO("%"),
  INCR("++"),
  DECR("--"),
  ASSIGN("="),
  // Comparison/relational operators
  EQ("=="),
  NOT_EQ("!="),
  LT("<"),
  GT(">"),
  LT_EQ("<="),
  GT_EQ(">="),
  // Logical operators
  NOT("!"),
  AND("&&"),
  OR("||"),
  // Bitwise Operators
  BW_NOT("~"),
  BW_AND("&"),
  BW_OR("|"),
  BW_XOR("^"),
  BW_LSHIFT("<<"),
  BW_RSHIFT(">>"),
  // Compound assignment operators
  PLUS_ASSIGN("+="),
  MINUS_ASSIGN("-="),
  MUL_ASSIGN("*="),
  DIV_ASSIGN("/="),
  MODULO_ASSIGN("%="),
  BW_AND_ASSIGN("&="),
  BW_OR_ASSIGN("|="),
  BW_XOR_ASSIGN("^="),
  BW_LSHIFT_ASSIGN("<<="),
  BW_RSHIFT_ASSIGN(">>="),
  // Member and pointer operators
  ARROW("->"), // ARROW?
  DOT("."), // DOT?
  DOT_STAR(".*"), // DOT_MUL?
  ARROW_STAR("->*"), // ARROW_MUL?

  // Delimiters
  SEMICOLON(";"),
  COLON(":"),
  COMMA(","),
  DOUBLECOLON("::"),
  BR_LEFT("("),
  BR_RIGHT(")"),
  CURLBR_LEFT("{"),
  CURLBR_RIGHT("}"),
  SQBR_LEFT("["),
  SQBR_RIGHT("]"),
  // Other operators
  QUEST("?"),
  ELLIPSIS("...");

  private final String value;

  private CxxPunctuator(String word) {
    this.value = word;
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
