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
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import java.math.BigInteger;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CppPunctuator;
import org.sonar.cxx.api.CxxTokenType;

public final class ExpressionEvaluator {

  private static final BigInteger UINT64_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);
  private static final Logger LOG = Loggers.get(ExpressionEvaluator.class);

  private final Parser<Grammar> parser;
  private final CxxPreprocessor preprocessor;

  public ExpressionEvaluator(CxxConfiguration conf, CxxPreprocessor preprocessor) {
    parser = CppParser.createConstantExpressionParser(conf);

    this.preprocessor = preprocessor;
  }

  public boolean eval(String constExpr) {
    return evalToInt(constExpr, null).compareTo(BigInteger.ZERO) != 0;
  }

  public boolean eval(AstNode constExpr) {
    return evalToInt(constExpr).compareTo(BigInteger.ZERO) != 0;
  }

  private BigInteger evalToInt(String constExpr, @Nullable AstNode exprAst) {
    AstNode constExprAst = null;
    try {
      constExprAst = parser.parse(constExpr);
    } catch (com.sonar.sslr.api.RecognitionException re) {
      if (exprAst != null) {
        LOG.warn("Error evaluating expression '{}' for AstExp '{}', assuming 0", constExpr, exprAst.getToken());
      } else {
        LOG.warn("Error evaluating expression '{}', assuming 0", constExpr);
      }
      LOG.debug("EvalToInt failed: {}", re);
      return BigInteger.ZERO;
    }

    return evalToInt(constExprAst);
  }

  private BigInteger evalToInt(AstNode exprAst) {
    LOG.trace("Evaluating expression: {}", exprAst);

    int noChildren = exprAst.getNumberOfChildren();
    if (noChildren == 0) {
      return evalLeaf(exprAst);
    } else if (noChildren == 1) {
      return evalOneChildAst(exprAst);
    }

    return evalComplexAst(exprAst);
  }

  private BigInteger evalLeaf(AstNode exprAst) {
    // Evaluation of leafs
    //
    AstNodeType nodeType = exprAst.getType();

    if (nodeType.equals(CxxTokenType.NUMBER)) {
      return evalNumber(exprAst.getTokenValue());
    } else if (nodeType.equals(CxxTokenType.CHARACTER)) {
      return evalCharacter(exprAst.getTokenValue());
    } else if (nodeType.equals(GenericTokenType.IDENTIFIER)) {
      String value = preprocessor.valueOf(exprAst.getTokenValue());
      return value == null ? BigInteger.ZERO : evalToInt(value, exprAst);
    } else {
      throw new EvaluationException("Unknown expression type '" + nodeType + "'");
    }
  }

  private BigInteger evalOneChildAst(AstNode exprAst) {
    // Evaluation of booleans and 'pass-through's
    //
    AstNodeType nodeType = exprAst.getType();
    if (nodeType.equals(CppGrammar.bool)) {
      return evalBool(exprAst.getTokenValue());
    }
    return evalToInt(exprAst.getFirstChild());
  }

  private BigInteger evalComplexAst(AstNode exprAst) {

    // More complex expressions with more than one child
    //
    AstNodeType nodeType = exprAst.getType();
    if (nodeType.equals(CppGrammar.unaryExpression)) {
      return evalUnaryExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.conditionalExpression)) {
      return evalConditionalExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.logicalOrExpression)) {
      return evalLogicalOrExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.logicalAndExpression)) {
      return evalLogicalAndExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.inclusiveOrExpression)) {
      return evalInclusiveOrExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.exclusiveOrExpression)) {
      return evalExclusiveOrExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.andExpression)) {
      return evalAndExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.equalityExpression)) {
      return evalEqualityExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.relationalExpression)) {
      return evalRelationalExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.shiftExpression)) {
      return evalShiftExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.additiveExpression)) {
      return evalAdditiveExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.multiplicativeExpression)) {
      return evalMultiplicativeExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.primaryExpression)) {
      return evalPrimaryExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.definedExpression)) {
      return evalDefinedExpression(exprAst);
    } else if (nodeType.equals(CppGrammar.functionlikeMacro)) {
      return evalFunctionlikeMacro(exprAst);
    } else if (nodeType.equals(CppGrammar.hasIncludeExpression)) {
      return evalHasIncludeExpression(exprAst);
    } else {
      LOG.error("'evalComplexAst' Unknown expression type '" + nodeType + "' for AstExt '"
        + exprAst.getToken() + "', assuming 0");
      return BigInteger.ZERO;
    }
  }

  // ///////////////// Primitives //////////////////////
  private static BigInteger evalBool(String boolValue) {
    return "true".equalsIgnoreCase(boolValue) ? BigInteger.ONE : BigInteger.ZERO;
  }

  private static BigInteger evalNumber(String intValue) {
    // the if expressions aren't allowed to contain floats
    BigInteger number;
    try {
      number = decode(intValue);
    } catch (java.lang.NumberFormatException nfe) {
      LOG.warn("Cannot decode the number '{}' falling back to value '{}' instead", intValue, BigInteger.ONE);
      number = BigInteger.ONE;
    }

    return number;
  }

  private static BigInteger evalCharacter(String charValue) {
    // TODO: replace this simplification by something more sane
    return "'\0'".equals(charValue) ? BigInteger.ZERO : BigInteger.ONE;
  }

  private static AstNode getNextOperand(@Nullable AstNode node) {
    AstNode sibling = node;
    if (sibling != null) {
      sibling = sibling.getNextSibling();
      if (sibling != null) {
        sibling = sibling.getNextSibling();
      }
    }
    return sibling;
  }

  // ////////////// logical expressions ///////////////////////////
  private BigInteger evalLogicalOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    boolean result = eval(operand);

    while (!result && ((operand = getNextOperand(operand)) != null)) {
      result = eval(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalLogicalAndExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    boolean result = eval(operand);

    while (result && ((operand = getNextOperand(operand)) != null)) {
      result = eval(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalEqualityExpression(AstNode exprAst) {
    AstNode lhs = exprAst.getFirstChild();
    AstNode operator = lhs.getNextSibling();
    AstNode rhs = operator.getNextSibling();
    AstNodeType operatorType = operator.getType();

    boolean result;
    if (operatorType.equals(CppPunctuator.EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) == 0;
    } else if (operatorType.equals(CppPunctuator.NOT_EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) != 0;
    } else {
      throw new EvaluationException("Unknown equality operator '" + operatorType + "'");
    }

    while ((operator = rhs.getNextSibling()) != null) {
      operatorType = operator.getType();
      rhs = operator.getNextSibling();
      if (operatorType.equals(CppPunctuator.EQ)) {
        result = result == eval(rhs);
      } else if (operatorType.equals(CppPunctuator.NOT_EQ)) {
        result = result != eval(rhs);
      } else {
        throw new EvaluationException("Unknown equality operator '" + operatorType + "'");
      }
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalRelationalExpression(AstNode exprAst) {
    AstNode lhs = exprAst.getFirstChild();
    AstNode operator = lhs.getNextSibling();
    AstNode rhs = operator.getNextSibling();
    AstNodeType operatorType = operator.getType();

    boolean result;
    if (operatorType.equals(CppPunctuator.LT)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) < 0;
    } else if (operatorType.equals(CppPunctuator.GT)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) > 0;
    } else if (operatorType.equals(CppPunctuator.LT_EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) <= 0;
    } else if (operatorType.equals(CppPunctuator.GT_EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) >= 0;
    } else {
      throw new EvaluationException("Unknown relational operator '" + operatorType + "'");
    }

    BigInteger resultAsInt;
    while ((operator = rhs.getNextSibling()) != null) {
      operatorType = operator.getType();
      rhs = operator.getNextSibling();

      resultAsInt = result ? BigInteger.ONE : BigInteger.ZERO;
      if (operatorType.equals(CppPunctuator.LT)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) < 0;
      } else if (operatorType.equals(CppPunctuator.GT)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) > 0;
      } else if (operatorType.equals(CppPunctuator.LT_EQ)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) <= 0;
      } else if (operatorType.equals(CppPunctuator.GT_EQ)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) >= 0;
      } else {
        throw new EvaluationException("Unknown relational operator '" + operatorType + "'");
      }
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  // ///////////////// bitwise expressions ///////////////////////
  private BigInteger evalAndExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.and(evalToInt(operand));
    }

    return result;
  }

  private BigInteger evalInclusiveOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.or(evalToInt(operand));
    }

    return result;
  }

  private BigInteger evalExclusiveOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.xor(evalToInt(operand));
    }

    return result;
  }

  // ///////////////// other ... ///////////////////
  private BigInteger evalUnaryExpression(AstNode exprAst) {
    // only 'unary-operator cast-expression' production is allowed in #if-context

    AstNode operator = exprAst.getFirstChild();
    AstNode operand = operator.getNextSibling();
    AstNodeType operatorType = operator.getFirstChild().getType();

    if (operatorType.equals(CppPunctuator.PLUS)) {
      return evalToInt(operand);
    } else if (operatorType.equals(CppPunctuator.MINUS)) {
      return evalToInt(operand).negate();
    } else if (operatorType.equals(CppPunctuator.NOT)) {
      boolean result = !eval(operand);
      return result ? BigInteger.ONE : BigInteger.ZERO;
    } else if (operatorType.equals(CppPunctuator.BW_NOT)) {
      //todo: need more information (signed/unsigned, data type length) to invert bits in all cases correct
      return evalToInt(operand).not().and(UINT64_MAX);
    } else {
      throw new EvaluationException("Unknown unary operator  '" + operatorType + "'");
    }
  }

  private BigInteger evalShiftExpression(AstNode exprAst) {
    AstNode rhs = exprAst.getFirstChild();
    AstNode operator;
    BigInteger result = evalToInt(rhs);

    while ((operator = rhs.getNextSibling()) != null) {
      AstNodeType operatorType = operator.getType();
      rhs = operator.getNextSibling();

      if (operatorType.equals(CppPunctuator.BW_LSHIFT)) {
        result = result.shiftLeft(evalToInt(rhs).intValue()).and(UINT64_MAX);
      } else if (operatorType.equals(CppPunctuator.BW_RSHIFT)) {
        result = result.shiftRight(evalToInt(rhs).intValue());
      } else {
        throw new EvaluationException("Unknown shift operator '" + operatorType + "'");
      }
    }

    return result;
  }

  private BigInteger evalAdditiveExpression(AstNode exprAst) {
    AstNode rhs = exprAst.getFirstChild();
    AstNode operator;
    BigInteger result = evalToInt(rhs);

    while ((operator = rhs.getNextSibling()) != null) {
      AstNodeType operatorType = operator.getType();
      rhs = operator.getNextSibling();

      if (operatorType.equals(CppPunctuator.PLUS)) {
        result = result.add(evalToInt(rhs));
      } else if (operatorType.equals(CppPunctuator.MINUS)) {
        result = result.subtract(evalToInt(rhs));
      } else {
        throw new EvaluationException("Unknown additive operator '" + operatorType + "'");
      }
    }

    return result;
  }

  private BigInteger evalMultiplicativeExpression(AstNode exprAst) {
    AstNode rhs = exprAst.getFirstChild();
    AstNode operator;
    BigInteger result = evalToInt(rhs);

    while ((operator = rhs.getNextSibling()) != null) {
      AstNodeType operatorType = operator.getType();
      rhs = operator.getNextSibling();

      if (operatorType.equals(CppPunctuator.MUL)) {
        result = result.multiply(evalToInt(rhs));
      } else if (operatorType.equals(CppPunctuator.DIV)) {
        result = result.divide(evalToInt(rhs));
      } else if (operatorType.equals(CppPunctuator.MODULO)) {
        result = result.mod(evalToInt(rhs));
      } else {
        throw new EvaluationException("Unknown multiplicative operator '" + operatorType + "'");
      }
    }

    return result;
  }

  private BigInteger evalConditionalExpression(AstNode exprAst) {
    if (exprAst.getNumberOfChildren() == 5) {
      AstNode decisionOperand = exprAst.getFirstChild();
      AstNode operator = decisionOperand.getNextSibling();
      AstNode trueCaseOperand = operator.getNextSibling();
      operator = trueCaseOperand.getNextSibling();
      AstNode falseCaseOperand = operator.getNextSibling();
      return eval(decisionOperand) ? evalToInt(trueCaseOperand) : evalToInt(falseCaseOperand);
    } else {
      AstNode decisionOperand = exprAst.getFirstChild();
      AstNode operator = decisionOperand.getNextSibling();
      operator = operator.getNextSibling();
      AstNode falseCaseOperand = operator.getNextSibling();
      BigInteger decision = evalToInt(decisionOperand);
      return decision.compareTo(BigInteger.ZERO) != 0 ? decision : evalToInt(falseCaseOperand);
    }
  }

  private BigInteger evalPrimaryExpression(AstNode exprAst) {
    // case "( expression )"
    AstNode caseNode = exprAst.getFirstChild();
    return evalToInt(caseNode.getNextSibling());
  }

  private BigInteger evalDefinedExpression(AstNode exprAst) {
    AstNode child = exprAst.getFirstChild();

    if (exprAst.getNumberOfChildren() != 2) {
      child = child.getNextSibling();
    }

    String macroName = child.getNextSibling().getTokenValue();
    String value = preprocessor.valueOf(macroName);
    LOG.trace("expanding '{}' to '{}'", macroName, value);

    return value == null ? BigInteger.ZERO : BigInteger.ONE;
  }

  private BigInteger evalFunctionlikeMacro(AstNode exprAst) {
    String macroName = exprAst.getFirstChild().getTokenValue();
    List<Token> tokens = exprAst.getTokens();
    List<Token> restTokens = tokens.subList(1, tokens.size());
    String value = preprocessor.expandFunctionLikeMacro(macroName, restTokens);

    if (value == null || "".equals(value)) {
      LOG.error("Undefined functionlike macro '{}' assuming 0", macroName);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Token : {}", exprAst.toString());
      }
      return BigInteger.ZERO;
    }

    return evalToInt(value, exprAst);
  }

  private BigInteger evalHasIncludeExpression(AstNode exprAst) {
    return preprocessor.expandHasIncludeExpression(exprAst) ? BigInteger.ONE : BigInteger.ZERO;
  }

  public static BigInteger decode(String number) {

    // This function is only responsible for providing a string and a radix to BigInteger.
    // The lexer ensures that the number has a valid format.
    int radix = 10;
    int begin = 0;
    if (number.length() > 2) {
      if (number.charAt(0) == '0') {
        switch (number.charAt(1)) {
          case 'x':
          case 'X':
            radix = 16; // 0x...
            begin = 2;
            break;
          case 'b':
          case 'B':
            radix = 2; // 0b...
            begin = 2;
            break;
          default:
            radix = 8; // 0...
            break;
        }
      }
    }

    StringBuilder sb = new StringBuilder(number.length());
    boolean suffix = false;
    for (int index = begin; index < number.length() && !suffix; index++) {
      char c = number.charAt(index);
      switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':

        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':

        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
          sb.append(c);
          break;

        case '\'': // ignore digit separator
          break;

        default: // suffix
          suffix = true;
          break;
      }
    }

    return new BigInteger(sb.toString(), radix);
  }
}
