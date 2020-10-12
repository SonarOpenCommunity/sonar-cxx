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
import org.sonar.cxx.CxxSquidConfiguration;

public class JsonCompilationDatabaseTest {

  @Test
  public void testGlobalSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    CxxCompilationUnitSettings cus = squidConfig.getGlobalCompilationUnitSettings();

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("UNIT_DEFINE")).isFalse();
    assertThat(cus.getDefines()).containsKey("GLOBAL_DEFINE");
    assertThat(cus.getIncludes().contains(Paths.get("/usr/local/include"))).isFalse();
    assertThat(cus.getIncludes()).contains(Paths.get("/usr/include"));
  }

  @Test
  public void testExtensionSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();
    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");
    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-extension.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines()).containsKey("UNIT_DEFINE");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes()).contains(Paths.get("/usr/local/include"));
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testCommandSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-command.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines()).containsKey("COMMAND_DEFINE");
    assertThat(cus.getDefines()).containsKey("COMMAND_SPACE_DEFINE");
    assertThat(cus.getDefines()).containsEntry("COMMAND_SPACE_DEFINE", "\" foo 'bar' zoo \"");
    assertThat(cus.getDefines()).containsKey("SIMPLE");
    assertThat(cus.getDefines()).containsEntry("SIMPLE", "1");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes()).contains(Paths.get("/usr/local/include"));
    assertThat(cus.getIncludes()).contains(Paths.get("/another/include/dir"));
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testArgumentParser() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-argument-parser.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();

    assertThat(cus.getDefines()).containsEntry("MACRO1", "1");
    assertThat(cus.getDefines()).containsEntry("MACRO2", "2");
    assertThat(cus.getDefines()).containsEntry("MACRO3", "1");
    assertThat(cus.getDefines()).containsEntry("MACRO4", "4");
    assertThat(cus.getDefines()).containsEntry("MACRO5", "\" a 'b' c \"");
    assertThat(cus.getDefines()).containsEntry("MACRO6", "\"With spaces, quotes and \\-es.\"");

    assertThat(cus.getIncludes()).contains(Paths.get("/aaa/bbb"));
    assertThat(cus.getIncludes()).contains(Paths.get("/ccc/ddd"));
    assertThat(cus.getIncludes()).contains(Paths.get("/eee/fff"));
    assertThat(cus.getIncludes()).contains(Paths.get("/ggg/hhh"));
    assertThat(cus.getIncludes()).contains(Paths.get("/iii/jjj"));
    assertThat(cus.getIncludes()).contains(Paths.get("/kkk/lll"));
    assertThat(cus.getIncludes()).contains(Paths.get("/mmm/nnn"));
    assertThat(cus.getIncludes()).contains(Paths.get("/ooo/ppp"));
  }

  @Test
  public void testArgumentSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines()).containsKey("ARG_DEFINE");
    assertThat(cus.getDefines()).containsKey("ARG_SPACE_DEFINE");
    assertThat(cus.getDefines()).containsEntry("ARG_SPACE_DEFINE", "\" foo 'bar' zoo \"");
    assertThat(cus.getDefines()).containsKey("SIMPLE");
    assertThat(cus.getDefines()).containsEntry("SIMPLE", "1");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes()).contains(Paths.get("/usr/local/include"));
    assertThat(cus.getIncludes()).contains(Paths.get("/another/include/dir"));
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testRelativeDirectorySettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get("src");
    Path absPath = cwd.resolve("test-with-relative-directory.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getIncludes()).contains(Paths.get("/usr/local/include"));
    assertThat(cus.getIncludes()).contains(Paths.get("src/another/include/dir"));
    assertThat(cus.getIncludes()).contains(Paths.get("parent/include/dir"));
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testArgumentAsListSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments-as-list.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines()).containsKey("ARG_DEFINE");
    assertThat(cus.getDefines()).containsKey("ARG_SPACE_DEFINE");
    assertThat(cus.getDefines()).containsEntry("ARG_SPACE_DEFINE", "\" foo 'bar' zoo \"");
    assertThat(cus.getDefines()).containsKey("SIMPLE");
    assertThat(cus.getDefines()).containsEntry("SIMPLE", "1");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes()).contains(Paths.get("/usr/local/include"));
    assertThat(cus.getIncludes()).contains(Paths.get("/another/include/dir"));
    assertThat(cus.getIncludes().contains(Paths.get("/usr/include"))).isFalse();
  }

  @Test
  public void testUnknownUnitSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    JsonCompilationDatabase.parse(squidConfig, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("unknown.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = squidConfig.getCompilationUnitSettings(filename);

    assertThat(cus).isNull();
  }

  @Test(expected = JsonMappingException.class)
  public void testInvalidJson() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/invalid.json");

    JsonCompilationDatabase.parse(squidConfig, file);
  }

  @Test(expected = FileNotFoundException.class)
  public void testFileNotFound() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/not-found.json");

    JsonCompilationDatabase.parse(squidConfig, file);
  }

}
