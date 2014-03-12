/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
/*
 * derived from Sonar .NET Plugin
 * Authors :: Jose Chillan, Alexandre Victoor and SonarSource
 */
/**
 * 
 */
package org.sonar.plugins.cxx.api;

/**
 * Generic exception class for the Cxx api
 * 
 */
public class CxxException extends Exception {

  private static final long serialVersionUID = -2730236966462112507L;

  /**
   * Creates a {@link CxxException}
   * 
   * @param message
   *          the message
   * @param cause
   *          the cause
   */
  public CxxException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a {@link CxxException}
   * 
   * @param cause
   *          the cause
   */
  public CxxException(String cause) {
    super(cause);
  }

}
