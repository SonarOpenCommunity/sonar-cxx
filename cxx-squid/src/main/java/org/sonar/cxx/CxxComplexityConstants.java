/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.AstNodeType;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.parser.CxxGrammarImpl;

public final class CxxComplexityConstants {

  /**
   * From original SonarQube documentation: <blockquote>The complexity is measured by the number of if, while, do, for,
   * ?:, catch, switch, case statements, and operators && and || (plus one) in the body of a constructor, method, static
   * initializer, or instance initializer.
   * </blockquote>
   *
   * @see CyclomaticComplexityExclusionAstNodeTypes
   */
  private static final AstNodeType[] CYCLOMATIC_COMPLEXITY_TYPES = new AstNodeType[]{
    CxxGrammarImpl.functionDefinition,
    CxxKeyword.IF,
    CxxKeyword.FOR,
    CxxKeyword.WHILE,
    CxxKeyword.CATCH,
    CxxKeyword.CASE,
    CxxKeyword.DEFAULT,
    CxxPunctuator.AND,
    CxxPunctuator.OR,
    CxxKeyword.AND,
    CxxKeyword.OR,
    CxxPunctuator.QUEST
  };

  private CxxComplexityConstants() {
    /* utility class */
  }

  public static AstNodeType[] getCyclomaticComplexityTypes() {
    return CYCLOMATIC_COMPLEXITY_TYPES.clone();
  }

}
