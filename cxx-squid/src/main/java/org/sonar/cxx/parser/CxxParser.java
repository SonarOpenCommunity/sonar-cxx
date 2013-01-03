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
import com.sonar.sslr.squid.SquidAstVisitorContextImpl;
import org.sonar.squid.api.SourceProject;

public final class CxxParser {
  private static class CxxParseEventPropagator extends ParsingEventListener{
    private CxxPreprocessor cxxpp;
    private SquidAstVisitorContext astVisitorContext;
    
    CxxParseEventPropagator(CxxPreprocessor cxxpp, SquidAstVisitorContext astVisitorContext){
      this.cxxpp = cxxpp;
      this.astVisitorContext = astVisitorContext;
    }

    public void beginLex() {
      this.cxxpp.beginPreprocessing(astVisitorContext.getFile());
    };
  }
  
  private static CxxParseEventPropagator parseEventPropagator;
  
  private CxxParser() {
  }
  
  public static Parser<CxxGrammar> create() {
    return create(new CxxConfiguration(),
                  new SquidAstVisitorContextImpl<CxxGrammar>(new SourceProject("")));
  }

  public static Parser<CxxGrammar> create(CxxConfiguration conf, SquidAstVisitorContext context) {
    CxxPreprocessor cxxpp = new CxxPreprocessor(conf, context);
    parseEventPropagator = new CxxParseEventPropagator(cxxpp, context);
    return Parser.builder((CxxGrammar) new CxxGrammarImpl())
      .withLexer(CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor()))
      .setParsingEventListeners(parseEventPropagator).build();
  }
  
  public static Parser<CxxGrammar> createConstantExpressionParser(CxxConfiguration conf) {
    CxxGrammar grammar = new CxxGrammarImpl();
    Parser<CxxGrammar> parser = Parser.builder(grammar)
      .withLexer(CxxLexer.create(conf))
      .build();
    parser.setRootRule(grammar.constant_expression);
    return parser;
  }
}
