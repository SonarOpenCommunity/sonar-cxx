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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.rule.RulesDefinition;
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
                                    + "    <rule key=\"key1\">\n"
                                    + "        <name><![CDATA[name1]]></name>\n"
                                    + "        <configKey><![CDATA[configKey1]]></configKey>\n"
                                    + "        <category name=\"category1\" />\n"
                                    + "        <description><![CDATA[description1]]></description>\n"
                                    + "    </rule>\n"
                                    + "    <rule key=\"key2\">\n"
                                    + "        <name><![CDATA[name2]]></name>\n"
                                    + "        <configKey><![CDATA[configKey2]]></configKey>\n"
                                    + "        <category name=\"category2\" />\n"
                                    + "        <description><![CDATA[description2]]></description>\n"
                                    + "    </rule>\n"
                                    + "</rules>";

  private final String profile3 = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n"
                                    + "<rules>\n"
                                    + "    <rule key=\"key1\">\n"
                                    + "        <name><![CDATA[name1]]></name>\n"
                                    + "        <configKey><![CDATA[configKey1]]></configKey>\n"
                                    + "        <category name=\"category1\" />\n"
                                    + "        <description><![CDATA[description1]]></description>\n"
                                    + "    </rule>\n"
                                    + "    <rule key=\"key3\">\n"
                                    + "        <name><![CDATA[name3]]></name>\n"
                                    + "        <configKey><![CDATA[configKey3]]></configKey>\n"
                                    + "        <category name=\"category3\" />\n"
                                    + "        <description><![CDATA[description3]]></description>\n"
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
    var rule = repo.rule("key1");
    assertThat(rule).isNotNull();

    assertThat(rule.key()).isEqualTo("key1");
    assertThat(rule.name()).isEqualTo("name1");
    assertThat(rule.internalKey()).isEqualTo("configKey1");
    assertThat(rule.htmlDescription()).isEqualTo("description1");
  }

  @Test
  public void verifyRulesWithSameKey() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, profile2 + "," + profile3);
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(3);

    var rule = repo.rule("key1");
    assertThat(rule).isNotNull();

    assertThat(rule.key()).isEqualTo("key1");
    assertThat(rule.name()).isEqualTo("name1");
    assertThat(rule.internalKey()).isEqualTo("configKey1");
    assertThat(rule.htmlDescription()).isEqualTo("description1");

    rule = repo.rule("key2");
    assertThat(rule).isNotNull();

    assertThat(rule.key()).isEqualTo("key2");
    assertThat(rule.name()).isEqualTo("name2");
    assertThat(rule.internalKey()).isEqualTo("configKey2");
    assertThat(rule.htmlDescription()).isEqualTo("description2");
  }

}
