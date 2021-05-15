/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;

/**
 * Visitor that create resources.
 */
public class SourceCodeBuilderVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private final SourceCodeBuilderCallback callback;
  private final AstNodeType[] astNodeTypes;

  public SourceCodeBuilderVisitor(SourceCodeBuilderCallback callback, AstNodeType... astNodeTypes) {
    this.callback = callback;
    this.astNodeTypes = astNodeTypes;
  }

  @Override
  public void init() {
    for (var astNodeType : astNodeTypes) {
      subscribeTo(astNodeType);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitNode(AstNode astNode) {
    getContext().addSourceCode(callback.createSourceCode(getContext().peekSourceCode(), astNode));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void leaveNode(AstNode astNode) {
    getContext().popSourceCode();
  }

}
