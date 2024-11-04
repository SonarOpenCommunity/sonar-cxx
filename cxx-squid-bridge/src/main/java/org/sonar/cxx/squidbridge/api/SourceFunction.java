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
package org.sonar.cxx.squidbridge.api; // cxx: in use

/**
 * Defines a function node in the SourceCode tree.
 *
 * @see SourceCode
 */
public class SourceFunction extends SourceCode {

  /**
   * Initializes a newly created function node.
   *
   * @param key key of the function
   */
  public SourceFunction(String key) {
    super(key);
  }

  /**
   * Initializes a newly created function node. Key is a combination of file key and method signature.
   *
   * @param sourceFile file the method belongs to
   * @param functionSignature signature of the function
   * @param startAtLine position in the source file
   */
  public SourceFunction(SourceFile sourceFile, String functionSignature, int startAtLine) {
    super(sourceFile.getKey() + "#" + functionSignature, functionSignature);
    setStartAtLine(startAtLine);
  }
}
