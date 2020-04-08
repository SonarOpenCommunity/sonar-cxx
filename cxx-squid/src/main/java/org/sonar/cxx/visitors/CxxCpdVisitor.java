/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.cxx.CxxSquidConfiguration;
import org.sonar.cxx.api.CxxMetric;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;

public class CxxCpdVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private final boolean ignoreLiterals;
  private final boolean ignoreIdentifiers;
  private int isFunctionDefinition;

  private List<CpdToken> cpdTokens = null;

  public CxxCpdVisitor(CxxSquidConfiguration squidConfig) {
    this.ignoreLiterals = squidConfig.getCpdIgnoreLiteral();
    this.ignoreIdentifiers = squidConfig.getCpdIgnoreIdentifier();
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionDefinition);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    cpdTokens = new ArrayList<>();
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    getContext().peekSourceCode().addData(CxxMetric.CPD_TOKENS_DATA, cpdTokens);
    cpdTokens = null;
  }

  @Override
  public void visitNode(AstNode node) {
    isFunctionDefinition++;
  }

  @Override
  public void leaveNode(AstNode node) {
    isFunctionDefinition--;
  }

  @Override
  public void visitToken(Token token) {
    if (isFunctionDefinition > 0 && !token.isGeneratedCode()) {
      String text;
      if (ignoreIdentifiers && token.getType().equals(GenericTokenType.IDENTIFIER)) {
        text = "_I";
      } else if (ignoreLiterals && token.getType().equals(CxxTokenType.NUMBER)) {
        text = "_N";
      } else if (ignoreLiterals && token.getType().equals(CxxTokenType.STRING)) {
        text = "_S";
      } else if (ignoreLiterals && token.getType().equals(CxxTokenType.CHARACTER)) {
        text = "_C";
      } else if (token.getType().equals(GenericTokenType.EOF)) {
        return;
      } else {
        text = token.getValue();
      }

      cpdTokens.add(new CpdToken(token.getLine(), token.getColumn(),
                                 token.getLine(), token.getColumn() + token.getValue().length(),
                                 text));
    }
  }

  public static class CpdToken {

    public int startLine;
    public int startCol;
    public int endLine;
    public int endCol;
    public String token;

    CpdToken(int startLine, int startCol, int endLine, int endCol, String token) {
      this.startLine = startLine;
      this.startCol = startCol;
      this.endLine = endLine;
      this.endCol = endCol;
      this.token = token;
    }
  }

}
