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

import java.util.List;

import org.sonar.cxx.api.CppPunctuator;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstVisitor;
import com.sonar.sslr.api.Grammar;

/**
 * Visitor that computes the NCLOCs in function body, leading and trailing {} do not count
 *
 * @param <GRAMMAR>
 */
public class CxxLinesOfCodeInFunctionBodyVisitor<GRAMMAR extends Grammar> extends SquidAstVisitor<GRAMMAR>
    implements AstVisitor {

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionBody);
  }

  @Override
  public void visitNode(AstNode node) {
    List<AstNode> allChilds = node.getDescendants(CxxGrammarImpl.statement, CppPunctuator.CURLBR_LEFT,
        CppPunctuator.CURLBR_RIGHT);
    int lines = 1;
    int firstLine = node.getTokenLine();
    if (allChilds != null && !allChilds.isEmpty()) {
      int previousLine = firstLine;
      for (AstNode child : allChilds) {
        int currentLine = child.getTokenLine();
        if (currentLine != previousLine) {
          lines++;
          previousLine = currentLine;
        }
      }
    }
    getContext().peekSourceCode().add(CxxMetric.LINES_OF_CODE_IN_FUNCTION_BODY, lines);
  }

}
