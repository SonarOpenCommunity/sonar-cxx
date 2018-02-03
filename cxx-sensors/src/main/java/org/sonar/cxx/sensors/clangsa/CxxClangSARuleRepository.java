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
package org.sonar.cxx.sensors.clangsa;

import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.CxxAbstractRuleRepository;

/**
 * {@inheritDoc}
 */
public class CxxClangSARuleRepository extends CxxAbstractRuleRepository {

  public static final String KEY = "ClangSA";
  public static final String CUSTOM_RULES_KEY = "clangsa.customRules";
  private static final String NAME = "Clang-SA";

  /**
   * {@inheritDoc}
   */
  public CxxClangSARuleRepository(ServerFileSystem fileSystem, RulesDefinitionXmlLoader xmlRuleLoader,
    CxxLanguage language) {
    super(fileSystem, xmlRuleLoader, KEY, NAME, CUSTOM_RULES_KEY, language);
  }

  @Override
  protected String fileName() {
    return "/clangsa.xml";
  }
}
