/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * The source code provider is responsible for locating source files and getting their content. A source file can be
 * specified both as an absolute and as a relative file system path. In the latter case the scanner searches a list of
 * directories (known to him) for a file with such a name.
 */
public class SourceCodeProvider {

  private static final Logger LOG = Loggers.get(SourceCodeProvider.class);

  private final List<Path> includeRoots = new LinkedList<>();
  private final Deque<State> ppState = new LinkedList<>();
  private final File contextFile;
  private String fileUnderAnalysisPath;

  public SourceCodeProvider(File contextFile) {
    // In case "physical" file is preprocessed, SquidAstVisitorContext::getFile() cannot return null.
    // Did you forget to setup the mock properly?
    Objects.requireNonNull(contextFile, "SquidAstVisitorContext::getFile() must be non-null!");
    pushFileState(contextFile);
    this.contextFile = contextFile;
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
          LOG.debug("add include path: '{}'", path.toString());
          includeRoots.add(path);
        } else {
          LOG.warn("invalid include root '{}'", path.toString());
        }
      } catch (IOException | InvalidPathException e) {
        LOG.error("invalid include root '{}'", path.toString(), e);
      }
    }
  }

  public void pushFileState(File currentFile) {
    ppState.push(new State(currentFile));
    fileUnderAnalysisPath = getFileUnderAnalysis().getAbsolutePath();
  }

  public void popFileState() {
    ppState.pop();
    fileUnderAnalysisPath = getFileUnderAnalysis().getAbsolutePath();
  }

  public void skipBlock(boolean state) {
    ppState.peek().skipBlock = state;
  }

  public boolean doSkipBlock() {
    return ppState.peek().skipBlock;
  }

  public boolean doNotSkipBlock() {
    return !ppState.peek().skipBlock;
  }

  public void expressionWas(boolean state) {
    ppState.peek().expression = state;
  }

  public boolean expressionWasTrue() {
    return ppState.peek().expression;
  }

  public boolean expressionWasFalse() {
    return !ppState.peek().expression;
  }

  public void nestedBlock(int dir) {
    ppState.peek().nestedBlock += dir;
  }

  public boolean isNestedBlock() {
    return ppState.peek().nestedBlock > 0;
  }

  public boolean isNotNestedBlock() {
    return ppState.peek().nestedBlock <= 0;
  }

  public File getFileUnderAnalysis() {
    return ppState.peek().fileUnderAnalysis;
  }

  public String getFileUnderAnalysisPath() {
    return fileUnderAnalysisPath;
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
        String cwd = getFileUnderAnalysis().getParent();
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
          for (var parent : ppState) {
            if (parent.fileUnderAnalysis != contextFile) {
              abspath = new File(parent.fileUnderAnalysis.getParentFile(), file.getPath());
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
          Path abspath = path.resolve(filename);
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
        LOG.error("cannot get canonical form of: '{}'", result, e);
      }
    }

    return result;
  }

  public String getSourceCode(File file, Charset charset) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    return new String(encoded, charset);
  }

  private static class State {

    private boolean skipBlock;
    private boolean expression;
    private int nestedBlock;
    private File fileUnderAnalysis;

    public State(@Nullable File fileUnderAnalysis) {
      this.skipBlock = false;
      this.expression = false;
      this.nestedBlock = 0;
      this.fileUnderAnalysis = fileUnderAnalysis;
    }

  }

}
