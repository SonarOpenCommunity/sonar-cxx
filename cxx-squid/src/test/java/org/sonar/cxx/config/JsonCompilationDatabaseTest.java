/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class JsonCompilationDatabaseTest {

  @Test
  void testGlobalSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
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
  void testExtensionSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();
    var file = new File("src/test/resources/jsondb/compile_commands.json");
    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get(".");
    var absPath = cwd.resolve("test-extension.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines)
      .isNotEmpty()
      .contains("UNIT_DEFINE 1")
      .contains("GLOBAL_DEFINE 1");
    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/usr/local/include"))
      .contains(unifyPath("/usr/include"));
  }

  @Test
  void testCommandSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get(".");
    var absPath = cwd.resolve("test-with-command.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines)
      .isNotEmpty()
      .contains("COMMAND_DEFINE 1")
      .contains("COMMAND_SPACE_DEFINE \" foo 'bar' zoo \"")
      .contains("SIMPLE 1")
      .contains("GLOBAL_DEFINE 1");
    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/usr/local/include"))
      .contains(unifyPath("/another/include/dir"))
      .contains(unifyPath("/usr/include"));
  }

  @Test
  void testArgumentParser() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get(".");
    var absPath = cwd.resolve("test-argument-parser.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines)
      .isNotEmpty()
      .contains("MACRO1 1")
      .contains("MACRO2 2")
      .contains("MACRO3 1")
      .contains("MACRO4 4")
      .contains("MACRO5 \" a 'b' c \"")
      .contains("MACRO6 \"With spaces, quotes and \\-es.\"");

    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/aaa/bbb"))
      .contains(unifyPath("/ccc/ddd"))
      .contains(unifyPath("/eee/fff"))
      .contains(unifyPath("/ggg/hhh"))
      .contains(unifyPath("/iii/jjj"))
      .contains(unifyPath("/kkk/lll"))
      .contains(unifyPath("/mmm/nnn"))
      .contains(unifyPath("/ooo/ppp"));
  }

  @Test
  void testArgumentSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get(".");
    var absPath = cwd.resolve("test-with-arguments.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines)
      .isNotEmpty()
      .contains("ARG_DEFINE 1")
      .contains("ARG_SPACE_DEFINE \" foo 'bar' zoo \"")
      .contains("SIMPLE 1")
      .contains("GLOBAL_DEFINE 1");
    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/usr/local/include"))
      .contains(unifyPath("/another/include/dir"))
      .contains(unifyPath("/usr/include"));
  }

  @Test
  void testRelativeDirectorySettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get("src");
    var absPath = cwd.resolve("test-with-relative-directory.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/usr/local/include"))
      .contains(unifyPath("src/another/include/dir"))
      .contains(unifyPath("parent/include/dir"))
      .contains(unifyPath("/usr/include"));
  }

  @Test
  void testArgumentAsListSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get(".");
    var absPath = cwd.resolve("test-with-arguments-as-list.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines)
      .isNotEmpty()
      .contains("ARG_DEFINE 1")
      .contains("ARG_SPACE_DEFINE \" foo 'bar' zoo \"")
      .contains("SIMPLE 1")
      .contains("GLOBAL_DEFINE 1");
    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/usr/local/include"))
      .contains(unifyPath("/another/include/dir"))
      .contains(unifyPath("/usr/include"));
  }

  @Test
  void testUnknownUnitSettings() throws Exception {
    var squidConfig = new CxxSquidConfiguration();

    var file = new File("src/test/resources/jsondb/compile_commands.json");

    var jsonDb = new JsonCompilationDatabase(squidConfig);
    jsonDb.parse(file);

    var cwd = Paths.get(".");
    var absPath = cwd.resolve("unknown.cpp");
    var filename = absPath.toAbsolutePath().normalize().toString();

    List<String> defines = squidConfig.getValues(filename, CxxSquidConfiguration.DEFINES);
    List<String> includes = squidConfig.getValues(filename, CxxSquidConfiguration.INCLUDE_DIRECTORIES);

    assertThat(defines)
      .isNotEmpty()
      .contains("GLOBAL_DEFINE 1");
    assertThat(includes)
      .isNotEmpty()
      .contains(unifyPath("/usr/include"));
  }

  @Test
  void testInvalidJson() {
    var squidConfig = new CxxSquidConfiguration();
    var file = new File("src/test/resources/jsondb/invalid.json");
    var jsonDb = new JsonCompilationDatabase(squidConfig);

    JsonMappingException thrown = catchThrowableOfType(() -> {
      jsonDb.parse(file);
    }, JsonMappingException.class);
    assertThat(thrown).isExactlyInstanceOf(JsonMappingException.class);
  }

  @Test
  void testFileNotFound() {
    var squidConfig = new CxxSquidConfiguration();
    var file = new File("src/test/resources/jsondb/not-found.json");
    var jsonDb = new JsonCompilationDatabase(squidConfig);

    FileNotFoundException thrown = catchThrowableOfType(() -> {
      jsonDb.parse(file);
    }, FileNotFoundException.class);
    assertThat(thrown).isExactlyInstanceOf(FileNotFoundException.class);
  }

  static private String unifyPath(String path) {
    return Paths.get(path).toString();
  }

}
