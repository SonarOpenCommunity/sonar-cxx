/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) SonarOpenCommunity
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.cxx.squidbridge.SquidAstVisitor;

public final class CxxChecks {

  private final CheckFactory checkFactory;
  private final Set<Checks<SquidAstVisitor<Grammar>>> checksByRepository = new HashSet<>();

  private CxxChecks(CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
  }

  public static CxxChecks createCxxCheck(CheckFactory checkFactory) {
    return new CxxChecks(checkFactory);
  }

  @SuppressWarnings("rawtypes")
  public CxxChecks addChecks(String repositoryKey, Iterable<Class> checkClass) {
    checksByRepository.add(checkFactory
      .<SquidAstVisitor<Grammar>>create(repositoryKey)
      .addAnnotatedChecks(checkClass));

    return this;
  }

  public CxxChecks addCustomChecks(@Nullable CustomCxxRulesDefinition[] customRulesDefinitions) {
    if (customRulesDefinitions != null) {
      for (var rulesDefinition : customRulesDefinitions) {
        addChecks(rulesDefinition.repositoryKey(), new ArrayList<>(Arrays.asList(rulesDefinition.checkClasses())));
      }
    }

    return this;
  }

  public List<SquidAstVisitor<Grammar>> all() {
    var allVisitors = new ArrayList<SquidAstVisitor<Grammar>>();

    for (var checks : checksByRepository) {
      allVisitors.addAll(checks.all());
    }

    return allVisitors;
  }

  @CheckForNull
  public RuleKey ruleKey(SquidAstVisitor<Grammar> check) {
    for (var checks : checksByRepository) {
      RuleKey ruleKey = checks.ruleKey(check);
      if (ruleKey != null) {
        return ruleKey;
      }
    }
    return null;
  }

  /**
   * Resolves the rule key for a check by its class rather than a live instance, for callers (e.g.
   * {@link org.sonar.cxx.utils.CxxReportIssue#getCheckClass()}) that only kept a {@code Class}
   * reference. A single {@code ruleId} string is not unique across repositories, so this looks up
   * the registered instance of {@code checkClass} and reuses its actual repository.
   *
   * @param checkClass the check's class
   * @return the resolved rule key, or null if no registered check has this class
   */
  @CheckForNull
  public RuleKey ruleKeyForClass(Class<?> checkClass) {
    for (var check : all()) {
      if (checkClass.equals(check.getClass())) {
        RuleKey ruleKey = ruleKey(check);
        if (ruleKey != null) {
          return ruleKey;
        }
      }
    }
    return null;
  }

  public Set<Checks<SquidAstVisitor<Grammar>>> getChecks() {
    return new HashSet<>(checksByRepository);
  }

}
