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
package com.sonar.cxx.sslr.impl.xpath;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.xpath.AstNodeNavigator.Attribute;
import java.net.URI;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AstNodeNavigatorTest {

  private AstNodeNavigator navigator;

  @BeforeEach
  public void setUp() {
    navigator = new AstNodeNavigator();
  }

  @Test
  public void getTextStringValue() {
    var thrown = catchThrowableOfType(
      () -> navigator.getTextStringValue(null),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getCommentStringValue() {
    var thrown = catchThrowableOfType(
      () -> navigator.getCommentStringValue(null),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getAttributeStringValue() throws Exception {
    var astNode = new AstNode(Token.builder()
      .setURI(new URI("tests://unittest"))
      .setType(GenericTokenType.IDENTIFIER)
      .setLine(1)
      .setColumn(2)
      .setValueAndOriginalValue("foo", "bar")
      .build());
    assertThat(navigator.getAttributeStringValue(new Attribute("tokenLine", astNode))).isEqualTo("1");
    assertThat(navigator.getAttributeStringValue(new Attribute("tokenColumn", astNode))).isEqualTo("2");
    assertThat(navigator.getAttributeStringValue(new Attribute("tokenValue", astNode))).isEqualTo("foo");
  }

  @Test
  public void getAttributeStringValue2() {
    var attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("foo");
    var thrown = catchThrowableOfType(
      () -> navigator.getAttributeStringValue(attribute),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getElementStringValue() {
    var thrown = catchThrowableOfType(
      () -> navigator.getElementStringValue(null),
      UnsupportedOperationException.class);
    assertThat(thrown)
      .hasMessage("Implicit nodes to string conversion is not supported. Use the tokenValue attribute instead.");
  }

  /* Namespaces */
  @Test
  public void getNamespacePrefix() {
    var thrown = catchThrowableOfType(
      () -> navigator.getNamespacePrefix(null),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getNamespaceStringValue() {
    var thrown = catchThrowableOfType(
      () -> navigator.getNamespaceStringValue(null),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  /* Attributes */
  @Test
  public void getAttributeName() {
    var attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("foo");
    assertThat(navigator.getAttributeName(attribute)).isEqualTo("foo");
  }

  @Test
  public void getAttributeQName() {
    var attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("foo");
    assertThat(navigator.getAttributeQName(attribute)).isEqualTo("foo");
  }

  /* Elements */
  @Test
  public void getAttributeNamespaceUri() {
    assertThat(navigator.getAttributeNamespaceUri(null)).isEqualTo("");
  }

  @Test
  public void getElementName() {
    var astNode = mock(AstNode.class);
    when(astNode.getName()).thenReturn("foo");
    assertThat(navigator.getElementName(astNode)).isEqualTo("foo");
  }

  @Test
  public void getElementQName() {
    var astNode = mock(AstNode.class);
    when(astNode.getName()).thenReturn("foo");
    assertThat(navigator.getElementQName(astNode)).isEqualTo("foo");
  }

  @Test
  public void getElementNamespaceUri() {
    assertThat(navigator.getElementNamespaceUri(null)).isEqualTo("");
  }

  /* Types */
  @Test
  public void isAttribute() {
    assertThat(navigator.isAttribute(mock(AstNodeNavigator.Attribute.class))).isTrue();
    assertThat(navigator.isAttribute(null)).isFalse();
  }

  @Test
  public void isComment() {
    assertThat(navigator.isComment(null)).isFalse();
  }

  @Test
  public void isDocument() {
    var astNode = mock(AstNode.class);
    var attribute = mock(Attribute.class);
    when(attribute.getAstNode()).thenReturn(astNode);
    assertThat(navigator.isDocument(attribute)).isFalse();
    assertThat(navigator.isDocument(astNode)).isFalse();
    assertThat(navigator.isDocument(navigator.getDocumentNode(astNode))).isTrue();
  }

  @Test
  public void isDocument2() {
    assertThat(navigator.isDocument(null)).isFalse();
  }

  @Test
  public void isElement() {
    assertThat(navigator.isElement(mock(AstNode.class))).isTrue();
    assertThat(navigator.isElement(null)).isFalse();
  }

  @Test
  public void isNamespace() {
    assertThat(navigator.isNamespace(null)).isFalse();
  }

  @Test
  public void isProcessingInstruction() {
    assertThat(navigator.isProcessingInstruction(null)).isFalse();
  }

  @Test
  public void isText() {
    assertThat(navigator.isText(null)).isFalse();
  }

  /* Navigation */
  @Test
  public void getDocumentNode() {
    var rootAstNode = mock(AstNode.class);
    var astNode = mock(AstNode.class);
    when(astNode.getParent()).thenReturn(rootAstNode);
    var attribute = mock(Attribute.class);
    when(attribute.getAstNode()).thenReturn(astNode);
    var documentNode = (AstNode) navigator.getDocumentNode(attribute);
    assertThat(documentNode.getName()).isEqualTo("[root]");
  }

  @Test
  public void getChildAxisIterator() {
    var attribute = mock(Attribute.class);
    assertThat(navigator.getChildAxisIterator(attribute).hasNext()).isFalse();
  }

  @Test
  public void getChildAxisIterator2() {
    var thrown = catchThrowableOfType(
      () -> navigator.getChildAxisIterator(new Object()),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getParentNode() {
    var rootAstNode = mock(AstNode.class);
    var astNode = mock(AstNode.class);
    when(astNode.getParent()).thenReturn(rootAstNode);
    var attribute = mock(Attribute.class);
    when(attribute.getAstNode()).thenReturn(astNode);
    assertThat(navigator.getParentNode(attribute)).isSameAs(astNode);
    assertThat(navigator.getParentNode(astNode)).isSameAs(rootAstNode);
  }

  @Test
  public void getParentNode2() {
    var thrown = catchThrowableOfType(
      () -> navigator.getParentNode(new Object()),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getParentAxisIterator() {
    var thrown = catchThrowableOfType(
      () -> navigator.getParentAxisIterator(new Object()),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  public void getAttributeAxisIterator() {
    var thrown = catchThrowableOfType(
      () -> navigator.getAttributeAxisIterator(new Object()),
      UnsupportedOperationException.class);
    assertThat(thrown).isExactlyInstanceOf(UnsupportedOperationException.class);
  }

  /* Unknown */
  @Test
  public void parseXPath() {
    assertThat(navigator.parseXPath(null)).isNull();
  }

}
