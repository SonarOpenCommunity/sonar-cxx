/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2021-2024 SonarOpenCommunity
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

import java.net.URL;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.Repository;

class ExternalDescriptionLoaderTest {

  private static final String REPO_KEY = "repoKey";
  private static final String LANGUAGE_KEY = "languageKey";

  private final RulesDefinition.Context context = new RulesDefinition.Context();
  private final NewRepository repository = context.createRepository(REPO_KEY, LANGUAGE_KEY);

  @Test
  void existing_rule_description() throws Exception {
    repository.createRule("ruleWithExternalInfo").setName("name1");
    var rule = buildRepository().rule("ruleWithExternalInfo");
    assertThat(rule).isNotNull();
    assertThat(rule.htmlDescription()).isEqualTo("description for ruleWithExternalInfo");
  }

  @Test
  void rule_with_non_external_description() throws Exception {
    repository.createRule("ruleWithoutExternalInfo").setName("name1").setHtmlDescription("my description");
    var rule = buildRepository().rule("ruleWithoutExternalInfo");
    assertThat(rule).isNotNull();
    assertThat(rule.htmlDescription()).isEqualTo("my description");
  }

  @Test
  void rule_without_description() {
    repository.createRule("ruleWithoutExternalInfo").setName("name1");

    IllegalStateException thrown = catchThrowableOfType(() -> {
      buildRepository().rule("ruleWithoutExternalInfo");
    }, IllegalStateException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  @Test
  void invalid_url() {
    var loader = new ExternalDescriptionLoader(repository, LANGUAGE_KEY);
    var rule = repository.createRule("ruleWithoutExternalInfo").setName("name1");

    IllegalStateException thrown = catchThrowableOfType(() -> {
      loader.addHtmlDescription(rule, new URL("file:///xx/yy"));
    }, IllegalStateException.class);
    assertThat(thrown).isExactlyInstanceOf(IllegalStateException.class);
  }

  private Repository buildRepository() {
    ExternalDescriptionLoader.loadHtmlDescriptions(repository, "/org/sonar/l10n/languageKey/rules/repoKey");
    repository.done();
    return context.repository(REPO_KEY);
  }

}
