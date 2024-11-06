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

import javax.annotation.Nullable;

/**
 * Defines a class node in the SourceCode tree.
 *
 * @see SourceCode
 */
public class SourceClass extends SourceCode {

  /**
   * Initializes a newly created class node.
   *
   * @param key key of the class
   * @param className name of the class
   */
  public SourceClass(String key, @Nullable String className) {
    super(key, className);
  }

  /**
   * Initializes a newly created class node.
   *
   * @param parentKey parent object to use for key generation
   * @param key key of the class
   * @param className name of the class
   *
   * @param startAtLine line the SourceClass object starts
   */
  public SourceClass(SourceCode parentKey, String key, @Nullable String className, int startAtLine) {
    super(parentKey, key, className, startAtLine);
  }

}
