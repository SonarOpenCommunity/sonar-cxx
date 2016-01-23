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
package org.sonar.plugins.cxx;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import org.sonar.squidbridge.commonrules.api.CommonRulesRepository; //@todo deprecated
import org.sonar.squidbridge.commonrules.internal.CommonRulesConstants;

/**
 *
 * @author Jorge Costa
 */
public class CxxCommonRulesEngineTest {

  @Test
  public void provide_extensions() {
    CxxCommonRulesEngine engine = new CxxCommonRulesEngine();
    assertThat(engine.provide()).isNotEmpty();
  }

  @Test
  public void enable_common_rules() {
    CxxCommonRulesEngine provider = new CxxCommonRulesEngine();
    CommonRulesRepository repo = provider.newRepository();
    assertThat(repo.enabledRuleKeys()).containsOnly(
      CommonRulesConstants.RULE_INSUFFICIENT_COMMENT_DENSITY,
      CommonRulesConstants.RULE_DUPLICATED_BLOCKS,
      CommonRulesConstants.RULE_FAILED_UNIT_TESTS,
      CommonRulesConstants.RULE_INSUFFICIENT_BRANCH_COVERAGE,
      CommonRulesConstants.RULE_INSUFFICIENT_LINE_COVERAGE,
      CommonRulesConstants.RULE_SKIPPED_UNIT_TESTS);
  }
}
