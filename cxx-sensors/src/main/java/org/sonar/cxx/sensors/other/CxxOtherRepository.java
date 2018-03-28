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
package org.sonar.cxx.sensors.other;

import java.io.StringReader;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxLanguage;

/**
 * Loads the external rules configuration file.
 */
public class CxxOtherRepository implements RulesDefinition {

  private static final Logger LOG = Loggers.get(CxxOtherRepository.class);
  public static final String KEY = "other";
  public static final String RULES_KEY = "other.rules";
  private final RulesDefinitionXmlLoader xmlRuleLoader;
  private static final String NAME = "Other";
  private final CxxLanguage language;

  /**
   * CxxOtherRepository
   * @param xmlRuleLoader to load rules from XML file
   * @param language for C or C++
   */
  public CxxOtherRepository(RulesDefinitionXmlLoader xmlRuleLoader, CxxLanguage language) {
    this.xmlRuleLoader = xmlRuleLoader;
    this.language = language;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(KEY + this.language.getRepositorySuffix(), 
                      this.language.getKey()).setName(NAME + this.language.getRepositorySuffix());

    xmlRuleLoader.load(repository, getClass().getResourceAsStream("/external-rule.xml"), "UTF-8");
    for (String ruleDefs : this.language.getStringArrayOption(RULES_KEY)) {
      if (ruleDefs != null && !ruleDefs.trim().isEmpty()) {
        try {
          xmlRuleLoader.load(repository, new StringReader(ruleDefs));
        } catch (IllegalStateException ex) {
          LOG.info("Cannot load rules XML '{}'", ex);
        }
      }
    }

    repository.done();
  }
}
