/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.parser;

import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Parser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.cxx.sslr.internal.matchers.ExpressionGrammar;

public class ParserAdapterTest {

  @TempDir
  File tempDir;

  private ExpressionGrammar grammar;
  private ParserAdapter parser;

  @BeforeEach
  public void setUp() {
    grammar = new ExpressionGrammar();
    parser = new ParserAdapter(Charset.forName("UTF-8"), grammar);
  }

  @Test
  public void should_return_grammar() {
    assertThat(parser.getGrammar()).isSameAs(grammar);
  }

  @Test
  public void should_parse_string() {
    parser.parse("1+1");
  }

  @Test
  public void should_not_parse_invalid_string() {
    var thrown = catchThrowableOfType(
      () -> parser.parse(""),
      RecognitionException.class);
    assertThat(thrown).hasMessage("Parse error at line 1 column 1:\n" + "\n" + "1: \n" + "   ^\n");
  }

  @Test
  public void should_parse_file() throws Exception {
    var file = new File(tempDir, "file.txt");
    try (
      var fileOutputStream = new FileOutputStream(file);
      var writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);) {
      writer.write("1+1");
    }
    parser.parse(file);
  }

  @Test
  public void should_not_parse_invalid_file() {
    var file = new File("notfound");
    var thrown = catchThrowableOfType(
      () -> parser.parse(file),
      RecognitionException.class);
    assertThat(thrown).isExactlyInstanceOf(RecognitionException.class);
  }

  @Test
  public void builder_should_not_create_new_instance_from_adapter() {
    assertThat(Parser.builder(parser).build()).isSameAs(parser);
  }

  @Test
  public void parse_tokens_unsupported() {
    List<Token> tokens = Collections.emptyList();
    var thrown = catchThrowableOfType(
      () -> parser.parse(tokens),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getRootRule_unsupported() {
    var thrown = catchThrowableOfType(
      () -> parser.getRootRule(),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

}
