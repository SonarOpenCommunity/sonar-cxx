/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2025 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.io.File;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.cxx.squidbridge.api.CheckMessage;
import org.sonar.cxx.squidbridge.api.CodeCheck;
import org.sonar.cxx.squidbridge.api.SourceCode;

public abstract class SquidAstVisitorContext<G extends Grammar> {

  public abstract File getFile();

  public abstract InputFile getInputFile();

  public abstract String getInputFileContent();

  public abstract List<String> getInputFileLines();

  public abstract G getGrammar();

  public abstract void addSourceCode(SourceCode child);

  public abstract void popSourceCode();

  public abstract SourceCode peekSourceCode();

  public abstract CommentAnalyser getCommentAnalyser();

  /**
   * Create a new file violation
   *
   * @param check the check which is creating this new violation (i.e. this function's caller)
   * @param message message describing the violation, can be formatted (see java.text.MessageFormat)
   * @param messageParameters optional message parameters (see java.text.MessageFormat)
   */
  public abstract void createFileViolation(CodeCheck check, String message, Object... messageParameters);

  /**
   * Create a new line violation caused by a given AST node
   *
   * @param check the check which is creating this new violation (i.e. this function's caller)
   * @param message message describing the violation, can be formatted (see java.text.MessageFormat)
   * @param node AST node which causing the violation
   * @param messageParameters optional message parameters (see java.text.MessageFormat)
   */
  public abstract void createLineViolation(CodeCheck check, String message, AstNode node, Object... messageParameters);

  /**
   * Create a new line violation caused by a given token
   *
   * @param check the check which is creating this new violation (i.e. this function's caller)
   * @param message message describing the violation, can be formatted (see java.text.MessageFormat)
   * @param token Token which causing the violation
   * @param messageParameters optional message parameters (see java.text.MessageFormat)
   */
  public abstract void createLineViolation(CodeCheck check, String message, Token token, Object... messageParameters);

  /**
   * Create a new line violation, not directly caused by an AST node nor a Token
   *
   * @param check the check which is creating this new violation (i.e. this function's caller)
   * @param message message describing the violation, can be formatted (see java.text.MessageFormat)
   * @param line line on which the violation must be created.
   * If zero or a negative number is passed, a file violation will be created instead of a line one
   * @param messageParameters optional message parameters (see java.text.MessageFormat)
   */
  public abstract void createLineViolation(CodeCheck check, String message, int line, Object... messageParameters);

  public abstract void log(CheckMessage message);

}
