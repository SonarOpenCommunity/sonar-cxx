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
package com.sonar.cxx.sslr.impl.ast;

import com.sonar.cxx.sslr.api.*;
import com.sonar.cxx.sslr.impl.MockTokenType;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class AstWalkerTest {

  private final AstWalker walker = new AstWalker();
  private AstNode ast1;
  private AstNode ast11;
  private AstNode ast12;
  private AstNode ast121;
  private AstNode ast122;
  private AstNode ast13;
  private AstNode astNodeWithToken;
  private Token token;

  private final AstNodeType animal = new AstNodeType() {
  };

  private final AstNodeType dog = new AstNodeType() {
  };

  private final AstNodeType cat = new AstNodeType() {
  };

  private final AstNodeType tiger = new AstNodeType() {
  };

  private final AstVisitor astVisitor = mock(AstVisitor.class);
  private final AstAndTokenVisitor astAndTokenVisitor = mock(AstAndTokenVisitor.class);

  @BeforeEach
  public void init() {
    token = mock(Token.class);
    when(token.getType()).thenReturn(MockTokenType.WORD);
    when(token.getValue()).thenReturn("word");
    ast1 = new AstNode(animal, "1", null);
    ast11 = new AstNode(dog, "11", null);
    ast12 = new AstNode(animal, "12", null);
    ast121 = new AstNode(animal, "121", null);
    ast122 = new AstNode(tiger, "122", null);
    ast13 = new AstNode(cat, "13", null);
    astNodeWithToken = new AstNode(token);

    ast1.addChild(ast11);
    ast1.addChild(ast12);
    ast1.addChild(ast13);
    ast12.addChild(ast121);
    ast12.addChild(ast122);
  }

  @Test
  void testVisitFileAndLeaveFileCalls() {
    when(astVisitor.getAstNodeTypesToVisit()).thenReturn(new ArrayList<>());
    walker.addVisitor(astVisitor);
    walker.walkAndVisit(ast1);
    verify(astVisitor).visitFile(ast1);
    verify(astVisitor).leaveFile(ast1);
    verify(astVisitor, never()).visitNode(ast11);
  }

  @Test
  void testVisitToken() {
    when(astAndTokenVisitor.getAstNodeTypesToVisit()).thenReturn(new ArrayList<>());
    walker.addVisitor(astAndTokenVisitor);
    walker.walkAndVisit(astNodeWithToken);
    verify(astAndTokenVisitor).visitFile(astNodeWithToken);
    verify(astAndTokenVisitor).leaveFile(astNodeWithToken);
    verify(astAndTokenVisitor).visitToken(token);
  }

  @Test
  void testVisitNodeAndLeaveNodeCalls() {
    when(astVisitor.getAstNodeTypesToVisit()).thenReturn(Arrays.asList(tiger));
    walker.addVisitor(astVisitor);
    walker.walkAndVisit(ast1);
    var inOrder = inOrder(astVisitor);
    inOrder.verify(astVisitor).visitNode(ast122);
    inOrder.verify(astVisitor).leaveNode(ast122);
    verify(astVisitor, never()).visitNode(ast11);
  }

  @Test
  void testAddVisitor() {
    var walker = new AstWalker();

    var astNodeType = mock(AstNodeType.class);

    var visitor1 = mock(AstVisitor.class);
    when(visitor1.getAstNodeTypesToVisit()).thenReturn(Arrays.asList(astNodeType));

    var visitor2 = mock(AstVisitor.class);
    when(visitor2.getAstNodeTypesToVisit()).thenReturn(Arrays.asList(astNodeType));

    walker.addVisitor(visitor1);
    walker.addVisitor(visitor2);
  }

}
