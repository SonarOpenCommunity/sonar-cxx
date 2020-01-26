/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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
    p.setRootRule(g.rule(CxxGrammarImpl.overloadableOperator));

    assertThat(p).matches("new");
    assertThat(p).matches("new[]");
    assertThat(p).matches("delete[]");
    assertThat(p).matches("()");
    assertThat(p).matches("[]");
  }

  @Test
  public void literalOperatorId_reallife() {
    p.setRootRule(g.rule(CxxGrammarImpl.literalOperatorId));
    
    // operator "" identifier
    //    the identifier to use as the ud-suffix
    assertThat(p).matches("operator \"\" _ud_suffix");
    
    // operator user-defined-string-literal (since C++14) 	
    //   the character sequence "" followed, without a space, by the character
    //   sequence that becomes the ud-suffix
    assertThat(p).matches("operator \"\"if");
    assertThat(p).matches("operator \"\"_ud_suffix");
  }
}
