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
package org.sonar.cxx.squidbridge.api; // cxx: in use

import java.nio.charset.Charset;

/**
 * Configuration for the AST Scanner.
 */
public class SquidConfiguration {

  private Charset charset = Charset.defaultCharset();
  private boolean stopSquidOnException = false;

  public SquidConfiguration() {
  }

  /**
   * Configuration object.
   *
   * @param charset to use for scanning
   */
  public SquidConfiguration(Charset charset) {
    this.charset = charset;
  }

  /**
   * Get default charset to use for scanning.
   *
   * @return default charset to use for scanning
   */
  public Charset getCharset() {
    return charset;
  }

  /**
   * Defines error behavior for scanner.
   *
   * @param stopSquidOnException true, stop on exception
   */
  public void setStopSquidOnException(boolean stopSquidOnException) {
    this.stopSquidOnException = stopSquidOnException;
  }

  /**
   * Get error behavior for scanner.
   *
   * @return true if scanner should stop on exception
   */
  public boolean stopSquidOnException() {
    return stopSquidOnException;
  }

}
