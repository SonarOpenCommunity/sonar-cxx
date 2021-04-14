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
package org.sonar.cxx.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.cxx.checks.api.UndocumentedApiCheck;
import org.sonar.cxx.checks.error.ParsingErrorCheck;
import org.sonar.cxx.checks.error.ParsingErrorRecoveryCheck;
import org.sonar.cxx.checks.file.FileEncodingCheck;
import org.sonar.cxx.checks.file.MissingNewLineAtEndOfFileCheck;
import org.sonar.cxx.checks.file.TabCharacterCheck;
import org.sonar.cxx.checks.metrics.ClassComplexityCheck;
import org.sonar.cxx.checks.metrics.FileComplexityCheck;
import org.sonar.cxx.checks.metrics.FunctionCognitiveComplexityCheck;
import org.sonar.cxx.checks.metrics.FunctionComplexityCheck;
import org.sonar.cxx.checks.metrics.TooLongLineCheck;
import org.sonar.cxx.checks.metrics.TooManyLinesOfCodeInFileCheck;
import org.sonar.cxx.checks.metrics.TooManyLinesOfCodeInFunctionCheck;
import org.sonar.cxx.checks.metrics.TooManyParametersCheck;
import org.sonar.cxx.checks.metrics.TooManyStatementsPerLineCheck;
import org.sonar.cxx.checks.naming.ClassNameCheck;
import org.sonar.cxx.checks.naming.FileNameCheck;
import org.sonar.cxx.checks.naming.FunctionNameCheck;
import org.sonar.cxx.checks.naming.MethodNameCheck;
import org.sonar.cxx.checks.regex.CommentRegularExpressionCheck;
import org.sonar.cxx.checks.regex.FileHeaderCheck;
import org.sonar.cxx.checks.regex.FileRegularExpressionCheck;
import org.sonar.cxx.checks.regex.FixmeTagPresenceCheck;
import org.sonar.cxx.checks.regex.LineRegularExpressionCheck;
import org.sonar.cxx.checks.regex.NoSonarCheck;
import org.sonar.cxx.checks.regex.TodoTagPresenceCheck;
import org.sonar.cxx.checks.xpath.XPathCheck;

public final class CheckList {

  public static final String REPOSITORY_KEY = "cxx";

  private CheckList() {
  }

  public static List<Class> getChecks() {
    return new ArrayList<>(Arrays.asList(
      UndocumentedApiCheck.class,
      ParsingErrorCheck.class,
      ParsingErrorRecoveryCheck.class,
      FileEncodingCheck.class,
      MissingNewLineAtEndOfFileCheck.class,
      TabCharacterCheck.class,
      ClassComplexityCheck.class,
      FileComplexityCheck.class,
      FunctionCognitiveComplexityCheck.class,
      FunctionComplexityCheck.class,
      TooLongLineCheck.class,
      TooManyLinesOfCodeInFileCheck.class,
      TooManyLinesOfCodeInFunctionCheck.class,
      TooManyParametersCheck.class,
      TooManyStatementsPerLineCheck.class,
      ClassNameCheck.class,
      FileNameCheck.class,
      FunctionNameCheck.class,
      MethodNameCheck.class,
      CommentRegularExpressionCheck.class,
      FileHeaderCheck.class,
      FileRegularExpressionCheck.class,
      FixmeTagPresenceCheck.class,
      LineRegularExpressionCheck.class,
      NoSonarCheck.class,
      TodoTagPresenceCheck.class,
      XPathCheck.class
    ));
  }

}
