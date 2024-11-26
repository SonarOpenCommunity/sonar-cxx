/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.sensors.clangtidy;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.cxx.sensors.utils.RulesDefinitionXml;
import org.sonar.cxx.sensors.utils.RulesDefinitionXmlLoader;

/**
 * {@inheritDoc}
 */
public class CxxClangTidyRuleRepository extends RulesDefinitionXml {

  private static final String LANGUAGE = "cxx";
  public static final String KEY = "clangtidy";
  private static final String NAME = "Clang-Tidy";
  private static final String FILE = "/clangtidy.xml";

  /**
   * {@inheritDoc}
   */
  public CxxClangTidyRuleRepository(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader) {
    super(fileSystem, xmlRuleLoader, LANGUAGE, KEY, NAME, FILE);
  }

  @Override
  public void prepareRule(NewRule rule) {
    // V1.3 repository name
    rule.addDeprecatedRuleKey("ClangTidy", rule.key());

    switch (rule.key()) {
      case "clang-diagnostic-c++14-extensions":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++1y-extensions");
        break;
      case "clang-diagnostic-c++17-compat":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++1z-compat");
        break;
      case "clang-diagnostic-c++17-compat-mangling":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++1z-compat-mangling");
        break;
      case "clang-diagnostic-c++17-extensions":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++1z-extensions");
        break;
      case "clang-diagnostic-c++20-compat":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++2a-compat");
        break;
      case "clang-diagnostic-c++20-compat-pedantic":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++2a-compat-pedantic");
        break;
      case "clang-diagnostic-c++20-extensions":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++2a-extensions");
        break;
      case "clang-diagnostic-c++23-extensions":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++2b-extensions");
        break;
      case "clang-diagnostic-pre-c++23-compat":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-pre-c++2b-compat");
        break;
      case "clang-diagnostic-pre-c++23-compat-pedantic":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-pre-c++2b-compat-pedantic");
        break;
      case "clang-diagnostic-c++26-compat":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++2c-compat");
        break;
      case "clang-diagnostic-c++26-extensions":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-c++2c-extensions");
        break;
      case "clang-diagnostic-pre-c++26-compat":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-pre-c++2c-compat");
        break;
      case "clang-diagnostic-pre-c++26-compat-pedantic":
        rule.addDeprecatedRuleKey(KEY, "clang-diagnostic-pre-c++2c-compat-pedantic");
        break;
    }
  }

}
