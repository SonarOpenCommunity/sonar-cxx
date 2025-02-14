/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

class CxxParserTest {

  private final String errSources = "/parser/bad/error_recovery_declaration.cc";
  private final String[] goodFiles = {"own", "VC", "GCC", "cli", "cuda", "examples"};
  private final String[] preprocessorFiles = {"preprocessor"};
  private final String[] cCompatibilityFiles = {"C", "C99"};
  private final String rootDir = "src/test/resources/parser";

  private File erroneousSources = null;
  private final SquidAstVisitorContext<Grammar> context;

  public CxxParserTest() throws URISyntaxException {
    super();
    erroneousSources = new File(CxxParserTest.class.getResource(errSources).toURI());
    context = mock(SquidAstVisitorContext.class);
  }

  @Test
  void testParsingCppSourceFiles() {
    var map = new HashMap<String, Integer>() {
      private static final long serialVersionUID = 6029310517902718597L;

      {
        // file, number of declarations
        put("ignore.hpp", 2);
        put("ignore1.cpp", 1);
        put("ignoreparam.hpp", 3);
        put("ignoreparam1.cpp", 1);
        put("inbuf1.cpp", 1);
        put("io1.cpp", 2);
        put("outbuf1.cpp", 1);
        put("outbuf1.hpp", 1);
        put("outbuf1x.cpp", 1);
        put("outbuf1x.hpp", 4);
        put("outbuf2.cpp", 1);
        put("outbuf2.hpp", 2);
        put("outbuf3.cpp", 1);
        put("outbuf3.hpp", 1);
        put("outbuf2.cpp", 1);
      }
    };

    Parser<Grammar> p = createParser(null, false, null);

    for (var file : listFiles(goodFiles, new String[]{"cc", "cpp", "hpp"})) {
      AstNode root = parse(p, file);
      verify(root, file, map);
    }
  }

  //@Test todo
  void testParsingCSourceFiles() {
    var map = new HashMap<String, Integer>() {
    };

    Parser<Grammar> p = createParser(null, false, null);

    for (var file : listFiles(cCompatibilityFiles, new String[]{"cc", "h"})) { // todo add "c"
      AstNode root = parse(p, file);
      verify(root, file, map);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testPreproccessorParsingSourceFiles() {
    var includes = Arrays.asList(
      "C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\INCLUDE",
      "C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\ATLMFC\\INCLUDE",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\ucrt",
      "C:\\Program Files (x86)\\Windows Kits\\NETFXSDK\\4.6.1\\include\\um",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\shared",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\um",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\winrt",
      "C:\\Workspaces\\boost\\boost_1_61_0",
      "resources",
      "resources\\parser\\preprocessor");

    var map = new HashMap<String, Integer>() {
      private static final long serialVersionUID = 1433381506274827684L;

      {
        // file, number of declarations
        put("variadic_macros.cpp", 1);
        put("apply_wrap.hpp", 0);
        put("boost_macros_short.hpp", 0);
        put("boost_macros.hpp", 0);
      }
    };

    var baseDir = new File("src/test").getAbsolutePath();
    Parser<Grammar> p = createParser(baseDir, false, includes);

    for (var file : listFiles(preprocessorFiles, new String[]{"cc", "cpp", "hpp", "h"})) {
      AstNode root = parse(p, file);
      verify(root, file, map);
    }
  }

  @Test
  void testParseErrorRecoveryDisabled() {
    Parser<Grammar> p = createParser(null, false, null);

    // The error recovery works, if:
    // - a syntacticly incorrect file causes a parse error when recovery is disabled
    assertThatThrownBy(() -> {
      parse(p, erroneousSources);
    }).isInstanceOf(IllegalStateException.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testParseErrorRecoveryEnabled() {
    var map = new HashMap<String, Integer>() {
      private static final long serialVersionUID = 3433381506274827684L;

      {
        // file, number of declarations
        put(erroneousSources.getAbsolutePath(), 5);
      }
    };

    Parser<Grammar> p = createParser(null, true, null);

    // The error recovery works, if:
    // - but doesn't cause such an error if we run with default settings
    AstNode root = parse(p, erroneousSources); //<-- this shouldn't throw now
    verify(root, erroneousSources, map);
  }

  private List<File> listFiles(String[] dirs, String[] extensions) {
    var files = new ArrayList<File>();
    for (var dir : dirs) {
      files.addAll(FileUtils.listFiles(new File(rootDir, dir), extensions, true));
    }
    return files;
  }

  private Parser<Grammar> createParser(String baseDir, boolean errorRecovery, @Nullable List<String> includes) {
    CxxSquidConfiguration squidConfig;
    if (baseDir != null) {
      squidConfig = new CxxSquidConfiguration(baseDir);
    } else {
      squidConfig = new CxxSquidConfiguration();
    }
    squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.ERROR_RECOVERY_ENABLED,
      errorRecovery ? "true" : "false");
    if (includes != null) {
      squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.INCLUDE_DIRECTORIES,
        includes);
    }

    return CxxParser.create(context, squidConfig);
  }

  private AstNode parse(Parser<Grammar> parser, File file) {
    when(context.getFile()).thenReturn(file);
    AstNode root = null;
    try {
      root = parser.parse(file);
      CxxParser.finishedParsing();
    } catch (Exception e) {
      throw new IllegalStateException(file.toString(), e);
    }
    return root;
  }

  void verify(AstNode root, File file, HashMap<String, Integer> map) {
    assertThat(root.hasChildren()).isTrue();
    if (map.containsKey(file.getName())) {
      List<AstNode> declarations = root.getDescendants(CxxGrammarImpl.declaration);
      assertThat(declarations)
        .as("check number of declarations for file '%s'", file.getName())
        .hasSize(map.get(file.getName()));
    }
  }
}
