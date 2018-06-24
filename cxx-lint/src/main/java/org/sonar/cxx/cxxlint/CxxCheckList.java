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
package org.sonar.cxx.cxxlint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CxxCheckList {

  public static final String REPOSITORY_KEY = "cxx";

  public static final String DEFAULT_PROFILE = "Sonar way";

  private CxxCheckList() {
  }

  public static List<Class> getChecks() {
    return new ArrayList<Class>(Arrays.asList(
      org.sonar.cxx.checks.BooleanEqualityComparisonCheck.class,
      org.sonar.cxx.checks.CollapsibleIfCandidateCheck.class,
      org.sonar.cxx.checks.CommentedCodeCheck.class,
      org.sonar.cxx.checks.HardcodedAccountCheck.class,
      org.sonar.cxx.checks.HardcodedIpCheck.class,
      org.sonar.cxx.checks.MagicNumberCheck.class,
      org.sonar.cxx.checks.MissingCurlyBracesCheck.class,
      org.sonar.cxx.checks.NestedStatementsCheck.class,
      org.sonar.cxx.checks.ReservedNamesCheck.class,
      org.sonar.cxx.checks.SafetyTagCheck.class,
      org.sonar.cxx.checks.StringLiteralDuplicatedCheck.class,
      org.sonar.cxx.checks.SwitchLastCaseIsDefaultCheck.class,
      org.sonar.cxx.checks.UnnamedNamespaceInHeaderCheck.class,
      org.sonar.cxx.checks.UseCorrectIncludeCheck.class,
      org.sonar.cxx.checks.UseCorrectTypeCheck.class,
      org.sonar.cxx.checks.UselessParenthesesCheck.class,
      org.sonar.cxx.checks.UsingNamespaceInHeaderCheck.class,
      org.sonar.cxx.checks.api.UndocumentedApiCheck.class,
      org.sonar.cxx.checks.error.MissingIncludeFileCheck.class,
      org.sonar.cxx.checks.error.ParsingErrorCheck.class,
      org.sonar.cxx.checks.error.ParsingErrorRecoveryCheck.class,
      org.sonar.cxx.checks.file.FileEncodingCheck.class,
      org.sonar.cxx.checks.file.MissingNewLineAtEndOfFileCheck.class,
      org.sonar.cxx.checks.file.TabCharacterCheck.class,
      org.sonar.cxx.checks.metrics.ClassComplexityCheck.class,
      org.sonar.cxx.checks.metrics.FileComplexityCheck.class,
      org.sonar.cxx.checks.metrics.FunctionCognitiveComplexityCheck.class,
      org.sonar.cxx.checks.metrics.FunctionComplexityCheck.class,
      org.sonar.cxx.checks.metrics.TooLongLineCheck.class,
      org.sonar.cxx.checks.metrics.TooManyLinesOfCodeInFileCheck.class,
      org.sonar.cxx.checks.metrics.TooManyLinesOfCodeInFunctionCheck.class,
      org.sonar.cxx.checks.metrics.TooManyParametersCheck.class,
      org.sonar.cxx.checks.metrics.TooManyStatementsPerLineCheck.class,
      org.sonar.cxx.checks.naming.ClassNameCheck.class,
      org.sonar.cxx.checks.naming.FileNameCheck.class,
      org.sonar.cxx.checks.naming.FunctionNameCheck.class,
      org.sonar.cxx.checks.naming.MethodNameCheck.class,
      org.sonar.cxx.checks.regex.CommentRegularExpressionCheck.class,
      org.sonar.cxx.checks.regex.FileHeaderCheck.class,
      org.sonar.cxx.checks.regex.FileRegularExpressionCheck.class,
      org.sonar.cxx.checks.regex.FixmeTagPresenceCheck.class,
      org.sonar.cxx.checks.regex.LineRegularExpressionCheck.class,
      org.sonar.cxx.checks.regex.NoSonarCheck.class,
      org.sonar.cxx.checks.regex.TodoTagPresenceCheck.class,
      org.sonar.cxx.checks.xpath.XPathCheck.class
    ));
  }

}
