/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import java.nio.charset.Charset;

public final class CppParser {

  private CppParser() {
  }

  public static Parser<Grammar> create(Charset charset) {
    return Parser.builder(CppGrammarImpl.create())
      .withLexer(CppLexer.create(charset))
      .build();
  }

  public static Parser<Grammar> createConstantExpressionParser(Charset charset) {
    Grammar grammar = CppGrammarImpl.create();
    Parser<Grammar> parser = Parser.builder(grammar)
      .withLexer(CppLexer.create(charset))
      .build();
    parser.setRootRule(grammar.rule(CppGrammarImpl.constantExpression));
    return parser;
  }

}
