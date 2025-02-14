/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.api;

/**
 * Specific Ast node types that can tell whether they should be skipped from being attached to the AST or not.
 *
 *
 * @see AstVisitor
 * @see Grammar
 * @see AstNode
 */
public interface AstNodeSkippingPolicy extends AstNodeType {

  /**
   * Some AstNode can be pretty useless and makes a global AST less readable. This method allows to automatically remove
   * those AstNode from the AST.
   *
   * @param node the node that should or not be removed from the AST
   * @return true if AstNode with this type must be skipped from the AST.
   */
  boolean hasToBeSkippedFromAst(AstNode node);

}
