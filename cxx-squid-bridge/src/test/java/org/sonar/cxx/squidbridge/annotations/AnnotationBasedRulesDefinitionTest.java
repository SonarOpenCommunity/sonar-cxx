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
import static org.fest.assertions.Assertions.assertThat;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.DebtRemediationFunction.Type;
import org.sonar.api.server.rule.*;
import org.sonar.api.server.rule.RulesDefinition.NewRepository;
import org.sonar.api.server.rule.RulesDefinition.Param;
import org.sonar.api.server.rule.RulesDefinition.Repository;
import org.sonar.check.Cardinality;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

public class AnnotationBasedRulesDefinitionTest {

  private static final String REPO_KEY = "repoKey";
  private static final String LANGUAGE_KEY_WITH_RESOURCE_BUNDLE = "languageKey";

  private RulesDefinition.Context context = new RulesDefinition.Context();

  @org.junit.Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void no_class_to_add() throws Exception {
    assertThat(buildRepository(false).rules()).isEmpty();
  }

  @Test
  public void class_without_rule_annotation() throws Exception {
    class NotRuleClass {
    }
    thrown.expect(IllegalArgumentException.class);
    buildSingleRuleRepository(NotRuleClass.class);
  }

  @Test
  public void rule_annotation_data() throws Exception {

    @Rule(key = "key1", name = "name1", description = "description1", tags = "mytag")
    class RuleClass {

      @RuleProperty(key = "param1Key", description = "param1 description")
      public String param1 = "x";
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertThat(rule.key()).isEqualTo("key1");
    assertThat(rule.name()).isEqualTo("name1");
    assertThat(rule.htmlDescription()).isEqualTo("description1");
    assertThat(rule.markdownDescription()).isNull();
    assertThat(rule.tags()).containsOnly("mytag");
    assertThat(rule.template()).isFalse();
    assertThat(rule.params()).hasSize(1);
    assertParam(rule.params().get(0), "param1Key", "param1 description");
  }

  @Rule(name = "name1", description = "description1")
  class RuleClassWithoutAnnotationDefinedKey {
  }

  @Test
  public void rule_without_explicit_key() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    buildSingleRuleRepository(RuleClassWithoutAnnotationDefinedKey.class);
  }

  @Test
  public void rule_without_explicit_key_can_be_acceptable() throws Exception {
    Repository repository = buildRepository(LANGUAGE_KEY_WITH_RESOURCE_BUNDLE, false, false,
                                            RuleClassWithoutAnnotationDefinedKey.class);
    var rule = repository.rules().get(0);
    assertThat(rule.key()).isEqualTo(RuleClassWithoutAnnotationDefinedKey.class.getCanonicalName());
    assertThat(rule.name()).isEqualTo("name1");
  }

  @Test
  public void external_names_and_descriptions() throws Exception {

    @Rule(key = "ruleWithExternalInfo")
    class RuleClass {

      @RuleProperty(key = "param1Key")
      public String param1 = "x";
      @RuleProperty
      public String param2 = "x";
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertThat(rule.key()).isEqualTo("ruleWithExternalInfo");
    assertThat(rule.name()).isEqualTo("external name for ruleWithExternalInfo");
    assertThat(rule.htmlDescription()).isEqualTo("description for ruleWithExternalInfo");
    assertThat(rule.params()).hasSize(2);
    assertParam(rule.params().get(0), "param1Key", "description for param1");
    assertParam(rule.params().get(1), "param2", null);
  }

  @Test
  public void no_name_and_no_resource_bundle() throws Exception {
    @Rule(key = "ruleWithExternalInfo")
    class RuleClass {
    }

    thrown.expect(IllegalStateException.class);
    buildRepository("languageWithoutBundle", false, false, RuleClass.class);
  }

  @Test
  public void rule_template() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1")
    @NoSqale
    @RuleTemplate
    class RuleClass {
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertThat(rule.template()).isTrue();
  }

  @Test
  public void cardinality_single() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1", cardinality = Cardinality.SINGLE)
    class RuleClass {
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertThat(rule.template()).isFalse();
  }

  @Test
  public void cardinality_multiple() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1", cardinality = Cardinality.MULTIPLE)
    class RuleClass {
    }

    thrown.expect(IllegalArgumentException.class);
    buildSingleRuleRepository(RuleClass.class);
  }

  @Test
  public void class_without_sqale_annotation() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1")
    class RuleClass {
    }

