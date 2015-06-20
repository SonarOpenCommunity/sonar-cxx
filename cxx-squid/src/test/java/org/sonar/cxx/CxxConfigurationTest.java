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
import org.fest.assertions.Assertions;

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
    config.setBaseDir(".");
    List<File> files = new ArrayList<File>();
    files.add(new File("src/test/resources/compiler/vc++13.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(13);    
    assertThat(config.getDefines().size()).isEqualTo(26 + 5);    
  }

  @Test
  public void shouldHandleSpecificCommonOptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformCommon.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);    
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(20 + 5);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines).as("_OPENMP");
    Assertions.assertThat(defines).as("_WIN32");
    Assertions.assertThat(defines).as("_M_IX86");
    Assertions.assertThat(defines).as("_M_IX86_FP");
    Assertions.assertThat(defines).as("_WCHAR_T_DEFINED");
    Assertions.assertThat(defines).as("_NATIVE_WCHAR_T_DEFINED");
    Assertions.assertThat(defines).as("_VC_NODEFAULTLIB");     
    Assertions.assertThat(defines).as("_OPENMP");
    Assertions.assertThat(defines).as("_MT");
    Assertions.assertThat(defines).as("_DLL");
    Assertions.assertThat(defines).as("_DEBUG");
    Assertions.assertThat(defines).as("_VC_NODEFAULTLIB");    
  }

  public void shouldHandleSpecificCommonWin32OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformCommonWin32.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(3);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines).as("_WIN32");
  }

  @Test
  public void shouldHandleSpecificCommonx64OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformCommonX64.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 5);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines).as("_Wp64");
    Assertions.assertThat(defines).as("_WIN32");
    Assertions.assertThat(defines).as("_WIN64");
    Assertions.assertThat(defines).as("_M_X64");
    Assertions.assertThat(defines).as("_M_IX86");
    Assertions.assertThat(defines).as("_M_IX86_FP");    
  }

  @Test
  public void shouldHandleSpecificV100OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<File>();
    files.add(new File("src/test/resources/compiler/platformToolsetv100.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);    
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(12 + 6);
    ValidateDefaultAsserts(defines);    
    Assertions.assertThat(defines).as("_CPPUNWIND");
    Assertions.assertThat(defines).as("_M_IX86");
    Assertions.assertThat(defines).as("_WIN32");
    Assertions.assertThat(defines).as("_M_IX86_FP");    
  }

  @Test
  public void shouldHandleSpecificV110OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<File>();
    files.add(new File("src/test/resources/compiler/platformToolsetv110.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);    
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(13 + 6);
    ValidateDefaultAsserts(defines);    
    Assertions.assertThat(defines).as("__cplusplus_winrt");
    Assertions.assertThat(defines).as("_CPPUNWIND");
    Assertions.assertThat(defines).as("_M_IX86");
    Assertions.assertThat(defines).as("_WIN32");
    Assertions.assertThat(defines).as("_M_IX86_FP");    
  }

  @Test
  public void shouldHandleSpecificV120OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformToolsetv120.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);    
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 6);
    ValidateDefaultAsserts(defines);    
    Assertions.assertThat(defines).as("__AVX2__");
    Assertions.assertThat(defines).as("__AVX__");
    Assertions.assertThat(defines).as("__cplusplus_winrt");
    Assertions.assertThat(defines).as("_CPPUNWIND");
    Assertions.assertThat(defines).as("_M_ARM_FP");
    Assertions.assertThat(defines).as("_WIN32");
    Assertions.assertThat(defines).as("_M_IX86");
    Assertions.assertThat(defines).as("_M_IX86_FP");       
  }

  @Test
  public void shouldHandleSpecificV140OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformToolsetv140.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);
    
    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);    
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 6);
    ValidateDefaultAsserts(defines);    
    Assertions.assertThat(defines).as("__AVX2__");
    Assertions.assertThat(defines).as("__AVX__");
    Assertions.assertThat(defines).as("__cplusplus_winrt");
    Assertions.assertThat(defines).as("_CPPUNWIND");
    Assertions.assertThat(defines).as("_M_ARM_FP");
    Assertions.assertThat(defines).as("_WIN64");
    Assertions.assertThat(defines).as("_M_IX86");
    Assertions.assertThat(defines).as("_M_IX86_FP");       
  }

  private void ValidateDefaultAsserts(List<String> defines) {
    Assertions.assertThat(defines).as("_INTEGRAL_MAX_BITS");        
    Assertions.assertThat(defines).as("_MSC_VER");
    Assertions.assertThat(defines).as("_MSC_BUILD");
    Assertions.assertThat(defines).as("_MSC_FULL_VER");
    Assertions.assertThat(defines).as("_MSC_VER");
    Assertions.assertThat(defines).as("__COUNTER__");
    Assertions.assertThat(defines).as("__DATE__");
    Assertions.assertThat(defines).as("__FILE__");
    Assertions.assertThat(defines).as("__LINE__");
    Assertions.assertThat(defines).as("__TIME__");
    Assertions.assertThat(defines).as("__TIMESTAMP__");  
    Assertions.assertThat(defines).as("_ATL_VER");
  }
  
}
