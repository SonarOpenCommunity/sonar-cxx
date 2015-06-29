/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.externalrules;

import java.io.StringReader;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.plugins.cxx.CxxLanguage;
import org.sonar.plugins.cxx.utils.CxxUtils;
import org.sonar.squidbridge.rules.SqaleXmlLoader;
import org.sonar.plugins.cxx.utils.CxxSqaleXmlLoader;

/**
 * Loads the external rules configuration file.
 */
public class CxxExternalRuleRepository implements RulesDefinition {

  public static final String KEY = "other";
  public static final String RULES_KEY = "sonar.cxx.other.rules";
  public static final String SQALES_KEY = "sonar.cxx.other.sqales";
  public final Settings settings;
  private final RulesDefinitionXmlLoader xmlRuleLoader;
  private static final String NAME = "Other";

  public CxxExternalRuleRepository(RulesDefinitionXmlLoader xmlRuleLoader, Settings settings) {
    this.xmlRuleLoader = xmlRuleLoader;
    this.settings = settings;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(KEY, CxxLanguage.KEY).setName(NAME);
    
    xmlRuleLoader.load(repository, getClass().getResourceAsStream("/external-rule.xml"), "UTF-8");
    for (String ruleDefs : settings.getStringArray(RULES_KEY)) {
      if (StringUtils.isNotBlank(ruleDefs)) {
        try {
          xmlRuleLoader.load(repository, new StringReader(ruleDefs));
        } catch (Exception ex) {
          CxxUtils.LOG.info("Cannot load rules XML '{}'", ex.getMessage());
        }
      }
    }

    SqaleXmlLoader.load(repository, "/com/sonar/sqale/cxx-model.xml");
    for (String sqaleDefs : settings.getStringArray(SQALES_KEY)) {
      if (StringUtils.isNotBlank(sqaleDefs)) {
        try {
          CxxSqaleXmlLoader.load(repository, new StringReader(sqaleDefs));
        } catch (Exception ex) {
          CxxUtils.LOG.info("Cannot load SQALE XML '{}'", ex.getMessage());
        }
      }
    }

    repository.done();
  }

}
