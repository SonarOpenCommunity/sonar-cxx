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
package org.sonar.plugins.cxx.pclint;

import org.junit.Test;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.XMLRuleParser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import org.sonar.api.config.Settings;

public class CxxPCLintRuleRepositoryTest {

  @Test
  public void createRulesTest() {
    CxxPCLintRuleRepository rulerep = new CxxPCLintRuleRepository(
        mock(ServerFileSystem.class),
        new XMLRuleParser(), new Settings());
    assertEquals(1410, rulerep.createRules().size());
  }
}
