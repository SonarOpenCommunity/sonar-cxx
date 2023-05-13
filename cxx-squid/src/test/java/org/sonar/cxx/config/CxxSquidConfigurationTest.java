/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CxxSquidConfigurationTest {

  private static final String VC_CHARSET = "UTF8";

  @Test
  void testEmptyDb() {
    var squidConfig = new CxxSquidConfiguration();
    Optional<String> value = squidConfig.get("level", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isEmpty();
    softly.assertAll();
  }

  @Test
  void emptySingleValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a", "b", "c");
    Optional<String> value = squidConfig.get("d", "e");

    var softly = new SoftAssertions();
    softly.assertThat(value).isEmpty();
    softly.assertAll();
  }

  @Test
  void identifierSingleValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value");
    Optional<String> value = squidConfig.get(CxxSquidConfiguration.GLOBAL, "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of("value"));
    softly.assertAll();
  }

  @Test
  void identifierMultiValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value1");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value2");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value3");
    List<String> values = squidConfig.getValues(CxxSquidConfiguration.GLOBAL, "key");

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(3);
    softly.assertThat(values.get(0)).isEqualTo("value1");
    softly.assertThat(values.get(1)).isEqualTo("value2");
    softly.assertThat(values.get(2)).isEqualTo("value3");
    softly.assertAll();
  }

  @Test
  void identifierParentSingleValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "key", "value1");
    squidConfig.add("a/b/c", "key", "value2");
    Optional<String> value = squidConfig.get(CxxSquidConfiguration.GLOBAL, "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of("value1"));
    softly.assertAll();
  }

  @Test
  void fileSingleValue() {
    var squidConfig = new CxxSquidConfiguration();
    var softly = new SoftAssertions();

    softly.assertThat(squidConfig.isUnitsEmpty()).isTrue();
    squidConfig.add("a/b/c", "key", "value");
    softly.assertThat(squidConfig.isUnitsEmpty()).isFalse();
    Optional<String> value = squidConfig.get("a/b/c", "key");

    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of("value"));
    softly.assertAll();
  }

  @Test
  void fileMultiValue1() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key", "value1");
    squidConfig.add("a/b/c", "key", "value2");
    squidConfig.add("a/b/c", "key", "value3");
    List<String> values = squidConfig.getValues("a/b/c", "key");

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(3);
    softly.assertThat(values.get(0)).isEqualTo("value1");
    softly.assertThat(values.get(1)).isEqualTo("value2");
    softly.assertThat(values.get(2)).isEqualTo("value3");
    softly.assertAll();
  }

  @Test
  void fileMultiValue2() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key", new String[]{"value1", "value2", "value3"});
    List<String> values = squidConfig.getValues("a/b/c", "key");

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(3);
    softly.assertThat(values.get(0)).isEqualTo("value1");
    softly.assertThat(values.get(1)).isEqualTo("value2");
    softly.assertThat(values.get(2)).isEqualTo("value3");
    softly.assertAll();
  }

  @Test
  void fileMultiValue3() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key", Arrays.asList("value1", "value2", "value3"));
    List<String> values = squidConfig.getValues("a/b/c", "key");

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(3);
    softly.assertThat(values.get(0)).isEqualTo("value1");
    softly.assertThat(values.get(1)).isEqualTo("value2");
    softly.assertThat(values.get(2)).isEqualTo("value3");
    softly.assertAll();
  }

  @Test
  void fileParentSingleValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "key", "value1");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "key", "value2");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value3");
    Optional<String> value = squidConfig.get("a/b/c", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of("value3"));
    softly.assertAll();
  }

  @Test
  void fileParentMultiValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "key", "value1");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "key", "value2");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value3");
    squidConfig.add("a/b/c", "key", "value4");

    var softly = new SoftAssertions();

    List<String> values = squidConfig.getValues("a/b/c", "key");
    softly.assertThat(values).hasSize(4);
    softly.assertThat(values.get(0)).isEqualTo("value4");
    softly.assertThat(values.get(1)).isEqualTo("value3");
    softly.assertThat(values.get(2)).isEqualTo("value2");
    softly.assertThat(values.get(3)).isEqualTo("value1");

    values = squidConfig.getValues(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "key");
    softly.assertThat(values).hasSize(2);
    softly.assertThat(values.get(0)).isEqualTo("value2");
    softly.assertThat(values.get(1)).isEqualTo("value1");

    softly.assertAll();
  }

  @Test
  void testSpecialCharInValue() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key", "<>&'\"");
    Optional<String> value = squidConfig.get("a/b/c", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of("<>&'\""));
    softly.assertAll();
  }

  @Test
  void testKeys() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key1", "value1");
    squidConfig.add("a/b/c", "key2", "value2");
    squidConfig.add("a/b/c", "key3", "value3");
    Optional<String> value1 = squidConfig.get("a/b/c", "key1");
    Optional<String> value2 = squidConfig.get("a/b/c", "key2");
    Optional<String> value3 = squidConfig.get("a/b/c", "key3");

    var softly = new SoftAssertions();
    softly.assertThat(value1).isNotEmpty();
    softly.assertThat(value1).isEqualTo(Optional.of("value1"));
    softly.assertThat(value2).isNotEmpty();
    softly.assertThat(value2).isEqualTo(Optional.of("value2"));
    softly.assertThat(value3).isNotEmpty();
    softly.assertThat(value3).isEqualTo(Optional.of("value3"));
    softly.assertAll();
  }

  @Test
  void testChildrenValues() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key", "value1");
    squidConfig.add("c/d/e", "key", "value2");
    squidConfig.add("f/g/h", "key", "value3");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value4");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "key", "value5");
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "key", "value6");
    List<String> values = squidConfig.getChildrenValues(CxxSquidConfiguration.UNITS, "key");

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(6);
    softly.assertThat(values.get(0)).isEqualTo("value1");
    softly.assertThat(values.get(1)).isEqualTo("value2");
    softly.assertThat(values.get(2)).isEqualTo("value3");
    softly.assertThat(values.get(3)).isEqualTo("value4");
    softly.assertThat(values.get(4)).isEqualTo("value5");
    softly.assertThat(values.get(5)).isEqualTo("value6");
    softly.assertAll();
  }

  @Test
  void testGetFiles() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("a/b/c", "key", "value1");
    squidConfig.add("c/d/e", "key", "value2");
    squidConfig.add("f/g/h", "key", "value3");
    List<Path> values = squidConfig.getFiles();

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(3);
    softly.assertThat(values.get(0)).isEqualTo(Path.of("a/b/c"));
    softly.assertThat(values.get(1)).isEqualTo(Path.of("c/d/e"));
    softly.assertThat(values.get(2)).isEqualTo(Path.of("f/g/h"));
    softly.assertAll();
  }

  @Test
  void testLevelValues() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "key", "value1");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "key", "value2");
    List<String> values = squidConfig.getLevelValues(CxxSquidConfiguration.GLOBAL, "key");

    var softly = new SoftAssertions();
    softly.assertThat(values).hasSize(1);
    softly.assertThat(values.get(0)).isEqualTo("value1");
    softly.assertAll();
  }

  @Test
  void testPathNames() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("/a/b/c.cpp", "key1", "value1");
    squidConfig.add("c:\\a\\b\\c.cpp", "key2", "value2");
    squidConfig.add("/X/Y/Z.cpp", "key3", "value3");
    squidConfig.add("C:\\X\\Y\\Z.cpp", "key4", "value4");
    Optional<String> value1 = squidConfig.get("/a/b/c.cpp", "key1");
    Optional<String> value2 = squidConfig.get("c:\\a\\b\\c.cpp", "key2");
    Optional<String> value3 = squidConfig.get("/x/y/z.cpp", "key3");
    Optional<String> value4 = squidConfig.get("c:/x/y/z.cpp", "key4");

    var softly = new SoftAssertions();
    softly.assertThat(value1).isNotEmpty();
    softly.assertThat(value1).isEqualTo(Optional.of("value1"));
    softly.assertThat(value2).isNotEmpty();
    softly.assertThat(value2).isEqualTo(Optional.of("value2"));
    softly.assertThat(value3).isNotEmpty();
    softly.assertThat(value3).isEqualTo(Optional.of("value3"));
    softly.assertThat(value4).isNotEmpty();
    softly.assertThat(value4).isEqualTo(Optional.of("value4"));
    softly.assertAll();
  }

  @Test
  void testBoolean() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("level", "key1", "True");
    squidConfig.add("level", "key2", "False");
    Optional<Boolean> value1 = squidConfig.getBoolean("level", "key1");
    Optional<Boolean> value2 = squidConfig.getBoolean("level", "key2");
    Optional<Boolean> value3 = squidConfig.getBoolean("level", "key3"); // does not exist

    var softly = new SoftAssertions();
    softly.assertThat(value1).isNotEmpty();
    softly.assertThat(value1).isEqualTo(Optional.of(true));
    softly.assertThat(value2).isNotEmpty();
    softly.assertThat(value2).isEqualTo(Optional.of(false));
    softly.assertThat(value3).isEmpty();
    softly.assertAll();
  }

  @Test
  void testInt() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("level", "key", "1");
    Optional<Integer> value = squidConfig.getInt("level", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of(1));
    softly.assertAll();
  }

  @Test
  void testLong() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("level", "key", String.valueOf(Long.MAX_VALUE));
    Optional<Long> value = squidConfig.getLong("level", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of(Long.MAX_VALUE));
    softly.assertAll();
  }

  @Test
  void testFloat() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("level", "key", String.valueOf(Float.MAX_VALUE));
    Optional<Float> value = squidConfig.getFloat("level", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of(Float.MAX_VALUE));
    softly.assertAll();
  }

  @Test
  void testDouble() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("level", "key", String.valueOf(Double.MAX_VALUE));
    Optional<Double> value = squidConfig.getDouble("level", "key");

    var softly = new SoftAssertions();
    softly.assertThat(value).isNotEmpty();
    softly.assertThat(value).isEqualTo(Optional.of(Double.MAX_VALUE));
    softly.assertAll();
  }

  @Test
  void testToString() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add("global1", "key1", "value1");
    squidConfig.add("global1", "key1", "value2");
    squidConfig.add("global2", "key1", "value1");
    squidConfig.add("a/b/c", "key1", "value1");
    squidConfig.add("a/b/c", "key1", "value2");
    squidConfig.add("a/b/c", "key2", "value1");
    squidConfig.add("a/b/c", "key2", "value2");
    squidConfig.add("d/e/f", "key1", "value1");
    var xml = squidConfig.toString();

    var softly = new SoftAssertions();
    softly.assertThat(xml).isNotEmpty();
    softly.assertThat(xml).contains("<global1>");
    softly.assertThat(xml).contains("<global2>");
    softly.assertThat(xml).contains("<File path=\"a/b/c\">");
    softly.assertThat(xml).contains("<File path=\"d/e/f\">");
    softly.assertAll();
  }

  @Test
  void emptyValueShouldReturnNoDirsOrDefines() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.readMsBuildFiles(new ArrayList<>(), VC_CHARSET);
    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    softly.assertThat(getDefines(squidConfig)).isEmpty();
    softly.assertAll();
  }

  @Test
  void emptyValueShouldUseIncludeDirsIfSet() {
    var squidConfig = new CxxSquidConfiguration();
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.INCLUDE_DIRECTORIES,
                    new String[]{"dir1", "dir2"});
    squidConfig.readMsBuildFiles(new ArrayList<>(), VC_CHARSET);
    assertThat(getIncludeDirectories(squidConfig)).hasSize(2);
  }

  @Test
  void correctlyCreatesConfiguration1() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/vc++13.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).hasSize(13);
    softly.assertThat(getDefines(squidConfig)).hasSize(26 + 5);
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificCommonOptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformCommon.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    softly.assertThat(defines).hasSize(20 + 5);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_OPENMP 200203");
    softly.assertThat(defines).contains("_WIN32");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertThat(defines).contains("_WCHAR_T_DEFINED 1");
    softly.assertThat(defines).contains("_NATIVE_WCHAR_T_DEFINED 1");
    softly.assertThat(defines).contains("_VC_NODEFAULTLIB");
    softly.assertThat(defines).contains("_MT");
    softly.assertThat(defines).contains("_DLL");
    softly.assertThat(defines).contains("_DEBUG");
    softly.assertThat(defines).contains("_VC_NODEFAULTLIB");
    softly.assertAll();
  }

  public void shouldHandleSpecificCommonWin32OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration();
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformCommonWin32.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    softly.assertThat(defines).hasSize(3);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_WIN32");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificCommonx64OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformCommonX64.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);

    var softly = new SoftAssertions();
    softly.assertThat(defines).hasSize(15 + 5);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_Wp64");
    softly.assertThat(defines).contains("_WIN32");
    softly.assertThat(defines).contains("_WIN64");
    softly.assertThat(defines).contains("_M_X64 100");
    softly.assertThat(defines.contains("_M_IX86")).isFalse();
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV100OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv100.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    softly.assertThat(defines).hasSize(12 + 6);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_CPPUNWIND");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_WIN32");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV110OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv110.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    softly.assertThat(defines).hasSize(13 + 5);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("__cplusplus_winrt 201009");
    softly.assertThat(defines).contains("_CPPUNWIND");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_WIN32");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertThat(defines).contains("_MSC_VER 1700");
    softly.assertThat(defines).contains("_MSC_FULL_VER 170061030");
    softly.assertThat(defines).contains("_ATL_VER 0x0B00");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV120OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv120.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    softly.assertThat(defines).hasSize(15 + 6);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("__AVX2__ 1");
    softly.assertThat(defines).contains("__AVX__ 1");
    softly.assertThat(defines).contains("__cplusplus_winrt 201009");
    softly.assertThat(defines).contains("_CPPUNWIND");
    softly.assertThat(defines).contains("_M_ARM_FP");
    softly.assertThat(defines).contains("_WIN32");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertThat(defines).contains("_MSC_VER 1800");
    softly.assertThat(defines).contains("_MSC_FULL_VER 180040629");
    softly.assertThat(defines).contains("_ATL_VER 0x0C00");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV140OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv140.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(15 + 6);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("__AVX2__ 1");
    softly.assertThat(defines).contains("__AVX__ 1");
    softly.assertThat(defines).contains("__cplusplus_winrt 201009");
    softly.assertThat(defines).contains("_CPPUNWIND");
    softly.assertThat(defines).contains("_M_ARM_FP");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertThat(defines).contains("_MSC_VER 1900");
    softly.assertThat(defines).contains("_MSC_FULL_VER 190024210");
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleTFSAgentV141OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/TFS-agent-msvc14.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).hasSize(2);
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(34);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_CPPUNWIND");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertThat(defines).contains("_MSC_VER 1910");
    softly.assertThat(defines).contains("_MSC_FULL_VER 191627030");
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleTFSAgentV141mpOptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/TFS-agent-msvc14-mp.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).hasSize(2);
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(34);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_CPPUNWIND");
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("_M_IX86_FP 2");
    softly.assertThat(defines).contains("_MSC_VER 1910");
    softly.assertThat(defines).contains("_MSC_FULL_VER 191627030");
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV141x86OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv141x86.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(15 + 12);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("__cplusplus 199711L");
    softly.assertThat(defines).contains("_MSC_VER 1910");
    softly.assertThat(defines).contains("_MSC_FULL_VER 191627030");
    // check atldef.h for _ATL_VER
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV141x64OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv141x64.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(15 + 14);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_M_IX86 600")).isFalse();
    softly.assertThat(defines).contains("__cplusplus 199711L");
    softly.assertThat(defines).contains("_MSC_VER 1910");
    softly.assertThat(defines).contains("_MSC_FULL_VER 191627030");
    // check atldef.h for _ATL_VER
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV143x86OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv143x86.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(15 + 12);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines).contains("_M_IX86 600");
    softly.assertThat(defines).contains("__cplusplus 201402L");
    softly.assertThat(defines).contains("_MSC_VER 1935");
    softly.assertThat(defines).contains("_MSC_FULL_VER 193532215");
    // check atldef.h for _ATL_VER
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleSpecificV143x64OptionsCorrectly() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/platformToolsetv143x64.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).isEmpty();
    List<String> defines = getDefines(squidConfig);
    assertThat(defines).hasSize(15 + 14);
    ValidateDefaultAsserts(softly, defines);
    softly.assertThat(defines.contains("_M_IX86 600")).isFalse();
    softly.assertThat(defines).contains("__cplusplus 201402L");
    softly.assertThat(defines).contains("_MSC_VER 1935");
    softly.assertThat(defines).contains("_MSC_FULL_VER 193532215");
    // check atldef.h for _ATL_VER
    softly.assertThat(defines).contains("_ATL_VER 0x0E00");
    softly.assertAll();
  }

  @Test
  void shouldHandleBuildLog() {
    var squidConfig = new CxxSquidConfiguration(".");
    var files = new ArrayList<File>();
    files.add(new File("src/test/resources/msbuild/ParallelBuildLog.txt"));
    squidConfig.readMsBuildFiles(files, VC_CHARSET);

    var softly = new SoftAssertions();
    softly.assertThat(getIncludeDirectories(squidConfig)).hasSize(15);
    softly.assertThat(getDefines(squidConfig)).hasSize(30);
    softly.assertAll();
  }

  @Test
  void testConfigFile(@TempDir Path tempDir) throws IOException {
    Path fileName = tempDir.resolve("config.xml");

    var squidConfig = new CxxSquidConfiguration(".");

    // add a value on each level
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "PREDEFINED_MACROS", "PREDEFINED_MACROS");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "SONAR_PROJECT_PROPERTIES",
                    "SONAR_PROJECT_PROPERTIES");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "GLOBAL", "GLOBAL");
    squidConfig.add("a/b/c", "UNIT", "UNIT");
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "l1a", "l1a");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "l2a", "l2a");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "l3a", "l3a");
    squidConfig.add("a/b/c", "l4a", "l4a");

    var softly = new SoftAssertions();

    softly.assertThat(squidConfig.writeToFile(fileName.toString())).isTrue();
    softly.assertThat(squidConfig.readFromFile(fileName.toString())).isTrue();

    // add new values after read (is it still working?)
    squidConfig.add(CxxSquidConfiguration.PREDEFINED_MACROS, "l1b", "l1b");
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "l2b", "l2b");
    squidConfig.add(CxxSquidConfiguration.GLOBAL, "l3b", "l3b");
    squidConfig.add("a/b/c", "l4b", "l4b");

    softly.assertThat(squidConfig.get(CxxSquidConfiguration.PREDEFINED_MACROS, "PREDEFINED_MACROS"))
      .contains("PREDEFINED_MACROS");
    softly.assertThat(squidConfig.get(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, "SONAR_PROJECT_PROPERTIES"))
      .contains("SONAR_PROJECT_PROPERTIES");
    softly.assertThat(squidConfig.get(CxxSquidConfiguration.GLOBAL, "GLOBAL"))
      .contains("GLOBAL");
    softly.assertThat(squidConfig.get("a/b/c", "UNIT"))
      .contains("UNIT");
    softly.assertThat(squidConfig.get("a/b/c", "l1a"))
      .contains("l1a");
    softly.assertThat(squidConfig.get("a/b/c", "l2b"))
      .contains("l2b");

    softly.assertAll();
  }

  private void ValidateDefaultAsserts(SoftAssertions softly, List<String> defines) {
    softly.assertThat(defines).contains("_INTEGRAL_MAX_BITS 64");
    softly.assertThat(defines).contains("_MSC_BUILD 1");
    softly.assertThat(defines).contains("__COUNTER__ 0");
    softly.assertThat(defines).contains("__DATE__ \"??? ?? ????\"");
    softly.assertThat(defines).contains("__FILE__ \"file\"");
    softly.assertThat(defines).contains("__LINE__ 1");
    softly.assertThat(defines).contains("__TIME__ \"??:??:??\"");
    softly.assertThat(defines).contains("__TIMESTAMP__ \"??? ?? ???? ??:??:??\"");
    softly.assertAll();
  }

  static private List<String> getDefines(CxxSquidConfiguration squidConfig) {
    var allDefines = new HashSet<String>();

    for (var elem : squidConfig.getChildrenValues(CxxSquidConfiguration.UNITS, CxxSquidConfiguration.DEFINES)) {
      allDefines.add(elem);
    }

    return new ArrayList<>(allDefines);
  }

  static private List<Path> getIncludeDirectories(CxxSquidConfiguration squidConfig) {
    var allIncludes = new HashSet<Path>();

    for (var elem : squidConfig.getChildrenValues(CxxSquidConfiguration.UNITS, CxxSquidConfiguration.INCLUDE_DIRECTORIES)) {
      allIncludes.add(Path.of(elem));
    }

    return new ArrayList<>(allIncludes);
  }

}
