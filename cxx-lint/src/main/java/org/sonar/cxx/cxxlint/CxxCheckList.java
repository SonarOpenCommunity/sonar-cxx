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
import org.sonar.cxx.checks.BooleanEqualityComparisonCheck;
import org.sonar.cxx.checks.ClassComplexityCheck;
import org.sonar.cxx.checks.CollapsibleIfCandidateCheck;
import org.sonar.cxx.checks.CommentRegularExpressionCheck;
import org.sonar.cxx.checks.CommentedCodeCheck;
import org.sonar.cxx.checks.FileComplexityCheck;
import org.sonar.cxx.checks.FileEncodingCheck;
import org.sonar.cxx.checks.FileHeaderCheck;
import org.sonar.cxx.checks.FileRegularExpressionCheck;
import org.sonar.cxx.checks.FixmeTagPresenceCheck;
import org.sonar.cxx.checks.FunctionComplexityCheck;
import org.sonar.cxx.checks.HardcodedAccountCheck;
import org.sonar.cxx.checks.HardcodedIpCheck;
import org.sonar.cxx.checks.LineRegularExpressionCheck;
import org.sonar.cxx.checks.MagicNumberCheck;
import org.sonar.cxx.checks.MissingCurlyBracesCheck;
import org.sonar.cxx.checks.MissingIncludeFileCheck;
import org.sonar.cxx.checks.MissingNewLineAtEndOfFileCheck;
import org.sonar.cxx.checks.NestedStatementsCheck;
import org.sonar.cxx.checks.NoSonarCheck;
import org.sonar.cxx.checks.ParsingErrorCheck;
import org.sonar.cxx.checks.ParsingErrorRecoveryCheck;
import org.sonar.cxx.checks.ReservedNamesCheck;
import org.sonar.cxx.checks.SafetyTagCheck;
import org.sonar.cxx.checks.StringLiteralDuplicatedCheck;
import org.sonar.cxx.checks.SwitchLastCaseIsDefaultCheck;
import org.sonar.cxx.checks.TabCharacterCheck;
import org.sonar.cxx.checks.TodoTagPresenceCheck;
import org.sonar.cxx.checks.TooLongLineCheck;
import org.sonar.cxx.checks.TooManyLinesOfCodeInFileCheck;
import org.sonar.cxx.checks.TooManyLinesOfCodeInFunctionCheck;
import org.sonar.cxx.checks.TooManyParametersCheck;
import org.sonar.cxx.checks.TooManyStatementsPerLineCheck;
import org.sonar.cxx.checks.UndocumentedApiCheck;
import org.sonar.cxx.checks.UnnamedNamespaceInHeaderCheck;
import org.sonar.cxx.checks.UseCorrectIncludeCheck;
import org.sonar.cxx.checks.UseCorrectTypeCheck;
import org.sonar.cxx.checks.UselessParenthesesCheck;
import org.sonar.cxx.checks.UsingNamespaceInHeaderCheck;
import org.sonar.cxx.checks.XPathCheck;
import org.sonar.cxx.checks.naming.ClassNameCheck;
import org.sonar.cxx.checks.naming.FileNameCheck;
import org.sonar.cxx.checks.naming.FunctionNameCheck;
import org.sonar.cxx.checks.naming.MethodNameCheck;

public final class CxxCheckList {

  public static final String REPOSITORY_KEY = "cxx";

  public static final String DEFAULT_PROFILE = "Sonar way";

  private CxxCheckList() {
  }

  public static List<Class> getChecks() {
    return new ArrayList<Class>(Arrays.asList(
      CollapsibleIfCandidateCheck.class,
      CommentedCodeCheck.class,
      CommentRegularExpressionCheck.class,
      FileComplexityCheck.class,
      ClassComplexityCheck.class,
      FileHeaderCheck.class,
      FileEncodingCheck.class,
      FileRegularExpressionCheck.class,
      LineRegularExpressionCheck.class,
      FixmeTagPresenceCheck.class,
      FunctionComplexityCheck.class,
      HardcodedAccountCheck.class,
      HardcodedIpCheck.class,
      MagicNumberCheck.class,
      MissingCurlyBracesCheck.class,
      MissingIncludeFileCheck.class,
      MissingNewLineAtEndOfFileCheck.class,
      NoSonarCheck.class,
      ParsingErrorCheck.class,
      ParsingErrorRecoveryCheck.class,
      ReservedNamesCheck.class,
      StringLiteralDuplicatedCheck.class,
      SwitchLastCaseIsDefaultCheck.class,
      TabCharacterCheck.class,
      TodoTagPresenceCheck.class,
      TooLongLineCheck.class,
      TooManyLinesOfCodeInFileCheck.class,
      TooManyStatementsPerLineCheck.class,
      UndocumentedApiCheck.class,
      UnnamedNamespaceInHeaderCheck.class,
      UselessParenthesesCheck.class,
      UseCorrectTypeCheck.class,
      UsingNamespaceInHeaderCheck.class,
      SafetyTagCheck.class,
      UseCorrectIncludeCheck.class,
      BooleanEqualityComparisonCheck.class,
      NestedStatementsCheck.class,
      TooManyParametersCheck.class,
      TooManyLinesOfCodeInFunctionCheck.class,
      // name checks
      ClassNameCheck.class,
      FileNameCheck.class,
      FunctionNameCheck.class,
      MethodNameCheck.class,
      // XPath
      XPathCheck.class
    ));
  }

}
