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
package org.sonar.cxx;

import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;

import com.sonar.sslr.api.AstNodeType;

public class CxxComplexityConstants {

  private CxxComplexityConstants() {
    /* utility class */
  }

  /**
   * From original SonarQube documentation: <blockquote>The complexity is
   * measured by the number of if, while, do, for, ?:, catch, switch, case
   * statements, and operators && and || (plus one) in the body of a
   * constructor, method, static initializer, or instance initializer.
   * </blockquote>
   *
   * @see CyclomaticComplexityExclusionAstNodeTypes
   */
  public static final AstNodeType[] CyclomaticComplexityAstNodeTypes = new AstNodeType[] {
      CxxGrammarImpl.functionDefinition,
      CxxKeyword.IF,
      CxxKeyword.FOR,
      CxxKeyword.WHILE,
      CxxKeyword.CATCH,
      CxxKeyword.CASE,
      CxxKeyword.DEFAULT,
      CxxPunctuator.AND,
      CxxPunctuator.OR,
      CxxPunctuator.QUEST };

}
