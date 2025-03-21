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
package org.sonar.cxx.checks.utils;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;

public final class CheckUtils {

  private CheckUtils() {
  }

  public static Pattern compileUserRegexp(@Nonnull String regexp, int flags) {
    Objects.requireNonNull(regexp, "regular expression has to be non null");
    if (regexp.isEmpty()) {
      throw new IllegalStateException("Empty regular expression");
    }
    try {
      return Pattern.compile(regexp, flags);
    } catch (PatternSyntaxException e) {
      throw new IllegalStateException("Unable to compile the regular expression: \"" + regexp + "\"", e);
    }
  }

  public static Pattern compileUserRegexp(String regexp) {
    return compileUserRegexp(regexp, 0);
  }

  public static boolean isIfStatement(AstNode node) {
    if (node.is(CxxGrammarImpl.selectionStatement)) {
      return node.getToken().getType().equals(CxxKeyword.IF);
    }
    return false;
  }

  public static boolean isSwitchStatement(AstNode node) {
    if (node.is(CxxGrammarImpl.selectionStatement)) {
      return node.getToken().getType().equals(CxxKeyword.SWITCH);
    }
    return false;
  }

  public static boolean isParenthesisedExpression(AstNode node) {
    return (node.is(CxxGrammarImpl.primaryExpression) && node.getParent().is(CxxGrammarImpl.expression)
            && node.getFirstChild().is(CxxPunctuator.BR_LEFT) && node.getLastChild().is(CxxPunctuator.BR_RIGHT)
            && !node.isCopyBookOrGeneratedNode());
  }

  public static boolean isIdentifierLabel(AstNode node) {
    if (node.is(CxxGrammarImpl.labeledStatement)) {
      return node.getToken().getType().equals(GenericTokenType.IDENTIFIER);
    }
    return false;
  }

  public static boolean isFunctionDefinition(AstNode node) {
    if (node.is(CxxGrammarImpl.functionDefinition)) {
      var decl = node.getFirstDescendant(CxxGrammarImpl.declarator);
      if (decl != null && decl.hasDescendant(CxxGrammarImpl.parametersAndQualifiers)) {
        return true;
      }
    }
    return false;
  }

}
