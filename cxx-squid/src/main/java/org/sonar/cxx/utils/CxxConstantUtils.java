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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.cxx.parser.CxxGrammarImpl;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxPunctuator;
import org.sonar.cxx.squidbridge.api.AstNodeSymbolExtension;
import org.sonar.cxx.squidbridge.api.Symbol;

/**
 * Utility methods for resolving constant values from C++ expressions.
 *
 * <p>This class provides compile-time constant resolution for literals,
 * const variables, enum constants, and constant expressions. It's adapted
 * from sonar-java's ExpressionUtils for C++ semantics.
 *
 * <p>Supported constant types:
 * <ul>
 *   <li>Literals: integers, longs, strings, booleans, characters</li>
 *   <li>Const variables with constant initializers</li>
 *   <li>Enum constants</li>
 *   <li>Unary expressions: -, +, !, ~</li>
 *   <li>Binary expressions: +, -, *, /, %, |, &amp;, ^</li>
 * </ul>
 */
public final class CxxConstantUtils {

  private CxxConstantUtils() {
  }

  /**
   * Attempts to resolve an expression node to a compile-time constant value.
   *
   * <p>Returns null if the expression is not a compile-time constant or if
   * the value cannot be determined.
   *
   * @param node an expression node from the AST
   * @return the constant value (Integer, Long, String, Boolean, or Character),
   *         or null if not a constant
   */
  @CheckForNull
  public static Object resolveAsConstant(@Nullable AstNode node) {
    if (node == null) {
      return null;
    }

    AstNode expression = skipParentheses(node);

    if (expression.is(CxxGrammarImpl.primaryExpression)) {
      return resolvePrimaryExpression(expression);
    }

    if (expression.is(CxxGrammarImpl.unaryExpression)) {
      return resolveUnaryExpression(expression);
    }

    if (expression.is(CxxGrammarImpl.additiveExpression,
                     CxxGrammarImpl.multiplicativeExpression,
                     CxxGrammarImpl.andExpression,
                     CxxGrammarImpl.exclusiveOrExpression,
                     CxxGrammarImpl.inclusiveOrExpression,
                     CxxGrammarImpl.shiftExpression)) {
      return resolveBinaryExpression(expression);
    }

    if (expression.is(CxxGrammarImpl.idExpression)) {
      return resolveIdentifier(expression);
    }

    // Recurse through single-child wrapper nodes (e.g. initializerClause →
    // assignmentExpression → ... → primaryExpression) that are not directly
    // recognized above.
    if (expression.getNumberOfChildren() == 1) {
      return resolveAsConstant(expression.getFirstChild());
    }

    return null;
  }

  /**
   * Resolves a primary expression (literal or identifier).
   */
  @CheckForNull
  private static Object resolvePrimaryExpression(AstNode primaryExpr) {
    for (AstNode child : primaryExpr.getChildren()) {
      if (child.is(GenericTokenType.IDENTIFIER)) {
        Symbol symbol = AstNodeSymbolExtension.getSymbol(child);
        return resolveSymbolValue(symbol);
      }

      String tokenValue = child.getTokenValue();
      if (tokenValue == null) {
        continue;
      }

      if (child.is(CxxKeyword.TRUE)) {
        return Boolean.TRUE;
      }
      if (child.is(CxxKeyword.FALSE)) {
        return Boolean.FALSE;
      }
      if (child.is(CxxKeyword.NULLPTR)) {
        return null;
      }

      if (isIntegerLiteral(tokenValue)) {
        return parseIntegerLiteral(tokenValue);
      }
      if (isStringLiteral(tokenValue)) {
        return parseStringLiteral(tokenValue);
      }
      if (isCharLiteral(tokenValue)) {
        return parseCharLiteral(tokenValue);
      }
    }

    AstNode idExpr = primaryExpr.getFirstDescendant(CxxGrammarImpl.idExpression);
    if (idExpr != null) {
      return resolveIdentifier(idExpr);
    }

    return null;
  }

  /**
   * Resolves an identifier to its constant value.
   */
  @CheckForNull
  private static Object resolveIdentifier(AstNode idExpr) {
    AstNode identifier = idExpr.getFirstDescendant(GenericTokenType.IDENTIFIER);
    if (identifier != null) {
      Symbol symbol = AstNodeSymbolExtension.getSymbol(identifier);
      return resolveSymbolValue(symbol);
    }
    return null;
  }

  /**
   * Resolves a symbol to its constant value.
   */
  @CheckForNull
  private static Object resolveSymbolValue(@Nullable Symbol symbol) {
    if (symbol == null || symbol.isUnknown()) {
      return null;
    }

    if (symbol.is(Symbol.Kind.ENUM_CONSTANT)) {
      AstNode declaration = symbol.declaration();
      if (declaration != null) {
        AstNode initializer = declaration.getFirstChild(CxxGrammarImpl.constantExpression);
        if (initializer != null) {
          return resolveAsConstant(initializer);
        }
      }
      return null;
    }

    if (symbol.isVariableSymbol() && symbol.isConst()) {
      AstNode declaration = symbol.declaration();
      if (declaration != null) {
        AstNode initializer = findInitializer(declaration);
        if (initializer != null) {
          return resolveAsConstant(initializer);
        }
      }
    }

    return null;
  }

