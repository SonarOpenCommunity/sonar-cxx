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
package org.sonar.cxx.utils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;

class CxxConstantUtilsTest {

  @Test
  void testResolveAsConstantNull() {
    assertThat(CxxConstantUtils.resolveAsConstant(null)).isNull();
  }

  @Test
  void testResolveIntegerLiteralDecimal() {
    var node = createPrimaryExpression("42");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(42);
  }

  @Test
  void testResolveIntegerLiteralHex() {
    var node = createPrimaryExpression("0xFF");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(255);
  }

  @Test
  void testResolveIntegerLiteralBinary() {
    var node = createPrimaryExpression("0b1010");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(10);
  }

  @Test
  void testResolveIntegerLiteralOctal() {
    var node = createPrimaryExpression("077");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(63);
  }

  @Test
  void testResolveIntegerWithDigitSeparators() {
    var node = createPrimaryExpression("1'000'000");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(1000000);
  }

  @Test
  void testResolveIntegerWithSuffix() {
    var node = createPrimaryExpression("42u");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(42);
  }

  @Test
  void testResolveLongLiteral() {
    var node = createPrimaryExpression("9999999999");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Long.class);
    assertThat(result).isEqualTo(9999999999L);
  }

  @Test
  void testResolveStringLiteral() {
    var node = createPrimaryExpression("\"hello world\"");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(String.class);
    assertThat(result).isEqualTo("hello world");
  }

  @Test
  void testResolveStringLiteralWithEscapes() {
    var node = createPrimaryExpression("\"hello\\nworld\"");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(String.class);
    assertThat(result).isEqualTo("hello\nworld");
  }

  @Test
  void testResolveCharLiteral() {
    var node = createPrimaryExpression("'A'");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Character.class);
    assertThat(result).isEqualTo('A');
  }

  @Test
  void testResolveCharLiteralEscape() {
    var node = createPrimaryExpression("'\\n'");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Character.class);
    assertThat(result).isEqualTo('\n');
  }

  @Test
  void testResolveBooleanTrue() {
    var node = createPrimaryExpressionWithKeyword(CxxKeyword.TRUE);
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Boolean.class);
    assertThat(result).isEqualTo(Boolean.TRUE);
  }

  @Test
  void testResolveBooleanFalse() {
    var node = createPrimaryExpressionWithKeyword(CxxKeyword.FALSE);
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Boolean.class);
    assertThat(result).isEqualTo(Boolean.FALSE);
  }

  @Test
  void testResolveUnaryMinusInteger() {
    var unaryNode = createUnaryExpression("-", "42");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(-42);
  }

  @Test
  void testResolveUnaryPlusInteger() {
    var unaryNode = createUnaryExpression("+", "42");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(42);
  }

  @Test
  void testResolveUnaryNotBoolean() {
    var unaryNode = createUnaryExpression("!", CxxKeyword.TRUE);
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Boolean.class);
    assertThat(result).isEqualTo(Boolean.FALSE);
  }

  @Test
  void testResolveUnaryBitwiseNot() {
    var unaryNode = createUnaryExpression("~", "5");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(~5);
  }

  @Test
  void testResolveBinaryAdditionIntegers() {
    var binaryNode = createBinaryExpression("10", "+", "20");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(30);
  }

  @Test
  void testResolveBinarySubtractionIntegers() {
    var binaryNode = createBinaryExpression("50", "-", "20");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(30);
  }

  @Test
  void testResolveBinaryMultiplicationIntegers() {
    var binaryNode = createBinaryExpression("6", "*", "7");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(42);
  }

  @Test
  void testResolveBinaryDivisionIntegers() {
    var binaryNode = createBinaryExpression("84", "/", "2");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(42);
  }

  @Test
  void testResolveBinaryModuloIntegers() {
    var binaryNode = createBinaryExpression("17", "%", "5");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(2);
  }

  @Test
  void testResolveBinaryBitwiseOr() {
    var binaryNode = createBinaryExpression("5", "|", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(7);
  }

  @Test
  void testResolveBinaryBitwiseAnd() {
    var binaryNode = createBinaryExpression("5", "&", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(1);
  }

  @Test
  void testResolveBinaryBitwiseXor() {
    var binaryNode = createBinaryExpression("5", "^", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(6);
  }

  @Test
  void testResolveBinaryLeftShift() {
    var binaryNode = createBinaryExpression("1", "<<", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(8);
  }

  @Test
  void testResolveBinaryRightShift() {
    var binaryNode = createBinaryExpression("16", ">>", "2");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class);
    assertThat(result).isEqualTo(4);
  }

  @Test
  void testResolveStringConcatenation() {
    var binaryNode = createBinaryExpression("\"hello\"", "+", "\" world\"");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(String.class);
    assertThat(result).isEqualTo("hello world");
  }

  @Test
  void testResolveDivisionByZeroReturnsNull() {
    var binaryNode = createBinaryExpression("42", "/", "0");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isNull();
  }

  @Test
  void testResolveNonConstantExpression() {
    var node = mock(AstNode.class);
    when(node.is(any())).thenReturn(false);
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isNull();
  }

  @Test
  void testResolveInvalidLiteral() {
    var node = createPrimaryExpression("not_a_number");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isNull();
  }

  private AstNode createPrimaryExpression(String literalValue) {
    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);

    var literalToken = mock(AstNode.class);
    when(literalToken.getTokenValue()).thenReturn(literalValue);

    when(primaryExpr.getChildren()).thenReturn(List.of(literalToken));
    when(primaryExpr.getFirstDescendant(any())).thenReturn(null);

    return primaryExpr;
  }

  private AstNode createPrimaryExpressionWithKeyword(CxxKeyword keyword) {
    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);

    var keywordToken = mock(AstNode.class);
    when(keywordToken.is(keyword)).thenReturn(true);
    when(keywordToken.getTokenValue()).thenReturn(keyword.getValue());

    when(primaryExpr.getChildren()).thenReturn(List.of(keywordToken));
    when(primaryExpr.getFirstDescendant(any())).thenReturn(null);

    return primaryExpr;
  }

  private AstNode createUnaryExpression(String operator, String operandValue) {
    var unaryExpr = mock(AstNode.class);
    when(unaryExpr.is(CxxGrammarImpl.unaryExpression)).thenReturn(true);

    var operatorNode = mock(AstNode.class, withSettings().lenient());
    when(operatorNode.getTokenValue()).thenReturn(operator);

    // Mock the is() checks for CxxPunctuator
    switch (operator) {
      case "-" -> when(operatorNode.is(CxxPunctuator.MINUS)).thenReturn(true);
      case "+" -> when(operatorNode.is(CxxPunctuator.PLUS)).thenReturn(true);
      case "!" -> when(operatorNode.is(CxxPunctuator.NOT)).thenReturn(true);
      case "~" -> when(operatorNode.is(CxxPunctuator.BW_NOT)).thenReturn(true);
    }

    var operand = createPrimaryExpression(operandValue);

    when(unaryExpr.getFirstChild()).thenReturn(operatorNode);
    when(unaryExpr.getLastChild()).thenReturn(operand);

    return unaryExpr;
  }

  private AstNode createUnaryExpression(String operator, CxxKeyword keywordOperand) {
    var unaryExpr = mock(AstNode.class);
    when(unaryExpr.is(CxxGrammarImpl.unaryExpression)).thenReturn(true);

    var operatorNode = mock(AstNode.class, withSettings().lenient());
    when(operatorNode.getTokenValue()).thenReturn(operator);

    // Mock the is() checks for CxxPunctuator
    switch (operator) {
      case "-" -> when(operatorNode.is(CxxPunctuator.MINUS)).thenReturn(true);
      case "+" -> when(operatorNode.is(CxxPunctuator.PLUS)).thenReturn(true);
      case "!" -> when(operatorNode.is(CxxPunctuator.NOT)).thenReturn(true);
      case "~" -> when(operatorNode.is(CxxPunctuator.BW_NOT)).thenReturn(true);
    }

    var operand = createPrimaryExpressionWithKeyword(keywordOperand);

    when(unaryExpr.getFirstChild()).thenReturn(operatorNode);
    when(unaryExpr.getLastChild()).thenReturn(operand);

    return unaryExpr;
  }

  private AstNode createBinaryExpression(String leftValue, String operator, String rightValue) {
    var binaryExpr = mock(AstNode.class);
    when(binaryExpr.is(
      CxxGrammarImpl.additiveExpression,
      CxxGrammarImpl.multiplicativeExpression,
      CxxGrammarImpl.andExpression,
      CxxGrammarImpl.exclusiveOrExpression,
      CxxGrammarImpl.inclusiveOrExpression,
      CxxGrammarImpl.shiftExpression
    )).thenReturn(true);

    var left = createPrimaryExpression(leftValue);
    var operatorNode = mock(AstNode.class);
    when(operatorNode.getTokenValue()).thenReturn(operator);
    var right = createPrimaryExpression(rightValue);

    when(binaryExpr.getChildren()).thenReturn(List.of(left, operatorNode, right));

    return binaryExpr;
  }
}
