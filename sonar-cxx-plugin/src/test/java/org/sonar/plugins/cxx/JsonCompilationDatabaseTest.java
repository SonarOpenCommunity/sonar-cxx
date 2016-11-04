/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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
package org.sonar.plugins.cxx;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeVersion;
import org.sonar.cxx.CxxCompilationUnitSettings;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.plugins.cxx.utils.JsonCompilationDatabase;

import com.fasterxml.jackson.databind.JsonMappingException;

public class JsonCompilationDatabaseTest {

  @Test
  public void testGlobalSettings() throws Exception {
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/compile_commands.json");

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
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/compile_commands.json");

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
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/compile_commands.json");

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
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/compile_commands.json");

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
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/compile_commands.json");

    new JsonCompilationDatabase(conf, file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("unknown.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    CxxCompilationUnitSettings cus = conf.getCompilationUnitSettings(filename);

    assertThat(cus).isNull();
  }

  @Test
  public void testInvalidJson() throws Exception {
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/invalid.json");

    try {
      new JsonCompilationDatabase(conf, file);
      assertThat(true).isFalse();
    } catch (JsonMappingException e) {
      // Expect to get exception
    }
  }

  @Test
  public void testFileNotFound() throws Exception {
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);
    CxxPlugin plugin = new CxxPlugin();
    plugin.define(context);
    CxxConfiguration conf = new CxxConfiguration();

    File file = new File("src/test/resources/org/sonar/plugins/cxx/json-compilation-database-project/not-found.json");

    try {
      new JsonCompilationDatabase(conf, file);
      assertThat(true).isFalse();
    } catch (FileNotFoundException e) {
      // Expect to get exception
    }
  }
}
