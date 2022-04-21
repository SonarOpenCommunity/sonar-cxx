/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.AstAndTokenVisitor;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import org.sonar.cxx.squidbridge.api.CheckMessage;

public abstract class AbstractNoSonarCheck<G extends Grammar> extends SquidCheck<G> implements AstAndTokenVisitor {

  @Override
  public void visitToken(Token token) {
    for (var trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        String[] commentLines = getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue())
          .split("(\r)?\n|\r", -1);
        int line = trivia.getToken().getLine();

        for (var commentLine : commentLines) {
          if (commentLine.contains("NOSONAR")) {
            var violation = new CheckMessage((Object) this,
                                         "Is NOSONAR usage acceptable or does it hide a real quality flaw?");
            violation.setLine(line);
            violation.setBypassExclusion(true);
            getContext().log(violation);
          }

          line++;
        }
      }
    }
  }

}
