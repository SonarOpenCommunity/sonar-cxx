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

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.DebtRemediationFunction.Type;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

public class CxxExternalRuleRepositoryTest {

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
  
  String sqale2 = "<?xml version=\"1.0\"?>\n"
    + "<sqale>\n"
    + "  <chc>\n"
    + "    <key>PORTABILITY</key>\n"
    + "    <name>Portability</name>\n"
    + "    <chc>\n"
    + "      <key>COMPILER_RELATED_PORTABILITY</key>\n"
    + "      <name>Compiler related portability</name>\n"
    + "      <chc>\n"
    + "        <rule-repo>other</rule-repo>\n"
    + "        <rule-key>key</rule-key>\n"
    + "        <prop>\n"
    + "          <key>remediationFunction</key>\n"
    + "          <txt>linear_offset</txt>\n"
    + "        </prop>\n"
    + "        <prop>\n"
    + "          <key>remediationFactor</key>\n"
    + "          <val>5</val>\n"
    + "          <txt>mn</txt>\n"
    + "        </prop>\n"
    + "        <prop>\n"
    + "          <key>offset</key>\n"
    + "          <val>10</val>\n"
    + "          <txt>mn</txt>\n"
    + "        </prop>\n"
    + "      </chc>\n"
    + "    </chc>\n"
    + "  </chc>\n"
    + "</sqale>";
  
  @Test
  public void verifyTemplateRuleIsFound() {
    CxxExternalRuleRepository def = new CxxExternalRuleRepository(
      new RulesDefinitionXmlLoader(), new Settings());

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxExternalRuleRepository.KEY);
    assertThat(repo.rules()).hasSize(1);
  }

  @Test
  public void createNonEmptyRulesTest() {
    Settings settings = new Settings();
    settings.appendProperty(CxxExternalRuleRepository.RULES_KEY, profile1);
    CxxExternalRuleRepository def = new CxxExternalRuleRepository(
      new RulesDefinitionXmlLoader(), settings);

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxExternalRuleRepository.KEY);
    assertThat(repo.rules()).hasSize(3);
  }

  @Test
  public void createNullRulesTest() {
    Settings settings = new Settings();
    settings.appendProperty(CxxExternalRuleRepository.RULES_KEY, null);
    CxxExternalRuleRepository def = new CxxExternalRuleRepository(
      new RulesDefinitionXmlLoader(), settings);

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxExternalRuleRepository.KEY);
    assertThat(repo.rules()).hasSize(1);
  }
  
  @Test
  public void verifyRuleValuesTest() {
    Settings settings = new Settings();
    settings.appendProperty(CxxExternalRuleRepository.RULES_KEY, profile2);
    settings.appendProperty(CxxExternalRuleRepository.SQALES_KEY, sqale2);
    CxxExternalRuleRepository def = new CxxExternalRuleRepository(
      new RulesDefinitionXmlLoader(), settings);

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxExternalRuleRepository.KEY);   
    Rule rule = repo.rule("key");
    assertThat(rule).isNotNull();
    
    // from rule.xml
    assertThat(rule.key()).isEqualTo("key");
    assertThat(rule.name()).isEqualTo("name");
    assertThat(rule.internalKey()).isEqualTo("configKey");
    assertThat(rule.htmlDescription()).isEqualTo("description");
    
    // from sqale.xml
    assertThat(rule.debtSubCharacteristic()).isEqualTo("COMPILER_RELATED_PORTABILITY");
    DebtRemediationFunction remediationFunction = rule.debtRemediationFunction();
    assertThat(remediationFunction).isNotNull();
    assertThat(remediationFunction.type()).isEqualTo(Type.LINEAR_OFFSET);
    assertThat(remediationFunction.coefficient()).isEqualTo("5min");
    assertThat(remediationFunction.offset()).isEqualTo("10min");
  }
}
