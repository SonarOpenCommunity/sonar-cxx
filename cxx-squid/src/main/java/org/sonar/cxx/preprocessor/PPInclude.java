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
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

class PPInclude {

  private static final Logger LOG = Loggers.get(PPInclude.class);
  private static int missingFileCounter = 0;

  private final CxxPreprocessor pp;
  private final Lexer fileLexer;
  private final Set<File> analysedFiles = new HashSet<>();

  PPInclude(CxxPreprocessor pp) {
    this.pp = pp;
    fileLexer = IncludeDirectiveLexer.create(pp);
  }

  /**
   * Included files have to be scanned with the (only) goal of gathering macros. Process include files using a special
   * lexer, which calls back only if it finds relevant preprocessor directives (#...).
   */
  void handleFile(AstNode ast, Token token) {
    File includeFile = findFile(ast);
    if (includeFile == null) {
      missingFileCounter++;
      String rootFilePath = pp.getCodeProvider().getFileUnderAnalysisPath();
      LOG.debug("[" + rootFilePath + ":" + token.getLine() + "]: preprocessor cannot find include file '"
                  + token.getValue() + "'");
    } else if (analysedFiles.add(includeFile.getAbsoluteFile())) {
      pp.getCodeProvider().pushFileState(includeFile);
      try {
        LOG.debug("process include file '{}'", includeFile.getAbsoluteFile());
        fileLexer.lex(pp.getCodeProvider().getSourceCode(includeFile, pp.getCharset()));
      } catch (IOException e) {
        LOG.error("[{}: preprocessor cannot read include file]: {}", includeFile.getAbsoluteFile(), e.getMessage());
      } finally {
        pp.getCodeProvider().popFileState();
      }
    }
  }

  /**
   * Extract the filename out of the include body and try to open the file.
   */
  @CheckForNull
  File findFile(AstNode ast) {
    AstNode includeBody = ast.getFirstDescendant(
      PPGrammarImpl.includeBody,
      PPGrammarImpl.expandedIncludeBody
    );
    if (includeBody != null) {
      String filename = null;
      var quoted = false;
      includeBody = includeBody.getFirstChild();
      switch ((PPGrammarImpl) includeBody.getType()) {
        case includeBodyQuoted:
          filename = includeBodyQuoted(includeBody);
          quoted = true;
          break;
        case includeBodyBracketed:
          filename = includeBodyBracketed(includeBody);
          break;
        case includeBodyFreeform:
          return includeBodyFreeform(includeBody);
        default:
          break;
      }

      if (filename != null) {
        return pp.getCodeProvider().getSourceCodeFile(filename, quoted);
      }
    }

    return null;
  }

  void clearAnalyzedFiles() {
    analysedFiles.clear();
  }

  static int getMissingFilesCounter() {
    return missingFileCounter;
  }

  static void resetMissingFilesCounter() {
    missingFileCounter = 0;
  }

  /**
   * Quoted: Extract the filename out of the include body.
   */
  private String includeBodyQuoted(AstNode includeBody) {
    String value = includeBody.getFirstChild().getTokenValue();
    return value.substring(1, value.length() - 1);
  }

  /**
   * Bracketed: Extract the filename out of the include body.
   */
  private String includeBodyBracketed(AstNode includeBody) {
    var next = includeBody.getFirstDescendant(PPPunctuator.LT).getNextSibling();
    var sb = new StringBuilder(256);
    while (true) {
      String value = next.getTokenValue();
      if (">".equals(value)) {
        break;
      }
      sb.append(value);
      next = next.getNextSibling();
    }

    return sb.toString();
  }

  /**
   * Freeform: Pipe the body of the include directive through a lexer to properly expand all macros
   * which may be in there. Extract the filename out of the resulting include body then.
   */
  @CheckForNull
  private File includeBodyFreeform(AstNode includeBody) {
    String macro = TokenUtils.merge(includeBody.getTokens(), "");
    String filename = TokenUtils.merge(pp.tokenizeMacro("", macro), "");
    AstNode astNode = pp.lineParser("#include " + filename);
    if ((astNode == null) || (astNode.getFirstDescendant(PPGrammarImpl.includeBodyFreeform) != null)) {
      return null; // stop evaluation if result is again freeform
    }
    return findFile(astNode);
  }

}
