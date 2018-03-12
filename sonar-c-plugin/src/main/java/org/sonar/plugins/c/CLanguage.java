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
package org.sonar.plugins.c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.api.config.Configuration;
import org.sonar.cxx.CxxLanguage;
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
import org.sonar.cxx.checks.FunctionCognitiveComplexityCheck;
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

/**
 *
 * @author jocs
 */
public class CLanguage extends CxxLanguage {

  /**
   * c key
   */
  public static final String KEY = "c";

  /**
   * c name
   */
  public static final String NAME = "C (Community)";

  /**
   * Default c source files suffixes
   */
  public static final String DEFAULT_SOURCE_SUFFIXES = ".c";
  public static final String DEFAULT_C_FILES = "*.c,*.C";

  /**
   * Default c header files suffixes
   */
  public static final String DEFAULT_HEADER_SUFFIXES = ".h";

  /**
   * c analysis parameters key
   */
  public static final String PROPSKEY = "c";

  /**
   * c repository key
   */
  public static final String REPOSITORY_KEY = "c";
  public static final String DEFAULT_PROFILE = "Sonar way";

  private final String[] sourceSuffixes;
  private final String[] headerSuffixes;
  private final String[] fileSuffixes;

  public CLanguage(Configuration settings) {
    super(KEY, NAME, settings);

    sourceSuffixes = createStringArray(settings.getStringArray(CPlugin.SOURCE_FILE_SUFFIXES_KEY), DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(settings.getStringArray(CPlugin.HEADER_FILE_SUFFIXES_KEY), DEFAULT_HEADER_SUFFIXES);
    fileSuffixes = mergeArrays(sourceSuffixes, headerSuffixes);
  }

  @Override
  public String[] getFileSuffixes() {
    return fileSuffixes.clone();
  }

  @Override
  public String[] getSourceFileSuffixes() {
    return sourceSuffixes.clone();
  }

  @Override
  public String getRepositorySuffix() {
    return "-c";
  }

  @Override
  public String[] getHeaderFileSuffixes() {
    return headerSuffixes.clone();
  }

  @Override
  public String getPropertiesKey() {
    return PROPSKEY;
  }

  private static String[] createStringArray(String[] values, String defaultValues) {
    if (values.length == 0) {
      return defaultValues.split(",");
    }
    return values;
  }

  private String[] mergeArrays(String[] array1, String[] array2) {
    String[] result = new String[array1.length + array2.length];
    System.arraycopy(sourceSuffixes, 0, result, 0, array1.length);
    System.arraycopy(headerSuffixes, 0, result, array1.length, array2.length);
    return result;
  }

  @Override
  public List<Class> getChecks() {
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
      FunctionCognitiveComplexityCheck.class,
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

  @Override
  public String getRepositoryKey() {
    return REPOSITORY_KEY;
  }
}
