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
package org.sonar.cxx.squidbridge.checks;

import com.google.common.collect.Maps;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import java.util.Map;

public abstract class AbstractOneStatementPerLineCheck<G extends Grammar> extends SquidCheck<G> {

  private final Map<Integer, Integer> statementsPerLine = Maps.newHashMap();

  public abstract AstNodeType getStatementRule();

  public abstract boolean isExcluded(AstNode statementNode);

  @Override
  public void init() {
    subscribeTo(getStatementRule());
  }

  @Override
  public void visitFile(AstNode astNode) {
    statementsPerLine.clear();
  }

  @Override
  public void visitNode(AstNode statementNode) {
    if (!isExcluded(statementNode)) {
      int line = statementNode.getTokenLine();

      if (!statementsPerLine.containsKey(line)) {
        statementsPerLine.put(line, 0);
      }

      statementsPerLine.put(line, statementsPerLine.get(line) + 1);
    }
  }

  @Override
  public void leaveFile(AstNode astNode) {
    for (Map.Entry<Integer, Integer> statementsAtLine : statementsPerLine.entrySet()) {
      if (statementsAtLine.getValue() > 1) {
        getContext().createLineViolation(this,
                                         "At most one statement is allowed per line, but {0} statements were found on this line.",
                                         statementsAtLine.getKey(),
                                         statementsAtLine.getValue());
      }
    }
  }

}
