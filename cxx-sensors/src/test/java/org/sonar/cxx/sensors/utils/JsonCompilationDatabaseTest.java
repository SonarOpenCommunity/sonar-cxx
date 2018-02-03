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

    new JsonCompilationDatabase(conf, file);

    CxxCompilationUnitSettings cus = conf.getGlobalCompilationUnitSettings();

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("UNIT_DEFINE")).isFalse();
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isTrue();
    assertThat(cus.getIncludes().contains("/usr/local/include")).isFalse();
    assertThat(cus.getIncludes().contains("/usr/include")).isTrue();
  }

  @Test
  public void testExtensionSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();
    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");
    new JsonCompilationDatabase(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-extension.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("UNIT_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains("/usr/local/include")).isTrue();
    assertThat(cus.getIncludes().contains("/usr/include")).isFalse();
  }

  @Test
  public void testCommandSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    new JsonCompilationDatabase(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-command.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("COMMAND_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("COMMAND_SPACE_DEFINE")).isTrue();
    assertThat(cus.getDefines().get("COMMAND_SPACE_DEFINE")).isEqualTo("\" foo 'bar' zoo \"");
    assertThat(cus.getDefines().containsKey("SIMPLE")).isTrue();
    assertThat(cus.getDefines().get("SIMPLE")).isEqualTo("");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains("/usr/local/include")).isTrue();
    assertThat(cus.getIncludes().contains("/another/include/dir")).isTrue();
    assertThat(cus.getIncludes().contains("/usr/include")).isFalse();
  }

  @Test
  public void testArgumentSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    new JsonCompilationDatabase(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNotNull();
    assertThat(cus.getDefines().containsKey("ARG_DEFINE")).isTrue();
    assertThat(cus.getDefines().containsKey("ARG_SPACE_DEFINE")).isTrue();
    assertThat(cus.getDefines().get("ARG_SPACE_DEFINE")).isEqualTo("\" foo 'bar' zoo \"");
    assertThat(cus.getDefines().containsKey("SIMPLE")).isTrue();
    assertThat(cus.getDefines().get("SIMPLE")).isEqualTo("");
    assertThat(cus.getDefines().containsKey("GLOBAL_DEFINE")).isFalse();
    assertThat(cus.getIncludes().contains("/usr/local/include")).isTrue();
    assertThat(cus.getIncludes().contains("/another/include/dir")).isTrue();
    assertThat(cus.getIncludes().contains("/usr/include")).isFalse();
  }

  @Test
  public void testUnknownUnitSettings() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/compile_commands.json");

    new JsonCompilationDatabase(conf, file);

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

    new JsonCompilationDatabase(conf, file);
  }

  @Test(expected = FileNotFoundException.class)
  public void testFileNotFound() throws Exception {
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/cxx/sensors/json-compilation-database-project/not-found.json");

    new JsonCompilationDatabase(conf, file);
  }
}
