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
package org.sonar.cxx.visitors;

import com.sonar.sslr.api.AstAndTokenVisitor;
import com.sonar.sslr.api.AstNode;
import static com.sonar.sslr.api.GenericTokenType.EOF;

import java.util.regex.Pattern;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.measures.MetricDef;

/**
 * Visitor that computes the number of lines of code of a file.
 *
 * @param <GRAMMAR>
 */
public class CxxLinesOfCodeVisitor<GRAMMAR extends Grammar>
  extends SquidAstVisitor<GRAMMAR> implements AstAndTokenVisitor {

  private final MetricDef metric;
  private int lastTokenLine;
  public static final Pattern EOLPattern = Pattern.compile("\\R");

  public CxxLinesOfCodeVisitor(MetricDef metric) {
    this.metric = metric;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitFile(AstNode node) {
    lastTokenLine = -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visitToken(Token token) {
    if (!token.getType().equals(EOF)) {
      /* Handle all the lines of the token */
      String[] tokenLines = EOLPattern.split(token.getValue(), -1);

      int firstLineAlreadyCounted = lastTokenLine == token.getLine() ? 1 : 0;
      getContext().peekSourceCode().add(metric, (double) tokenLines.length - firstLineAlreadyCounted);

      lastTokenLine = token.getLine() + tokenLines.length - 1;
    }
  }

}
