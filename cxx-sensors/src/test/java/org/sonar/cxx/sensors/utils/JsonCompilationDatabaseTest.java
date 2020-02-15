/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2020 SonarOpenCommunity
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
package org.sonar.cxx.sensors.utils;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.sonar.cxx.CxxCompilationUnitSettings;
import org.sonar.cxx.CxxConfiguration;

public class JsonCompilationDatabaseTest {

  @Test
  public void testGlobalSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    CxxCompilationUnitSettings cus = conf.getGlobalCompilationUnitSettings();

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("UNIT_DEFINE")).isFalse();
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isFalse();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isTrue();
  }

  @Test
  public void testExtensionSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();
    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");
    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-extension.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("UNIT_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testCommandSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-command.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("COMMAND_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("COMMAND_SPACE_DEFINE")).isTrue();
    assertThat(cus.getDefines().get("COMMAND_SPACE_DEFINE")).isEqualTo("\" foo 'bar' zoo \"");
    assertThat(cus.getDefines().containsKey("SIMPLE")).isTrue();
    assertThat(cus.getDefines().get("SIMPLE")).isEqualTo("1");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/another/include/dir"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testArgumentParser() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-argument-parser.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();

    assertThat(cus.getDefines().get("MACRO1")).isEqualTo("1");
    assertThat(cus.getDefines().get("MACRO2")).isEqualTo("2");
    assertThat(cus.getDefines().get("MACRO3")).isEqualTo("1");
    assertThat(cus.getDefines().get("MACRO4")).isEqualTo("4");
    assertThat(cus.getDefines().get("MACRO5")).isEqualTo("\" a 'b' c \"");
    assertThat(cus.getDefines().get("MACRO6")).isEqualTo("\"With spaces, quotes and \\-es.\"");

    assertThat(cus.getIncludes().contains(Paths.get("/aaa/bbb"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/ccc/ddd"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/eee/fff"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/ggg/hhh"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/iii/jjj"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/kkk/lll"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/mmm/nnn"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/ooo/ppp"))).isTrue();
  }

  @Test
  public void testArgumentSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("ARG_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("ARG_SPACE_DEFINE")).isTrue();
    assertThat(cus.getDefines().get("ARG_SPACE_DEFINE")).isEqualTo("\" foo 'bar' zoo \"");
    assertThat(cus.getDefines().containsKey("SIMPLE")).isTrue();
    assertThat(cus.getDefines().get("SIMPLE")).isEqualTo("1");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/another/include/dir"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testRelativeDirectorySettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get("src");
    Path absPath = cwd.resolve("test-with-relative-directory.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("src/another/include/dir"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("parent/include/dir"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testArgumentAsListSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments-as-list.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("ARG_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("ARG_SPACE_DEFINE")).isTrue();
    assertThat(cus.getDefines().get("ARG_SPACE_DEFINE")).isEqualTo("\" foo 'bar' zoo \"");
    assertThat(cus.getDefines().containsKey("SIMPLE")).isTrue();
    assertThat(cus.getDefines().get("SIMPLE")).isEqualTo("1");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/another/include/dir"))).isTrue();
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testUnknownUnitSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("unknown.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNull();
  }

  @Test(expected = JsonMappingException.class)
  public void testInvalidJson() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/invalid.json");

    JsonCompilationDatabase.parse(conf, file);
  }

  @Test(expected = FileNotFoundException.class)
  public void testFileNotFound() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/not-found.json");

    JsonCompilationDatabase.parse(conf, file);
  }

}
