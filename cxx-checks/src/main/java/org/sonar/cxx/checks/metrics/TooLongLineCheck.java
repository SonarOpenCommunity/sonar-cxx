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
package org.sonar.cxx.checks;

import com.google.common.io.Files;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.tag.Tag;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * TooLongLineCheck - similar Vera++ rule L004 "Line too long"
 *
 */
@Rule(
  key = "TooLongLine",
  name = "Lines of code should not be too long",
  tags = {Tag.BRAIN_OVERLOAD},
  priority = Priority.MINOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class TooLongLineCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static final int DEFAULT_MAXIMUM_LINE_LENHGTH = 160;
  private static final int DEFAULT_TAB_WIDTH = 8;

  /**
   * maximumLineLength
   */
  @RuleProperty(
    key = "maximumLineLength",
    description = "The maximum authorized line length",
    defaultValue = "" + DEFAULT_MAXIMUM_LINE_LENHGTH)
  public int maximumLineLength = DEFAULT_MAXIMUM_LINE_LENHGTH;

  /**
   * tabWidth
   */
  @RuleProperty(
    key = "tabWidth",
    description = "Number of spaces in a 'tab' character",
    defaultValue = "" + DEFAULT_TAB_WIDTH)
  public int tabWidth = DEFAULT_TAB_WIDTH;

  private Charset charset = Charset.forName("UTF-8");

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode astNode) {
    List<String> lines;
    try {
      lines = Files.readLines(getContext().getFile(), charset);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      int length = 0;
      for (char c : line.toCharArray()) {
        if (c == '\t') {
          ++length;
        }
      }
      length = line.length() + length * (tabWidth - 1);
      if (length > maximumLineLength) {
        getContext().createLineViolation(this,
          "Split this {0} characters long line (which is greater than {1} authorized).",
          i + 1, length, maximumLineLength);
      }
    }
  }

}
