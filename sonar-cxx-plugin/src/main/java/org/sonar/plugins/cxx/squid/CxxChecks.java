/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.cxx.squid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.cxx.api.CustomCxxRulesDefinition;
import org.sonar.squidbridge.SquidAstVisitor;

import com.google.common.collect.Lists;
import com.sonar.sslr.api.Grammar;

public class CxxChecks {

  private final CheckFactory checkFactory;
  private Set<Checks<SquidAstVisitor<Grammar>>> checksByRepository = new HashSet<>();

  private CxxChecks(CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
  }

  public static CxxChecks createCxxCheck(CheckFactory checkFactory) {
    return new CxxChecks(checkFactory);
  }

  @SuppressWarnings("rawtypes")
  public CxxChecks addChecks(String repositoryKey, List<Class> checkClass) {
    checksByRepository.add(checkFactory
      .<SquidAstVisitor<Grammar>>create(repositoryKey)
      .addAnnotatedChecks(checkClass));

    return this;
  }

  public CxxChecks addCustomChecks(@Nullable CustomCxxRulesDefinition[] customRulesDefinitions) {
    if (customRulesDefinitions != null) {
      for (CustomCxxRulesDefinition rulesDefinition : customRulesDefinitions) {
        addChecks(rulesDefinition.repositoryKey(), Lists.newArrayList(rulesDefinition.checkClasses()));
      }
    }

    return this;
  }

  public List<SquidAstVisitor<Grammar>> all() {
    List<SquidAstVisitor<Grammar>> allVisitors = new ArrayList<>();

    for (Checks<SquidAstVisitor<Grammar>> checks : checksByRepository) {
      allVisitors.addAll(checks.all());
    }

    return allVisitors;
  }

  @Nullable
  public RuleKey ruleKey(SquidAstVisitor<Grammar> check) {
    RuleKey ruleKey;

    for (Checks<SquidAstVisitor<Grammar>> checks : checksByRepository) {
      ruleKey = checks.ruleKey(check);

      if (ruleKey != null) {
        return ruleKey;
      }
    }
    return null;
  }
}
