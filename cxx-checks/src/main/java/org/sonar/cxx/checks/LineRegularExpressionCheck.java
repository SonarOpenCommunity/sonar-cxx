/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.checks;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.google.common.io.Files;

import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.utils.SonarException;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;
import com.sonar.sslr.api.AstNode;

@Rule(
  key = "LineRegularExpression",
  cardinality = Cardinality.MULTIPLE,
  priority = Priority.MAJOR)

public class LineRegularExpressionCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static final String DEFAULT_MATCH_FILE_PATTERN = "";
  private static final String DEFAULT_REGULAR_EXPRESSION = "";
  private static final String DEFAULT_MESSAGE = "The regular expression matches this line";

  private Charset charset;
  private Pattern pattern;

  @RuleProperty(
    key = "matchFilePattern",
    defaultValue = DEFAULT_MATCH_FILE_PATTERN)
  public String matchFilePattern = DEFAULT_MATCH_FILE_PATTERN;

  @RuleProperty(
    key = "regularExpression",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  @RuleProperty(
    key = "message",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  @Override
  public void init() {
    try {
      pattern = Pattern.compile(regularExpression);
    } catch (PatternSyntaxException e) {
      throw new SonarException(e);
    }
  }

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode fileNode) {
    if (fileNode != null) {
      if (matchFile()) {
        List<String> lines;
        try {
          lines = Files.readLines(getContext().getFile(), charset);
        } catch (IOException e) {
          throw new SonarException(e);
        }
        for (int i = 0; i < lines.size(); ++i) {
          Matcher matcher = pattern.matcher(lines.get(i));
          if (matcher.find()) {
            getContext().createLineViolation(this, message, i + 1);
          }
        }
      }
    }
  }

  private boolean matchFile() {
    if (!matchFilePattern.isEmpty()) {
      WildcardPattern filePattern = WildcardPattern.create(matchFilePattern);
      String path = PathUtils.sanitize(getContext().getFile().getPath());
      return filePattern.match(path);
    }
    return true;
  }

}
