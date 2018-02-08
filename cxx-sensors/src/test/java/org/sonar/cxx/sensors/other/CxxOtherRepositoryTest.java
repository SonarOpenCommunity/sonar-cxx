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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.when;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxOtherRepositoryTest {

  String profile1 = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n"
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

  String profile2 = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n"
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
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxOtherRepository.RULES_KEY))
      .thenReturn(new String[]{null});

    CxxOtherRepository def = new CxxOtherRepository(
      new RulesDefinitionXmlLoader(), language);

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(1);
  }

  @Test
  public void createNonEmptyRulesTest() {

    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxOtherRepository.RULES_KEY))
      .thenReturn(new String[]{profile1});

    CxxOtherRepository def = new CxxOtherRepository(
      new RulesDefinitionXmlLoader(), language);

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(3);
  }

  @Test
  public void createNullRulesTest() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxOtherRepository.RULES_KEY))
      .thenReturn(new String[]{null});

    CxxOtherRepository def = new CxxOtherRepository(
      new RulesDefinitionXmlLoader(), language);

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxOtherRepository.KEY);
    assertThat(repo.rules()).hasSize(1);
  }

  @Test
  public void verifyRuleValuesTest() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    when(language.getStringArrayOption(CxxOtherRepository.RULES_KEY))
      .thenReturn(new String[]{profile2});

    CxxOtherRepository def = new CxxOtherRepository(
      new RulesDefinitionXmlLoader(), language);

    RulesDefinition.Context context = new RulesDefinition.Context();
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
