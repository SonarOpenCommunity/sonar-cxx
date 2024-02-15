/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.nio.charset.Charset;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class PPParserTest {

  @Test
  void testCreate() {
    Parser<Grammar> result = PPParser.create(Charset.defaultCharset());
    assertThat(result.getRootRule().getName()).isEqualTo("preprocessorLine");
  }

  //@Test
  void testParse() {
    Parser<Grammar> result = PPParser.create(Charset.defaultCharset());
    AstNode astNode = result.parse("#define HELLO WORLD")
      .getFirstDescendant(PPGrammarImpl.objectlikeMacroDefinition);

    assertThat(astNode.getChildren())
      .hasSize(3)
      .matches(t -> PPKeyword.DEFINE.equals(t.get(0).getType()))
      .matches(t -> GenericTokenType.IDENTIFIER.equals(t.get(1).getType()))
      .matches(t -> PPGrammarImpl.replacementList.equals(t.get(2).getType()));
  }

}
