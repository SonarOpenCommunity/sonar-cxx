/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import com.google.common.collect.Sets;
import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;

import static com.sonar.sslr.api.GenericTokenType.EOL;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.Version;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.squidbridge.SquidAstVisitor;

import static org.sonar.cxx.parser.CxxGrammarImpl.LOG;

/**
 * Visitor that computes {@link CoreMetrics#NCLOC_DATA_KEY} and {@link CoreMetrics#COMMENT_LINES_DATA_KEY} metrics.
 */
public class CxxFileLinesVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private final FileLinesContextFactory fileLinesContextFactory;

  private static final Version SQ_6_2 = Version.create(6, 2);
  private boolean isSQ_6_2_or_newer;
  private Set<Integer> linesOfCode = Sets.newHashSet();
  private Set<Integer> linesOfComments = Sets.newHashSet();
  private Set<Integer> executableLines = Sets.newHashSet();
  private final FileSystem fileSystem;
  private final Map<InputFile, Set<Integer>> allLinesOfCode;

  /**
   * CxxFileLinesVisitor computes Metrics
   * @param fileLinesContextFactory interface for LOC
   * @param fileSystem for sensor
   * @param context of Sensor
   * @param linesOfCode executable lines
   */
  public CxxFileLinesVisitor(FileLinesContextFactory fileLinesContextFactory, FileSystem fileSystem,
                             SensorContext context, Map<InputFile, Set<Integer>> linesOfCode) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.fileSystem = fileSystem;
    this.allLinesOfCode = linesOfCode;
    if (context.getSonarQubeVersion().isGreaterThanOrEqual(SQ_6_2)) {
      isSQ_6_2_or_newer = true;
    }
  }

  @Override
  public void visitToken(Token token) {
    if (token.getType().equals(GenericTokenType.EOF)) {
      return;
    }

    if (!token.getType().equals(EOL)) {
      /* Handle all the lines of the token */
      String[] tokenLines = token.getValue().split("\n", -1);
      for (int line = token.getLine(); line < token.getLine() + tokenLines.length; line++) {
        linesOfCode.add(line);
        executableLines.add(line);
      }
    }

    List<Trivia> trivias = token.getTrivia();
    for (Trivia trivia : trivias) {
      if (trivia.isComment()) {
        linesOfComments.add(trivia.getToken().getLine());
      }
    }
  }

  @Override
  public void leaveFile(AstNode astNode) {
    InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().is(getContext().getFile()));
    if (inputFile == null){
      throw new IllegalStateException("InputFile is null, but it should not be.");
    }
    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);

    int fileLength = getContext().peekSourceCode().getInt(CxxMetric.LINES);
    LOG.debug("file lines = {}", fileLength);
    for (int line = 1; line <= fileLength; line++) {
      setCoreMetric(fileLinesContext, linesOfCode.contains(line), line, CoreMetrics.NCLOC_DATA_KEY);
      setCoreMetric(fileLinesContext, linesOfComments.contains(line), line, CoreMetrics.COMMENT_LINES_DATA_KEY);
      if(isSQ_6_2_or_newer) {
        setCoreMetric(fileLinesContext, executableLines.contains(line), line, CoreMetrics.EXECUTABLE_LINES_DATA_KEY);
      }
    }
    fileLinesContext.save();

    allLinesOfCode.put(inputFile, linesOfCode);

    linesOfCode.clear();
    linesOfComments.clear();
    executableLines.clear();
  }

  /**
   * @param fileLinesContext
   * @param line
   */
  private void setCoreMetric(FileLinesContext fileLinesContext, boolean exists, int line, String metricKey) {
    if (exists) {
      fileLinesContext.setIntValue(metricKey, line, 1);
    } else {
      fileLinesContext.setIntValue(metricKey, line, 0);
    }
  }

}
