/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.cxx.squidbridge.api.CheckMessage;
import org.sonar.cxx.squidbridge.api.CodeCheck;
import org.sonar.cxx.squidbridge.api.SourceCode;
import org.sonar.cxx.squidbridge.api.SourceFile;
import org.sonar.cxx.squidbridge.api.SourceProject;
import org.sonar.cxx.squidbridge.measures.MetricDef;

public class SquidAstVisitorContextImpl<G extends Grammar> extends SquidAstVisitorContext<G> {

  private final Deque<SourceCode> sourceCodeStack = new ArrayDeque<SourceCode>();
  private G grammar;
  private File file;
  private InputFile inputFile;
  private final SourceProject project;
  private CommentAnalyser commentAnalyser;

  public SquidAstVisitorContextImpl(SourceProject project) {
    if (project == null) {
      throw new IllegalArgumentException("project cannot be null.");
    }

    this.project = project;
    sourceCodeStack.push(project);
  }

  public void setGrammar(G grammar) {
    this.grammar = grammar;
  }

  public void setCommentAnalyser(CommentAnalyser commentAnalyser) {
    this.commentAnalyser = commentAnalyser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommentAnalyser getCommentAnalyser() {
    return commentAnalyser;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSourceCode(SourceCode child) {
    peekSourceCode().addChild(child);
    sourceCodeStack.push(child);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void popSourceCode() {
    sourceCodeStack.pop();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SourceCode peekSourceCode() {
    return sourceCodeStack.peek();
  }

  public void setFile(File file, MetricDef filesMetric) {
    popTillSourceProject();
    this.file = file;
    SourceFile sourceFile = new SourceFile(file.getAbsolutePath(), file.getName());
    addSourceCode(sourceFile);
    peekSourceCode().setMeasure(filesMetric, 1);
  }

  public void setInputFile(InputFile inputFile, MetricDef filesMetric) {
    this.inputFile = inputFile;
    setFile(new File(inputFile.uri().getPath()), filesMetric);
  }

  protected void popTillSourceProject() {
    while (!(peekSourceCode() instanceof SourceProject)) {
      popSourceCode();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public File getFile() {
    return file;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public InputFile getInputFile() {
    return inputFile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getInputFileContent() {
    try {
      return inputFile.contents();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getInputFileLines() {
    return Arrays.asList(getInputFileContent().split("(?:\r)?\n|\r", -1));
  }

  public SourceProject getProject() {
    return project;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public G getGrammar() {
    return grammar;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createFileViolation(CodeCheck check, String message, Object... messageParameters) {
    createLineViolation(check, message, -1, messageParameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createLineViolation(CodeCheck check, String message, AstNode node, Object... messageParameters) {
    createLineViolation(check, message, node.getToken(), messageParameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createLineViolation(CodeCheck check, String message, Token token, Object... messageParameters) {
    createLineViolation(check, message, token.getLine(), messageParameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createLineViolation(CodeCheck check, String message, int line, Object... messageParameters) {
    CheckMessage checkMessage = new CheckMessage((Object) check, message, messageParameters);
    if (line > 0) {
      checkMessage.setLine(line);
    }
    log(checkMessage);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void log(CheckMessage message) {
    if (peekSourceCode() instanceof SourceFile) {
      peekSourceCode().log(message);
    } else if (peekSourceCode().getParent(SourceFile.class) != null) {
      peekSourceCode().getParent(SourceFile.class).log(message);
    } else {
      throw new IllegalStateException("Unable to log a check message on source code '"
                                        + (peekSourceCode() == null ? "[NULL]" : peekSourceCode().getKey()) + "'");
    }
  }

}
