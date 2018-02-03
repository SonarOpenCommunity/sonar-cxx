/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.plugins.c;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.cxx.CxxLanguage;

public class CRuleRepositoryTest {

  private MapSettings settings = new MapSettings();

  @Test
  public void rulesTest() {
    CxxLanguage checks = new CLanguage(settings.asConfig());
    RulesDefinition.Context context = new RulesDefinition.Context();
    assertThat(context.repositories()).isEmpty();

    new CRuleRepository(checks).define(context);

    assertThat(context.repositories()).hasSize(1);
    assertThat(context.repository(checks.getRepositoryKey()).rules()).hasSize(45);
  }
}
