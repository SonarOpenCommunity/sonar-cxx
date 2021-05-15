/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.checks.file;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.squidbridge.checks.SquidCheck;
import org.sonar.cxx.tag.Tag;

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
public class TabCharacterCheck extends SquidCheck<Grammar> {

  private static final boolean DEFAULT_CREATE_LINE_VIOLATION = false;

  /**
   * createLineViolation
   */
  @RuleProperty(
    key = "createLineViolation",
    description = "Create violations per line (default is one per file)",
    defaultValue = "" + DEFAULT_CREATE_LINE_VIOLATION)
  public boolean createLineViolation = DEFAULT_CREATE_LINE_VIOLATION;

  @Override
  public void visitFile(AstNode astNode) {
    var nr = 0;
    for (var line : getContext().getInputFileLines()) {
      ++nr;
      if (line.contains("\t")) {
        if (createLineViolation) {
          getContext().createLineViolation(
            this,
            "Replace all tab characters in this line by sequences of white-spaces.", nr);
        } else {
          getContext().createFileViolation(
            this,
            "Replace all tab characters in this file by sequences of white-spaces.");
          break;
        }
      }
    }
  }

}
