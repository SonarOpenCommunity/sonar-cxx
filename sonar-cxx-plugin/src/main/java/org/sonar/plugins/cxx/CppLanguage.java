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
package org.sonar.plugins.cxx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sonar.api.config.Configuration;
import org.sonar.cxx.CxxLanguage;

/**
 * CppLanguage
 *
 * @author jocs
 */
public class CppLanguage extends CxxLanguage {

  /**
   * cxx key
   */
  public static final String KEY = "c++";

  /**
   * cxx name
   */
  public static final String NAME = "C++ (Community)";

  /**
   * Default cxx source files suffixes
   */
  public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
  public static final String DEFAULT_C_FILES = "*.c,*.C";

  /**
   * Default cxx header files suffixes
   */
  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  /**
   * cxx analysis parameters key
   */
  public static final String PROPSKEY = "cxx";

  /**
   * cxx repository key
   */
  public static final String REPOSITORY_KEY = "cxx";
  public static final String DEFAULT_PROFILE = "Sonar way";

  private final String[] sourceSuffixes;
  private final String[] headerSuffixes;
  private final String[] fileSuffixes;

  /**
   * @param settings
   */
  public CppLanguage(Configuration settings) {
    super(KEY, NAME, PROPSKEY, settings);

    sourceSuffixes = createStringArray(settings.getStringArray(CxxPlugin.SOURCE_FILE_SUFFIXES_KEY),
      DEFAULT_SOURCE_SUFFIXES);
    headerSuffixes = createStringArray(settings.getStringArray(CxxPlugin.HEADER_FILE_SUFFIXES_KEY),
      DEFAULT_HEADER_SUFFIXES);
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
  public String[] getHeaderFileSuffixes() {
    return headerSuffixes.clone();
  }

  @Override
  public List<Class> getChecks() {
    return new ArrayList<>(Arrays.asList(
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

  @Override
  public String getRepositoryKey() {
    return REPOSITORY_KEY;
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
}
