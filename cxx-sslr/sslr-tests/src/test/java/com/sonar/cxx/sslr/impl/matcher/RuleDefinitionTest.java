/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
package com.sonar.cxx.sslr.impl.matcher;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleDefinitionTest {

  @Test
  void testEmptyIs() {
    var javaClassDefinition = new RuleDefinition("JavaClassDefinition");
    var thrown = catchThrowableOfType(javaClassDefinition::is, IllegalStateException.class);
    assertThat(thrown.getMessage()).isEqualTo("The rule 'JavaClassDefinition' should at least contains one matcher.");
  }

  @Test
  void testMoreThanOneDefinitionForASigleRuleWithIs() {
    var javaClassDefinition = new RuleDefinition("JavaClassDefinition");
    javaClassDefinition.is("option1");
    var thrown = catchThrowableOfType(
      () -> javaClassDefinition.is("option2"),
      IllegalStateException.class
    );
    assertThat(thrown)
      .hasMessage("The rule 'JavaClassDefinition' has already been defined somewhere in the grammar.");
  }

  @Test
  void testSkipFromAst() {
    var ruleBuilder = new RuleDefinition("MyRule");
    assertThat(ruleBuilder.hasToBeSkippedFromAst(null)).isFalse();

    ruleBuilder.skip();
    assertThat(ruleBuilder.hasToBeSkippedFromAst(null)).isTrue();
  }

  @Test
  void testSkipFromAstIf() {
    var ruleBuilder = new RuleDefinition("MyRule");
    ruleBuilder.skipIfOneChild();

    var tokenParent = mock(Token.class);
    when(tokenParent.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(tokenParent.getValue()).thenReturn("parent");
    var parent = new AstNode(tokenParent);

    var tokenChild1 = mock(Token.class);
    when(tokenChild1.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(tokenChild1.getValue()).thenReturn("child1");
    var child1 = new AstNode(tokenChild1);

    var tokenChild2 = mock(Token.class);
    when(tokenChild2.getType()).thenReturn(GenericTokenType.IDENTIFIER);
    when(tokenChild2.getValue()).thenReturn("child2");
    var child2 = new AstNode(tokenChild2);

    parent.addChild(child1);
    parent.addChild(child2);
    child1.addChild(child2);

    assertThat(ruleBuilder.hasToBeSkippedFromAst(parent)).isFalse();
    assertThat(ruleBuilder.hasToBeSkippedFromAst(child2)).isFalse();
    assertThat(ruleBuilder.hasToBeSkippedFromAst(child1)).isTrue();
  }
}
