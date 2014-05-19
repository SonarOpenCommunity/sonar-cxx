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

import org.junit.Test;
import org.sonar.api.rules.XMLRuleParser;

import static org.fest.assertions.Assertions.assertThat;
import org.sonar.api.config.Settings;

public class CxxExternalRuleRepositoryTest {

  String profile = "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n" +
          "<rules>\n" +
          "    <rule key=\"cpplint.readability/nolint-0\">\n" +
          "        <name><![CDATA[ Unknown NOLINT error category: %s  % category]]></name>\n" +
          "        <configKey><![CDATA[cpplint.readability/nolint-0@CPP_LINT]]></configKey>\n" +
          "        <category name=\"readability\" />\n" +
          "        <description><![CDATA[  Unknown NOLINT error category: %s  % category ]]></description>\n" +
          "    </rule>\n" +
          "    <rule key=\"cpplint.readability/fn_size-0\">\n" +
          "        <name>name</name>\n" +
          "        <configKey>key</configKey>\n" +
          "        <category name=\"readability\" />\n" +
          "        <description>descr</description>\n" +
          "    </rule></rules>";

  @Test
  public void createEmptyRulesTest() {
    CxxExternalRuleRepository rulerep = new CxxExternalRuleRepository(
        new XMLRuleParser(), new Settings());
    assertThat(rulerep.createRules()).hasSize(0);
  }

  @Test
  public void createNonEmptyRulesTest() {
    Settings settings = new Settings();
    settings.appendProperty(CxxExternalRuleRepository.RULES_KEY, profile);
    CxxExternalRuleRepository rulerep = new CxxExternalRuleRepository(
      new XMLRuleParser(), settings);
    assertThat(rulerep.createRules()).hasSize(2);
  }

  @Test
  public void createNullRulesTest() {
    Settings settings = new Settings();
    settings.appendProperty(CxxExternalRuleRepository.RULES_KEY, null);
    CxxExternalRuleRepository rulerep = new CxxExternalRuleRepository(
      new XMLRuleParser(), settings);
    assertThat(rulerep.createRules()).hasSize(0);
  }
}
