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
 * TabCharacterCheck - similar Vera++ rule L002 "Don't use tab characters"
 *
 */
@Rule(
  key = "TabCharacter",
  name = "Tabulation characters should not be used",
  tags = {Tag.CONVENTION},
  priority = Priority.MINOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class TabCharacterCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static final boolean DEFAULT_CREATE_LINE_VIOLATION = false;
  private Charset charset = Charset.forName("UTF-8");

  /**
   * createLineViolation
   */
  @RuleProperty(
    key = "createLineViolation",
    description = "Create violations per line (default is one per file)",
    defaultValue = "" + DEFAULT_CREATE_LINE_VIOLATION)
  public boolean createLineViolation = DEFAULT_CREATE_LINE_VIOLATION;

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
      if (lines.get(i).contains("\t")) {
        if (createLineViolation) {
          getContext().createLineViolation(this,
            "Replace all tab characters in this line by sequences of white-spaces.", i + 1);
        } else {
          getContext().createFileViolation(this,
            "Replace all tab characters in this file by sequences of white-spaces.");
          break;
        }
      }
    }
  }

}
