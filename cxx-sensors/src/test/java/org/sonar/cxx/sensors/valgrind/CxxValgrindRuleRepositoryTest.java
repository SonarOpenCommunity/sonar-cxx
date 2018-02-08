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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.cxx.CxxLanguage;
import org.sonar.cxx.sensors.utils.TestUtils;

public class CxxValgrindRuleRepositoryTest {

  @Test
  public void shouldContainProperNumberOfRules() {
    CxxLanguage language = TestUtils.mockCxxLanguage();
    CxxValgrindRuleRepository def = new CxxValgrindRuleRepository(mock(ServerFileSystem.class), new RulesDefinitionXmlLoader(), language);
    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(CxxValgrindRuleRepository.KEY);
    assertEquals(16, repo.rules().size());
  }

  @Test
  public void containsValidFormatInExtensionRulesOldFormat() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/cxx/sensors/rules-repository/CustomRulesOldFormat.xml"));
    CxxLanguage language = TestUtils.mockCxxLanguage();
    CxxValgrindRuleRepository obj = new CxxValgrindRuleRepository(filesystem, new RulesDefinitionXmlLoader(), language);
    CxxValgrindRuleRepository def = spy(obj);
    doReturn(extensionFile).when(def).getExtensions(CxxValgrindRuleRepository.KEY, "xml");

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(CxxValgrindRuleRepository.KEY);
    assertEquals(18, repo.rules().size());
  }

  @Test
  public void containsValidFormatInExtensionRulesNewFormat() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/cxx/sensors/rules-repository/CustomRulesNewFormat.xml"));
    CxxLanguage language = TestUtils.mockCxxLanguage();
    CxxValgrindRuleRepository obj = new CxxValgrindRuleRepository(filesystem, new RulesDefinitionXmlLoader(), language);
    CxxValgrindRuleRepository def = spy(obj);
    doReturn(extensionFile).when(def).getExtensions(CxxValgrindRuleRepository.KEY, "xml");

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(CxxValgrindRuleRepository.KEY);
    assertEquals(17, repo.rules().size());
  }

  @Test //@todo check if new behaviour is ok: Exception is replaced by error message in LOG file
  public void containsInvalidFormatInExtensionRulesNewFormat() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/cxx/sensors/rules-repository/CustomRulesInvalid.xml"));
    CxxLanguage language = TestUtils.mockCxxLanguage();
    CxxValgrindRuleRepository obj = new CxxValgrindRuleRepository(filesystem, new RulesDefinitionXmlLoader(), language);
    CxxValgrindRuleRepository def = spy(obj);
    doReturn(extensionFile).when(def).getExtensions(CxxValgrindRuleRepository.KEY, "xml");

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(CxxValgrindRuleRepository.KEY);
    assertEquals(16, repo.rules().size());
  }

  @Test //@todo check if new behaviour is ok: Exception is replaced by error message in LOG file
  public void containsEmptyExtensionRulesFile() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/cxx/sensors/rules-repository/CustomRulesEmptyFile.xml"));
    CxxLanguage language = TestUtils.mockCxxLanguage();
    CxxValgrindRuleRepository obj = new CxxValgrindRuleRepository(filesystem, new RulesDefinitionXmlLoader(), language);
    CxxValgrindRuleRepository def = spy(obj);
    doReturn(extensionFile).when(def).getExtensions(CxxValgrindRuleRepository.KEY, "xml");

    RulesDefinition.Context context = new RulesDefinition.Context();
    def.define(context);
    RulesDefinition.Repository repo = context.repository(CxxValgrindRuleRepository.KEY);
    assertEquals(16, repo.rules().size());
  }
}
