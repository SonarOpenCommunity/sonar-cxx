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
package org.sonar.cxx.visitors;

import org.sonar.cxx.parser.CxxGrammarImpl;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.squid.SquidAstVisitor;
import com.sonar.sslr.squid.SquidAstVisitorContext;

public class CxxParseErrorLoggerVisitor <GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR> implements AstAndTokenVisitor {

  private SquidAstVisitorContext context = null;

  public CxxParseErrorLoggerVisitor(SquidAstVisitorContext context){
    this.context = context;
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.recoveredDeclaration);
  }

  @Override
  public void visitNode(AstNode node) {
    AstNode identifierAst = node.getFirstChild(GenericTokenType.IDENTIFIER);
    if( identifierAst != null ) {
      CxxGrammarImpl.LOG.warn("[{}:{}]: syntax error, skip '{}'", new Object[] {context.getFile(), node.getToken().getLine(), identifierAst.getTokenValue()});
    }
  }

  public void visitToken(Token token) {
  }
}
