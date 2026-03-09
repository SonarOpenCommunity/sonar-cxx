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
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.AstNodeTypeExtension;
import org.sonar.cxx.squidbridge.api.SourceCodeSymbol;
import org.sonar.cxx.squidbridge.api.Symbol;

class CxxConstantUtilsTest {

  @Test
  void testResolveAsConstantNull() {
    assertThat(CxxConstantUtils.resolveAsConstant(null)).isNull();
  }

  @Test
  void testResolveIntegerLiteralDecimal() {
    var node = createPrimaryExpression("42");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(42);
  }

  @Test
  void testResolveIntegerLiteralHex() {
    var node = createPrimaryExpression("0xFF");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(255);
  }

  @Test
  void testResolveIntegerLiteralBinary() {
    var node = createPrimaryExpression("0b1010");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(10);
  }

  @Test
  void testResolveIntegerLiteralOctal() {
    var node = createPrimaryExpression("077");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(63);
  }

  @Test
  void testResolveIntegerWithDigitSeparators() {
    var node = createPrimaryExpression("1'000'000");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(1000000);
  }

  @Test
  void testResolveIntegerWithSuffix() {
    var node = createPrimaryExpression("42u");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(42);
  }

  @Test
  void testResolveLongLiteral() {
    var node = createPrimaryExpression("9999999999");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Long.class).isEqualTo(9999999999L);
  }

  @Test
  void testResolveStringLiteral() {
    var node = createPrimaryExpression("\"hello world\"");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(String.class).isEqualTo("hello world");
  }

  @Test
  void testResolveStringLiteralWithEscapes() {
    var node = createPrimaryExpression("\"hello\\nworld\"");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(String.class).isEqualTo("hello\nworld");
  }

  @Test
  void testResolveCharLiteral() {
    var node = createPrimaryExpression("'A'");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Character.class).isEqualTo('A');
  }

  @Test
  void testResolveCharLiteralEscape() {
    var node = createPrimaryExpression("'\\n'");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Character.class).isEqualTo('\n');
  }

  @Test
  void testResolveBooleanTrue() {
    var node = createPrimaryExpressionWithKeyword(CxxKeyword.TRUE);
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Boolean.class).isEqualTo(Boolean.TRUE);
  }

  @Test
  void testResolveBooleanFalse() {
    var node = createPrimaryExpressionWithKeyword(CxxKeyword.FALSE);
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isInstanceOf(Boolean.class).isEqualTo(Boolean.FALSE);
  }

  @Test
  void testResolveUnaryMinusInteger() {
    var unaryNode = createUnaryExpression("-", "42");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(-42);
  }

  @Test
  void testResolveUnaryPlusInteger() {
    var unaryNode = createUnaryExpression("+", "42");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(42);
  }

  @Test
  void testResolveUnaryNotBoolean() {
    var unaryNode = createUnaryExpression("!", CxxKeyword.TRUE);
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Boolean.class).isEqualTo(Boolean.FALSE);
  }

  @Test
  void testResolveUnaryBitwiseNot() {
    var unaryNode = createUnaryExpression("~", "5");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(~5);
  }

  @Test
  void testResolveBinaryAdditionIntegers() {
    var binaryNode = createBinaryExpression("10", "+", "20");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(30);
  }

  @Test
  void testResolveBinarySubtractionIntegers() {
    var binaryNode = createBinaryExpression("50", "-", "20");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(30);
  }

  @Test
  void testResolveBinaryMultiplicationIntegers() {
    var binaryNode = createBinaryExpression("6", "*", "7");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(42);
  }

  @Test
  void testResolveBinaryDivisionIntegers() {
    var binaryNode = createBinaryExpression("84", "/", "2");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(42);
  }

  @Test
  void testResolveBinaryModuloIntegers() {
    var binaryNode = createBinaryExpression("17", "%", "5");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(2);
  }

  @Test
  void testResolveBinaryBitwiseOr() {
    var binaryNode = createBinaryExpression("5", "|", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(7);
  }

  @Test
  void testResolveBinaryBitwiseAnd() {
    var binaryNode = createBinaryExpression("5", "&", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(1);
  }

  @Test
  void testResolveBinaryBitwiseXor() {
    var binaryNode = createBinaryExpression("5", "^", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(6);
  }

  @Test
  void testResolveBinaryLeftShift() {
    var binaryNode = createBinaryExpression("1", "<<", "3");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(8);
  }

  @Test
  void testResolveBinaryRightShift() {
    var binaryNode = createBinaryExpression("16", ">>", "2");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Integer.class).isEqualTo(4);
  }

  @Test
  void testResolveStringConcatenation() {
    var binaryNode = createBinaryExpression("\"hello\"", "+", "\" world\"");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(String.class).isEqualTo("hello world");
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

    var operatorNode = mock(AstNode.class);
    lenient().when(operatorNode.getTokenValue()).thenReturn(operator);

    // Mock the is() checks for CxxPunctuator
    switch (operator) {
      case "-" -> when(operatorNode.is(CxxPunctuator.MINUS)).thenReturn(true);
      case "+" -> when(operatorNode.is(CxxPunctuator.PLUS)).thenReturn(true);
      case "!" -> when(operatorNode.is(CxxPunctuator.NOT)).thenReturn(true);
      case "~" -> when(operatorNode.is(CxxPunctuator.BW_NOT)).thenReturn(true);
      default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
    }

    var operand = createPrimaryExpression(operandValue);

    when(unaryExpr.getFirstChild()).thenReturn(operatorNode);
    when(unaryExpr.getLastChild()).thenReturn(operand);

    return unaryExpr;
  }

  private AstNode createUnaryExpression(String operator, CxxKeyword keywordOperand) {
    var unaryExpr = mock(AstNode.class);
    when(unaryExpr.is(CxxGrammarImpl.unaryExpression)).thenReturn(true);

    var operatorNode = mock(AstNode.class);
    lenient().when(operatorNode.getTokenValue()).thenReturn(operator);

    // Mock the is() checks for CxxPunctuator
    switch (operator) {
      case "-" -> when(operatorNode.is(CxxPunctuator.MINUS)).thenReturn(true);
      case "+" -> when(operatorNode.is(CxxPunctuator.PLUS)).thenReturn(true);
      case "!" -> when(operatorNode.is(CxxPunctuator.NOT)).thenReturn(true);
      case "~" -> when(operatorNode.is(CxxPunctuator.BW_NOT)).thenReturn(true);
      default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
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
        CxxGrammarImpl.shiftExpression)).thenReturn(true);

    var left = createPrimaryExpression(leftValue);
    var operatorNode = mock(AstNode.class);
    when(operatorNode.getTokenValue()).thenReturn(operator);
    var right = createPrimaryExpression(rightValue);

    when(binaryExpr.getChildren()).thenReturn(List.of(left, operatorNode, right));

    return binaryExpr;
  }

  // =========================================================================
  // Symbol resolution chain tests
  // =========================================================================

  @AfterEach
  void cleanup() {
    AstNodeSymbolExtension.clear();
    AstNodeTypeExtension.clear();
  }

  @Test
  void testResolveIdentifierNullSymbol() {
    // primaryExpression with idExpression → IDENTIFIER with no symbol attached
    // → resolveSymbolValue(null) → null
    var identifier = mock(AstNode.class);
    // No symbol set on identifier: AstNodeSymbolExtension.getSymbol returns null

    var idExpr = mock(AstNode.class);
    when(idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER)).thenReturn(identifier);

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of());
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(idExpr);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isNull();
  }

  @Test
  void testResolveEnumConstantWithInitializer() {
    // Build an enum constant symbol whose declaration has a constantExpression
    // child of "42"
    var enumSymbol = new SourceCodeSymbol("MY_CONST", Symbol.Kind.ENUM_CONSTANT, null);

    // Innermost value: primaryExpression containing literal "42"
    var literalToken = mock(AstNode.class);
    when(literalToken.getTokenValue()).thenReturn("42");
    var constExpr = mock(AstNode.class);
    when(constExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(constExpr.getChildren()).thenReturn(List.of(literalToken));
    when(constExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(null);

    var declNode = mock(AstNode.class);
    when(declNode.getFirstChild(CxxGrammarImpl.constantExpression)).thenReturn(constExpr);
    enumSymbol.setDeclaration(declNode);

    // Attach symbol to identifier and hook up resolution path
    var identifier = mock(AstNode.class);
    AstNodeSymbolExtension.setSymbol(identifier, enumSymbol);

    var idExpr = mock(AstNode.class);
    when(idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER)).thenReturn(identifier);

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of());
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(idExpr);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isEqualTo(42);
  }

  @Test
  void testResolveEnumConstantWithoutDeclaration() {
    // Enum constant with no declaration → resolveSymbolValue returns null
    var enumSymbol = new SourceCodeSymbol("MY_CONST", Symbol.Kind.ENUM_CONSTANT, null);

    var identifier = mock(AstNode.class);
    AstNodeSymbolExtension.setSymbol(identifier, enumSymbol);

    var idExpr = mock(AstNode.class);
    when(idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER)).thenReturn(identifier);

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of());
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(idExpr);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isNull();
  }

  @Test
  void testResolveConstVariableViaFindInitializer() {
    // const variable whose initializer resolves to 314
    var varSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("PI", null) {
      @Override
      public boolean isConst() {
        return true;
      }
    };

    // Innermost literal "314"
    var literalToken = mock(AstNode.class);
    when(literalToken.getTokenValue()).thenReturn("314");
    var innerExpr = mock(AstNode.class);
    when(innerExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(innerExpr.getChildren()).thenReturn(List.of(literalToken));
    when(innerExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(null);

    var initClause = mock(AstNode.class);
    when(initClause.getFirstChild()).thenReturn(innerExpr);

    var initializer = mock(AstNode.class);
    when(initializer.getFirstChild(CxxGrammarImpl.initializerClause)).thenReturn(initClause);

    var initDeclarator = mock(AstNode.class);
    when(initDeclarator.is(CxxGrammarImpl.initDeclarator)).thenReturn(true);
    when(initDeclarator.getFirstChild(CxxGrammarImpl.initializer)).thenReturn(initializer);
    when(initDeclarator.getParent()).thenReturn(null);

    var declNode = mock(AstNode.class);
    when(declNode.getParent()).thenReturn(initDeclarator);
    varSymbol.setDeclaration(declNode);

    var identifier = mock(AstNode.class);
    AstNodeSymbolExtension.setSymbol(identifier, varSymbol);

    var idExpr = mock(AstNode.class);
    when(idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER)).thenReturn(identifier);

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of());
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(idExpr);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isEqualTo(314);
  }

  @Test
  void testResolveBinaryAdditionLongs() {
    // Use values > Integer.MAX_VALUE to force Long path in resolveArithmetic
    var binaryNode = createBinaryExpression("9999999999", "+", "1");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(Long.class).isEqualTo(10000000000L);
  }

  @Test
  void testResolveIdExpressionDirectPath() {
    // resolveAsConstant dispatches idExpression directly (not via
    // primaryExpression)
    var idExpr = mock(AstNode.class);
    when(idExpr.is(CxxGrammarImpl.idExpression)).thenReturn(true);

    var identifier = mock(AstNode.class);
    when(idExpr.getFirstDescendant(com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER))
        .thenReturn(identifier);
    // No symbol attached → null
    assertThat(CxxConstantUtils.resolveAsConstant(idExpr)).isNull();
  }

  @Test
  void testResolveIdentifierUnknownSymbol() {
    // UNKNOWN_SYMBOL is unknown → resolveSymbolValue returns null
    var identifier = mock(AstNode.class);
    AstNodeSymbolExtension.setSymbol(identifier, Symbol.UNKNOWN_SYMBOL);

    var idExpr = mock(AstNode.class);
    when(idExpr.is(CxxGrammarImpl.idExpression)).thenReturn(true);
    when(idExpr.getFirstDescendant(com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER))
        .thenReturn(identifier);

    assertThat(CxxConstantUtils.resolveAsConstant(idExpr)).isNull();
  }

  @Test
  void testResolveRawStringLiteral() {
    // R"(hello world)" — raw string with empty delimiter
    var node = createPrimaryExpression("R\"(hello world)\"");
    var result = CxxConstantUtils.resolveAsConstant(node);
    assertThat(result).isEqualTo("hello world");
  }

  // =========================================================================
  // Long unary operator tests
  // =========================================================================

  @Test
  void testResolveUnaryMinusLong() {
    // applyUnaryNegate Long branch: 9999999999 > Integer.MAX_VALUE → parsed as Long
    var unaryNode = createUnaryExpression("-", "9999999999");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Long.class).isEqualTo(-9999999999L);
  }

  @Test
  void testResolveUnaryBitwiseNotLong() {
    // applyUnaryBitwiseNot Long branch
    var unaryNode = createUnaryExpression("~", "9999999999");
    var result = CxxConstantUtils.resolveAsConstant(unaryNode);
    assertThat(result).isInstanceOf(Long.class).isEqualTo(~9999999999L);
  }

  // =========================================================================
  // isZero() Long branch and modulo-by-zero tests
  // =========================================================================

  @Test
  void testResolveModuloByZeroReturnsNull() {
    var binaryNode = createBinaryExpression("42", "%", "0");
    assertThat(CxxConstantUtils.resolveAsConstant(binaryNode)).isNull();
  }

  @Test
  void testResolveDivisionByLongZeroReturnsNull() {
    // 0x0000000000 has 10 hex digits → parsed as Long(0); covers isZero Long branch
    var binaryNode = createBinaryExpression("9999999999", "/", "0x0000000000");
    assertThat(CxxConstantUtils.resolveAsConstant(binaryNode)).isNull();
  }

  // =========================================================================
  // Binary expression edge cases
  // =========================================================================

  @Test
  void testResolveBinaryExpressionTooFewChildren() {
    // < 3 children → resolveBinaryExpression returns null immediately
    var binaryExpr = mock(AstNode.class);
    when(binaryExpr.is(
        CxxGrammarImpl.additiveExpression,
        CxxGrammarImpl.multiplicativeExpression,
        CxxGrammarImpl.andExpression,
        CxxGrammarImpl.exclusiveOrExpression,
        CxxGrammarImpl.inclusiveOrExpression,
        CxxGrammarImpl.shiftExpression)).thenReturn(true);
    when(binaryExpr.getChildren()).thenReturn(List.of(mock(AstNode.class), mock(AstNode.class)));
    assertThat(CxxConstantUtils.resolveAsConstant(binaryExpr)).isNull();
  }

  @Test
  void testResolveBinaryUnknownOperator() {
    // operator "&&" is not in the switch → default → null
    var binaryNode = createBinaryExpression("10", "&&", "5");
    assertThat(CxxConstantUtils.resolveAsConstant(binaryNode)).isNull();
  }

  @Test
  void testResolveStringRightConcatenation() {
    // left is Integer, right is String → resolvePlus returns left + rightStr
    var binaryNode = createBinaryExpression("42", "+", "\" world\"");
    var result = CxxConstantUtils.resolveAsConstant(binaryNode);
    assertThat(result).isInstanceOf(String.class).isEqualTo("42 world");
  }

  // =========================================================================
  // Single-child wrapper and null token value tests
  // =========================================================================

  @Test
  void testResolveAsConstantSingleChildWrapper() {
    // A node that is not a recognized expression type but has exactly 1 child
    // → resolveAsConstant recurses into the child
    var primary = createPrimaryExpression("99");
    var wrapper = mock(AstNode.class);
    when(wrapper.getNumberOfChildren()).thenReturn(1);
    when(wrapper.getFirstChild()).thenReturn(primary);
    assertThat(CxxConstantUtils.resolveAsConstant(wrapper)).isEqualTo(99);
  }

  @Test
  void testResolveChildTokenNullValue() {
    // A primaryExpression child where getTokenValue() returns null → resolveChildToken returns null
    var nullTokenChild = mock(AstNode.class);
    // getTokenValue() returns null by default for Mockito mocks

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of(nullTokenChild));
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(null);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isNull();
  }

  // =========================================================================
  // Char escape sequence tests
  // =========================================================================

  @Test
  void testResolveCharEscapeTab() {
    // '\t' → tab character
    var node = createPrimaryExpression("'\\t'");
    assertThat(CxxConstantUtils.resolveAsConstant(node))
        .isInstanceOf(Character.class).isEqualTo('\t');
  }

  @Test
  void testResolveCharEscapeCarriageReturn() {
    // '\r' → carriage return character
    var node = createPrimaryExpression("'\\r'");
    assertThat(CxxConstantUtils.resolveAsConstant(node))
        .isInstanceOf(Character.class).isEqualTo('\r');
  }

  @Test
  void testResolveCharEscapeNull() {
    // '\0' → null character
    var node = createPrimaryExpression("'\\0'");
    assertThat(CxxConstantUtils.resolveAsConstant(node))
        .isInstanceOf(Character.class).isEqualTo('\0');
  }

  @Test
  void testResolveCharEscapeBackslash() {
    // '\\' → backslash character
    var node = createPrimaryExpression("'\\\\'");
    assertThat(CxxConstantUtils.resolveAsConstant(node))
        .isInstanceOf(Character.class).isEqualTo('\\');
  }

  @Test
  void testResolveCharEscapeSingleQuote() {
    // '\'' → single-quote character
    var node = createPrimaryExpression("'\\''");
    assertThat(CxxConstantUtils.resolveAsConstant(node))
        .isInstanceOf(Character.class).isEqualTo('\'');
  }

  @Test
  void testResolveCharEscapeUnknown() {
    // '\z' → unknown escape → null (default case in resolveEscapeChar)
    var node = createPrimaryExpression("'\\z'");
    assertThat(CxxConstantUtils.resolveAsConstant(node)).isNull();
  }

  // =========================================================================
  // Raw string literal edge cases
  // =========================================================================

  @Test
  void testResolveRawStringLiteralNoParenthesis() {
    // R"hello" — missing '(' → delimEnd == -1 → returns null
    var node = createPrimaryExpression("R\"hello\"");
    assertThat(CxxConstantUtils.resolveAsConstant(node)).isNull();
  }

  @Test
  void testResolveRawStringLiteralNoClosingDelimiter() {
    // R"(hello — missing closing ')""' → contentEnd == -1 → returns null
    var node = createPrimaryExpression("R\"(hello");
    assertThat(CxxConstantUtils.resolveAsConstant(node)).isNull();
  }

  // =========================================================================
  // Variable symbol resolution edge cases
  // =========================================================================

  @Test
  void testResolveNonConstVariableReturnsNull() {
    // isVariableSymbol() = true, isConst() = false (default) → returns null
    var varSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("x", null);

    var identifier = mock(AstNode.class);
    AstNodeSymbolExtension.setSymbol(identifier, varSymbol);

    var idExpr = mock(AstNode.class);
    when(idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER)).thenReturn(identifier);

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of());
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(idExpr);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isNull();
  }

  @Test
  void testResolveConstVariableNoInitDeclarator() {
    // const variable whose parent chain has no initDeclarator → findInitializer returns null
    var varSymbol = new SourceCodeSymbol.SourceCodeVariableSymbol("PI", null) {
      @Override
      public boolean isConst() {
        return true;
      }
    };

    var declNode = mock(AstNode.class);
    when(declNode.getParent()).thenReturn(null); // parent is null → findInitializer returns null
    varSymbol.setDeclaration(declNode);

    var identifier = mock(AstNode.class);
    AstNodeSymbolExtension.setSymbol(identifier, varSymbol);

    var idExpr = mock(AstNode.class);
    when(idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER)).thenReturn(identifier);

    var primaryExpr = mock(AstNode.class);
    when(primaryExpr.is(CxxGrammarImpl.primaryExpression)).thenReturn(true);
    when(primaryExpr.getChildren()).thenReturn(List.of());
    when(primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression)).thenReturn(idExpr);

    assertThat(CxxConstantUtils.resolveAsConstant(primaryExpr)).isNull();
  }
}
