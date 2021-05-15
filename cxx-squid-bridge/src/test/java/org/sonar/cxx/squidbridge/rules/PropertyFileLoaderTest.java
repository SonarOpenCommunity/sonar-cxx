/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021 SonarOpenCommunity
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
/**
 * fork of SSLR Squid Bridge: https://github.com/SonarSource/sslr-squid-bridge/tree/2.6.1
 * Copyright (C) 2010 SonarSource / mailto: sonarqube@googlegroups.com / license: LGPL v3
 */
package org.sonar.cxx.squidbridge.rules;

import java.io.IOException;
import java.io.InputStream;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.api.server.rule.RulesDefinition.Rule;

public class PropertyFileLoaderTest {

  private static final String PARAM_KEY = "param1";
  private static final String RULE_KEY = "rule1";

  private RulesDefinition.Context context = new RulesDefinition.Context();
  private NewRepository repository = context.createRepository("repoKey", "languageKey");

  @Test
  public void rule_and_parameter_defined_in_property_file() throws Exception {
    var newRule = repository.createRule(RULE_KEY);
    newRule.setHtmlDescription("desc");
    newRule.createParam(PARAM_KEY);
    PropertyFileLoader.loadNames(repository, "/rules/names.properties");
    Rule rule = buildRepository().rule(RULE_KEY);
    assertThat(rule.name()).isEqualTo("my rule name1");
    assertThat(rule.param(PARAM_KEY).description()).isEqualTo("my param description1");
  }

  @Test
  public void rule_and_parameter_not_defined_in_property_file() throws Exception {
    var newRule = repository.createRule(RULE_KEY);
    newRule.setName("ruleName1");
    newRule.setHtmlDescription("desc");
    var newParam = newRule.createParam(PARAM_KEY);
    newParam.setDescription("paramName1");
    PropertyFileLoader.loadNames(repository, "/rules/empty.properties");
    Rule rule = buildRepository().rule(RULE_KEY);
    assertThat(rule.name()).isEqualTo("ruleName1");
    assertThat(rule.param(PARAM_KEY).description()).isEqualTo("paramName1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_resource_is_not_found() throws Exception {
    PropertyFileLoader.loadNames(repository, "/rules/unknown.properties");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_resource_has_invalid_format() throws Exception {
    InputStream stream = mock(InputStream.class);
    doThrow(new IOException()).when(stream).read((byte[]) any());
    PropertyFileLoader.loadNames(repository, stream);
  }

  private Repository buildRepository() {
    repository.done();
    return context.repository("repoKey");
  }

}
