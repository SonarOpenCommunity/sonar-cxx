/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/vc++13.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(11);
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
    Assertions.assertThat(defines.contains("_OPENMP 200203")).isTrue();
    Assertions.assertThat(defines.contains("_WIN32")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86 600")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    Assertions.assertThat(defines.contains("_WCHAR_T_DEFINED 1")).isTrue();
    Assertions.assertThat(defines.contains("_NATIVE_WCHAR_T_DEFINED 1")).isTrue();
    Assertions.assertThat(defines.contains("_VC_NODEFAULTLIB")).isTrue();
    Assertions.assertThat(defines.contains("_MT")).isTrue();
    Assertions.assertThat(defines.contains("_DLL")).isTrue();
    Assertions.assertThat(defines.contains("_DEBUG")).isTrue();
    Assertions.assertThat(defines.contains("_VC_NODEFAULTLIB")).isTrue();
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
    Assertions.assertThat(defines.contains("_WIN32")).isTrue();
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
    Assertions.assertThat(defines.contains("_Wp64")).isTrue();
    Assertions.assertThat(defines.contains("_WIN32")).isTrue();
    Assertions.assertThat(defines.contains("_WIN64")).isTrue();
    Assertions.assertThat(defines.contains("_M_X64 100")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86")).isFalse();
    Assertions.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
  }

  @Test
  public void shouldHandleSpecificV100OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformToolsetv100.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(12 + 6);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86 600")).isTrue();
    Assertions.assertThat(defines.contains("_WIN32")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
  }

  @Test
  public void shouldHandleSpecificV110OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformToolsetv110.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(13 + 5);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines.contains("__cplusplus_winrt 201009")).isTrue();
    Assertions.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86 600")).isTrue();
    Assertions.assertThat(defines.contains("_WIN32")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_VER 1700")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_FULL_VER 1700610301")).isTrue();
    Assertions.assertThat(defines.contains("_ATL_VER 0x0B00")).isTrue();    
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
    Assertions.assertThat(defines.contains("__AVX2__ 1")).isTrue();
    Assertions.assertThat(defines.contains("__AVX__ 1")).isTrue();
    Assertions.assertThat(defines.contains("__cplusplus_winrt 201009")).isTrue();
    Assertions.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    Assertions.assertThat(defines.contains("_M_ARM_FP")).isTrue();
    Assertions.assertThat(defines.contains("_WIN32")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86 600")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_VER 1800")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_FULL_VER 180031101")).isTrue();
    Assertions.assertThat(defines.contains("_ATL_VER 0x0C00")).isTrue();
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
    Assertions.assertThat(defines.contains("__AVX2__ 1")).isTrue();
    Assertions.assertThat(defines.contains("__AVX__ 1")).isTrue();
    Assertions.assertThat(defines.contains("__cplusplus_winrt 201009")).isTrue();
    Assertions.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    Assertions.assertThat(defines.contains("_M_ARM_FP")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86 600")).isTrue();
    Assertions.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_VER 1900")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_FULL_VER 190024215")).isTrue();
    Assertions.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
  }

  @Test
  public void shouldHandleSpecificV141x86OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformToolsetv141x86.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 12);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines.contains("_M_IX86 600")).isTrue();    
    Assertions.assertThat(defines.contains("__cplusplus 199711L")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_VER 1910")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_FULL_VER 191024629")).isTrue();
    // check atldef.h for _ATL_VER
    Assertions.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
  }

  @Test
  public void shouldHandleSpecificV141x64OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/platformToolsetv141x64.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 14);
    ValidateDefaultAsserts(defines);
    Assertions.assertThat(defines.contains("_M_IX86 600")).isFalse();
    Assertions.assertThat(defines.contains("__cplusplus 199711L")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_VER 1910")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_FULL_VER 191024629")).isTrue();
    // check atldef.h for _ATL_VER
    Assertions.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
  }
  @Test
  public void shouldHandleBuildLog() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/compiler/ParallelBuildLog.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(15);
    assertThat(config.getDefines().size()).isEqualTo(30);
  }  

  private void ValidateDefaultAsserts(List<String> defines) {
    Assertions.assertThat(defines.contains("_INTEGRAL_MAX_BITS 64")).isTrue();
    Assertions.assertThat(defines.contains("_MSC_BUILD 1")).isTrue();
    Assertions.assertThat(defines.contains("__COUNTER__ 0")).isTrue();
    Assertions.assertThat(defines.contains("__DATE__ \"??? ?? ????\"")).isTrue();
    Assertions.assertThat(defines.contains("__FILE__ \"file\"")).isTrue();
    Assertions.assertThat(defines.contains("__LINE__ 1")).isTrue();
    Assertions.assertThat(defines.contains("__TIME__ \"??:??:??\"")).isTrue();
    Assertions.assertThat(defines.contains("__TIMESTAMP__ \"??? ?? ???? ??:??:??\"")).isTrue();

  }

}
