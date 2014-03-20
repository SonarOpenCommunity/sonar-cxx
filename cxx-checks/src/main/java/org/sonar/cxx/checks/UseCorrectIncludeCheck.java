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

import com.google.common.io.Files;
import com.google.common.base.Strings;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.squid.checks.SquidCheck;

import org.sonar.api.utils.SonarException;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;

import java.nio.charset.Charset;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.List;

@Rule(
  key = "UseCorrectInclude",
  priority = Priority.BLOCKER)

public class UseCorrectIncludeCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {
    
  private static final String DEFAULT_REGULAR_EXPRESSION = "#include.*(?>\"|\\<)[\\\\/\\.]+";
  private static final String DEFAULT_MESSAGE = "Use correct #include directives";
  
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;
  public String message = DEFAULT_MESSAGE;
  private Pattern pattern = null;
  private Charset charset;

  @Override
  public void init() {
    if (!Strings.isNullOrEmpty(regularExpression)) {
      try {
        pattern = Pattern.compile(regularExpression, Pattern.DOTALL);
      } catch (RuntimeException e) {
        throw new SonarException("Unable to compile regular expression: " + regularExpression, e);
      }
    }
  }
  
  @Override
  public void visitFile(AstNode astNode) {
    List<String> lines;
    try {
      lines = Files.readLines(getContext().getFile(), charset);
    } catch (IOException e) {
      throw new SonarException(e);
    }
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (pattern.matcher(line).find()) {
        getContext().createLineViolation(this, "Do not use relative path for #include directive.", i + 1);
      }
    }
  }
  
  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }


}
