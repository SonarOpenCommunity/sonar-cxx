/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
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
  BW_XOR("ˆ"),
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
  BW_XOR_ASSIGN("ˆ="),
  BW_LSHIFT_ASSIGN("<<="),
  BW_RSHIFT_ASSIGN(">>="),

  // Member and pointer operators
  DEREF("*"),
  ADDRESS_OF("&"),
  DEREF_MEMBER("->"), // ARROW?
  MEMBER("."), // DOT?
  MEMBER_OBJ(".*"), // DOT_MUL?
  DEREF_MEMBER_OBJ("->*"), // ARROW_MUL?

  // Delimiters
  SEMICOLON(";"),
  COLON(":"),
  COMMA(","),
  SCOPE("::"),
  BR_LEFT("("),
  BR_RIGHT(")"),
  CURLBR_LEFT("{"),
  CURLBR_RIGHT("}"),
  SQBR_LEFT("["),
  SQBR_RIGHT("]"),

  // Other operators
  TERNARY_CHECK("?"),
  TERNARY_ELSE(":"),
  ELLIPSIS("..."),
  PREPR("#");

  // REST, TODO:
  // <:
  // :>
  // <%
  // %>
  // %:
  // ##
  // %:%:

  private final String value;

  private CxxPunctuator(String word) {
    this.value = word;
  }

  public String getName() {
    return name();
  }

  public String getValue() {
    return value;
  }

  public boolean hasToBeSkippedFromAst(AstNode node) {
    return false;
  }

}
