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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.symbol.NewSymbol;
import org.sonar.api.batch.sensor.symbol.NewSymbolTable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxCompilationUnitSettings;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.sensors.eclipsecdt.EclipseCDTException;
import org.sonar.cxx.sensors.eclipsecdt.EclipseCDTParser;
import org.sonar.cxx.sensors.eclipsecdt.LinebasedTextRange;
import org.sonar.cxx.sensors.utils.CxxUtils;
import org.sonar.squidbridge.SquidAstVisitor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstVisitor;
import com.sonar.sslr.api.Grammar;

public class CxxEclipseCDTVisitor extends SquidAstVisitor<Grammar> implements AstVisitor {

  private static final Logger LOG = Loggers.get(CxxEclipseCDTVisitor.class);

  private final SensorContext context;
  private final CxxConfiguration configuration;

  public CxxEclipseCDTVisitor(SensorContext context, CxxConfiguration cxxConfiguration) {
    this.configuration = cxxConfiguration;
    this.context = context;
  }

  private String[] getIncludePaths(String compilationUnit) {
    CxxCompilationUnitSettings cus = configuration.getCompilationUnitSettings(compilationUnit);
    if (cus != null) {
      List<String> includes = cus.getIncludes();
      return cus.getIncludes().toArray(new String[includes.size()]);
    } else {
      return new String[0];
    }
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    InputFile inputFile = context.fileSystem()
        .inputFile(context.fileSystem().predicates().is(getContext().getFile().getAbsoluteFile()));

    if (inputFile == null) {
      LOG.warn("Unable to locate the source file " + getContext().getFile().getAbsoluteFile().getAbsolutePath());
    }

    try {
      String path = inputFile.uri().getPath();
      EclipseCDTParser parser = new EclipseCDTParser(path, getIncludePaths(path));

      Map<LinebasedTextRange, Set<LinebasedTextRange>> table = parser.generateSymbolTable();

      NewSymbolTable newSymbolTable = context.newSymbolTable();
      newSymbolTable.onFile(inputFile);
      for (Map.Entry<LinebasedTextRange, Set<LinebasedTextRange>> entry : table.entrySet()) {
        LinebasedTextRange declaration = entry.getKey();
        Set<LinebasedTextRange> references = entry.getValue();

        NewSymbol declarationSymbol = newSymbolTable.newSymbol(declaration.start().line(),
            declaration.start().lineOffset(), declaration.end().line(), declaration.end().lineOffset());
        for (LinebasedTextRange reference : references) {
          try {
            declarationSymbol.newReference(reference.start().line(), reference.start().lineOffset(),
                reference.end().line(), reference.end().lineOffset());
          } catch (Exception e) {
            LOG.debug("Couldn't add a symbol reference because of {}", CxxUtils.getStackTrace(e));
          }
        }
      }

      newSymbolTable.save();

    } catch (EclipseCDTException e) {
      LOG.warn("EclipseCDT failure: " + CxxUtils.getStackTrace(e));
    } catch (Exception e) {
      LOG.warn("Generic exception: " + CxxUtils.getStackTrace(e));
    }
  }

}
