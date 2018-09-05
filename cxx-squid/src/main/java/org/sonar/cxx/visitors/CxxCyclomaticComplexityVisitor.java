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

import java.util.List;

import javax.annotation.Nullable;

import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.SquidAstVisitorContext;
import org.sonar.squidbridge.metrics.ComplexityVisitor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;

/**
 * Decorator for {@link org.sonar.squidbridge.metrics.ComplexityVisitor} in
 * order to prevent visiting of generated {@link com.sonar.sslr.api.AstNode}s
 *
 * Inheritance is not possible, since the class
 * {@link org.sonar.squidbridge.metrics.ComplexityVisitor} is marked as final
 *
 * @param <G>
 */
public class CxxCyclomaticComplexityVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  private ComplexityVisitor<G> visitor;

  public CxxCyclomaticComplexityVisitor(ComplexityVisitor<G> visitor) {
    this.visitor = visitor;
  }

  @Override
  public void visitNode(AstNode astNode) {
    final Token token = astNode.getToken();
    if (token != null && token.isGeneratedCode()) {
      return;
    }
    visitor.visitNode(astNode);
  }

  @Override
  public void leaveNode(AstNode astNode) {
    final Token token = astNode.getToken();
    if (token != null && token.isGeneratedCode()) {
      return;
    }
    visitor.leaveNode(astNode);
  }

  @Override
  public void init() {
    visitor.init();
  }

  @Override
  public void setContext(SquidAstVisitorContext<G> context) {
    visitor.setContext(context);
  }

  @Override
  public SquidAstVisitorContext<G> getContext() {
    return visitor.getContext();
  }

  @Override
  public List<AstNodeType> getAstNodeTypesToVisit() {
    return visitor.getAstNodeTypesToVisit();
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    visitor.visitFile(astNode);
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    visitor.leaveFile(astNode);
  }

  @Override
  public void destroy() {
    visitor.destroy();
  }
}
