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
package org.sonar.cxx.visitors;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstVisitor;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.TokenType;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.SquidAstVisitorContext;

public class CxxParseErrorLoggerVisitor<GRAMMAR extends Grammar>
    extends SquidAstVisitor<GRAMMAR> implements AstVisitor {

  private final SquidAstVisitorContext<?> context;
  private static final Logger LOG = Loggers.get(CxxParseErrorLoggerVisitor.class);

  public CxxParseErrorLoggerVisitor(SquidAstVisitorContext<?> context) {
    this.context = context;
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.recoveredDeclaration);
  }

  @Override
  public void visitNode(AstNode node) {
    List<AstNode> children = node.getChildren();
    StringBuilder sb = new StringBuilder();
    int identifierLine = -1;

    for (AstNode child : children) {
      sb.append(child.getTokenValue());
      TokenType type = child.getToken().getType();

      if (type.equals(GenericTokenType.IDENTIFIER)) {
        // save position of last identifier for message
        identifierLine = child.getTokenLine();
        sb.append(' ');
      } else if (type.equals(CxxPunctuator.CURLBR_LEFT)) {
        // part with CURLBR_LEFT is typically an ignored declaration
        if (identifierLine != -1) {
          LOG.warn("[{}:{}]: skip declaration: {}",
            context.getFile(), identifierLine, sb.toString());
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
      LOG.warn("[{}:{}]:    syntax error: {}",
        context.getFile(), identifierLine, sb.toString());
    }
  }
}
