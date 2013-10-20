/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
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
package org.sonar.plugins.cxx.cppcheck;

import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.XMLRuleParser;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class CxxCppCheckRuleRepositoryTest {
  @Test
  public void createRulesTest() {
    CxxCppCheckRuleRepository rulerep = new CxxCppCheckRuleRepository(
        mock(ServerFileSystem.class), new XMLRuleParser());
    assertThat(rulerep.createRules()).hasSize(304);
  }
}
