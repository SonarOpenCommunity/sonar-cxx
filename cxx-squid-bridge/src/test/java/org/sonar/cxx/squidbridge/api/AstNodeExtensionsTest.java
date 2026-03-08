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
import static org.sonar.cxx.squidbridge.api.AstNodeExtensions.*;
import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.Token;
import org.junit.jupiter.api.Test;

class AstNodeExtensionsTest {

  @Test
  void testFirstToken() {
    var token = createToken("test");
    var node = new AstNode(token);

    assertThat(firstToken(node)).isEqualTo(token);
    assertThat(firstToken(node)).isSameAs(node.getToken());
  }

  @Test
  void testFirstTokenWithNull() {
    assertThat(firstToken(null)).isNull();
  }

  @Test
  void testLastToken() {
    var token = createToken("test");
    var node = new AstNode(token);

    assertThat(lastToken(node)).isEqualTo(token);
    assertThat(lastToken(node)).isSameAs(node.getLastToken());
  }

  @Test
  void testLastTokenWithNull() {
    assertThat(lastToken(null)).isNull();
  }

  @Test
  void testLastTokenWithChildren() {
    var parentToken = createToken("parent");
    var childToken = createToken("child");

    var parent = new AstNode(parentToken);
    var child = new AstNode(childToken);
    parent.addChild(child);

    assertThat(lastToken(parent)).isEqualTo(childToken);
  }

  @Test
  void testParent() {
    var parentNode = new AstNode(createToken("parent"));
    var childNode = new AstNode(createToken("child"));
    parentNode.addChild(childNode);

    assertThat(parent(childNode)).isEqualTo(parentNode);
    assertThat(parent(childNode)).isSameAs(childNode.getParent());
  }

  @Test
  void testParentOfRootNode() {
    var rootNode = new AstNode(createToken("root"));

    assertThat(parent(rootNode)).isNull();
  }

  @Test
  void testParentWithNull() {
    assertThat(parent(null)).isNull();
  }

  @Test
  void testSymbol() {
    var node = new AstNode(createToken("test"));
    var sym = new SourceCodeSymbol("myVar", Symbol.Kind.VARIABLE, null);

    AstNodeSymbolExtension.setSymbol(node, sym);

    assertThat(symbol(node)).isEqualTo(sym);
  }

  @Test
  void testSymbolWithNull() {
    assertThat(symbol(null)).isNull();
  }

  @Test
  void testSymbolNotSet() {
    var node = new AstNode(createToken("test"));

    assertThat(symbol(node)).isNull();
  }

  @Test
  void testIsSingleType() {
    var type = new TestNodeType("TEST");
    var node = new AstNode(type, "test", createToken("test"));

    assertThat(is(node, type)).isTrue();
  }

  @Test
  void testIsMultipleTypes() {
    var type1 = new TestNodeType("TYPE1");
    var type2 = new TestNodeType("TYPE2");
    var node = new AstNode(type1, "test", createToken("test"));

    assertThat(is(node, type1, type2)).isTrue();
    assertThat(is(node, type2)).isFalse();
  }

  @Test
  void testIsWithNull() {
    var type = new TestNodeType("TEST");

    assertThat(is(null, type)).isFalse();
  }

  @Test
  void testSymbolType() {
    var node = new AstNode(createToken("test"));
    var type = Type.Types.of("std::string");
    AstNodeTypeExtension.setType(node, type);

    assertThat(symbolType(node)).isEqualTo(type);
    assertThat(symbolType(node).is("std::string")).isTrue();

    AstNodeTypeExtension.clear();
  }

  @Test
  void testSymbolTypeWithNull() {
    assertThat(symbolType(null)).isNull();
  }

  @Test
  void testSymbolTypeNotSet() {
    var node = new AstNode(createToken("test"));
    assertThat(symbolType(node)).isNull();
  }

  private Token createToken(String value) {
    return Token.builder()
      .setLine(1)
      .setColumn(0)
      .setValueAndOriginalValue(value)
      .setType(new TestTokenType())
      .setURI(java.net.URI.create("file:///test.cpp"))
      .build();
  }

  private static class TestNodeType implements AstNodeType {
    private final String name;

    TestNodeType(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
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
