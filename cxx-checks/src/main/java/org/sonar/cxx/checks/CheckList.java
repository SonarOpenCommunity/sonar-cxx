/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
import java.util.List;
import java.util.Arrays;

import org.sonar.cxx.checks.naming.ClassNameCheck;
import org.sonar.cxx.checks.naming.FileNameCheck;
import org.sonar.cxx.checks.naming.FunctionNameCheck;
import org.sonar.cxx.checks.naming.MethodNameCheck;

public final class CheckList {

  public static final String REPOSITORY_KEY = "cxx";

  public static final String DEFAULT_PROFILE = "Sonar way";

  private CheckList() {
  }

  public static List<Class> getChecks() {
    return new ArrayList<Class>(Arrays.asList(
      CollapsibleIfCandidateCheck.class,
      CommentedCodeCheck.class,
      CommentRegularExpressionCheck.class,
      CycleBetweenPackagesCheck.class,
      DuplicatedIncludeCheck.class,
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
