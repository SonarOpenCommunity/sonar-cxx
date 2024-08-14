/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import com.sonar.cxx.sslr.api.Grammar;
import java.util.ArrayList;
import java.util.Collections;
import javax.annotation.CheckForNull;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Rule;
import org.sonar.cxx.squidbridge.SquidAstVisitor;
import org.sonar.cxx.squidbridge.checks.SquidCheck;

class CxxChecksTest {

  private static final String DEFAULT_REPOSITORY_KEY = "DefaultRuleRepository";
  private static final String DEFAULT_RULE_KEY = "MyRule";
  private static final String CUSTOM_REPOSITORY_KEY = "CustomRuleRepository";
  private static final String CUSTOM_RULE_KEY = "MyCustomRule";

  private MyCustomPlSqlRulesDefinition customRulesDefinition;
  private CheckFactory checkFactory;

  @BeforeEach
  public void setUp() {
    var activeRules = new ActiveRulesBuilder()
      .addRule(new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(DEFAULT_REPOSITORY_KEY, DEFAULT_RULE_KEY))
        .build())
      .addRule(new NewActiveRule.Builder()
        .setRuleKey(RuleKey.of(CUSTOM_REPOSITORY_KEY, CUSTOM_RULE_KEY))
        .build())
      .build();
    checkFactory = new CheckFactory(activeRules);

    customRulesDefinition = new MyCustomPlSqlRulesDefinition();
    var context = new RulesDefinition.Context();
    customRulesDefinition.define(context);
  }

  @SuppressWarnings("rawtypes")
  @Test
  void shouldReturnDefaultChecks() {
    var checks = CxxChecks.createCxxCheck(checkFactory);
    checks.addChecks(DEFAULT_REPOSITORY_KEY, new ArrayList<>(Collections.singletonList(MyRule.class)));

    SquidAstVisitor<Grammar> defaultCheck = check(checks, DEFAULT_REPOSITORY_KEY, DEFAULT_RULE_KEY);

    assertThat(checks.all()).hasSize(1);
    assertThat(checks.ruleKey(defaultCheck)).isNotNull();
    assertThat(checks.ruleKey(defaultCheck).rule()).isEqualTo(DEFAULT_RULE_KEY);
    assertThat(checks.ruleKey(defaultCheck).repository()).isEqualTo(DEFAULT_REPOSITORY_KEY);
  }

  @Test
  void shouldReturnCustomChecks() {
    var checks = CxxChecks.createCxxCheck(checkFactory);
    checks.addCustomChecks(new CustomCxxRulesDefinition[]{customRulesDefinition});

    SquidAstVisitor<Grammar> customCheck = check(checks, CUSTOM_REPOSITORY_KEY, CUSTOM_RULE_KEY);

    assertThat(checks.all()).hasSize(1);
    assertThat(checks.ruleKey(customCheck)).isNotNull();
    assertThat(checks.ruleKey(customCheck).rule()).isEqualTo(CUSTOM_RULE_KEY);
    assertThat(checks.ruleKey(customCheck).repository()).isEqualTo(CUSTOM_REPOSITORY_KEY);
  }

  @Test
  void shouldWorkWithoutCustomChecks() {
    var checks = CxxChecks.createCxxCheck(checkFactory);
    checks.addCustomChecks(null);
    assertThat(checks.all()).isEmpty();
  }

  @SuppressWarnings("rawtypes")
  @Test
  void shouldNotReturnRuleKeyIfCheckDoesNotExists() {
    var checks = CxxChecks.createCxxCheck(checkFactory);
    checks.addChecks(DEFAULT_REPOSITORY_KEY, new ArrayList<>(Collections.singletonList(MyRule.class)));
    assertThat(checks.ruleKey(new MyCustomRule())).isNull();
  }

  @CheckForNull
  public SquidAstVisitor<Grammar> check(CxxChecks cxxChecks, String repository, String rule) {
    RuleKey key = RuleKey.of(repository, rule);

    SquidAstVisitor<Grammar> check;
    for (var checks : cxxChecks.getChecks()) {
      check = checks.of(key);

      if (check != null) {
        return check;
      }
    }
    return null;
  }

  @Rule(key = DEFAULT_RULE_KEY, name = "This is the default rule", description = "desc")
  public static class MyRule extends SquidCheck<Grammar> {
  }

  @Rule(key = CUSTOM_RULE_KEY, name = "This is the custom rule", description = "desc")
  public static class MyCustomRule extends SquidCheck<Grammar> {
  }

  public static class MyCustomPlSqlRulesDefinition extends CustomCxxRulesDefinition {

    @Override
    public String repositoryName() {
      return "Custom Rule Repository";
    }

    @Override
    public String repositoryKey() {
      return CUSTOM_REPOSITORY_KEY;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] checkClasses() {
      return new Class[]{MyCustomRule.class};
    }

  }

}
