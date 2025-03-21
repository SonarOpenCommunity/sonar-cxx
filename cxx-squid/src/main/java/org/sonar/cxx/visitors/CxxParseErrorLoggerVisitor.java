/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.squidbridge.SquidAstVisitor;

public class CxxParseErrorLoggerVisitor<GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR> {

  private static final String SYNTAX_ERROR_MSG
    = "Source code parser: {} syntax error(s) detected. "
    + "Syntax errors could cause invalid software metric values."
    + " Root cause are typically missing includes, "
    + "missing macros or compiler specific extensions.";
  private static final Logger LOG = LoggerFactory.getLogger(CxxParseErrorLoggerVisitor.class);
  private static int errors = 0;

  public static void finalReport() {
    if (errors != 0) {
      LOG.warn(SYNTAX_ERROR_MSG, errors);
    }
  }

  public static void resetReport() {
    errors = 0;
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.recoveredDeclaration);
  }

  @Override
  public void visitNode(AstNode node) {
    errors++;
    if (!LOG.isDebugEnabled()) {
      return;
    }

    List<AstNode> children = node.getChildren();
    var sb = new StringBuilder(512);
    int identifierLine = -1;

    for (var child : children) {
      sb.append(child.getTokenValue());
      var type = child.getToken().getType();

      if (type.equals(GenericTokenType.IDENTIFIER)) {
        // save position of last identifier for message
        identifierLine = child.getTokenLine();
        sb.append(' ');
      } else if (type.equals(CxxPunctuator.CURLBR_LEFT)) {
        // part with CURLBR_LEFT is typically an ignored declaration
        if (identifierLine != -1) {
          LOG.debug("[{}:{}]: skip declaration: {}",
            getContext().getFile(), identifierLine, sb);
          sb.setLength(0);
          identifierLine = -1;
        }
      } else if (type.equals(CxxPunctuator.CURLBR_RIGHT)) {
        sb.setLength(0);
        identifierLine = -1;
      } else {
        sb.append(' ');
      }
    }

    if (identifierLine != -1 && sb.length() > 0) {
      // part without CURLBR_LEFT is typically a syntax error
      LOG.debug("[{}:{}]:    syntax error: {}",
        getContext().getFile(), identifierLine, sb);
    }
  }

}
