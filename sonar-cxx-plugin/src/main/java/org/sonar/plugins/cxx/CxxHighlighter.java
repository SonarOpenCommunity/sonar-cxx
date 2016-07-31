/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import javax.annotation.Nullable;
import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.TokenLocation;

public class CxxHighlighter extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private NewHighlighting newHighlighting;
  private final SensorContext context;

  public CxxHighlighter(SensorContext context) {
    this.context = context;
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    newHighlighting = context.newHighlighting();
    InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().is(getContext().getFile().getAbsoluteFile()));
    newHighlighting.onFile(inputFile);
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    newHighlighting.save();
  }

  @Override
  public void visitToken(Token token) {
    if (!token.isGeneratedCode()) {
      if (token.getType().equals(CxxTokenType.NUMBER)) {
        highlight(token, TypeOfText.CONSTANT);
      } else if (token.getType() instanceof CxxKeyword) {
        highlight(token, TypeOfText.KEYWORD);
      } else if (token.getType().equals(CxxTokenType.STRING)) {
        highlight(token, TypeOfText.STRING);
      }

      for (Trivia trivia : token.getTrivia()) {
        highlight(trivia.getToken(), TypeOfText.COMMENT);
      }
    }
  }

  private void highlight(Token token, TypeOfText typeOfText) {
    TokenLocation tokenLocation = new TokenLocation(token);
    newHighlighting.highlight(tokenLocation.startLine(), tokenLocation.startLineOffset(), tokenLocation.endLine(), tokenLocation.endLineOffset(), typeOfText);
  }

}
