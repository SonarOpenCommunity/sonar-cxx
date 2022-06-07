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

/**
 * Includes other source file into current source file at the line immediately after the directive.
 * <code>
 * #include < h-char-sequence > new-line (1)
 * #include " q-char-sequence " new-line (2)
 * #include pp-tokens new-line (3)
 * </code>
 */
public class PPInclude {

  private static final Logger LOG = Loggers.get(PPInclude.class);

  private final CxxPreprocessor pp;
  private final Lexer fileLexer;
  private final Set<File> analysedFiles = new HashSet<>();
  private final List<Path> standardIncludeDirs = new ArrayList<>();
  private final PPState state;

  private int missingFileCounter = 0;

  public PPInclude(CxxPreprocessor pp, @Nonnull File contextFile) {
    this.pp = pp;
    fileLexer = IncludeDirectiveLexer.create(pp);
    state = PPState.build(contextFile);
  }

  public PPState state() {
    return state;
  }

  /**
   * Define the standard include directories for form (1).
   *
   * Hints:
   * - directories that do not exist are not included in the list to optimize the subsequent search
   *
   * @param includeDirs standard include directories
   * @param baseDir in case directories are relative, they are made absolute to baseDir
   */
  public void setStandardIncludeDirs(List<String> includeDirs, String baseDir) {
    for (var dir : includeDirs) {
      var path = Paths.get(dir);
      try {
        if (!path.isAbsolute()) {
          path = Paths.get(baseDir).resolve(path);
        }
        path = path.toRealPath(); // IOException if the file does not exist

        if (Files.isDirectory(path)) {
          standardIncludeDirs.add(path);
        } else {
          LOG.warn("preprocessor: invalid include file directory '{}'", path.toString());
        }
      } catch (IOException | InvalidPathException e) {
        LOG.error("preprocessor: {} '{}'", e.getMessage(), path.toString());
      }
    }
  }

  public List<Path> getStandardIncludeDirs() {
    return standardIncludeDirs;
  }

  /**
   * Included files have to be scanned with the (only) goal of gathering macros. Process include files using a special
   * lexer, which calls back only if it finds relevant preprocessor directives (#...).
   *
   * @param ast AST node to handle
   * @param token current token
   */
  public void handleFile(AstNode ast, Token token) {
    File includeFile = searchFile(ast);
    if (includeFile == null) {
      missingFileCounter++;
      String rootFilePath = state().getFileUnderAnalysisPath();
      LOG.debug("[" + rootFilePath + ":" + token.getLine() + "]: preprocessor cannot find include file '"
                  + token.getValue() + "'");
    } else if (analysedFiles.add(includeFile.getAbsoluteFile())) {
      state().pushFileState(includeFile);
      try {
        LOG.debug("process include file '{}'", includeFile.getAbsoluteFile());
        fileLexer.lex(getSourceCode(includeFile, pp.getCharset()));
      } catch (IOException e) {
        LOG.error("preprocessor: {} '{}'", e.getMessage(), includeFile.getAbsoluteFile());
      } finally {
        state().popFileState();
      }
    }
  }

  /**
   * Searches for a header and returns the file containing the contents of the header (from AST).
   *
   * @param ast AST node with include body to search for filename
   * @return file containing the contents of the header
   */
  @CheckForNull
  public File searchFile(AstNode ast) {
    AstNode includeBody = ast.getFirstDescendant(
      PPGrammarImpl.includeBody,
      PPGrammarImpl.expandedIncludeBody
    );
    if (includeBody != null) {
      String filename = null;
      var quoted = false;
      includeBody = includeBody.getFirstChild();
      switch ((PPGrammarImpl) includeBody.getType()) {
        case includeBodyBracketed: // (1)
          filename = includeBodyBracketed(includeBody);
          break;
        case includeBodyQuoted: // (2)
          filename = includeBodyQuoted(includeBody);
          quoted = true;
          break;
        case includeBodyFreeform: // (3)
          return includeBodyFreeform(includeBody);
        default:
          break;
      }

      if (filename != null) {
        return searchFile(filename, quoted);
      }
    }

    return null;
  }

