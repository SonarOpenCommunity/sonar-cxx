/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import org.junit.jupiter.api.Test;
import static org.sonar.cxx.squidbridge.metrics.ResourceParser.scanFile;

class AbstractOneStatementPerLineCheckTest {

  private final CheckMessagesVerifierRule checkMessagesVerifier = new CheckMessagesVerifierRule();

  private static class Check extends AbstractOneStatementPerLineCheck<Grammar> {

    @Override
    public AstNodeType getStatementRule() {
      return MiniCGrammar.STATEMENT;
    }

    @Override
    public boolean isExcluded(AstNode statementNode) {
      return statementNode.getFirstChild().is(MiniCGrammar.COMPOUND_STATEMENT);
    }

  }

  @Test
  void detected() {
    checkMessagesVerifier.verify(scanFile("/checks/one_statement_per_line.mc", new Check()).getCheckMessages())
      .next().atLine(7).withMessage(
      "At most one statement is allowed per line, but 2 statements were found on this line.");
  }

}
