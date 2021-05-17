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
package org.sonar.cxx.squidbridge.annotations;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.check.Rule;

public class AnnotationBasedProfileBuilderTest {

  private static final String KEY1 = "key1";
  private static final String REPO_KEY = "repo1";
  private static final String PROFILE_NAME = "prfile1";
  private static final String LANGUAGE = "language1";

  private RuleFinder ruleFinder = mock(RuleFinder.class);
  private ValidationMessages messages = ValidationMessages.create();
  org.sonar.api.rules.Rule rule = mock(org.sonar.api.rules.Rule.class);
  private AnnotationBasedProfileBuilder builder = new AnnotationBasedProfileBuilder(ruleFinder);

  @Before
  public void setupRuleFinder() {
    when(rule.getSeverity()).thenReturn(RulePriority.MINOR);
    when(rule.isEnabled()).thenReturn(true);
    when(ruleFinder.findByKey(REPO_KEY, KEY1)).thenReturn(rule);
  }

  @Test
  public void should_add_a_rule_with_the_annotation() throws Exception {
    @ActivatedByDefault
    @Rule(key = KEY1)
    class RuleActivatedByDefault {
    }

    RulesProfile profile = build(RuleActivatedByDefault.class);
    assertThat(profile.getActiveRules()).hasSize(1);
    var activeRule = profile.getActiveRules().get(0);
    assertThat(activeRule.getRule()).isEqualTo(rule);
    assertThat(activeRule.getSeverity().toString()).isEqualTo(Severity.MINOR);
    assertThat(messages.getWarnings()).isEmpty();
  }

  @Test
  public void should_not_add_a_rule_without_the_annotation() throws Exception {
    @Rule(key = KEY1)
    class RuleNotActivatedByDefault {
    }

    RulesProfile profile = build(RuleNotActivatedByDefault.class);
    assertThat(profile.getActiveRules()).isEmpty();
    assertThat(messages.getWarnings()).isEmpty();
  }

  @Test
  public void unknown_rule_key() throws Exception {
    @ActivatedByDefault
    @Rule(key = "unknownKey")
    class RuleActivatedByDefaultWithUnknownKey {
    }

    RulesProfile profile = build(RuleActivatedByDefaultWithUnknownKey.class);
    assertThat(profile.getActiveRules()).isEmpty();
    assertThat(messages.getWarnings()).hasSize(1);
    assertThat(messages.getWarnings().get(0)).matches(".*not found.*unknownKey.*");
  }

  @Test
  public void should_ignore_class_without_rule_annotation() throws Exception {
    @ActivatedByDefault
    class ClassWithoutRuleAnnotation {
    }

    RulesProfile profile = build(ClassWithoutRuleAnnotation.class);
    assertThat(profile.getActiveRules()).isEmpty();
    assertThat(messages.getWarnings()).hasSize(1);
    assertThat(messages.getWarnings().get(0)).matches(".*ClassWithoutRuleAnnotation.*no Rule annotation.*");
  }

  private RulesProfile build(Class<?> clazz) {
    Collection<Class> annotatedClasses = ImmutableList.<Class>of(clazz);
    return builder.build(REPO_KEY, PROFILE_NAME, LANGUAGE, annotatedClasses, messages);
  }

}
