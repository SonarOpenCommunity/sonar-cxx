/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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
package org.sonar.cxx.checks;

import javax.annotation.CheckForNull;

import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.squidbridge.checks.SquidCheck;

import com.sonar.sslr.api.Grammar;

/**
 * Companion of {@link org.sonar.plugins.cxx.squid.DependencyAnalyzer} which actually does the job of finding cycles and
 * creating the violations.
 */
@Rule(key = CycleBetweenPackagesCheck.RULE_KEY,
      priority = Priority.MAJOR)

public class CycleBetweenPackagesCheck extends SquidCheck<Grammar> {

  public static final String RULE_KEY = "CycleBetweenPackages";

  /**
   * @return null, if this check is inactive
   */
  @CheckForNull
  public static ActiveRule getActiveRule(ActiveRules rules) {
    return rules.find(RuleKey.of(CheckList.REPOSITORY_KEY, RULE_KEY));
  }

  @Override
  public String toString() {
    return RULE_KEY + " rule";
  }

}
