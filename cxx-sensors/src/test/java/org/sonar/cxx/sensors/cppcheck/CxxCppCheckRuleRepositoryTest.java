/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.sensors.cppcheck;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.sensors.utils.RulesDefinitionXmlLoader;

class CxxCppCheckRuleRepositoryTest {

  @Test
  void createRulesTest() {
    var def = new CxxCppCheckRuleRepository(
      mock(ServerFileSystem.class), new RulesDefinitionXmlLoader());

    var context = new RulesDefinition.Context();
    def.define(context);

    RulesDefinition.Repository repo = context.repository(CxxCppCheckRuleRepository.KEY);
    assertThat(repo.rules()).hasSize(712);
  }

}
