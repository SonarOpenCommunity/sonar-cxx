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
package org.sonar.cxx.parser;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import java.io.File;
import java.util.Collection;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.lexer.CxxLexer;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.sonar.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.squidbridge.api.SourceProject;

public final class CxxParser {

  private static volatile CxxPreprocessor cxxpp;

  private CxxParser() {
  }

  public static void finishedParsing(File path) {
    cxxpp.finishedPreprocessing(path);
  }

  public static Collection<CxxPreprocessor.Include> getIncludedFiles(File path) {
    return cxxpp.getIncludedFiles(path);
  }

  public static Collection<CxxPreprocessor.Include> getMissingIncludeFiles(File path) {
    return cxxpp.getMissingIncludeFiles(path);
  }

  public static Parser<Grammar> create(CxxLanguage language) {
    return create(new SquidAstVisitorContextImpl<>(new SourceProject("")),
      new CxxConfiguration(), language);
  }

  public static Parser<Grammar> create(CxxLanguage language, SquidAstVisitorContext<Grammar> context) {
    return create(context, new CxxConfiguration(), language);
  }

  public static Parser<Grammar> create(SquidAstVisitorContext<Grammar> context, CxxConfiguration conf,
    CxxLanguage language) {
    cxxpp = new CxxPreprocessor(context, conf, language);
    return Parser.builder(CxxGrammarImpl.create(conf))
      .withLexer(CxxLexer.create(conf, cxxpp, new JoinStringsPreprocessor()))
      .build();
  }
}
