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
package org.sonar.cxx;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

public class CxxConfigurationTest {

  @Test
  public void emptyValueShouldReturnNoDirsOrDefines() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog("", "vc++");
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    assertThat(config.getDefines().size()).isEqualTo(0);
  }
  
  @Test
  public void emptyValueShouldReturnWhenNull() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog(null, "vc++");
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    assertThat(config.getDefines().size()).isEqualTo(0);
  }
  
  @Test
  public void emptyValueShouldUseIncludeDirsIfSet() {
    CxxConfiguration config = new CxxConfiguration();
    String[] data = {"dir1", "dir2"};
    config.setIncludeDirectories(data);
    config.setCompilationPropertiesWithBuildLog("", "vc++");
    assertThat(config.getIncludeDirectories().size()).isEqualTo(2);
  }  
  
  @Test
  public void correctlyCreatesConfiguration() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog(
            (new File("src/test/resources/compiler/vc++13.txt")).getAbsolutePath(), "vc++");
    assertThat(config.getIncludeDirectories().size()).isEqualTo(11);
    assertThat(config.getIncludeDirectories().get(0)).isEqualTo("C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\include");
    assertThat(config.getIncludeDirectories().get(1)).isEqualTo("C:\\Program Files (x86)\\Microsoft Visual Studio 12.0\\VC\\atlmfc\\include");
    assertThat(config.getIncludeDirectories().get(2)).endsWith("sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\memlib\\interface");
    assertThat(config.getIncludeDirectories().get(3)).isEqualTo("C:\\Program Files (x86)\\Windows Kits\\8.1\\Include\\um");
    assertThat(config.getIncludeDirectories().get(4)).isEqualTo("C:\\Program Files (x86)\\Windows Kits\\8.1\\Include\\shared");
    assertThat(config.getIncludeDirectories().get(5)).endsWith("Packages\\gtestmock.1.7.2\\build\\native\\include\\googletest");
    assertThat(config.getIncludeDirectories().get(6)).isEqualTo("C:\\Program Files (x86)\\Windows Kits\\8.1\\Include\\winrt");
    assertThat(config.getIncludeDirectories().get(7)).endsWith("sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\tools\\interface");
    assertThat(config.getIncludeDirectories().get(8)).endsWith("sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project\\PathHandling\\interface");
    assertThat(config.getIncludeDirectories().get(9)).endsWith("Packages\\gtestmock.1.7.2\\build\\native\\include");
    assertThat(config.getIncludeDirectories().get(10)).endsWith("sonar-cxx\\integration-tests\\testdata\\googletest_bullseye_vs_project");
    
    assertThat(config.getDefines().size()).isEqualTo(9);
    assertThat(config.getDefines().get(0)).isEqualTo("OS_NT");
    assertThat(config.getDefines().get(1)).isEqualTo("_ITERATOR_DEBUG_LEVEL 0");
    assertThat(config.getDefines().get(2)).isEqualTo("NT");
    assertThat(config.getDefines().get(3)).isEqualTo("ANSI_HEADER");
    assertThat(config.getDefines().get(4)).isEqualTo("_SCL_SECURE_NO_WARNINGS");
    assertThat(config.getDefines().get(5)).isEqualTo("WIN32");
    assertThat(config.getDefines().get(6)).isEqualTo("USEMEMLIB");
    assertThat(config.getDefines().get(7)).isEqualTo("_MBCS");
    assertThat(config.getDefines().get(8)).isEqualTo("GTEST_LINKED_AS_SHARED_LIBRARY 0");
    
  }

}
