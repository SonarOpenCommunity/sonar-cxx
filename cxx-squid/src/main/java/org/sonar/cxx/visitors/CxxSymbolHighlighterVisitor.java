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
package org.sonar.cxx.visitors;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.api.Symbol;
import org.sonar.cxx.squidbridge.api.SymbolTable;

/**
 * Publishes cross-reference symbol highlighting data: for every symbol resolved by {@link
 * CxxSymbolResolverVisitor}, the location of its declaration and of every usage (closes
 * SymbolTable support #1401). Runs after {@link CxxSymbolResolverVisitor} in the same AST walk.
 */
public class CxxSymbolHighlighterVisitor<G extends Grammar> extends SquidAstVisitor<G> {

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    SymbolTable rootScope = getContext().getSymbolTable();
    if (rootScope == null) {
      return;
    }
    List<SymbolReference> references = new ArrayList<>();
    collectSymbolReferences(rootScope, references);
    getContext().peekSourceCode().addData(CxxMetric.SYMBOL_TABLE_DATA, references);
  }

  /**
   * Walks a scope and its descendants, collecting one {@link SymbolReference} per symbol with a
   * resolvable declaration and at least one usage. Macro-expanded tokens
   * ({@link Token#isGeneratedCode()}) are skipped, matching {@link CxxHighlighterVisitor}: their
   * reported position can fall outside the real source line's character range.
   */
  private static void collectSymbolReferences(SymbolTable scope, List<SymbolReference> out) {
    for (Symbol symbol : scope.getSymbols()) {
      AstNode declarationNode = symbol.declaration();
      if (declarationNode == null || declarationNode.getToken().isGeneratedCode()
          || symbol.usages().isEmpty()) {
        continue;
      }
      List<TokenSpan> usageSpans = new ArrayList<>();
      for (Symbol.Usage usage : symbol.usages()) {
        AstNode usageNode = usage.node();
        if (usageNode != null && !usageNode.getToken().isGeneratedCode()) {
          usageSpans.add(TokenSpan.of(usageNode));
        }
      }
      if (!usageSpans.isEmpty()) {
        out.add(new SymbolReference(TokenSpan.of(declarationNode), usageSpans));
      }
    }
    for (SymbolTable child : scope.getChildren()) {
      collectSymbolReferences(child, out);
    }
  }

  /** One resolved symbol's declaration span plus every usage span. */
  public static final class SymbolReference {

    private final TokenSpan declaration;
    private final List<TokenSpan> usages;

    SymbolReference(TokenSpan declaration, List<TokenSpan> usages) {
      this.declaration = declaration;
      this.usages = usages;
    }

    public TokenSpan declaration() {
      return declaration;
    }

    public List<TokenSpan> usages() {
      return usages;
    }
  }

  /** A single token's source location, in the shape {@code NewSymbolTable} expects. */
  public static final class TokenSpan {

    private final int startLine;
    private final int startLineOffset;
    private final int endLine;
    private final int endLineOffset;

    private TokenSpan(int startLine, int startLineOffset, int endLine, int endLineOffset) {
      this.startLine = startLine;
      this.startLineOffset = startLineOffset;
      this.endLine = endLine;
      this.endLineOffset = endLineOffset;
    }

    static TokenSpan of(AstNode node) {
      Token token = node.getToken();
      int line = token.getLine();
      int column = token.getColumn();
      return new TokenSpan(line, column, line, column + token.getValue().length());
    }

    public int startLine() {
      return startLine;
    }

    public int startLineOffset() {
      return startLineOffset;
    }

    public int endLine() {
      return endLine;
    }

    public int endLineOffset() {
      return endLineOffset;
    }
  }
}
