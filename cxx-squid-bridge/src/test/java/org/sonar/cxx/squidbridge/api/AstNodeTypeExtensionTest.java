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

class AstNodeTypeExtensionTest {

  @AfterEach
  void cleanup() {
    AstNodeTypeExtension.clear();
    AstNodeSymbolExtension.clear();
  }

  @Test
  void testSetAndGetType() {
    var node = createNode();
    var type = Type.Types.of("std::string");

    AstNodeTypeExtension.setType(node, type);

    assertThat(AstNodeTypeExtension.getType(node)).isEqualTo(type);
    assertThat(AstNodeTypeExtension.hasType(node)).isTrue();
    assertThat(AstNodeTypeExtension.hasExplicitType(node)).isTrue();
  }

  @Test
  void testGetTypeReturnsNullWhenNotSet() {
    var node = createNode();

    assertThat(AstNodeTypeExtension.getType(node)).isNull();
    assertThat(AstNodeTypeExtension.hasType(node)).isFalse();
    assertThat(AstNodeTypeExtension.hasExplicitType(node)).isFalse();
  }

  @Test
  void testGetTypeWithNullNode() {
    assertThat(AstNodeTypeExtension.getType(null)).isNull();
    assertThat(AstNodeTypeExtension.hasType(null)).isFalse();
    assertThat(AstNodeTypeExtension.hasExplicitType(null)).isFalse();
  }

  @Test
  void testSetTypeWithNullNode() {
    var type = Type.Types.of("int");
    AstNodeTypeExtension.setType(null, type);
    assertThat(AstNodeTypeExtension.size()).isEqualTo(0);
  }

  @Test
  void testSetTypeWithNullType() {
    var node = createNode();
    AstNodeTypeExtension.setType(node, null);
    assertThat(AstNodeTypeExtension.hasExplicitType(node)).isFalse();
  }

  @Test
  void testFallbackToSymbolDerivedType() {
    var node = createNode();
    var symbol = new SourceCodeSymbol("myVar", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(node, symbol);

    // No explicit type set, but symbol is available
    assertThat(AstNodeTypeExtension.hasExplicitType(node)).isFalse();
    assertThat(AstNodeTypeExtension.hasType(node)).isTrue();

    var type = AstNodeTypeExtension.getType(node);
    assertThat(type).isNotNull();
    assertThat(type.fullyQualifiedName()).isEqualTo("myVar");
  }

  @Test
  void testExplicitTypeTakesPrecedence() {
    var node = createNode();

    // Set both symbol and explicit type
    var symbol = new SourceCodeSymbol("symbolName", Symbol.Kind.VARIABLE, null);
    AstNodeSymbolExtension.setSymbol(node, symbol);

    var explicitType = Type.Types.of("std::string");
    AstNodeTypeExtension.setType(node, explicitType);

    // Explicit type should win
    var type = AstNodeTypeExtension.getType(node);
    assertThat(type).isEqualTo(explicitType);
    assertThat(type.is("std::string")).isTrue();
  }

  @Test
  void testRemoveType() {
    var node = createNode();
    var type = Type.Types.of("int");

    AstNodeTypeExtension.setType(node, type);
    assertThat(AstNodeTypeExtension.hasExplicitType(node)).isTrue();

    AstNodeTypeExtension.removeType(node);
    assertThat(AstNodeTypeExtension.hasExplicitType(node)).isFalse();
  }

  @Test
  void testClear() {
    var node1 = createNode();
    var node2 = createNode();

    AstNodeTypeExtension.setType(node1, Type.Types.of("int"));
    AstNodeTypeExtension.setType(node2, Type.Types.of("double"));
    assertThat(AstNodeTypeExtension.size()).isEqualTo(2);

    AstNodeTypeExtension.clear();
    assertThat(AstNodeTypeExtension.size()).isEqualTo(0);
    assertThat(AstNodeTypeExtension.hasExplicitType(node1)).isFalse();
    assertThat(AstNodeTypeExtension.hasExplicitType(node2)).isFalse();
  }

  @Test
  void testOverwriteType() {
    var node = createNode();
    var type1 = Type.Types.of("int");
    var type2 = Type.Types.of("double");

    AstNodeTypeExtension.setType(node, type1);
    assertThat(AstNodeTypeExtension.getType(node)).isEqualTo(type1);

    AstNodeTypeExtension.setType(node, type2);
    assertThat(AstNodeTypeExtension.getType(node)).isEqualTo(type2);
    assertThat(AstNodeTypeExtension.size()).isEqualTo(1);
  }

  @Test
  void testUnknownSymbolDoesNotDeriveType() {
    var node = createNode();
    AstNodeSymbolExtension.setSymbol(node, Symbol.UNKNOWN_SYMBOL);

    assertThat(AstNodeTypeExtension.getType(node)).isNull();
    assertThat(AstNodeTypeExtension.hasType(node)).isFalse();
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
