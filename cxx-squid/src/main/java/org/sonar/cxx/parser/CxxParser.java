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
package org.sonar.cxx.parser;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import java.io.File;
import java.lang.ref.WeakReference;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;
import org.sonar.cxx.squidbridge.SquidAstVisitorContextImpl;
import org.sonar.cxx.squidbridge.api.SourceProject;

public final class CxxParser {

  private static WeakReference<CxxPreprocessor> currentPreprocessorInstance;

  private CxxParser() {
  }

  public static void finishedParsing(File path) {
    currentPreprocessorInstance.get().finishedPreprocessing(path);
  }

  public static Parser<Grammar> create() {
    return create(new SquidAstVisitorContextImpl<>(new SourceProject("")),
                  new CxxSquidConfiguration());
  }

  public static Parser<Grammar> create(SquidAstVisitorContext<Grammar> context) {
    return create(context, new CxxSquidConfiguration());
  }

  public static Parser<Grammar> create(SquidAstVisitorContext<Grammar> context, CxxSquidConfiguration squidConfig) {
    var cxxpp = new CxxPreprocessor(context, squidConfig);
    currentPreprocessorInstance = new WeakReference<>(cxxpp);
    return Parser.builder(CxxGrammarImpl.create(squidConfig))
      .withLexer(CxxLexer.create(squidConfig.getCharset(), cxxpp, new JoinStringsPreprocessor()))
      .build();
  }

}
