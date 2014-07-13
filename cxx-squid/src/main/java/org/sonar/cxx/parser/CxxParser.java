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
package org.sonar.cxx.parser;

import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.squid.SquidAstVisitorContext;
import com.sonar.sslr.squid.SquidAstVisitorContextImpl;
import org.sonar.cxx.CxxConfiguration;

import com.sonar.sslr.api.Grammar;

import org.sonar.cxx.lexer.CxxLexer;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.squid.api.SourceProject;

import java.io.File;

public final class CxxParser {
  private static CxxPreprocessor cxxpp = null;

  private CxxParser() {
  }

  public static void finishedParsing(File path){
    cxxpp.finishedPreprocessing(path);
  }

  public static Parser<Grammar> create() {
    return create(new SquidAstVisitorContextImpl<Grammar>(new SourceProject("")),
                  new CxxConfiguration());
  }

  public static Parser<Grammar> create(SquidAstVisitorContext<Grammar> context) {
    return create(context, new CxxConfiguration());
  }

  public static Parser<Grammar> create(SquidAstVisitorContext<Grammar> context,
                                       CxxConfiguration conf) {
    cxxpp = new CxxPreprocessor(context, conf);
    return Parser.builder(CxxGrammarImpl.create(conf))
      .withLexer(CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor()))
      .build();
  }
}
