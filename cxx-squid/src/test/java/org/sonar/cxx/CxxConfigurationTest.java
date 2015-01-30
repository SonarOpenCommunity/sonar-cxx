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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CxxConfigurationTest {

  private static final String vcKey = "Visual C++";
  private static final String vcCharSet = "UTF8";
  
  @Test
  public void emptyValueShouldReturnNoDirsOrDefines() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog(new ArrayList<File>(), vcKey, vcCharSet);
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    assertThat(config.getDefines().size()).isEqualTo(0);
  }
  
  @Test
  public void emptyValueShouldReturnWhenNull() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog(null, vcKey, vcCharSet);
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    assertThat(config.getDefines().size()).isEqualTo(0);
  }
  
  @Test
  public void emptyValueShouldUseIncludeDirsIfSet() {
    CxxConfiguration config = new CxxConfiguration();
    String[] data = {"dir1", "dir2"};
    config.setIncludeDirectories(data);
    config.setCompilationPropertiesWithBuildLog(new ArrayList<File>(), vcKey, vcCharSet);
    assertThat(config.getIncludeDirectories().size()).isEqualTo(2);
  }  
  
  @Test
  public void correctlyCreatesConfiguration1() {
    CxxConfiguration config = new CxxConfiguration();
    List<File> files = new ArrayList<File>();
    files.add(new File("src/test/resources/compiler/vc++13.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    assertThat(config.getIncludeDirectories().size()).isEqualTo(11);    
    assertThat(config.getDefines().size()).isEqualTo(7);    
  }
  
  @Test
  public void correctlyCreatesConfiguration2() {
    CxxConfiguration config = new CxxConfiguration();
    List<File> files = new ArrayList<File>();
    files.add(new File("src/test/resources/compiler/compiler-report.log"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    assertThat(config.getIncludeDirectories().size()).isEqualTo(7);    
    assertThat(config.getDefines().size()).isEqualTo(22);    
  }

}
