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
package org.sonar.cxx.checks.utils;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;

public class CheckUtils {

  private CheckUtils() {
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

}
