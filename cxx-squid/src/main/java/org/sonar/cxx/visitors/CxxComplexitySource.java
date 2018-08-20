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

import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;

import com.sonar.sslr.api.AstNodeType;

/**
 * Structure, that tracks all nodes, which increase the code complexity
 */
public class CxxComplexitySource {
  public CxxComplexitySource(int line, AstNodeType nodeType, int nesting) {
    super();
    this.line = line;
    this.type = nodeType;
    this.nesting = nesting;
  }

  public String getLine() {
    return Integer.valueOf(line).toString();
  }

  private String getNodeDescripton() {
    if (type == CxxGrammarImpl.functionDefinition) {
      return "function definition";
    } else if (type == CxxKeyword.IF || type == CxxGrammarImpl.selectionStatement) {
      return "if statement";
    } else if (type == CxxKeyword.ELSE) {
      return "else statement";
    } else if (type == CxxKeyword.FOR) {
      return "for loop";
    } else if (type == CxxKeyword.WHILE) {
      return "while loop";
    } else if (type == CxxGrammarImpl.iterationStatement) {
      return "iteration statement";
    } else if (type == CxxKeyword.CATCH || type == CxxGrammarImpl.handler) {
      return "catch-clause";
    } else if (type == CxxKeyword.CASE || type == CxxKeyword.DEFAULT) {
      return "switch label";
    } else if (type == CxxKeyword.GOTO) {
      return "goto statement";
    } else if (type == CxxPunctuator.AND || type == CxxPunctuator.OR || type == CxxGrammarImpl.logicalAndExpression
        || type == CxxGrammarImpl.logicalOrExpression) {
      return "logical operator";
    } else if (type == CxxPunctuator.QUEST) {
      return "conditional operator";
    }
    return "";
  }

  public String getExplanation() {
    if (nesting == 0) {
      return "+1: " + getNodeDescripton();
    } else {
      return new StringBuilder().append("+").append(1 + nesting).append(": ").append(getNodeDescripton())
          .append(" (incl ").append(nesting).append(" for nesting)").toString();
    }
  }

  private final int line;
  private final AstNodeType type;
  private final int nesting;
}
