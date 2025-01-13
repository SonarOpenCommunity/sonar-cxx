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
// copy of https://github.com/SonarSource/sonar-plugin-api/blob/master/plugin-api/src/test/java/org/sonar/api/server/rule/RulesDefinitionXmlLoaderTest.java
// Don't remove copyright below!

/*
 * Sonar Plugin API
 * Copyright (C) 2009-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.cxx.sensors.utils;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.impl.RulesDefinitionContext;
import org.sonar.api.server.rule.RulesDefinition;

public class RulesDefinitionXmlLoaderTest {

  RulesDefinitionXmlLoader underTest = new RulesDefinitionXmlLoader();

  @Test
  void parseXml() {
    InputStream input = getClass()
      .getResourceAsStream("/org/sonar/cxx/sensors/utils/RulesDefinitionXmlLoader/rules.xml");
    RulesDefinition.Repository repository = load(input, StandardCharsets.UTF_8.name());
    assertThat(repository.rules()).hasSize(2);

    RulesDefinition.Rule rule = repository.rule("complete");
    assertThat(rule.key()).isEqualTo("complete");
    assertThat(rule.name()).isEqualTo("Complete");
    assertThat(rule.htmlDescription()).isEqualTo("Description of Complete");
    assertThat(rule.severity()).isEqualTo(Severity.BLOCKER);
    assertThat(rule.template()).isTrue();
    assertThat(rule.status()).isEqualTo(RuleStatus.BETA);
    assertThat(rule.internalKey()).isEqualTo("Checker/TreeWalker/LocalVariableName");
    assertThat(rule.type()).isEqualTo(RuleType.BUG);
    assertThat(rule.tags()).containsOnly(
      "misra",
      "spring"
    );
    assertThat(rule.deprecatedRuleKeys()).containsOnly(
      RuleKey.of(repository.key(), "deprecatedKey1"),
      RuleKey.of(repository.key(), "deprecatedKey2")
    );

    assertThat(rule.params()).hasSize(2);
    RulesDefinition.Param ignore = rule.param("ignore");
    assertThat(ignore.key()).isEqualTo("ignore");
    assertThat(ignore.description()).isEqualTo("Ignore ?");
    assertThat(ignore.defaultValue()).isEqualTo("false");

    rule = repository.rule("minimal");
    assertThat(rule.key()).isEqualTo("minimal");
    assertThat(rule.name()).isEqualTo("Minimal");
    assertThat(rule.htmlDescription()).isEqualTo("Description of Minimal");
    assertThat(rule.params()).isEmpty();
    assertThat(rule.status()).isEqualTo(RuleStatus.READY);
    assertThat(rule.severity()).isEqualTo(Severity.MAJOR);
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    """
    <rules>
      <rule>
        <name>Foo</name>
      </rule>
    </rules>
    """,
    """
    <rules>
      <rule>
        <key>foo</key>
        <name>Foo</name>
        <param></param>
      </rule>
    </rules>
    """,
    """
    <rules>
      <rule>
        <key>foo</key>
        <name>Foo</name>
        <param>
          <key>key</key>
          <type>INVALID</type>
        </param>
      </rule>
    </rules>
    """
  })
  void failIfInvalidXml(String xml) {
    assertThatThrownBy(() -> load(IOUtils.toInputStream(xml, StandardCharsets.UTF_8),
      StandardCharsets.UTF_8.name()))
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void failIfInvalidXml() {
    InputStream input = getClass().getResourceAsStream(
      "/org/sonar/cxx/sensors/utils/RulesDefinitionXmlLoader/invalid.xml"
    );
    assertThatThrownBy(() -> load(input, StandardCharsets.UTF_8.name()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("XML is not valid");
  }

  @Test
  void testUtf8Encoding() {
    InputStream input = getClass().getResourceAsStream(
      "/org/sonar/cxx/sensors/utils/RulesDefinitionXmlLoader/utf8.xml"
    );
    RulesDefinition.Repository repository = load(input, StandardCharsets.UTF_8.name());

    assertThat(repository.rules()).hasSize(1);
    RulesDefinition.Rule rule = repository.rules().get(0);
    assertThat(rule.key()).isEqualTo("com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck");
    assertThat(rule.name()).isEqualTo("M & M");
    assertThat(rule.htmlDescription().charAt(0)).isEqualTo('\u00E9');
    assertThat(rule.htmlDescription().charAt(1)).isEqualTo('\u00E0');
    assertThat(rule.htmlDescription().charAt(2)).isEqualTo('\u0026');
  }

  @Test
  void testUtf8EncodingWithBom() {
    InputStream input = getClass().getResourceAsStream(
      "/org/sonar/cxx/sensors/utils/RulesDefinitionXmlLoader/utf8-with-bom.xml"
    );
    RulesDefinition.Repository repository = load(input, StandardCharsets.UTF_8.name());

    assertThat(repository.rules()).hasSize(1);
    RulesDefinition.Rule rule = repository.rules().get(0);
    assertThat(rule.key()).isEqualTo("com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck");
    assertThat(rule.name()).isEqualTo("M & M");
    assertThat(rule.htmlDescription().charAt(0)).isEqualTo('\u00E9');
    assertThat(rule.htmlDescription().charAt(1)).isEqualTo('\u00E0');
    assertThat(rule.htmlDescription().charAt(2)).isEqualTo('\u0026');
  }

  @Test
  void supportDeprecatedFormat() {
    // the deprecated format uses some attributes instead of nodes
    InputStream input = getClass().getResourceAsStream(
      "/org/sonar/cxx/sensors/utils/RulesDefinitionXmlLoader/deprecated.xml"
    );
    RulesDefinition.Repository repository = load(input, StandardCharsets.UTF_8.name());

    assertThat(repository.rules()).hasSize(1);
    RulesDefinition.Rule rule = repository.rules().get(0);
    assertThat(rule.key()).isEqualTo("org.sonar.it.checkstyle.MethodsCountCheck");
    assertThat(rule.internalKey()).isEqualTo("Checker/TreeWalker/org.sonar.it.checkstyle.MethodsCountCheck");
    assertThat(rule.severity()).isEqualTo(Severity.CRITICAL);
    assertThat(rule.htmlDescription()).isEqualTo("Count methods");
    assertThat(rule.param("minMethodsCount")).isNotNull();
  }

  @Test
  void testDefaultValues() {
    var xml = """
    <rules>
      <rule>
        <key>1</key>
        <name>One</name>
        <description>Desc</description>
      </rule>
    </rules>
    """;
    RulesDefinition.Rule rule = load(xml).rule("1");
    assertThat(rule.severity()).isEqualTo("MAJOR");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    DebtRemediationFunction function = rule.debtRemediationFunction();
    assertThat(function).isNotNull();
    assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.CONSTANT_ISSUE);
    assertThat(function.gapMultiplier()).isNull();
    assertThat(function.baseEffort()).isEqualTo("5min");
  }

  @Test
  void testDefaultInfoValues() {
    var xml = """
      <rules>
        <rule>
          <key>1</key>
          <name>One</name>
          <description>Desc</description>
          <severity>INFO</severity>
        </rule>
      </rules>
      """;
    RulesDefinition.Rule rule = load(xml).rule("1");
    assertThat(rule.severity()).isEqualTo("INFO");
    assertThat(rule.type()).isEqualTo(RuleType.CODE_SMELL);
    DebtRemediationFunction function = rule.debtRemediationFunction();
    assertThat(function).isNull();
  }

  @Test
  void testLinearRemediationFunction() {
    var xml = """
      <rules>
        <rule>
          <key>1</key>
          <name>One</name>
          <description>Desc</description>
          <gapDescription>lines</gapDescription>
          <remediationFunction>LINEAR</remediationFunction>
          <remediationFunctionGapMultiplier>2d 3h</remediationFunctionGapMultiplier>
        </rule>
      </rules>
      """;
    RulesDefinition.Rule rule = load(xml).rule("1");
    assertThat(rule.gapDescription()).isEqualTo("lines");
    DebtRemediationFunction function = rule.debtRemediationFunction();
    assertThat(function).isNotNull();
    assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.LINEAR);
    assertThat(function.gapMultiplier()).isEqualTo("2d3h");
    assertThat(function.baseEffort()).isNull();
  }

  @Test
  void testLinearWithOffsetRemediationFunction() {
    var xml = """
      <rules>
        <rule>
          <key>1</key>
          <name>One</name>
          <description>Desc</description>
          <effortToFixDescription>lines</effortToFixDescription>
          <remediationFunction>LINEAR_OFFSET</remediationFunction>
          <remediationFunctionGapMultiplier>2d 3h</remediationFunctionGapMultiplier>
          <remediationFunctionBaseEffort>5min</remediationFunctionBaseEffort>
        </rule>
      </rules>
      """;
    RulesDefinition.Rule rule = load(xml).rule("1");
    assertThat(rule.gapDescription()).isEqualTo("lines");
    DebtRemediationFunction function = rule.debtRemediationFunction();
    assertThat(function).isNotNull();
    assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.LINEAR_OFFSET);
    assertThat(function.gapMultiplier()).isEqualTo("2d3h");
    assertThat(function.baseEffort()).isEqualTo("5min");
  }

  @Test
  void testConstantRemediationFunction() {
    var xml = """
    <rules>
      <rule>
        <key>1</key>
        <name>One</name>
        <description>Desc</description>
        <remediationFunction>CONSTANT_ISSUE</remediationFunction>
        <remediationFunctionBaseEffort>5min</remediationFunctionBaseEffort>
      </rule>
    </rules>
    """;
    RulesDefinition.Rule rule = load(xml).rule("1");
    DebtRemediationFunction function = rule.debtRemediationFunction();
    assertThat(function).isNotNull();
    assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.CONSTANT_ISSUE);
    assertThat(function.gapMultiplier()).isNull();
    assertThat(function.baseEffort()).isEqualTo("5min");
  }

  @Test
  void failIfInvalidRemediationFunction() {
    var xml = """
      <rules>
        <rule>
          <key>1</key>
          <name>One</name>
          <description>Desc</description>
          <remediationFunction>UNKNOWN</remediationFunction>
        </rule>
      </rules>
      """;
    assertThatThrownBy(() -> load(xml))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Fail to load the rule with key [squid:1]")
      .hasCauseInstanceOf(IllegalArgumentException.class)
      .hasRootCauseMessage("No enum constant org.sonar.api.server.debt.DebtRemediationFunction.Type.UNKNOWN");
  }

  @Test
  void failIfUnsupportedDescriptionFormat() {
    var xml = """
      <rules>
        <rule>
          <key>1</key>
          <name>One</name>
          <description>Desc</description>
          <descriptionFormat>UNKNOWN</descriptionFormat>
        </rule>
      </rules>
      """;
    assertThatThrownBy(() -> load(xml).rule("1"))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("Fail to load the rule with key [squid:1]")
      .hasCauseInstanceOf(IllegalArgumentException.class)
      .hasRootCauseMessage(
        "No enum constant org.sonar.cxx.sensors.utils.RulesDefinitionXmlLoader.DescriptionFormat.UNKNOWN");
  }

  @Test
  void testDeprecatedRemediationFunction() {
    var xml = """
      <rules>
        <rule>
          <key>1</key>
          <name>One</name>
          <description>Desc</description>
          <effortToFixDescription>lines</effortToFixDescription>
          <debtRemediationFunction>LINEAR_OFFSET</debtRemediationFunction>
          <debtRemediationFunctionCoefficient>2d 3h</debtRemediationFunctionCoefficient>
          <debtRemediationFunctionOffset>5min</debtRemediationFunctionOffset>
        </rule>
      </rules>
      """;
    RulesDefinition.Rule rule = load(xml).rule("1");
    assertThat(rule.gapDescription()).isEqualTo("lines");
    DebtRemediationFunction function = rule.debtRemediationFunction();
    assertThat(function).isNotNull();
    assertThat(function.type()).isEqualTo(DebtRemediationFunction.Type.LINEAR_OFFSET);
    assertThat(function.gapMultiplier()).isEqualTo("2d3h");
    assertThat(function.baseEffort()).isEqualTo("5min");
  }

  private RulesDefinition.Repository load(InputStream input, String encoding) {
    RulesDefinition.Context context = new RulesDefinitionContext();
    RulesDefinition.NewRepository newRepository = context.createRepository("squid", "java");
    underTest.load(newRepository, input, encoding);
    newRepository.done();
    return context.repository("squid");
  }

  private RulesDefinition.Repository load(String xml) {
    RulesDefinition.Context context = new RulesDefinitionContext();
    RulesDefinition.NewRepository newRepository = context.createRepository("squid", "java");
    underTest.load(newRepository, new StringReader(xml));
    newRepository.done();
    return context.repository("squid");
  }
}
