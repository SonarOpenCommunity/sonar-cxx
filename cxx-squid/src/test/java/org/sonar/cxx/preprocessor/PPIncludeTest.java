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
package org.sonar.cxx.preprocessor;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.impl.Parser;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.sonar.cxx.squidbridge.SquidAstVisitorContext;

public class PPIncludeTest {

  private CxxPreprocessor pp;
  private PPInclude include;
  private Parser<Grammar> lineParser;
  private Path foo;

  @TempDir
  File tempDir;

  @BeforeEach
  public void setUp() throws IOException {
    var context = mock(SquidAstVisitorContext.class);
    when(context.getFile()).thenReturn(new File("dummy")); // necessary for init
    pp = new CxxPreprocessor(context);
    pp.init();
    include = new PPInclude(pp);
    lineParser = PPParser.create(Charset.defaultCharset());

    // create include file
    Path root = tempDir.toPath();
    foo = Files.createFile(root.resolve("foo.h"));
  }

  @Test
  public void testFindIncludedFileQuoted() {
    AstNode ast = lineParser.parse("#include " + "\"" + foo.toAbsolutePath().toString() + "\"");
    File result = include.findIncludedFile(ast);
    assertThat(result).isEqualTo(foo.toFile());
  }

  @Test
  public void testFindIncludedFileBracketed() {
    AstNode ast = lineParser.parse("#include " + "<" + foo.toAbsolutePath().toString() + ">");
    File result = include.findIncludedFile(ast);
    assertThat(result).isEqualTo(foo.toFile());
  }

}
