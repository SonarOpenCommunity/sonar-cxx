/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package org.sonar.cxx.sslr.tests; // cxx: in use

import com.sonar.cxx.sslr.api.Rule;
import com.sonar.cxx.sslr.impl.Parser;

/**
 * @since 1.16
 */
public final class Assertions {

  private Assertions() {
  }

  /**
   * Creates a new instance of <code>{@link RuleAssert}</code>.
   * @param actual the value to be the target of the assertions methods.
   * @return the created assertion object.
   */
  public static RuleAssert assertThat(Rule actual) {
    return new RuleAssert(actual);
  }

  /**
   * Creates a new instance of <code>{@link ParserAssert}</code>.
   * @param actual the value to be the target of the assertions methods.
   * @return the created assertion object.
   */
  public static ParserAssert assertThat(Parser actual) {
    return new ParserAssert(actual);
  }

}
