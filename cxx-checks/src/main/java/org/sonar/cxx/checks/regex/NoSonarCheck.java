/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.checks.regex;

import com.sonar.cxx.sslr.api.AstAndTokenVisitor;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.NoSqale;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

/**
 * Note that {@link com.sonar.cxx.sslr.squid.checks.AbstractNoSonarCheck} can't be used because of bug SSLRSQBR-16.
 */
@Rule(
  key = "NoSonar",
  name = "Avoid use of //NOSONAR marker",
  priority = Priority.INFO)
@ActivatedByDefault
@NoSqale
public class NoSonarCheck extends SquidCheck<Grammar> implements AstAndTokenVisitor {

  private static final Pattern EOL_PATTERN = Pattern.compile("\\R");

  @Override
  public void visitToken(Token token) {
    for (var trivia : token.getTrivia()) {
      if (trivia.isComment()) {
        String[] commentLines = EOL_PATTERN
          .split(getContext().getCommentAnalyser().getContents(trivia.getToken().getOriginalValue()), -1);
        int line = trivia.getToken().getLine();

        for (var commentLine : commentLines) {
          if (commentLine.contains("NOSONAR")) {
            getContext().createLineViolation(
              this,
              "Is //NOSONAR used to exclude false-positive or to hide real quality flaw ?",
              line);
          }
          line++;
        }
      }
    }
  }

}
