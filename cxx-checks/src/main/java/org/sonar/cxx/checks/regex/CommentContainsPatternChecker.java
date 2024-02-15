/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.Token;
import java.util.regex.Pattern;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

class CommentContainsPatternChecker {

  private static final Pattern EOL_PATTERN = Pattern.compile("\\R");
  private final SquidCheck<?> check;
  private final String pattern;
  private final String message;
  private final Pattern p;

  /**
   * CommentContainsPatternChecker
   *
   * @param check
   * @param pattern
   * @param message
   */
  public CommentContainsPatternChecker(SquidCheck<?> check, String pattern, String message) {
    this.check = check;
    this.pattern = pattern;
    this.message = message;
    p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
  }

  /**
   * visitToken
   *
   * @param token
   */
  public void visitToken(Token token) {
    for (var trivia : token.getTrivia()) {
      if (!trivia.isComment()) {
        continue;
      }
      var triviaToken = trivia.getToken();
      String comment = triviaToken.getOriginalValue();
      int line = triviaToken.getLine();
      if (indexOfIgnoreCase(comment) != -1) {
        String[] lines = EOL_PATTERN.split(comment);

        for (var i = 0; i < lines.length; i++) {
          int start = indexOfIgnoreCase(lines[i]);
          if (start != -1 && !isLetterAround(lines[i], start)) {
            check.getContext().createLineViolation(check, message, line + i);
          }
        }
      }
    }
  }

  private int indexOfIgnoreCase(String str) {
    var m = p.matcher(str);
    return m.find() ? m.start() : -1;
  }

  private boolean isLetterAround(String line, int start) {
    int end = start + pattern.length();

    var pre = start > 0 ? Character.isLetter(line.charAt(start - 1)) : false;
    var post = end < line.length() - 1 ? Character.isLetter(line.charAt(end)) : false;

    return pre || post;
  }

}
