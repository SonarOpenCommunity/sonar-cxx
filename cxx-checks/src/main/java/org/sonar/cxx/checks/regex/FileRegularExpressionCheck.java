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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.sonar.api.utils.PathUtils;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.visitors.CxxCharsetAwareVisitor;
import org.sonar.squidbridge.annotations.NoSqale;
import org.sonar.squidbridge.annotations.RuleTemplate;
import org.sonar.squidbridge.checks.SquidCheck;

/**
 * FileRegularExpressionCheck
 *
 */
@Rule(
  key = "FileRegularExpression",
  name = "File RegEx rule",
  priority = Priority.MAJOR)
@RuleTemplate
@NoSqale
public class FileRegularExpressionCheck extends SquidCheck<Grammar> implements CxxCharsetAwareVisitor {

  private static final String DEFAULT_MATCH_FILE_PATTERN = "";
  private static final boolean DEFAULT_INVERT_FILE_PATTERN = false;
  private static final String DEFAULT_REGULAR_EXPRESSION = "";
  private static final boolean DEFAULT_INVERT_REGULAR_EXPRESSION = false;
  private static final String DEFAULT_MESSAGE = "The regular expression matches this file";

  private Charset charset = Charset.forName("UTF-8");
  private CharsetDecoder decoder;
  private Pattern pattern;

  /**
   * matchFilePattern
   */
  @RuleProperty(
    key = "matchFilePattern",
    description = "Ant-style matching patterns for path",
    defaultValue = DEFAULT_MATCH_FILE_PATTERN)
  public String matchFilePattern = DEFAULT_MATCH_FILE_PATTERN;

  /**
   * invertFilePattern
   */
  @RuleProperty(
    key = "invertFilePattern",
    description = "Invert file pattern comparison",
    defaultValue = "" + DEFAULT_INVERT_FILE_PATTERN)
  public boolean invertFilePattern = DEFAULT_INVERT_FILE_PATTERN;

  /**
   * regularExpression
   */
  @RuleProperty(
    key = "regularExpression",
    description = "The regular expression",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  /**
   * invertRegularExpression
   */
  @RuleProperty(
    key = "invertRegularExpression",
    description = "Invert regular expression comparison",
    defaultValue = "" + DEFAULT_INVERT_REGULAR_EXPRESSION)
  public boolean invertRegularExpression = DEFAULT_INVERT_REGULAR_EXPRESSION;

  /**
   * message
   */
  @RuleProperty(
    key = "message",
    description = "The violation message",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  @Override
  public void init() {
    try {
      pattern = Pattern.compile(regularExpression);
      decoder = charset.newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPLACE);
      decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    } catch (PatternSyntaxException ex) {
      throw new IllegalStateException(ex);
    } catch (IllegalArgumentException ex2) {
      throw new IllegalStateException(ex2);
    }
  }

  @Override
  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void visitFile(AstNode fileNode) {
    try {
      if (!compare(invertFilePattern, matchFile())) {
        return;
      }
      Matcher matcher = pattern.matcher(fromFile(getContext().getFile()));
      if (compare(invertRegularExpression, matcher.find())) {
        getContext().createFileViolation(this, message);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
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

  private CharSequence fromFile(File file) throws IOException {
    try (FileInputStream input = new FileInputStream(file)) {
      FileChannel channel = input.getChannel();
      ByteBuffer bbuf = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
      return decoder.decode(bbuf);
    }
  }

  private static boolean compare(boolean invert, boolean condition) {
    return invert ? !condition : condition;
  }
}
