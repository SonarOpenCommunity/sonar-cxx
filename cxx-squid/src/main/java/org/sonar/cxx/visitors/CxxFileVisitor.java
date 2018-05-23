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
import com.sonar.sslr.api.Grammar;
import org.sonar.cxx.parser.CxxParser;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.SquidAstVisitorContext;

public class CxxFileVisitor<GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR>
    implements AstVisitor {

  private SquidAstVisitorContext<?> context;

  public CxxFileVisitor(SquidAstVisitorContext<?> context) {
    this.context = context;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitFile(AstNode node) {
    CxxParser.finishedParsing(context.getFile());
  }
}
