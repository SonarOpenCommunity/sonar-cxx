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
package org.sonar.cxx.parser;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.events.ParsingEventListener;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxGrammar;
import org.sonar.cxx.lexer.CxxLexer;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;

public final class CxxParser {

  private CxxParser() {
  }

  public static Parser<CxxGrammar> create(ParsingEventListener... parsingEventListeners) {
    return create(new CxxConfiguration(), parsingEventListeners);
  }

  public static Parser<CxxGrammar> create(CxxConfiguration conf, ParsingEventListener... parsingEventListeners) {
    return Parser.builder((CxxGrammar) new CxxGrammarImpl())
      .withLexer(CxxLexer.create(conf, new CxxPreprocessor(conf), new JoinStringsPreprocessor()))
     .setParsingEventListeners(parsingEventListeners).build();
  }

  public static Parser<CxxGrammar> create(CxxConfiguration conf, SquidAstVisitorContext context,
                                          ParsingEventListener... parsingEventListeners) {
    return Parser.builder((CxxGrammar) new CxxGrammarImpl())
      .withLexer(CxxLexer.create(conf, new CxxPreprocessor(conf, context), new JoinStringsPreprocessor()))
      .setParsingEventListeners(parsingEventListeners).build();
  }

}
