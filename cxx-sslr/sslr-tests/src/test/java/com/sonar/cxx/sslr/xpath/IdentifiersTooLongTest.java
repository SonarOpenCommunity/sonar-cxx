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
import static com.sonar.cxx.sslr.test.minic.MiniCParser.parseFile;
import com.sonar.cxx.sslr.xpath.api.AstNodeXPathQuery;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdentifiersTooLongTest {

  private AstNode fileNode;

  @BeforeEach
  public void setUp() {
    fileNode = parseFile("/xpath/identifiersTooLong.mc");
  }

  @Test
  void valuesTest() {
    AstNodeXPathQuery<AstNode> xpath = AstNodeXPathQuery.create("//IDENTIFIER[string-length(@tokenValue) > 10]");

    var nodes = xpath.selectNodes(fileNode);

    assertThat(nodes).hasSize(3);
    assertThat(nodes.get(0).getTokenValue()).isEqualTo("aaaaaaaaa11");
    assertThat(nodes.get(0).getTokenLine()).isEqualTo(3);
    assertThat(nodes.get(1).getTokenValue()).isEqualTo("bbbbbbbbbbbbb15");
    assertThat(nodes.get(1).getTokenLine()).isEqualTo(10);
    assertThat(nodes.get(2).getTokenValue()).isEqualTo("ccccccccc11");
    assertThat(nodes.get(2).getTokenLine()).isEqualTo(12);
  }

  @Test
  void noResultValuesTest() {
    AstNodeXPathQuery<AstNode> xpath = AstNodeXPathQuery.create("//IDENTIFIER[string-length(@tokenValue) > 50]");

    var nodes = xpath.selectNodes(fileNode);

    assertThat(nodes).isEmpty();
  }

}
