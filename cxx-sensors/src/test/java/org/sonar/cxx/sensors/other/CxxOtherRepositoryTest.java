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
package org.sonar.cxx.sensors.other;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.sensors.utils.RulesDefinitionXmlLoader;

class CxxOtherRepositoryTest {

  private final MapSettings settings = new MapSettings();

  private final String profile1 = """
                                  <?xml version="1.0" encoding="ASCII"?>
                                  <rules>
                                      <rule key="cpplint.readability/nolint-0">
                                          <name><![CDATA[ Unknown NOLINT error category: %s  % category]]></name>
                                          <configKey><![CDATA[cpplint.readability/nolint-0@CPP_LINT]]></configKey>
                                          <category name="readability" />
                                          <description><![CDATA[  Unknown NOLINT error category: %s  % category ]]></description>
                                      </rule>
                                      <rule key="cpplint.readability/fn_size-0">
                                          <name>name</name>
                                          <configKey>key</configKey>
                                          <category name="readability" />
                                          <description>descr</description>
                                      </rule>
                                  </rules>
                                  """;

  private final String profile2 = """
                                  <?xml version="1.0" encoding="ASCII"?>
                                  <rules>
                                      <rule key="key1">
                                          <name><![CDATA[name1]]></name>
                                          <configKey><![CDATA[configKey1]]></configKey>
                                          <category name="category1" />
                                          <description><![CDATA[description1]]></description>
                                      </rule>
                                      <rule key="key2">
                                          <name><![CDATA[name2]]></name>
                                          <configKey><![CDATA[configKey2]]></configKey>
                                          <category name="category2" />
                                          <description><![CDATA[description2]]></description>
                                      </rule>
                                  </rules>
                                  """;

  private final String profile3 = """
                                  <?xml version="1.0" encoding="ASCII"?>
                                  <rules>
                                      <rule key="key1">
                                          <name><![CDATA[name1]]></name>
                                          <configKey><![CDATA[configKey1]]></configKey>
                                          <category name="category1" />
                                          <description><![CDATA[description1]]></description>
                                      </rule>
                                      <rule key="key3">
                                          <name><![CDATA[name3]]></name>
                                          <configKey><![CDATA[configKey3]]></configKey>
                                          <category name="category3" />
                                          <description><![CDATA[description3]]></description>
                                      </rule>
                                  </rules>
                                  """;

  @Test
  void verifyTemplateRuleIsFound() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, "");
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(2);
  }

  @Test
  void createNonEmptyRulesTest() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, profile1);
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(4);
  }

  @Test
  void createNullRulesTest() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, "");
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(2);
  }

  @Test
  void verifyRuleValuesTest() {
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
  void verifyRulesWithSameKey() {
    settings.setProperty(CxxOtherSensor.RULES_KEY, profile2 + "," + profile3);
    var def = new CxxOtherRepository(settings.asConfig(), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(4);

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
