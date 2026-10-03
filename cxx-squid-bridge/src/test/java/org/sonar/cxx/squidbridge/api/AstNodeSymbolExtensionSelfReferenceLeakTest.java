/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.assertThat;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Reproduces the exact registration pattern {@code CxxSymbolResolverVisitor} uses (a symbol whose
 * {@link Symbol#declaration()} is the very node it is registered under in {@link
 * AstNodeSymbolExtension}) and asserts the entry becomes collectible once nothing outside the map
 * holds the node -- i.e. that {@code declarationNode} no longer strongly references its own map
 * key, which used to keep every entry permanently reachable regardless of {@code WeakHashMap}
 * semantics.
 */
class AstNodeSymbolExtensionSelfReferenceLeakTest {

  @AfterEach
  void cleanup() {
    AstNodeSymbolExtension.clear();
    AstNodeTypeExtension.clear();
  }

  @Test
  void selfReferencingSymbolIsCollectedOnceUnreachable() throws InterruptedException {
    registerSelfReferencingSymbolAndDropAllOtherReferences();

    // A strong back-reference from declarationNode would keep this from ever reaching zero.
    boolean collected = false;
    for (int attempt = 0; attempt < 20 && !collected; attempt++) {
      System.gc();
      Thread.sleep(50L);
      collected = AstNodeSymbolExtension.size() == 0;
    }

    assertThat(collected)
      .as("the self-registered symbol's own declaration node must become collectible")
      .isTrue();
  }

  /**
   * Isolated in its own method so the local {@code AstNode}/{@code Symbol} variables go out of
   * scope (and become eligible for collection) as soon as it returns, rather than staying pinned
   * by the test method's own stack frame for the rest of the test.
   */
  private void registerSelfReferencingSymbolAndDropAllOtherReferences() {
    AstNode node = createNode();
    SourceCodeSymbol symbol = new SourceCodeSymbol("selfRef", Symbol.Kind.VARIABLE, null);
    symbol.setDeclaration(node);

    AstNodeSymbolExtension.setSymbol(node, symbol);

    assertThat(AstNodeSymbolExtension.size()).isEqualTo(1);
    assertThat(symbol.declaration()).isSameAs(node);
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
