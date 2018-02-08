/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.sensors.visitors;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.io.File;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.cpd.NewCpdTokens;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.squidbridge.SquidAstVisitor;

public class CxxCpdVisitor extends SquidAstVisitor<Grammar> implements AstAndTokenVisitor {

  private static final Logger LOG = Loggers.get(CxxCpdVisitor.class);

  private final SensorContext sensorContext;
  private final Boolean ignoreLiterals;
  private final Boolean ignoreIdentifiers;
  private InputFile inputFile;
  private NewCpdTokens cpdTokens;
  private int isFunctionDefinition;

  public CxxCpdVisitor(SensorContext sensorContext, Boolean ignoreLiterals, Boolean ignoreIdentifiers) {
    this.sensorContext = sensorContext;
    this.ignoreLiterals = ignoreLiterals;
    this.ignoreIdentifiers = ignoreIdentifiers;
  }

  @Override
  public void init() {
    subscribeTo(CxxGrammarImpl.functionDefinition);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    File file = getContext().getFile();
    inputFile = sensorContext.fileSystem().inputFile(sensorContext.fileSystem().predicates().is(file));
    cpdTokens = sensorContext.newCpdTokens().onFile(inputFile);
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    cpdTokens.save();
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

      try {
        TextRange range = inputFile.newRange(token.getLine(), token.getColumn(),
          token.getLine(), token.getColumn() + token.getValue().length());
        cpdTokens.addToken(range, text);
      } catch (IllegalArgumentException | IllegalStateException e) {
        // ignore range errors: parsing errors could lead to wrong location data
        if (LOG.isDebugEnabled()) {
          LOG.debug("CPD error in file '{}' at line:{}, column:{}", getContext().getFile().getAbsoluteFile(),
            token.getLine(), token.getColumn());
        }
      }
    }
  }

}
