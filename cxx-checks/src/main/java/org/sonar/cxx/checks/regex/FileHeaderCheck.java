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
package org.sonar.cxx.checks.regex;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.checks.utils.CheckUtils;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.checks.SquidCheck;

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
public class FileHeaderCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

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

  private Charset defaultCharset = StandardCharsets.UTF_8;
  private String[] expectedLines = null;
  private Pattern searchPattern = null;

  private static boolean matches(String[] expectedLines, BufferedReader br) throws IOException {
    for (var expectedLine : expectedLines) {
      var line = br.readLine();
      if (!expectedLine.equals(line)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void setCharset(Charset charset) {
    this.defaultCharset = charset;
  }

  @Override
  public void init() {
    if (isRegularExpression) {
      if (searchPattern == null) {
        searchPattern = CheckUtils.compileUserRegexp(getHeaderFormat(), Pattern.DOTALL);
      }
    } else {
      expectedLines = headerFormat.split("\\R");
    }
  }

  @Override
  public void visitFile(AstNode astNode) {
    try {
      if (isRegularExpression) {
        String fileContent = CheckUtils.getFileContent(getContext().getFile(), defaultCharset);
        checkRegularExpression(fileContent);
      } else {
        try ( var br = new BufferedReader(CheckUtils.getInputSteam(getContext().getFile(), defaultCharset))) {
          if (!matches(expectedLines, br)) {
            getContext().createFileViolation(this, MESSAGE);
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
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
    Matcher matcher = searchPattern.matcher(fileContent);
    if (!matcher.find() || matcher.start() != 0) {
      getContext().createFileViolation(this, MESSAGE);
    }
  }

}
