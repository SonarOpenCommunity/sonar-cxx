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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PPInclude {

  private static final Logger LOG = Loggers.get(PPInclude.class);
  private static int missingFileCounter = 0;

  private final CxxPreprocessor pp;
  private final Lexer fileLexer;
  private final Set<File> analysedFiles = new HashSet<>();
  private final List<Path> includeRoots = new ArrayList<>();
  private final PPState state;

  public PPInclude(CxxPreprocessor pp, @Nonnull File contextFile) {
    this.pp = pp;
    fileLexer = IncludeDirectiveLexer.create(pp);
    state = PPState.build(contextFile);
  }

  public PPState State() {
    return state;
  }

  public void setIncludeRoots(List<String> roots, String baseDir) {
    for (var root : roots) {
      var path = Paths.get(root);
      try {
        if (!path.isAbsolute()) {
          path = Paths.get(baseDir).resolve(path);
        }
        path = path.toRealPath(); // IOException if the file does not exist

        if (Files.isDirectory(path)) {
          includeRoots.add(path);
        } else {
          LOG.warn("preprocessor: invalid include file directory '{}'", path.toString());
        }
      } catch (IOException | InvalidPathException e) {
        LOG.error("preprocessor: invalid include file directory '{}'", path.toString());
      }
    }
  }

  public List<Path> getIncludeRoots() {
    return includeRoots;
  }

  /**
   * Included files have to be scanned with the (only) goal of gathering macros. Process include files using a special
   * lexer, which calls back only if it finds relevant preprocessor directives (#...).
   */
  public void handleFile(AstNode ast, Token token) {
    File includeFile = findFile(ast);
    if (includeFile == null) {
      missingFileCounter++;
      String rootFilePath = State().getFileUnderAnalysisPath();
      LOG.debug("[" + rootFilePath + ":" + token.getLine() + "]: preprocessor cannot find include file '"
                  + token.getValue() + "'");
    } else if (analysedFiles.add(includeFile.getAbsoluteFile())) {
      State().pushFileState(includeFile);
      try {
        LOG.debug("process include file '{}'", includeFile.getAbsoluteFile());
        fileLexer.lex(getSourceCode(includeFile, pp.getCharset()));
      } catch (IOException e) {
        LOG.error("[{}: preprocessor cannot read include file]: {}", includeFile.getAbsoluteFile(), e.getMessage());
      } finally {
        State().popFileState();
      }
    }
  }

  /**
   * Extract the filename out of the include body and try to open the file.
   */
  @CheckForNull
  public File findFile(AstNode ast) {
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
        return getSourceCodeFile(filename, quoted);
      }
    }

    return null;
  }

  @CheckForNull
  public File getSourceCodeFile(String filename, boolean quoted) {
    File result = null;
    var file = new File(filename);

    // If the file name is fully specified for an include file that has a path that includes a colon
    // (for example F:\MSVC\SPECIAL\INCL\TEST.H) the preprocessor follows the path.
    if (file.isAbsolute()) {
      if (file.isFile()) {
        result = file;
      }
    } else {
      if (quoted) {
        // Quoted form: The preprocessor searches for include files in this order:
        String cwd = State().getFileUnderAnalysis().getParent();
        if (cwd == null) {
          cwd = ".";
        }
        var abspath = new File(new File(cwd), file.getPath());
        if (abspath.isFile()) {
          // 1) In the same directory as the file that contains the #include statement.
          result = abspath;
        } else {
          result = null; // 3) fallback to use include paths instead of local folder

          // 2) In the directories of the currently opened include files, in the reverse order in which they were opened.
          //    The search begins in the directory of the parent include file and continues upward through the
          //    directories of any grandparent include files.
          for (var parent : State().getStack()) {
            if (parent.getFile() != State().getContextFile()) {
              abspath = new File(parent.getFile().getParentFile(), file.getPath());
              if (abspath.exists()) {
                result = abspath;
                break;
              }
            }
          }
        }
      }

      // Angle-bracket form: lookup relative to to the include roots.
      // The quoted case falls back to this, if its special handling wasn't successful.
      if (result == null) {
        for (var path : includeRoots) {
          var abspath = path.resolve(filename);
          if (Files.isRegularFile(abspath)) {
            result = abspath.toFile();
            break;
          }
        }
      }
    }

    if (result != null) {
      try {
        result = result.getCanonicalFile();
      } catch (java.io.IOException e) {
        LOG.error("preprocessor: cannot get canonical form of: '{}'", result);
      }
    }

    return result;
  }

  public String getSourceCode(File file, Charset defaultCharset) throws IOException {
    try (var bomInputStream = new BOMInputStream(new FileInputStream(file),
                                             ByteOrderMark.UTF_8,
                                             ByteOrderMark.UTF_16LE,
                                             ByteOrderMark.UTF_16BE,
                                             ByteOrderMark.UTF_32LE,
                                             ByteOrderMark.UTF_32BE)) {
      ByteOrderMark bom = bomInputStream.getBOM();
      Charset charset = bom != null ? Charset.forName(bom.getCharsetName()) : defaultCharset;
      byte[] bytes = bomInputStream.readAllBytes();
      return new String(bytes, charset);
    }
  }

  public static int getMissingFilesCounter() {
    return missingFileCounter;
  }

  public static void resetMissingFilesCounter() {
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
