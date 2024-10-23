/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.checks.regex;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.checks.utils.CheckUtils;
import org.sonar.cxx.squidbridge.annotations.ActivatedByDefault;
import org.sonar.cxx.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

/**
 * FileHeaderCheck - similar Vera++ rule T013 "No copyright notice found"
 *
 */
@Rule(
  key = "FileHeader",
  name = "Copyright and license headers should be defined in all source files",
  priority = Priority.BLOCKER,
  tags = {})
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class FileHeaderCheck extends SquidCheck<Grammar> {

  private static final String DEFAULT_HEADER_FORMAT = "";
  private static final String MESSAGE = "Add or update the header of this file.";

  /**
   * headerFormat
   */
  @RuleProperty(
    key = "headerFormat",
    description = "Expected copyright and license header (plain text)",
    type = "TEXT",
    defaultValue = DEFAULT_HEADER_FORMAT)
  public String headerFormat = DEFAULT_HEADER_FORMAT;

  /**
   * isRegularExpression
   */
  @RuleProperty(
    key = "isRegularExpression",
    description = "Whether the headerFormat is a regular expression",
    defaultValue = "false")
  public boolean isRegularExpression = false;

  private String[] expectedLines = null;
  private Pattern searchPattern = null;

  @Override
  public void init() {
    if (isRegularExpression) {
      if (searchPattern == null) {
        searchPattern = CheckUtils.compileUserRegexp(getHeaderFormat(), Pattern.DOTALL);
      }
    } else {
      expectedLines = headerFormat.split("(?:\r)?\n|\r");
    }
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    if (isRegularExpression) {
      String fileContent = getContext().getInputFileContent();
      checkRegularExpression(fileContent);
    } else {
      if (!matches(expectedLines, getContext().getInputFileLines())) {
        getContext().createFileViolation(this, MESSAGE);
      }
    }
  }

  private String getHeaderFormat() {
    String format = headerFormat;
    if (format.charAt(0) != '^') {
      format = "^" + format;
    }
    return format;
  }

  private void checkRegularExpression(String fileContent) {
    var matcher = searchPattern.matcher(fileContent);
    if (!matcher.find() || matcher.start() != 0) {
      getContext().createFileViolation(this, MESSAGE);
    }
  }

  private static boolean matches(String[] expectedLines, List<String> lines) {
    var result = false;

    if (expectedLines.length <= lines.size()) {
      result = true;

      Iterator<String> it = lines.iterator();
      for (var expectedLine : expectedLines) {
        String line = it.next();
        if (!line.equals(expectedLine)) {
          result = false;
          break;
        }
      }
    }

    return result;
  }

}
