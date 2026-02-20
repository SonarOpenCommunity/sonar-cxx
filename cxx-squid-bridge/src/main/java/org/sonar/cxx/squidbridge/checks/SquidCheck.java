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
package org.sonar.cxx.squidbridge.checks; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import javax.annotation.CheckForNull;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.CodeCheck;
import org.sonar.cxx.squidbridge.api.PreciseIssue;

public abstract class SquidCheck<G extends Grammar> extends SquidAstVisitor<G> implements CodeCheck {

  @Override
  @CheckForNull
  public String getKey() {
    return null;
  }

  /**
   * Add a precise issue to the current file.
   *
   * @param node the AST node where the issue occurs
   * @param message the issue message
   * @return the PreciseIssue for further configuration (secondary locations, cost, etc.)
   */
  public PreciseIssue addIssue(AstNode node, String message) {
    var issue = new PreciseIssue(this, node, message);
    getContext().addIssue(issue);
    return issue;
  }

  /**
   * Add a pre-configured precise issue to the current file.
   *
   * @param issue the precise issue to add
   */
  public void addIssue(PreciseIssue issue) {
    getContext().addIssue(issue);
  }

  /**
   * Create a precise issue without immediately adding it.
   *
   * @param node the AST node where the issue occurs
   * @param message the issue message
   * @return a new PreciseIssue (not yet reported)
   */
  public PreciseIssue createIssue(AstNode node, String message) {
    return new PreciseIssue(this, node, message);
  }

}