  /**
   * Finds the initializer expression for a variable declaration.
   */
  @CheckForNull
  private static AstNode findInitializer(AstNode declaration) {
    AstNode parent = declaration.getParent();
    while (parent != null && !parent.is(CxxGrammarImpl.initDeclarator)) {
      parent = parent.getParent();
    }

    if (parent != null) {
      AstNode initializer = parent.getFirstChild(CxxGrammarImpl.initializer);
      if (initializer != null) {
        AstNode initClause = initializer.getFirstChild(CxxGrammarImpl.initializerClause);
        if (initClause != null) {
          return initClause.getFirstChild();
        }
      }
    }
    return null;
  }

  /**
   * Resolves a unary expression.
   */
  @CheckForNull
  private static Object resolveUnaryExpression(AstNode unaryExpr) {
    AstNode operator = unaryExpr.getFirstChild();
    if (operator == null) {
      return null;
    }

    AstNode operand = unaryExpr.getLastChild();
    if (operand == null) {
      return null;
    }

    Object value = resolveAsConstant(operand);
    if (value == null) {
      return null;
    }

    if (operator.is(CxxPunctuator.MINUS)) {
      if (value instanceof Long longValue) {
        return -longValue;
      } else if (value instanceof Integer intValue) {
        return -intValue;
      }
    } else if (operator.is(CxxPunctuator.PLUS)) {
      return value;
    } else if (operator.is(CxxPunctuator.NOT)) {
      if (value instanceof Boolean boolValue) {
        return !boolValue;
      }
    } else if (operator.is(CxxPunctuator.BW_NOT)) {
      if (value instanceof Long longValue) {
        return ~longValue;
      } else if (value instanceof Integer intValue) {
        return ~intValue;
      }
    }

    return null;
  }

  /**
   * Resolves a binary expression.
   */
  @CheckForNull
  private static Object resolveBinaryExpression(AstNode binaryExpr) {
    var children = binaryExpr.getChildren();
    if (children.size() < 3) {
      return null;
    }

    AstNode left = children.get(0);
    AstNode operator = children.get(1);
    AstNode right = children.get(2);

    Object leftValue = resolveAsConstant(left);
    Object rightValue = resolveAsConstant(right);

    if (leftValue == null || rightValue == null) {
      return null;
    }

    String op = operator.getTokenValue();
    if (op == null) {
      return null;
    }

    return switch (op) {
      case "+" -> resolvePlus(leftValue, rightValue);
      case "-" -> resolveMinus(leftValue, rightValue);
      case "*" -> resolveMultiply(leftValue, rightValue);
      case "/" -> resolveDivide(leftValue, rightValue);
      case "%" -> resolveModulo(leftValue, rightValue);
      case "|" -> resolveBitwiseOr(leftValue, rightValue);
      case "&" -> resolveBitwiseAnd(leftValue, rightValue);
      case "^" -> resolveBitwiseXor(leftValue, rightValue);
      case "<<" -> resolveLeftShift(leftValue, rightValue);
      case ">>" -> resolveRightShift(leftValue, rightValue);
      default -> null;
    };
  }

  @CheckForNull
  private static Object resolvePlus(Object left, Object right) {
    if (left instanceof String leftStr) {
      return leftStr + right;
    }
    if (right instanceof String rightStr) {
      return left + rightStr;
    }
    return resolveArithmetic(left, right, Long::sum, Integer::sum);
  }

