/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.AstVisitor;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.cxx.squidbridge.api.CodeVisitor;

/**
 * Base class to visit an AST (Abstract Syntactic Tree) generated by a parser.
 * <p/>
 * Methods are visited in the following sequential order : init(), visitFile(), visitNode(), leaveNode(), leaveFile()
 * and destroy()
 */
public abstract class SquidAstVisitor<G extends Grammar> implements CodeVisitor, AstVisitor {

  private final List<AstNodeType> astNodeTypesToVisit = new ArrayList<>();
  private SquidAstVisitorContext<G> context = null;

  /**
   * This method can't be overridden. Used by AstScanners to inject contexts into the actual visitors.
   */
  public void setContext(SquidAstVisitorContext<G> context) {
    if (this.context != null) {
      throw new IllegalStateException("setContext() must only be called once.");
    }
    this.context = context;
  }

  /**
   * This method can't be overridden. Returns the injected context, which the visitors can use.
   */
  public SquidAstVisitorContext<G> getContext() {
    return context;
  }

  /**
   * This method can't be overridden. The method subscribeTo(AstNodeType... astNodeTypes) must be used to while
   * overriding the public void
   * init() method.
   */
  @Override
  public List<AstNodeType> getAstNodeTypesToVisit() {
    return new ArrayList(astNodeTypesToVisit);
  }

  /**
   * This method must called into the init() method when an AST visitor wants to subscribe to a set of AST node type.
   */
  public void subscribeTo(AstNodeType... astNodeTypes) {
    astNodeTypesToVisit.addAll(Arrays.asList(astNodeTypes));
  }

  /**
   * Initialize the visitor. This is the time to verify that the visitor has everything required to perform it job. This
   * method is called
   * once.
   */
  public void init() {
  }

  /**
   * @param astNode AST node or null in case of parse error
   */
  @Override
  public void visitFile(@Nullable AstNode astNode) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitNode(AstNode astNode) {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void leaveNode(AstNode astNode) {
  }

  /**
   * @param astNode AST node or null in case of parse error
   */
  @Override
  public void leaveFile(@Nullable AstNode astNode) {
  }

  /**
   * Destroy the visitor. It is being retired from service.
   */
  public void destroy() {
  }

}
