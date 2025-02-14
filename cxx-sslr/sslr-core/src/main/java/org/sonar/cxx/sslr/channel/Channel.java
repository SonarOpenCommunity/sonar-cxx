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
package org.sonar.cxx.sslr.channel; // cxx: in use

public abstract class Channel<O> {

  /**
   * Tries to consume the character stream at the current reading cursor position (provided by the {@link org.sonar.cxx.sslr.channel.CodeReader}). If
   * the character stream is consumed the method must return true and the OUTPUT object can be fed.
   * 
   * @param code
   *          the handle on the input character stream
   * @param output
   *          the OUTPUT that can be optionally fed by the Channel
   * @return false if the Channel doesn't want to consume the character stream, true otherwise.
   */
  public abstract boolean consume(CodeReader code, O output);
}
