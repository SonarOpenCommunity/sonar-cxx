/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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
package org.sonar.cxx.lexer;

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.config.CxxSquidConfiguration;
import org.sonar.cxx.parser.CxxLexer;
import org.sonar.cxx.preprocessor.CxxPreprocessor;
import org.sonar.cxx.preprocessor.JoinStringsPreprocessor;
import org.sonar.cxx.utils.TestUtils;
import org.sonar.squidbridge.SquidAstVisitorContext;

public class CxxLexerIncludeTest {

  @Test
  public void quoted_include_without_IncludeDirectories() {
    // Quoted form / preprocessor include file search order:
    // 1) In the same directory as the file that contains the #include statement.

    String result = tryInclude("\"a.h\"", "INCLUDE", absolute(""), null);
    assertThat(result).isEqualTo("\"using: include/a.h\"");
  }

  @Test
  public void quoted_include_with_IncludeDirectories1() {
    // Quoted form / preprocessor include file search order:
    // 1) In the same directory as the file that contains the #include statement.

    String result = tryInclude("\"a.h\"", "INCLUDE", absolute("B", "C"), null);
    assertThat(result).isEqualTo("\"using: include/a.h\"");
  }

  @Test
  public void quoted_include_with_IncludeDirectories2() {
    // Quoted form / preprocessor include file search order:
    // 2) In the directories of the currently opened include files, in the reverse order in which they were opened.
    // The search begins in the directory of the parent include file and continues upward through the directories of
    // any grandparent include files.

    String result = tryInclude("\"p.h\"", "INCLUDE", absolute(""), null);
    assertThat(result).isEqualTo("\"using: include/a.h\"");
  }

  @Test
  public void quoted_include_with_IncludeDirectories3a() {
    // Quoted form / preprocessor include file search order:
    // 3) Along the path that's specified by each /I compiler option.

    String result = tryInclude("\"b.h\"", "INCLUDE", absolute("B", "C"), null);
    assertThat(result).isEqualTo("\"using: include/B/b.h\"");
  }

  @Test
  public void quoted_include_with_IncludeDirectories3b() {
    // Quoted form / preprocessor include file search order:
    // 3) Along the path that's specified by each /I compiler option.

    String result = tryInclude("\"c.h\"", "INCLUDE", absolute("B", "C"), null);
    assertThat(result).isEqualTo("\"using: include/C/c.h\"");
  }

  @Test
  public void bracket_include_without_IncludeDirectories() {
    // Angle-bracket form / preprocessor include file search order:
    // 1) Along the path that's specified by each /I compiler option (IncludeDirectories).

    String result = tryInclude("<a.h>", "INCLUDE", absolute(""), null);
    assertThat(result).isEqualTo("INCLUDE"); // should not find the include
  }

  @Test
  public void bracket_include_with_IncludeDirectories1a() {
    // Angle-bracket form / preprocessor include file search order:
    // 1) Along the path that's specified by each /I compiler option (IncludeDirectories).

    String result = tryInclude("<a.h>", "INCLUDE", absolute("."), null);
    assertThat(result).isEqualTo("\"using: include/a.h\"");
  }

  @Test
  public void bracket_include_with_IncludeDirectories1b() {
    // Angle-bracket form / preprocessor include file search order:
    // 1) Along the path that's specified by each /I compiler option (IncludeDirectories).

    String result = tryInclude("<b.h>", "INCLUDE", absolute("B", "C"), null);
    assertThat(result).isEqualTo("\"using: include/B/b.h\"");
  }

  @Test
  public void bracket_include_with_IncludeDirectories1c() {
    // Angle-bracket form / preprocessor include file search order:
    // 1) Along the path that's specified by each /I compiler option (IncludeDirectories).

    String result = tryInclude("<a.h>", "INCLUDE", absolute("B", "C"), null);
    assertThat(result).isEqualTo("\"using: include/B/a.h\"");
  }

  @Test
  public void compilation_database_settings_propagated() {
    // test: are compilation database settings propagated in case of nested includes

    List<String> defines = Arrays.asList("GLOBAL 1");
    String result = tryInclude("\"p.h\"", "PROPAGATED", absolute(""), defines);
    assertThat(result).isEqualTo("\"propagated\"");
  }

  @Test
  public void bracket_import_with_IncludeDirectories() {
    // Angle-bracket form / preprocessor include file search order:
    // 1) Along the path that's specified by each /I compiler option (IncludeDirectories).

    String result = tryImport("<a.h>", "INCLUDE", absolute("."), null);
    assertThat(result).isEqualTo("\"using: include/a.h\"");
  }

  private File root() {
    return TestUtils.loadResource("/preprocessor/include");
  }

  private List<String> absolute(String... path) {
    var result = new ArrayList<String>();
    for (String item : path) {
      if (!item.isBlank()) {
        result.add(new File(root(), item).getAbsolutePath());
      }
    }
    return result;
  }

  private String tryInclude(String include, String macro,
                            @Nullable List<String> includeDirectories,
                            @Nullable List<String> defines) {
    return tryCmd("#include", include, macro, includeDirectories, defines);
  }

  private String tryImport(String include, String macro,
                           @Nullable List<String> includeDirectories,
                           @Nullable List<String> defines) {
    return tryCmd("import", include, macro, includeDirectories, defines);
  }

  private String tryCmd(String cmd, String include, String macro,
                        @Nullable List<String> includeDirectories,
                        @Nullable List<String> defines) {
    var squidConfig = new CxxSquidConfiguration();
    if (includeDirectories != null) {
      squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.INCLUDE_DIRECTORIES,
                      includeDirectories);
    }
    if (defines != null) {
      squidConfig.add(CxxSquidConfiguration.SONAR_PROJECT_PROPERTIES, CxxSquidConfiguration.DEFINES,
                      defines);
    }

    var file = new File(root(), "root.cpp");
    SquidAstVisitorContext<Grammar> context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(file);

    var pp = new CxxPreprocessor(context, squidConfig);
    var lexer = CxxLexer.create(squidConfig.getCharset(), pp, new JoinStringsPreprocessor());

    String fileContent = cmd + " " + include + "\n" + macro;
    List<Token> tokens = lexer.lex(fileContent);
    return tokens.get(0).getValue();
  }

}
