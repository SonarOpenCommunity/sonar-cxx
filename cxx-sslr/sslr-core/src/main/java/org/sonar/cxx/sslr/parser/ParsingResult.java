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
package org.sonar.cxx.sslr.parser; // cxx: in use

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.internal.matchers.InputBuffer;
import org.sonar.cxx.sslr.internal.matchers.ParseNode;

/**
 * Parsing result.
 *
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.</p>
 *
 * @since 1.16
 */
public class ParsingResult {

  private final boolean matched;
  private final ParseNode parseTreeRoot;
  private final InputBuffer inputBuffer;
  private final ParseError parseError;

  public ParsingResult(@Nonnull InputBuffer inputBuffer, boolean matched, @Nullable ParseNode parseTreeRoot,
                       @Nullable ParseError parseError) {
    this.inputBuffer = Objects.requireNonNull(inputBuffer, "inputBuffer");
    this.matched = matched;
    this.parseTreeRoot = parseTreeRoot;
    this.parseError = parseError;
  }

  public InputBuffer getInputBuffer() {
    return inputBuffer;
  }

  public boolean isMatched() {
    return matched;
  }

  public ParseError getParseError() {
    return parseError;
  }

  // @VisibleForTesting
  public ParseNode getParseTreeRoot() {
    return parseTreeRoot;
  }

}
