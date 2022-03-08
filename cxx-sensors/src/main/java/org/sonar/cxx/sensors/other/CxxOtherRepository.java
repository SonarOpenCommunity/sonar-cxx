/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
import java.nio.charset.StandardCharsets;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Loads the external rules configuration file.
 */
public class CxxOtherRepository implements RulesDefinition {

  public static final String KEY = "other";
  private static final String NAME = "Other";

  private static final Logger LOG = Loggers.get(CxxOtherRepository.class);

  private final RulesDefinitionXmlLoader xmlRuleLoader;
  private final Configuration config;

  /**
   * CxxOtherRepository
   *
   * @param xmlRuleLoader to load rules from XML file
   */
  public CxxOtherRepository(Configuration config, RulesDefinitionXmlLoader xmlRuleLoader) {
    this.xmlRuleLoader = xmlRuleLoader;
    this.config = config;
  }

  @Override
  public void define(Context context) {
    var repository = context.createRepository(KEY, "cxx")
      .setName(NAME);
    var validate = context.createRepository("Validate", "cxx")
      .setName("validate");

    xmlRuleLoader.load(repository, getClass().getResourceAsStream("/external-rule.xml"), StandardCharsets.UTF_8.name());
    for (var ruleDefs : this.config.getStringArray(CxxOtherSensor.RULES_KEY)) {
      if (ruleDefs != null && !ruleDefs.trim().isEmpty()) {
        try {
          // read rules first into dummy repository to check if there are errors
          xmlRuleLoader.load(validate, new StringReader(ruleDefs));
          // in case of no errors read again into real repository
          xmlRuleLoader.load(repository, new StringReader(ruleDefs));
        } catch (IllegalStateException e) {
          // In case of an error, ignore the whole XML block. The loading happens during the server start,
          // errors are critical and can cause that the server cannot be started anymore.
          var xml = ruleDefs.substring(0, Math.min(ruleDefs.length(), 120))
            .replaceAll("\\R", "").replaceAll(">[ ]+<", "><");
          LOG.error("Cannot load rule definions for 'sonar.cxx.other.rules', '{}', XML '{}...', skipping",
                    e.getMessage(), xml);
        }
      }
    }

    repository.done();
  }

}
