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
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.squidbridge.checks.SquidCheck;
import org.sonar.cxx.tag.Tag;

@Rule(
  key = "MissingNewLineAtEndOfFile",
  name = "Files should contain an empty new line at the end",
  tags = {Tag.CONVENTION},
  priority = Priority.MINOR)
@ActivatedByDefault
@SqaleConstantRemediation("1min")
public class MissingNewLineAtEndOfFileCheck extends SquidCheck<Grammar> {

  @Override
  public void visitFile(AstNode astNode) {
    if (isEmptyOrNotEndingWithNewLine(getContext().getInputFileContent())) {
      getContext().createFileViolation(this, "Add a new line at the end of this file.");
    }
  }

  private static boolean isEmptyOrNotEndingWithNewLine(String content) {
    if (content.isEmpty()) {
      return true;
    }
    char lastChar = content.charAt(content.length() - 1);
    return lastChar != '\n' && lastChar != '\r';
  }

}
