/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.nio.charset.Charset;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;

final class PPParser {

  private PPParser() {
  }

  static Parser<Grammar> create(Charset charset) {
    return Parser.builder(PPGrammarImpl.create())
      .withLexer(PPLexer.create(charset))
      .build();
  }

  static Parser<Grammar> create(GrammarRuleKey rootRuleKey, Charset charset) {
    var grammar = PPGrammarImpl.create();
    Parser<Grammar> parser = Parser.builder(grammar)
      .withLexer(PPLexer.create(charset))
      .build();
    parser.setRootRule(grammar.rule(rootRuleKey));
    return parser;
  }

  /**
   * Convert string to AST (for UnitTest).
   */
  static AstNode lineParser(String source) {
    Parser<Grammar> lineParser = PPParser.create(Charset.defaultCharset());
    return lineParser.parse(source);
  }
}
