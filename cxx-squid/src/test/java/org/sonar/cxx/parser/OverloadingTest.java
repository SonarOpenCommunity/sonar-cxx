/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import org.junit.Test;
import static org.sonar.sslr.tests.Assertions.assertThat;

public class OverloadingTest extends ParserBaseTestHelper {

  @Test
  public void operatorFunctionId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.operatorFunctionId));

    assertThat(p).matches("operator()");
  }

  @Test
  public void operator() {
    p.setRootRule(g.rule(CxxGrammarImpl.operator));

    assertThat(p).matches("new");
    assertThat(p).matches("new[]");
    assertThat(p).matches("delete[]");
    assertThat(p).matches("()");
    assertThat(p).matches("[]");
  }
}
