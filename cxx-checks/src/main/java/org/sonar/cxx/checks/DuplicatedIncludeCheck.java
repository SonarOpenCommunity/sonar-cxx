/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.cxx.checks;

import javax.annotation.CheckForNull;

import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.squidbridge.checks.SquidCheck;
import com.sonar.sslr.api.Grammar;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.cxx.tag.Tag;

/**
 * Companion of {@link org.sonar.plugins.cxx.squid.DependencyAnalyzer} which
 * actually does the job of finding duplicated includes
 */
@Rule(
  key = "DuplicatedInclude",
  name = "Avoid including the same file multiple times",
  tags = {Tag.PREPROCESSOR},
  priority = Priority.MAJOR)
@ActivatedByDefault
@SqaleConstantRemediation("5min")
public class DuplicatedIncludeCheck extends SquidCheck<Grammar> {

  public static final String RULE_KEY = "DuplicatedInclude";

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
