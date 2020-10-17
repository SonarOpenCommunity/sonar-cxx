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
package org.sonar.cxx.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class JsonCompilationDatabaseTest {

  @Test
  public void testGlobalSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    List<String> defines = squidConfig.getValues(CxxSquidConfiguration.GLOBAL,
                                                 CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(CxxSquidConfiguration.GLOBAL,
                                                  CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).doesNotContain("UNIT_DEFINE 1");
    assertThat(defines).contains("GLOBAL_DEFINE 1");
    assertThat(includes).isNotEmpty();
    assertThat(includes).doesNotContain(unifyPath("/usr/local/include"));
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test
  public void testExtensionSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();
    var file = new File("src/test/resources/jsondb/compile_commands.json");
    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-extension.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).contains("UNIT_DEFINE 1");
    assertThat(defines).contains("GLOBAL_DEFINE 1");
    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/usr/local/include"));
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test
  public void testCommandSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-command.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).contains("COMMAND_DEFINE 1");
    assertThat(defines).contains("COMMAND_SPACE_DEFINE \" foo 'bar' zoo \"");
    assertThat(defines).contains("SIMPLE 1");
    assertThat(defines).contains("GLOBAL_DEFINE 1");
    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/usr/local/include"));
    assertThat(includes).contains(unifyPath("/another/include/dir"));
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test
  public void testArgumentParser() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-argument-parser.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).contains("MACRO1 1");
    assertThat(defines).contains("MACRO2 2");
    assertThat(defines).contains("MACRO3 1");
    assertThat(defines).contains("MACRO4 4");
    assertThat(defines).contains("MACRO5 \" a 'b' c \"");
    assertThat(defines).contains("MACRO6 \"With spaces, quotes and \\-es.\"");

    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/aaa/bbb"));
    assertThat(includes).contains(unifyPath("/ccc/ddd"));
    assertThat(includes).contains(unifyPath("/eee/fff"));
    assertThat(includes).contains(unifyPath("/ggg/hhh"));
    assertThat(includes).contains(unifyPath("/iii/jjj"));
    assertThat(includes).contains(unifyPath("/kkk/lll"));
    assertThat(includes).contains(unifyPath("/mmm/nnn"));
    assertThat(includes).contains(unifyPath("/ooo/ppp"));
  }

  @Test
  public void testArgumentSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).contains("ARG_DEFINE 1");
    assertThat(defines).contains("ARG_SPACE_DEFINE \" foo 'bar' zoo \"");
    assertThat(defines).contains("SIMPLE 1");
    assertThat(defines).contains("GLOBAL_DEFINE 1");
    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/usr/local/include"));
    assertThat(includes).contains(unifyPath("/another/include/dir"));
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test
  public void testRelativeDirectorySettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get("src");
    Path absPath = cwd.resolve("test-with-relative-directory.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/usr/local/include"));
    assertThat(includes).contains(unifyPath("src/another/include/dir"));
    assertThat(includes).contains(unifyPath("parent/include/dir"));
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test
  public void testArgumentAsListSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("test-with-arguments-as-list.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).contains("ARG_DEFINE 1");
    assertThat(defines).contains("ARG_SPACE_DEFINE \" foo 'bar' zoo \"");
    assertThat(defines).contains("SIMPLE 1");
    assertThat(defines).contains("GLOBAL_DEFINE 1");
    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/usr/local/include"));
    assertThat(includes).contains(unifyPath("/another/include/dir"));
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test
  public void testUnknownUnitSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    Path cwd = Paths.get(".");
    Path absPath = cwd.resolve("unknown.cpp");
    String filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines).isNotEmpty();
    assertThat(defines).contains("GLOBAL_DEFINE 1");
    assertThat(includes).isNotEmpty();
    assertThat(includes).contains(unifyPath("/usr/include"));
  }

  @Test(expected = JsonMappingException.class)
  public void testInvalidJson() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/invalid.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);
  }

  @Test(expected = FileNotFoundException.class)
  public void testFileNotFound() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/not-found.json");

    JsonCompilationDatabase jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);
  }

  static private String unifyPath(String path) {
    String x = Paths.get(path).toString();
    return x;
  }

}