    thrown.expect(IllegalArgumentException.class);
    buildRepository(true, RuleClass.class);
  }

  @Test
  public void class_with_nosqale_annotation() throws Exception {

    @Rule(key = "key1", name = "name1", description = "description1")
    @NoSqale
    class RuleClass {
    }

    Repository repository = buildRepository(true, RuleClass.class);
    assertThat(repository.rules()).hasSize(1);
  }

  @Test
  public void class_with_sqale_constant_remediation() throws Exception {

    @Rule(key = "key1", name = "name1", description = "description1")
    @SqaleConstantRemediation("10min")
    class RuleClass {
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertRemediation(rule, Type.CONSTANT_ISSUE, null, "10min", null);
  }

  @Test
  public void class_with_sqale_linear_remediation() throws Exception {

    @Rule(key = "key1", name = "name1", description = "description1")
    @SqaleLinearRemediation(coeff = "2h", effortToFixDescription = "Effort to test one uncovered condition")
    class RuleClass {
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertRemediation(rule, Type.LINEAR, "2h", null, "Effort to test one uncovered condition");
  }

  @Test
  public void class_with_sqale_linear_with_offset_remediation() throws Exception {

    @Rule(key = "key1", name = "name1", description = "description1")
    @SqaleLinearWithOffsetRemediation(coeff = "5min", offset = "1h",
                                      effortToFixDescription = "Effort to test one uncovered condition")
    class RuleClass {
    }

    RulesDefinition.Rule rule = buildSingleRuleRepository(RuleClass.class);
    assertRemediation(rule, Type.LINEAR_OFFSET, "5min", "1h", "Effort to test one uncovered condition");
  }

  @Test
  public void class_with_several_sqale_remediation_annotations() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1")
    @SqaleConstantRemediation("10min")
    @SqaleLinearRemediation(coeff = "2h", effortToFixDescription = "Effort to test one uncovered condition")
    class RuleClass {
    }

    thrown.expect(IllegalArgumentException.class);
    buildSingleRuleRepository(RuleClass.class);
  }

  @Test
  public void invalid_sqale_annotation() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1")
    @SqaleConstantRemediation("xxx")
    class MyInvalidRuleClass {
    }

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("MyInvalidRuleClass");
    buildSingleRuleRepository(MyInvalidRuleClass.class);
  }

  @Test
  public void rule_not_created_by_RulesDefinitionAnnotationLoader() throws Exception {
    @Rule
    class RuleClass {
    }
    var newRepository = context.createRepository(REPO_KEY, "language1");
    var rulesDef = new AnnotationBasedRulesDefinition(newRepository, "language1");
    thrown.expect(IllegalStateException.class);
    rulesDef.newRule(RuleClass.class, false);
  }

  @Test
  public void load_method_with_class_without_sqale_annotation() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1")
    class RuleClass {
    }
    thrown.expect(IllegalArgumentException.class);
    load(RuleClass.class);
  }

  @Test
  public void load_method_with_class_with_sqale_annotations() throws Exception {
    @Rule(key = "key1", name = "name1", description = "description1")
    @SqaleConstantRemediation("10min")
    class RuleClass {
    }
    Repository repository = load(RuleClass.class);
    assertThat(repository.rules()).hasSize(1);
  }

  private void assertRemediation(RulesDefinition.Rule rule, Type type, String coeff, String offset, String effortDesc) {
    DebtRemediationFunction remediationFunction = rule.debtRemediationFunction();
    assertThat(remediationFunction.type()).isEqualTo(type);
    assertThat(remediationFunction.gapMultiplier()).isEqualTo(coeff);
    assertThat(remediationFunction.baseEffort()).isEqualTo(offset);
    assertThat(rule.gapDescription()).isEqualTo(effortDesc);
  }

  private void assertParam(Param param, String expectedKey, String expectedDescription) {
    assertThat(param.key()).isEqualTo(expectedKey);
    assertThat(param.name()).isEqualTo(expectedKey);
    assertThat(param.description()).isEqualTo(expectedDescription);
  }

  private RulesDefinition.Rule buildSingleRuleRepository(Class<?> ruleClass) {
    Repository repository = buildRepository(false, ruleClass);
    assertThat(repository.rules()).hasSize(1);
    return repository.rules().get(0);
  }

  private Repository buildRepository(boolean failIfSqaleNotFound, Class<?>... classes) {
    return buildRepository(LANGUAGE_KEY_WITH_RESOURCE_BUNDLE, failIfSqaleNotFound, true, classes);
  }

  private Repository buildRepository(String languageKey, boolean failIfSqaleNotFound, boolean failIfNoExplicitKey,
                                     Class... classes) {
    NewRepository newRepository = createRepository(languageKey);
    new AnnotationBasedRulesDefinition(newRepository, languageKey)
      .addRuleClasses(failIfSqaleNotFound, failIfNoExplicitKey, ImmutableList.copyOf(classes));
    newRepository.done();
    return context.repository(REPO_KEY);
  }

  private Repository load(Class... classes) {
    String languageKey = LANGUAGE_KEY_WITH_RESOURCE_BUNDLE;
    NewRepository newRepository = createRepository(languageKey);
    AnnotationBasedRulesDefinition.load(newRepository, languageKey, ImmutableList.copyOf(classes));
    newRepository.done();
    return context.repository(REPO_KEY);
  }

  private NewRepository createRepository(String languageKey) {
    return context.createRepository(REPO_KEY, languageKey);
  }

}
