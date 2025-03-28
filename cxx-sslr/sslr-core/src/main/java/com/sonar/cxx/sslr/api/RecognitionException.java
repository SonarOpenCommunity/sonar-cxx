/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package com.sonar.cxx.sslr.api; // cxx: in use

import com.sonar.cxx.sslr.impl.LexerException;

/**
 * Exception that can be thrown during the operation of the parser.
 *
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.</p>
 */
public class RecognitionException extends RuntimeException {

  private final int line;

  /**
   * Constructs a new parser exception.
   *
   * @param e LexerException to wrap
   * @see LexerException
   */
  public RecognitionException(LexerException e) {
    super("Lexer error: " + e.getMessage(), e);
    this.line = 0;
  }

  /**
   * Constructs a new parser exception.
   *
   * @param line source code line where the parsing error has occurred
   * @param message the detail message
   *
   * @since 1.16
   */
  public RecognitionException(int line, String message) {
    super(message);
    this.line = line;
  }

  /**
   * Constructs a new parser exception.
   *
   * @param line source code line where the parsing error has occurred
   * @param message the detail message
   * @param cause the cause which is saved for later retrieval by the {@link #getCause()} method
   *
   * @since 1.16
   */
  public RecognitionException(int line, String message, Throwable cause) {
    super(message, cause);
    this.line = line;
  }

  /**
   * Source code line where the parsing error has occurred.
   *
   * @return line
   */
  public int getLine() {
    return line;
  }

}
