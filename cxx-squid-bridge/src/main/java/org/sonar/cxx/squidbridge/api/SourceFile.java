/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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
package org.sonar.cxx.squidbridge.api;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines a source file node in the SourceCode tree.
 *
 * @see SourceCode
 */
public class SourceFile extends SourceCode {

  private final Set<Integer> noSonarTagLines = new HashSet<>();

  /**
   * Initializes a newly created source file node.
   *
   * @param key key of the file
   */
  public SourceFile(String key) {
    super(key);
    setStartAtLine(1);
  }

  /**
   * Initializes a newly created source file node.
   *
   * @param key key of the file
   * @param fileName name of the file
   */
  public SourceFile(String key, String fileName) {
    super(key, fileName);
    setStartAtLine(1);
  }

  /**
   * Mark one line in the file //NOSONAR.
   *
   * @param line single line that should be marked //NOSONAR
   */
  public void addNoSonarTagLine(int line) {
    noSonarTagLines.add(line);
  }

  /**
   * Check if a line in the source file is marked with //NOSONAR.
   *
   * @param lineNumber line number to check
   * @return true if the source code line is marked with //NOSONAR
   */
  public boolean hasNoSonarTagAtLine(int lineNumber) {
    return noSonarTagLines.contains(lineNumber);
  }

  /**
   * Mark some lines in the file //NOSONAR.
   *
   * @param noSonarTagLines collection of lines that should be marked //NOSONAR
   */
  public void addNoSonarTagLines(Set<Integer> noSonarTagLines) {
    this.noSonarTagLines.addAll(noSonarTagLines);
  }

  /**
   * Get all lines marked with //NOSONAR
   *
   * @return container with all lines marked with //NOSONAR
   */
  public Set<Integer> getNoSonarTagLines() {
    return noSonarTagLines;
  }

}
