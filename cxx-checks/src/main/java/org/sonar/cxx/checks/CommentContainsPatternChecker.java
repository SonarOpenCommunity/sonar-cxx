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
package org.sonar.cxx.checks;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.annotations.RuleTemplate;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * CommentContainsPatternChecker
 */
@Rule(
  key = "CommentContainsPatternChecker",
  name = "Regular expression on comment",
  priority = Priority.MAJOR)
@RuleTemplate
@NoSqale
public class CommentContainsPatternChecker {

  @RuleProperty(
    key = "check",
    description = "The Squid check")
  private final SquidCheck<?> check;

  @RuleProperty(
    key = "pattern",
    description = "The regular expression")
  private final String pattern;

  @RuleProperty(
    key = "message",
    description = "The violation message")
  private final String message;

  private Pattern p;

  private static final Pattern EOLPattern = Pattern.compile("\\R");

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
    for (Trivia trivia : token.getTrivia()) {
      if (!trivia.isComment()) {
        continue;
      }
      Token triviaToken = trivia.getToken();
      String comment = triviaToken.getOriginalValue();
      int line = triviaToken.getLine();
      if (indexOfIgnoreCase(comment) != -1) {
        String[] lines = EOLPattern.split(comment);

        for (int i = 0; i < lines.length; i++) {
          int start = indexOfIgnoreCase(lines[i]);
          if (start != -1 && !isLetterAround(lines[i], start)) {
            check.getContext().createLineViolation(check, message, line + i);
          }
        }
      }
    }
  }

  private int indexOfIgnoreCase(String str) {
    Matcher m = p.matcher(str);
    return m.find() ? m.start() : -1;
  }

  private boolean isLetterAround(String line, int start) {
    int end = start + pattern.length();

    boolean pre = start > 0 ? Character.isLetter(line.charAt(start - 1)) : false;
    boolean post = end < line.length() - 1 ? Character.isLetter(line.charAt(end)) : false;

    return pre || post;
  }

}