  @CheckForNull
  private static Object resolveMinus(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a - b, (a, b) -> a - b);
  }

  @CheckForNull
  private static Object resolveMultiply(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a * b, (a, b) -> a * b);
  }

  @CheckForNull
  private static Object resolveDivide(Object left, Object right) {
    try {
      return resolveArithmetic(left, right, (a, b) -> a / b, (a, b) -> a / b);
    } catch (ArithmeticException e) {
      return null;
    }
  }

  @CheckForNull
  private static Object resolveModulo(Object left, Object right) {
    try {
      return resolveArithmetic(left, right, (a, b) -> a % b, (a, b) -> a % b);
    } catch (ArithmeticException e) {
      return null;
    }
  }

  @CheckForNull
  private static Object resolveBitwiseOr(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a | b, (a, b) -> a | b);
  }

  @CheckForNull
  private static Object resolveBitwiseAnd(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a & b, (a, b) -> a & b);
  }

  @CheckForNull
  private static Object resolveBitwiseXor(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a ^ b, (a, b) -> a ^ b);
  }

  @CheckForNull
  private static Object resolveLeftShift(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a << b, (a, b) -> a << b);
  }

  @CheckForNull
  private static Object resolveRightShift(Object left, Object right) {
    return resolveArithmetic(left, right, (a, b) -> a >> b, (a, b) -> a >> b);
  }

  @FunctionalInterface
  private interface LongBinaryOperator {
    Long apply(Long a, Long b);
  }

  @FunctionalInterface
  private interface IntBinaryOperator {
    Integer apply(Integer a, Integer b);
  }

  @CheckForNull
  private static Object resolveArithmetic(Object left, Object right,
                                          LongBinaryOperator longOp,
                                          IntBinaryOperator intOp) {
    try {
      if (left instanceof Integer leftInt && right instanceof Integer rightInt) {
        return intOp.apply(leftInt, rightInt);
      } else if (left instanceof Number leftNum && right instanceof Number rightNum) {
        return longOp.apply(leftNum.longValue(), rightNum.longValue());
      }
    } catch (ArithmeticException e) {
      return null;
    }
    return null;
  }

  /**
   * Skip parentheses to get the actual expression.
   */
  private static AstNode skipParentheses(AstNode node) {
    AstNode current = node;
    while (current != null) {
      var children = current.getChildren();
      if (children.size() == 3) {
        AstNode first = children.get(0);
        AstNode last = children.get(2);
        if (first != null && last != null
          && "(".equals(first.getTokenValue())
          && ")".equals(last.getTokenValue())) {
          current = children.get(1);
        } else {
          break;
        }
      } else {
        break;
      }
    }
    return current;
  }

  private static boolean isIntegerLiteral(String tokenValue) {
    if (tokenValue == null || tokenValue.isEmpty()) {
      return false;
    }
    return tokenValue.matches("\\d[\\d']*[uUlL]*")
      || tokenValue.matches("0[xX][\\da-fA-F][\\da-fA-F']*[uUlL]*")
      || tokenValue.matches("0[bB][01][01']*[uUlL]*")
      || tokenValue.matches("0[0-7][0-7']*[uUlL]*");
  }

  private static boolean isStringLiteral(String tokenValue) {
    return tokenValue != null
      && (tokenValue.startsWith("\"") || tokenValue.startsWith("R\""));
  }

  private static boolean isCharLiteral(String tokenValue) {
    return tokenValue != null && tokenValue.startsWith("'");
  }

  @CheckForNull
  private static Object parseIntegerLiteral(String literal) {
    try {
      String cleaned = literal.replaceAll("['uUlL]", "");

      if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) {
        String hex = cleaned.substring(2);
        if (hex.length() <= 8) {
          return Integer.parseUnsignedInt(hex, 16);
        }
        return Long.parseUnsignedLong(hex, 16);
      }

      if (cleaned.startsWith("0b") || cleaned.startsWith("0B")) {
        String bin = cleaned.substring(2);
        if (bin.length() <= 31) {
          return Integer.parseUnsignedInt(bin, 2);
        }
        return Long.parseUnsignedLong(bin, 2);
      }

      if (cleaned.startsWith("0") && cleaned.length() > 1) {
        String oct = cleaned.substring(1);
        if (oct.length() <= 10) {
          return Integer.parseUnsignedInt(oct, 8);
        }
        return Long.parseUnsignedLong(oct, 8);
      }

      long value = Long.parseLong(cleaned);
      if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
        return (int) value;
      }
      return value;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @CheckForNull
  private static String parseStringLiteral(String literal) {
    if (literal.startsWith("R\"")) {
      int delimStart = 2;
      int delimEnd = literal.indexOf('(', delimStart);
      if (delimEnd == -1) {
        return null;
      }
      String delim = literal.substring(delimStart, delimEnd);
      int contentStart = delimEnd + 1;
      int contentEnd = literal.lastIndexOf(')' + delim + '"');
      if (contentEnd == -1) {
        return null;
      }
      return literal.substring(contentStart, contentEnd);
    }

    if (literal.length() < 2) {
      return null;
    }
    return literal.substring(1, literal.length() - 1)
      .replace("\\n", "\n")
      .replace("\\t", "\t")
      .replace("\\r", "\r")
      .replace("\\\"", "\"")
      .replace("\\\\", "\\");
  }

  @CheckForNull
  private static Character parseCharLiteral(String literal) {
    if (literal.length() < 3) {
      return null;
    }
    String content = literal.substring(1, literal.length() - 1);
    if (content.length() == 1) {
      return content.charAt(0);
    }
    if (content.startsWith("\\") && content.length() == 2) {
      return switch (content.charAt(1)) {
        case 'n' -> '\n';
        case 't' -> '\t';
        case 'r' -> '\r';
        case '0' -> '\0';
        case '\\' -> '\\';
        case '\'' -> '\'';
        default -> null;
      };
    }
    return null;
  }
}
