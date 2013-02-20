/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.impl.Parser;
import org.sonar.cxx.CxxConfiguration;

public final class CppParser {
  private CppParser() {
  }
  
  public static Parser<CppGrammar> create(CxxConfiguration conf) {
    return Parser.builder(new CppGrammar())
      .withLexer(CppLexer.create(conf))
      .build();
  }
  
  public static Parser<CppGrammar> createConstantExpressionParser(CxxConfiguration conf) {
    CppGrammar grammar = new CppGrammar();
    Parser<CppGrammar> parser = Parser.builder(grammar)
      .withLexer(CppLexer.create(conf))
      .build();
    parser.setRootRule(grammar.constant_expression);
    return parser;
  }
}
