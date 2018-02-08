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
package org.sonar.cxx.parser;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;

import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.CxxFileTesterHelper;
import org.sonar.squidbridge.SquidAstVisitorContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CxxParserTest extends ParserBaseTestHelper {

  String errSources = "/parser/bad/error_recovery_declaration.cc";
  String[] goodFiles = {"own", "VC", "cli", "cuda", "examples"};
  String[] preprocessorFiles = {"preprocessor"};
  String[] cCompatibilityFiles = {"C", "C99"};
  String rootDir = "src/test/resources/parser";
  File erroneousSources = null;

  public CxxParserTest() {
    super();
    try {
      erroneousSources = new File(CxxParserTest.class.getResource(errSources).toURI());
    } catch (java.net.URISyntaxException e) {
    }
  }

  @Test
  public void testParsingOnDiverseSourceFiles() {
    Collection<File> files = listFiles(goodFiles, new String[]{"cc", "cpp", "hpp"});
    HashMap<String,Integer> map = new HashMap<String,Integer>() {
      private static final long serialVersionUID = 6029310517902718597L;
      {
          put("ignore.hpp", 2);
          put("ignore1.cpp", 2);
          put("ignoreparam.hpp", 4);
          put("ignoreparam1.cpp", 2);
          put("inbuf1.cpp", 2);
          put("io1.cpp", 3);
          put("outbuf1.cpp", 2);
          put("outbuf1.hpp", 2);
          put("outbuf1x.cpp", 2);
          put("outbuf1x.hpp", 4);
          put("outbuf2.cpp", 2);
          put("outbuf2.hpp", 3);
          put("outbuf3.cpp", 2);
          put("outbuf3.hpp", 2);
          put("outbuf2.cpp", 2);
        }};
    for (File file : files) {
      AstNode root = p.parse(file);
      CxxParser.finishedParsing(file);
      if (map.containsKey(file.getName())) {
        assertThat(root.getNumberOfChildren()).as("check number of nodes for file %s",file.getName()).isEqualTo(map.get(file.getName())); 
      } else {
        assertThat(root.hasChildren()).isTrue();
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testPreproccessorParsingOnDiverseSourceFiles() {
    conf = new CxxConfiguration();
    conf.setErrorRecoveryEnabled(false);
    String baseDir = new File("src/test").getAbsolutePath();
    conf.setBaseDir(baseDir);
    conf.setIncludeDirectories(Arrays.asList(
      "C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\INCLUDE",
      "C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\VC\\ATLMFC\\INCLUDE",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\ucrt",
      "C:\\Program Files (x86)\\Windows Kits\\NETFXSDK\\4.6.1\\include\\um",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\shared",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\um",
      "C:\\Program Files (x86)\\Windows Kits\\10\\include\\10.0.10586.0\\winrt",
      "C:\\Workspaces\\boost\\boost_1_61_0",
      "resources",
      "resources\\parser\\preprocessor")
    );

    HashMap<String,Integer> map = new HashMap<String,Integer>() {
      private static final long serialVersionUID = 1433381506274827684L;
      {
        put("variadic_macros.cpp", 2);
        put("apply_wrap.hpp", 1);
        put("boost_macros_short.hpp", 1);
        put("boost_macros.hpp", 1);
      }};

    p = CxxParser.create(mock(SquidAstVisitorContext.class), conf, CxxFileTesterHelper.mockCxxLanguage());
    Collection<File> files = listFiles(preprocessorFiles, new String[]{"cc", "cpp", "hpp", "h"});
    for (File file : files) {
      AstNode root = p.parse(file);
      CxxParser.finishedParsing(file);
      if (map.containsKey(file.getName())) {
        assertThat(root.getNumberOfChildren()).as("check number of nodes for file %s",file.getName()).isEqualTo(map.get(file.getName())); 
      } else {
        assertThat(root.hasChildren()).isTrue();
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParsingInCCompatMode() { //ToDo: Fix this compatibility test - currently it is useless
    // The C-compatibility replaces c++ keywords, which aren't keywords in C,
    // with non-keyword-strings via the preprocessor.
    // This mode works if such a file causes parsing errors when the mode
    // is switched off and doesn't, if the mode is switched on.

    File cfile = (File) listFiles(cCompatibilityFiles, new String[]{"c"}).toArray()[0];

    SquidAstVisitorContext<Grammar> context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(cfile);

    conf.setCFilesPatterns(new String[]{""});
    p = CxxParser.create(context, conf, CxxFileTesterHelper.mockCxxLanguage());
    try {
      AstNode root = p.parse(cfile);
      assertThat(root.getNumberOfChildren()).isEqualTo(2);
    } catch (com.sonar.sslr.api.RecognitionException re) {
    }

    conf.setCFilesPatterns(new String[]{"*.c"});
    p = CxxParser.create(context, conf, CxxFileTesterHelper.mockCxxLanguage());
    AstNode root = p.parse(cfile);
    assertThat(root.getNumberOfChildren()).isEqualTo(2);  
  }

  @Test
  public void testParseErrorRecoveryDisabled() {
    // The error recovery works, if:
    // - a syntacticly incorrect file causes a parse error when recovery is disabled
    assertThatThrownBy(() -> {p.parse(erroneousSources);}).isInstanceOf(com.sonar.sslr.api.RecognitionException.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testParseErrorRecoveryEnabled() {
    // The error recovery works, if:
    // - but doesn't cause such an error if we run with default settings
    conf.setErrorRecoveryEnabled(true);
    p = CxxParser.create(mock(SquidAstVisitorContext.class), conf, CxxFileTesterHelper.mockCxxLanguage());
    AstNode root = p.parse(erroneousSources); //<-- this shouldn't throw now
    assertThat(root.getNumberOfChildren()).isEqualTo(6);
  }

  private Collection<File> listFiles(String[] dirs, String[] extensions) {
    List<File> files = new ArrayList<>();
    for (String dir : dirs) {
      files.addAll(FileUtils.listFiles(new File(rootDir, dir), extensions, true));
    }
    return files;
  }

}
