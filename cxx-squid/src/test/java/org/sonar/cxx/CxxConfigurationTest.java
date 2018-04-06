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
package org.sonar.cxx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.fest.assertions.Assertions;
import org.junit.Test;

public class CxxConfigurationTest {

  private static final String vcKey = "Visual C++";
  private static final String vcCharSet = "UTF8";

  @Test
  public void emptyValueShouldReturnNoDirsOrDefines() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog(new ArrayList<File>(), vcKey, vcCharSet);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    softly.assertThat(config.getDefines().size()).isEqualTo(0);
    softly.assertAll();
  }

  @Test
  public void emptyValueShouldReturnWhenNull() {
    CxxConfiguration config = new CxxConfiguration();
    config.setCompilationPropertiesWithBuildLog(null, vcKey, vcCharSet);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    assertThat(config.getDefines().size()).isEqualTo(0);
    softly.assertAll();
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
    files.add(new File("src/test/resources/logfile/vc++13.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(13);
    softly.assertThat(config.getDefines().size()).isEqualTo(26 + 5);
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificCommonOptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformCommon.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    softly.assertThat(defines.size()).isEqualTo(20 + 5);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_OPENMP 200203")).isTrue();
    softly.assertThat(defines.contains("_WIN32")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertThat(defines.contains("_WCHAR_T_DEFINED 1")).isTrue();
    softly.assertThat(defines.contains("_NATIVE_WCHAR_T_DEFINED 1")).isTrue();
    softly.assertThat(defines.contains("_VC_NODEFAULTLIB")).isTrue();
    softly.assertThat(defines.contains("_MT")).isTrue();
    softly.assertThat(defines.contains("_DLL")).isTrue();
    softly.assertThat(defines.contains("_DEBUG")).isTrue();
    softly.assertThat(defines.contains("_VC_NODEFAULTLIB")).isTrue();
    softly.assertAll();
  }

  public void shouldHandleSpecificCommonWin32OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformCommonWin32.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    softly.assertThat(defines.size()).isEqualTo(3);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_WIN32")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificCommonx64OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformCommonX64.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(defines.size()).isEqualTo(15 + 5);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_Wp64")).isTrue();
    softly.assertThat(defines.contains("_WIN32")).isTrue();
    softly.assertThat(defines.contains("_WIN64")).isTrue();
    softly.assertThat(defines.contains("_M_X64 100")).isTrue();
    softly.assertThat(defines.contains("_M_IX86")).isFalse();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificV100OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformToolsetv100.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    softly.assertThat(defines.size()).isEqualTo(12 + 6);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_WIN32")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificV110OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformToolsetv110.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    softly.assertThat(defines.size()).isEqualTo(13 + 5);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("__cplusplus_winrt 201009")).isTrue();
    softly.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_WIN32")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1700")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 1700610301")).isTrue();
    softly.assertThat(defines.contains("_ATL_VER 0x0B00")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificV120OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformToolsetv120.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    softly.assertThat(defines.size()).isEqualTo(15 + 6);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("__AVX2__ 1")).isTrue();
    softly.assertThat(defines.contains("__AVX__ 1")).isTrue();
    softly.assertThat(defines.contains("__cplusplus_winrt 201009")).isTrue();
    softly.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    softly.assertThat(defines.contains("_M_ARM_FP")).isTrue();
    softly.assertThat(defines.contains("_WIN32")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1800")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 180031101")).isTrue();
    softly.assertThat(defines.contains("_ATL_VER 0x0C00")).isTrue();
    softly.assertAll();
    }

  @Test
  public void shouldHandleSpecificV140OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformToolsetv140.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 6);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("__AVX2__ 1")).isTrue();
    softly.assertThat(defines.contains("__AVX__ 1")).isTrue();
    softly.assertThat(defines.contains("__cplusplus_winrt 201009")).isTrue();
    softly.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    softly.assertThat(defines.contains("_M_ARM_FP")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1900")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 190024215")).isTrue();
    softly.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleTFSAgentV141OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/TFS-agent-msvc14.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(2);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(34);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1910")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 191024629")).isTrue();
    softly.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleTFSAgentV141mpOptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/TFS-agent-msvc14-mp.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(2);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(34);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_CPPUNWIND")).isTrue();
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("_M_IX86_FP 2")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1910")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 191024629")).isTrue();
    softly.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificV141x86OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformToolsetv141x86.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 12);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_M_IX86 600")).isTrue();
    softly.assertThat(defines.contains("__cplusplus 199711L")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1910")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 191024629")).isTrue();
    // check atldef.h for _ATL_VER
    softly.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleSpecificV141x64OptionsCorrectly() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/platformToolsetv141x64.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(0);
    List<String> defines = config.getDefines();
    assertThat(defines.size()).isEqualTo(15 + 14);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_M_IX86 600")).isFalse();
    softly.assertThat(defines.contains("__cplusplus 199711L")).isTrue();
    softly.assertThat(defines.contains("_MSC_VER 1910")).isTrue();
    softly.assertThat(defines.contains("_MSC_FULL_VER 191024629")).isTrue();
    // check atldef.h for _ATL_VER
    softly.assertThat(defines.contains("_ATL_VER 0x0E00")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldHandleBuildLog() {
    CxxConfiguration config = new CxxConfiguration();
    config.setBaseDir(".");
    List<File> files = new ArrayList<>();
    files.add(new File("src/test/resources/logfile/ParallelBuildLog.txt"));
    config.setCompilationPropertiesWithBuildLog(files, vcKey, vcCharSet);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(config.getIncludeDirectories().size()).isEqualTo(15);
    softly.assertThat(config.getDefines().size()).isEqualTo(30);
    softly.assertAll();
  }

  private void ValidateDefaultAsserts(SoftAssertions softly, List<String> defines) {
    softly.assertThat(defines.contains("_INTEGRAL_MAX_BITS 64")).isTrue();
    softly.assertThat(defines.contains("_MSC_BUILD 1")).isTrue();
    softly.assertThat(defines.contains("__COUNTER__ 0")).isTrue();
    softly.assertThat(defines.contains("__DATE__ \"??? ?? ????\"")).isTrue();
    softly.assertThat(defines.contains("__FILE__ \"file\"")).isTrue();
    softly.assertThat(defines.contains("__LINE__ 1")).isTrue();
    softly.assertThat(defines.contains("__TIME__ \"??:??:??\"")).isTrue();
    softly.assertThat(defines.contains("__TIMESTAMP__ \"??? ?? ???? ??:??:??\"")).isTrue();
    softly.assertAll();
  }

  @Test
  public void shouldGetSourceFilesList() {
    CxxConfiguration config = new CxxConfiguration();

    String[] files = new String[]{"testfile", "anotherfile", "thirdfile"};

    for (String filename : files) {
      config.addCompilationUnitSettings(filename, new CxxCompilationUnitSettings());
    }

    List<File> sourceFiles = config.getCompilationUnitSourceFiles();

    assertThat(sourceFiles.size()).isEqualTo(files.length);

    for (File file : sourceFiles) {
      Assertions.assertThat(files).contains(file.getName()).as(file.getName());
    }
  }
}
