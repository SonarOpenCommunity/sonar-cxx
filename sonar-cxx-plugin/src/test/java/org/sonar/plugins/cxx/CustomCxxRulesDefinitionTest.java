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
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.cxx.squidbridge.checks.SquidCheck;
import org.sonar.cxx.tag.Tag;

class CustomCxxRulesDefinitionTest {

  private static final String REPOSITORY_NAME = "Custom Rule Repository";
  private static final String REPOSITORY_KEY = "CustomRuleRepository";

  private static final String RULE_NAME = "This is my custom rule";
  private static final String RULE_KEY = "MyCustomRule";

  @Test
  void test() {
    var rulesDefinition = new MyCustomPlSqlRulesDefinition();
    var context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository(REPOSITORY_KEY);

    assertThat(repository.name()).isEqualTo(REPOSITORY_NAME);
    assertThat(repository.rules()).hasSize(1);

    RulesDefinition.Rule alertUseRule = repository.rule(RULE_KEY);
    assertThat(alertUseRule).isNotNull();
    assertThat(alertUseRule.name()).isEqualTo(RULE_NAME);

    for (var rule : repository.rules()) {
      for (var param : rule.params()) {
        assertThat(param.description()).as("description for " + param.key()).isNotEmpty();
      }
    }
  }

  public static class MyCustomPlSqlRulesDefinition extends CustomCxxRulesDefinition {

    @Override
    public String repositoryName() {
      System.out.println(REPOSITORY_NAME);
      return REPOSITORY_NAME;
    }

    @Override
    public String repositoryKey() {
      return REPOSITORY_KEY;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] checkClasses() {
      return new Class[]{MyCustomRule.class};
    }

  }

  @Rule(
    key = RULE_KEY,
    name = RULE_NAME,
    description = "desc",
    tags = {Tag.BUG})
  public class MyCustomRule extends SquidCheck<Grammar> {

    @RuleProperty(
      key = "customParam",
      description = "Custom parameter",
      defaultValue = "value")
    public String customParam = "value";
  }

}
