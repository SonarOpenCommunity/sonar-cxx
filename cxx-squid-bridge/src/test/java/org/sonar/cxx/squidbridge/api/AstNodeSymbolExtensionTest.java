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
package org.sonar.cxx.squidbridge.api;

import static org.assertj.core.api.Assertions.*;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AstNodeSymbolExtensionTest {

  @AfterEach
  void cleanup() {
    AstNodeSymbolExtension.clear();
  }

  @Test
  void testSetAndGetSymbol() {
    var node = createNode();
    var symbol = new SourceCodeSymbol("myVar", Symbol.Kind.VARIABLE, null);

    AstNodeSymbolExtension.setSymbol(node, symbol);

    assertThat(AstNodeSymbolExtension.getSymbol(node)).isEqualTo(symbol);
    assertThat(AstNodeSymbolExtension.hasSymbol(node)).isTrue();
  }

  @Test
  void testGetSymbolReturnsNullWhenNotSet() {
    var node = createNode();

    assertThat(AstNodeSymbolExtension.getSymbol(node)).isNull();
    assertThat(AstNodeSymbolExtension.hasSymbol(node)).isFalse();
  }

  @Test
  void testGetSymbolWithNullNode() {
    assertThat(AstNodeSymbolExtension.getSymbol(null)).isNull();
    assertThat(AstNodeSymbolExtension.hasSymbol(null)).isFalse();
  }

  @Test
  void testSetSymbolWithNullNode() {
    var symbol = new SourceCodeSymbol("test", Symbol.Kind.VARIABLE, null);

    AstNodeSymbolExtension.setSymbol(null, symbol);

    assertThat(AstNodeSymbolExtension.size()).isEqualTo(0);
  }

  @Test
  void testSetSymbolWithNullSymbol() {
    var node = createNode();

    AstNodeSymbolExtension.setSymbol(node, null);

    assertThat(AstNodeSymbolExtension.hasSymbol(node)).isFalse();
  }

  @Test
  void testRemoveSymbol() {
    var node = createNode();
    var symbol = new SourceCodeSymbol("myVar", Symbol.Kind.VARIABLE, null);

    AstNodeSymbolExtension.setSymbol(node, symbol);
    assertThat(AstNodeSymbolExtension.hasSymbol(node)).isTrue();

    AstNodeSymbolExtension.removeSymbol(node);
    assertThat(AstNodeSymbolExtension.hasSymbol(node)).isFalse();
  }

  @Test
  void testClear() {
    var node1 = createNode();
    var node2 = createNode();
    var symbol1 = new SourceCodeSymbol("var1", Symbol.Kind.VARIABLE, null);
    var symbol2 = new SourceCodeSymbol("var2", Symbol.Kind.VARIABLE, null);

    AstNodeSymbolExtension.setSymbol(node1, symbol1);
    AstNodeSymbolExtension.setSymbol(node2, symbol2);
    assertThat(AstNodeSymbolExtension.size()).isEqualTo(2);

    AstNodeSymbolExtension.clear();
    assertThat(AstNodeSymbolExtension.size()).isEqualTo(0);
    assertThat(AstNodeSymbolExtension.hasSymbol(node1)).isFalse();
    assertThat(AstNodeSymbolExtension.hasSymbol(node2)).isFalse();
  }

  @Test
  void testMultipleNodes() {
    var node1 = createNode();
    var node2 = createNode();
    var symbol1 = new SourceCodeSymbol("var1", Symbol.Kind.VARIABLE, null);
    var symbol2 = new SourceCodeSymbol("func1", Symbol.Kind.FUNCTION, null);

    AstNodeSymbolExtension.setSymbol(node1, symbol1);
    AstNodeSymbolExtension.setSymbol(node2, symbol2);

    assertThat(AstNodeSymbolExtension.getSymbol(node1)).isEqualTo(symbol1);
    assertThat(AstNodeSymbolExtension.getSymbol(node2)).isEqualTo(symbol2);
    assertThat(AstNodeSymbolExtension.size()).isEqualTo(2);
  }

  @Test
  void testOverwriteSymbol() {
    var node = createNode();
    var symbol1 = new SourceCodeSymbol("var1", Symbol.Kind.VARIABLE, null);
    var symbol2 = new SourceCodeSymbol("var2", Symbol.Kind.VARIABLE, null);

    AstNodeSymbolExtension.setSymbol(node, symbol1);
    assertThat(AstNodeSymbolExtension.getSymbol(node)).isEqualTo(symbol1);

    AstNodeSymbolExtension.setSymbol(node, symbol2);
    assertThat(AstNodeSymbolExtension.getSymbol(node)).isEqualTo(symbol2);
    assertThat(AstNodeSymbolExtension.size()).isEqualTo(1);
  }

  private AstNode createNode() {
    return new AstNode(Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue("test")
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build());
  }

  private static class TestTokenType implements com.sonar.cxx.sslr.api.TokenType {
    @Override
    public String getName() {
      return "TEST";
    }

    @Override
    public String getValue() {
      return "test";
    }

    @Override
    public boolean hasToBeSkippedFromAst(AstNode node) {
      return false;
    }
  }
}
