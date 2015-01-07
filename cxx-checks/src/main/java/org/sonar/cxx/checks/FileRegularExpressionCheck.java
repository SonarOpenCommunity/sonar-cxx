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

import com.sonar.sslr.api.AstNode;
import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import com.sonar.sslr.api.Grammar;
import org.sonar.api.utils.SonarException;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.checks.SquidCheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;

@Rule(
  key = "FileRegularExpression",
  cardinality = Cardinality.MULTIPLE,
  priority = Priority.MAJOR)

public class FileRegularExpressionCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static final String DEFAULT_MATCH_FILE_PATTERN = "";
  private static final String DEFAULT_REGULAR_EXPRESSION = "";
  private static final String DEFAULT_MESSAGE = "The regular expression matches this file";

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
      try {
        if (!matchFile()) {
          return;
        }
        Matcher matcher = pattern.matcher(fromFile(getContext().getFile(), charset));
        if (matcher.find()) {
          getContext().createFileViolation(this, message);
        }
      } catch (IOException e) {
        throw new SonarException(e);
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

    private static CharSequence fromFile(File file, Charset charset) throws IOException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            FileChannel channel = input.getChannel();
            ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
            CharBuffer cbuf = charset.newDecoder().decode(bbuf);
            return cbuf;
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
    
}

