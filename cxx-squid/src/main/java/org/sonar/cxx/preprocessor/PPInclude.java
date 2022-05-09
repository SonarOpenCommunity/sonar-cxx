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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.io.File;
import javax.annotation.CheckForNull;

class PPInclude {

  private CxxPreprocessor pp;

  public PPInclude(CxxPreprocessor pp) {
    this.pp = pp;
  }

  @CheckForNull
  File findIncludedFile(AstNode ast) {
    String includedFileName = null;
    var quoted = false;

    var node = ast.getFirstDescendant(PPGrammarImpl.includeBodyQuoted);
    if (node != null) {
      String value = node.getFirstChild().getTokenValue();
      includedFileName = value.substring(1, value.length() - 1);
      quoted = true;
    } else if ((node = ast.getFirstDescendant(PPGrammarImpl.includeBodyBracketed)) != null) {
      node = node.getFirstDescendant(PPPunctuator.LT).getNextSibling();
      var sb = new StringBuilder(256);
      while (true) {
        String value = node.getTokenValue();
        if (">".equals(value)) {
          break;
        }
        sb.append(value);
        node = node.getNextSibling();
      }

      includedFileName = sb.toString();
    } else if ((node = ast.getFirstDescendant(PPGrammarImpl.includeBodyFreeform)) != null) {
      // expand and recurse
      String includeBody = TokenUtils.merge(node.getTokens(), "");
      String expandedIncludeBody = TokenUtils.merge(pp.tokenizeMacro("", includeBody), "");

      AstNode astNode = pp.lineParser("#include " + expandedIncludeBody);
      if ((astNode == null) || (astNode.getFirstDescendant(PPGrammarImpl.includeBodyFreeform) != null)) {
        return null;
      }

      return findIncludedFile(astNode);
    }

    if (includedFileName != null) {
      return pp.getCodeProvider().getSourceCodeFile(includedFileName, quoted);
    }

    return null;
  }

}
