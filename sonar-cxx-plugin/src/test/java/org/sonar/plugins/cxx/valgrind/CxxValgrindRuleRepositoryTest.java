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
package org.sonar.plugins.cxx.valgrind;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;

import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.platform.ServerFileSystem;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.plugins.cxx.TestUtils;

public class CxxValgrindRuleRepositoryTest {
  @Test
  public void shouldContainProperNumberOfRules() {
    CxxValgrindRuleRepository repo = new CxxValgrindRuleRepository(mock(ServerFileSystem.class), new XMLRuleParser(), new Settings());
    assertEquals(repo.createRules().size(), 16);
  }

  @Test
  public void containsValidFormatInExtensionRulesOldFormat() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<File>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/plugins/cxx/rules-repository/CustomRulesOldFormat.xml"));
    when(filesystem.getExtensions(CxxValgrindRuleRepository.KEY, "xml")).thenReturn(extensionFile);
    CxxValgrindRuleRepository repo = new CxxValgrindRuleRepository(filesystem, new XMLRuleParser(), new Settings());
    assertEquals(repo.createRules().size(), 18);
  }

  @Test
  public void containsValidFormatInExtensionRulesNewFormat() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<File>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/plugins/cxx/rules-repository/CustomRulesNewFormat.xml"));
    when(filesystem.getExtensions(CxxValgrindRuleRepository.KEY, "xml")).thenReturn(extensionFile);
    CxxValgrindRuleRepository repo = new CxxValgrindRuleRepository(filesystem, new XMLRuleParser(), new Settings());
    assertEquals(repo.createRules().size(), 17);
  }

  @Test(expected = org.sonar.api.utils.SonarException.class)
  public void containsInvalidFormatInExtensionRulesNewFormat() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<File>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/plugins/cxx/rules-repository/CustomRulesInvalid.xml"));
    when(filesystem.getExtensions(CxxValgrindRuleRepository.KEY, "xml")).thenReturn(extensionFile);
    CxxValgrindRuleRepository repo = new CxxValgrindRuleRepository(filesystem, new XMLRuleParser(), new Settings());
    repo.createRules();
  }

  @Test(expected = org.sonar.api.utils.SonarException.class)
  public void containsEmptyExtensionRulesFile() {
    ServerFileSystem filesystem = mock(ServerFileSystem.class);
    ArrayList<File> extensionFile = new ArrayList<File>();
    extensionFile.add(TestUtils.loadResource("/org/sonar/plugins/cxx/rules-repository/CustomRulesEmptyFile.xml"));
    when(filesystem.getExtensions(CxxValgrindRuleRepository.KEY, "xml")).thenReturn(extensionFile);
    CxxValgrindRuleRepository repo = new CxxValgrindRuleRepository(filesystem, new XMLRuleParser(), new Settings());
    repo.createRules();
  }
}
