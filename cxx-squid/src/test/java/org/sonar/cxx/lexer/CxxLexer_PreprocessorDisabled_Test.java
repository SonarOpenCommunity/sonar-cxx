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
package org.sonar.cxx.lexer;

import com.sonar.sslr.impl.Lexer;
import static com.sonar.sslr.test.lexer.LexerMatchers.hasToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.cxx.api.CxxTokenType;

public class CxxLexer_PreprocessorDisabled_Test {

  private static Lexer lexer;

  @BeforeClass
  public static void init() {
    lexer = CxxLexer.create();
  }

  @Test
  public void preprocessor_directives() {
    assertThat(lexer.lex("#include <iostream>"), hasToken("#include <iostream>", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("# include <iostream>"), hasToken("# include <iostream>", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex(" # include <iostream>"), hasToken("# include <iostream>", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("#define lala"), hasToken("#define lala", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("# define lala"), hasToken("# define lala", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex(" # define lala"), hasToken("# define lala", CxxTokenType.PREPROCESSOR));

    assertThat(lexer.lex("#include <iostream>")).hasSize(2);
    assertThat(lexer.lex("#define\\\ncontinued line")).hasSize(2);
    assertThat(lexer.lex("#include <iostream>\n1"), hasToken("1", CxxTokenType.NUMBER));
  }

  @Test
  public void preprocessor_continued_define() {
    assertThat(lexer.lex("#define M\\\n"
      + "0"),
      hasToken("#define M 0", CxxTokenType.PREPROCESSOR));
  }

  @Test
  public void preprocessor_directive_with_multiline_comment() {
    assertThat(lexer.lex("#define A B/*CCC*/\n"),
      hasToken("#define A B", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("#define A B/**/C\n"),
      hasToken("#define A BC", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("#define A B/*C\n\n\nC*/\n"),
      hasToken("#define A B", CxxTokenType.PREPROCESSOR));
    assertThat(lexer.lex("#define A B*/\n"),
      hasToken("#define A B*/", CxxTokenType.PREPROCESSOR));
  }
}