  /**
   * Searches for a header and returns the file containing the contents of the header (from filename).
   *
   * Typical implementations search only standard include directories for syntax (1). The standard C++ library and the
   * standard C library are implicitly included in these standard include directories. The standard include directories
   * usually can be controlled by the user through compiler options.
   *
   * The intent of syntax (2) is to search for the files that are not controlled by the implementation. Typical
   * implementations first search the directory where the current file resides then falls back to (1).
   *
   * search order:
   * - Absolute path names are used without modification. Only the specified path is searched.
   * - if quoted, search quoted (fallback bracketed form)
   * - search bracketed form
   *
   * @param filename filename to search for
   * @param quoted true if quoted include filename (else bracketed filename)
   * @return file containing the contents of the header
   */
  @CheckForNull
  public File searchFile(String filename, boolean quoted) {
    File result = null;
    var file = new File(filename);

    if (file.isAbsolute()) {
      if (file.isFile()) {
        result = file;
      }
    } else {
      if (quoted) {
        result = searchQuoted(file);
      }
      if (result == null) {
        result = searchBracketed(filename);
      }
    }

    if (result != null) {
      try {
        result = result.getCanonicalFile();
      } catch (java.io.IOException e) {
        LOG.error("preprocessor: {} '{}'", e.getMessage(), result);
      }
    }

    return result;
  }

  /**
   * Returns the contents of the source file.
   *
   * @param file file to read the contents
   * @param defaultCharset character set to use if file has no BOM
   * @return returns the contents of the file
   */
  public String getSourceCode(File file, Charset defaultCharset) throws IOException {
    try (var bomInputStream = new BOMInputStream(new FileInputStream(file),
                                             ByteOrderMark.UTF_8,
                                             ByteOrderMark.UTF_16LE,
                                             ByteOrderMark.UTF_16BE,
                                             ByteOrderMark.UTF_32LE,
                                             ByteOrderMark.UTF_32BE)) {
      var bom = bomInputStream.getBOM();
      Charset charset = bom != null ? Charset.forName(bom.getCharsetName()) : defaultCharset;
      byte[] bytes = bomInputStream.readAllBytes();
      return new String(bytes, charset);
    }
  }

  public int getMissingFilesCounter() {
    return missingFileCounter;
  }

  /**
   * (1) Search bracketed filename.
   *
   * Search The named source file in the standard include directories in the defined order.
   */
  @CheckForNull
  private File searchBracketed(String filename) {
    for (var path : standardIncludeDirs) {
      var abspath = path.resolve(filename);
      if (Files.isRegularFile(abspath)) {
        return abspath.toFile();
      }
    }
    return null;
  }

  /**
   * (2) Search quoted filename.
   *
   * The named source file is searched for in an implementation-defined manner. If this search is not supported, or if
   * the search fails, the directive is reprocessed as if it reads syntax (1) with the identical contained sequence
   * (including > characters, if any) from the original directive.
   *
   * Searches for include files in this order:
   * 1. In the same directory as the file that contains the #include statement.
   * 2. In the directories of the currently opened include files, in the reverse order in which they were opened. The
   * search begins in the directory of the parent include file and continues upward through the directories of any
   * grandparent include files.
   * 3. Fallback to use standard include directories of bracketed form (1).
   */
  @CheckForNull
  private File searchQuoted(File file) {
    String cwd = state().getFileUnderAnalysis().getParent();
    if (cwd == null) {
      cwd = ".";
    }
    var absPath = new File(new File(cwd), file.getPath());
    if (absPath.isFile()) {
      return absPath;
    }

    for (var include : state().getStack()) {
      if (!include.getFile().equals(state().getContextFile())) {
        absPath = new File(include.getFile().getParentFile(), file.getPath());
        if (absPath.exists()) {
          return absPath;
        }
      }
    }

    return null;
  }

  /**
   * (1) Bracketed: get filename.
   */
  private static String includeBodyBracketed(AstNode includeBody) {
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
   * (2) Quoted: get filename.
   */
  private static String includeBodyQuoted(AstNode includeBody) {
    String value = includeBody.getFirstChild().getTokenValue();
    return value.substring(1, value.length() - 1);
  }

  /**
   * (3) Freeform: The preprocessing tokens after include in the directive are processed just as in normal text (i.e.,
   * each identifier currently defined as a macro name is replaced by its replacement list of preprocessing tokens). If
   * the directive resulting after all replacements does not match one of the two previous forms, the behavior is
   * undefined. The method by which a sequence of preprocessing tokens between a < and a > preprocessing token pair or a
   * pair of " characters is combined into a single header name preprocessing token is implementation-defined.
   */
  @CheckForNull
  private File includeBodyFreeform(AstNode includeBody) {
    String macro = TokenUtils.merge(includeBody.getTokens(), "");
    String filename = TokenUtils.merge(pp.tokenize(macro), "");
    AstNode astNode = pp.lineParser("#include " + filename);
    if ((astNode == null) || (astNode.getFirstDescendant(PPGrammarImpl.includeBodyFreeform) != null)) {
      return null; // stop evaluation if result is again freeform
    }
    return searchFile(astNode);
  }

}
