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
 * C++ Standard, Section 2.12 "Keywords"
 *
 * In this list are only C++ keywords allowed. All other extensions must be handled as identifiers (e.g. final,
 * override, ...)
 */
public enum CxxKeyword implements TokenType {

  ALIGNAS("alignas"),
  ALIGNOF("alignof"),
  ASM("asm"),
  AUTO("auto"),
  BOOL("bool"),
  BREAK("break"),
  CASE("case"),
  CATCH("catch"),
  CHAR("char"),
  CHAR16_T("char16_t"),
  CHAR32_T("char32_t"),
  CLASS("class"),
  CONST("const"),
  CONSTEXPR("constexpr"),
  CONST_CAST("const_cast"),
  CONTINUE("continue"),
  DECLTYPE("decltype"),
  DEFAULT("default"),
  DELETE("delete"),
  DO("do"),
  DOUBLE("double"),
  DYNAMIC_CAST("dynamic_cast"),
  ELSE("else"),
  ENUM("enum"),
  EXPLICIT("explicit"),
  EXPORT("export"),
  EXTERN("extern"),
  FALSE("false"),
  FLOAT("float"),
  FOR("for"),
  FRIEND("friend"),
  GOTO("goto"),
  IF("if"),
  INLINE("inline"),
  INT("int"),
  LONG("long"),
  MUTABLE("mutable"),
  NAMESPACE("namespace"),
  NEW("new"),
  NOEXCEPT("noexcept"),
  NULLPTR("nullptr"),
  OPERATOR("operator"),
  PRIVATE("private"),
  PROTECTED("protected"),
  PUBLIC("public"),
  REGISTER("register"),
  REINTERPRET_CAST("reinterpret_cast"),
  RETURN("return"),
  SHORT("short"),
  SIGNED("signed"),
  SIZEOF("sizeof"),
  STATIC("static"),
  STATIC_ASSERT("static_assert"),
  STATIC_CAST("static_cast"),
  STRUCT("struct"),
  SWITCH("switch"),
  TEMPLATE("template"),
  THIS("this"),
  THREAD_LOCAL("thread_local"),
  THROW("throw"),
  TRUE("true"),
  TRY("try"),
  TYPEDEF("typedef"),
  TYPENAME("typename"),
  UNION("union"),
  UNSIGNED("unsigned"),
  USING("using"),
  VIRTUAL("virtual"),
  VOID("void"),
  VOLATILE("volatile"),
  WCHAR_T("wchar_t"),
  WHILE("while"),
  // Operators
  AND("and"),
  AND_EQ("and_eq"),
  BITAND("bitand"),
  BITOR("bitor"),
  COMPL("compl"),
  NOT("not"),
  NOT_EQ("not_eq "),
  OR("or"),
  OR_EQ("or_eq"),
  XOR("xor"),
  XOR_EQ("xor_eq"),
  TYPEID("typeid"),
  // C++/CLI keywords
  GCNEW("gcnew");
  private final String value;

  private CxxKeyword(String value) {
    this.value = value;
  }

  public String getName() {
    return name();
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean hasToBeSkippedFromAst(AstNode node) {
    return false;
  }

  public static String[] keywordValues() {
    CxxKeyword[] keywordsEnum = CxxKeyword.values();
    String[] keywords = new String[keywordsEnum.length];
    for (int i = 0; i < keywords.length; i++) {
      keywords[i] = keywordsEnum[i].getValue();
    }
    return keywords;
  }

}
