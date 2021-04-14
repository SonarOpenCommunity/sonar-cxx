/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

public class CxxOtherRepositoryTest {

  private final MapSettings settings = new MapSettings();

  private final String profile1 = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n"
                                    + "<rules>\n"
                                    + "    <rule key=\"cpplint.readability/nolint-0\">\n"
                                    + "        <name><![CDATA[ Unknown NOLINT error category: %s  % category]]></name>\n"
                                  + "        <configKey><![CDATA[cpplint.readability/nolint-0@CPP_LINT]]></configKey>\n"
                                    + "        <category name=\"readability\" />\n"
                                    + "        <description><![CDATA[  Unknown NOLINT error category: %s  % category ]]></description>\n"
                                  + "    </rule>\n"
                                    + "    <rule key=\"cpplint.readability/fn_size-0\">\n"
                                    + "        <name>name</name>\n"
                                    + "        <configKey>key</configKey>\n"
                                    + "        <category name=\"readability\" />\n"
                                    + "        <description>descr</description>\n"
                                    + "    </rule></rules>";

  private final String profile2 = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n"
                                    + "<rules>\n"
                                    + "    <rule key=\"key\">\n"
                                    + "        <name><![CDATA[name]]></name>\n"
                                    + "        <configKey><![CDATA[configKey]]></configKey>\n"
                                    + "        <category name=\"category\" />\n"
                                    + "        <description><![CDATA[description]]></description>\n"
                                    + "    </rule>\n"
                                    + "</rules>";

  @Test
  public void verifyTemplateRuleIsFound() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, "");
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(1);
  }

  @Test
  public void createNonEmptyRulesTest() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, profile1);
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(3);
  }

  @Test
  public void createNullRulesTest() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, "");
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(1);
  }

  @Test
  public void verifyRuleValuesTest() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, profile2);
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    Rule rule = repo.rule("key");
    assertThat(rule).isNotNull();

    // from rule.xml
    assertThat(rule.key()).isEqualTo("key");
    assertThat(rule.name()).isEqualTo("name");
    assertThat(rule.internalKey()).isEqualTo("configKey");
    assertThat(rule.htmlDescription()).isEqualTo("description");

  }

}
