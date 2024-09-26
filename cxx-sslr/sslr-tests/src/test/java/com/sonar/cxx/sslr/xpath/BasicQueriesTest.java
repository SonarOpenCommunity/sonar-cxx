/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
package com.sonar.cxx.sslr.xpath;

import com.sonar.cxx.sslr.api.AstNode;
import static com.sonar.cxx.sslr.api.GenericTokenType.EOF;
import com.sonar.cxx.sslr.test.minic.MiniCGrammar;
import static com.sonar.cxx.sslr.test.minic.MiniCParser.parseFile;
import com.sonar.cxx.sslr.xpath.api.AstNodeXPathQuery;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BasicQueriesTest {

  private AstNode fileNode;

  @BeforeEach
  public void init() {
    fileNode = parseFile("/xpath/basicQueries.mc");
  }

  @Test
  void compilationUnitTest() {
    var xpath = AstNodeXPathQuery.create("/COMPILATION_UNIT");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode);
  }

  @Test
  void anyCompilationUnitTest() {
    var xpath = AstNodeXPathQuery.create("//COMPILATION_UNIT");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode);
  }

  @Test
  void compilationUnitWithPredicateWithEOFTest() {
    var xpath = AstNodeXPathQuery.create("/COMPILATION_UNIT[not(not(EOF))]");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode);
  }

  @Test
  void compilationUnitWithPredicateWithoutEOFTest() {
    var xpath = AstNodeXPathQuery.create("/COMPILATION_UNIT[not(EOF)]");
    assertThat(xpath.selectSingleNode(fileNode)).isNull();
  }

  @Test
  void EOFTest() {
    var xpath = AstNodeXPathQuery.create("/COMPILATION_UNIT/EOF");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode.getFirstDescendant(EOF));
  }

  @Test
  void anyEOFTest() {
    var xpath = AstNodeXPathQuery.create("//EOF");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode.getFirstDescendant(EOF));
  }

  @Test
  void getTokenValueAttributeTest() {
    var xpath = AstNodeXPathQuery.create("string(/COMPILATION_UNIT/@tokenValue)");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo("int");
  }

  @Test
  void getTokenLineAttributeTest() {
    var xpath = AstNodeXPathQuery.create("string(/COMPILATION_UNIT/@tokenLine)");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo("2");
  }

  @Test
  void getTokenColumnAttributeTest() {
    var xpath = AstNodeXPathQuery.create("string(/COMPILATION_UNIT/@tokenColumn)");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo("0");
  }

  @Test
  void getSecondDeclarationTest() {
    var xpath1 = AstNodeXPathQuery.create("/COMPILATION_UNIT/DEFINITION[@tokenLine=4]");
    var xpath2 = AstNodeXPathQuery.create("/COMPILATION_UNIT/DEFINITION[2]");
    var declarationAtLineFour = fileNode.getChildren().get(1);
    assertThat(declarationAtLineFour.is(MiniCGrammar.DEFINITION)).isTrue();
    assertThat(declarationAtLineFour.getTokenLine()).isEqualTo(4);
    assertThat(xpath1.selectSingleNode(fileNode)).isEqualTo(declarationAtLineFour);
    assertThat(xpath1.selectSingleNode(fileNode)).isEqualTo(xpath2.selectSingleNode(fileNode));
  }

  @Test
  void identifiersCountTest() {
    var xpath = AstNodeXPathQuery.create("/COMPILATION_UNIT[count(//IDENTIFIER) = 2]");
    assertThat(xpath.selectSingleNode(fileNode)).isEqualTo(fileNode);
  }

  @Test
  void getIdentifiersTest() {
    AstNodeXPathQuery<AstNode> xpath = AstNodeXPathQuery.create("//IDENTIFIER");
    var nodes = xpath.selectNodes(fileNode);
    assertThat(nodes).hasSize(2);
    assertThat(nodes.get(0).getTokenValue()).isEqualTo("a");
    assertThat(nodes.get(1).getTokenValue()).isEqualTo("b");
  }

}
