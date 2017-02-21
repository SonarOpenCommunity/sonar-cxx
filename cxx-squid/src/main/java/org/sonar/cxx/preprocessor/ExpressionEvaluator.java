/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
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

import java.math.BigInteger;
import java.util.List;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.CxxConfiguration;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import org.sonar.cxx.api.CxxTokenType;
import com.sonar.sslr.api.GenericTokenType;
import org.sonar.cxx.api.CppPunctuator;

public final class ExpressionEvaluator {

  private static final BigInteger UINT64_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);
  public static final Logger LOG = Loggers.get(ExpressionEvaluator.class);

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

  private BigInteger evalToInt(String constExpr, AstNode exprAst) {
    AstNode constExprAst = null;
    try {
      constExprAst = parser.parse(constExpr);
    } catch (com.sonar.sslr.api.RecognitionException re) {
      if (exprAst != null) {
        LOG.warn("Error evaluating expression '{}' for AstExp '{}', assuming 0", constExpr, exprAst.getToken());
      } else {
        LOG.warn("Error evaluating expression '{}', assuming 0", constExpr);
      }

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

    if (CxxTokenType.NUMBER == nodeType) {
      return evalNumber(exprAst.getTokenValue());
    } else if (CxxTokenType.CHARACTER == nodeType) {
      return evalCharacter(exprAst.getTokenValue());
    } else if (GenericTokenType.IDENTIFIER == nodeType) {
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
    if (CppGrammar.bool == nodeType) {
      return evalBool(exprAst.getTokenValue());
    }
    return evalToInt(exprAst.getFirstChild());
  }

  private BigInteger evalComplexAst(AstNode exprAst) {

    // More complex expressions with more than one child
    //
    AstNodeType nodeType = exprAst.getType();
    if (CppGrammar.unaryExpression == nodeType) {
      return evalUnaryExpression(exprAst);
    } else if (CppGrammar.conditionalExpression == nodeType) {
      return evalConditionalExpression(exprAst);
    } else if (CppGrammar.logicalOrExpression == nodeType) {
      return evalLogicalOrExpression(exprAst);
    } else if (CppGrammar.logicalAndExpression == nodeType) {
      return evalLogicalAndExpression(exprAst);
    } else if (CppGrammar.inclusiveOrExpression == nodeType) {
      return evalInclusiveOrExpression(exprAst);
    } else if (CppGrammar.exclusiveOrExpression == nodeType) {
      return evalExclusiveOrExpression(exprAst);
    } else if (CppGrammar.andExpression == nodeType) {
      return evalAndExpression(exprAst);
    } else if (CppGrammar.equalityExpression == nodeType) {
      return evalEqualityExpression(exprAst);
    } else if (CppGrammar.relationalExpression == nodeType) {
      return evalRelationalExpression(exprAst);
    } else if (CppGrammar.shiftExpression == nodeType) {
      return evalShiftExpression(exprAst);
    } else if (CppGrammar.additiveExpression == nodeType) {
      return evalAdditiveExpression(exprAst);
    } else if (CppGrammar.multiplicativeExpression == nodeType) {
      return evalMultiplicativeExpression(exprAst);
    } else if (CppGrammar.primaryExpression == nodeType) {
      return evalPrimaryExpression(exprAst);
    } else if (CppGrammar.definedExpression == nodeType) {
      return evalDefinedExpression(exprAst);
    } else if (CppGrammar.functionlikeMacro == nodeType) {
      return evalFunctionlikeMacro(exprAst);
    } else {
      LOG.error("'evalComplexAst' Unknown expression type '" + nodeType + "' for AstExt '" + exprAst.getToken() + "', assuming 0");
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

  private static AstNode getNextOperand(AstNode node) {
    if (node != null) {
      node = node.getNextSibling();
      if (node != null) {
        node = node.getNextSibling();
      }
    }
    return node;
  }

  // ////////////// logical expressions ///////////////////////////
  private BigInteger evalLogicalOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    boolean result = eval(operand);

    while ((result != true) && ((operand = getNextOperand(operand)) != null)) {
      result = result || eval(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalLogicalAndExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    boolean result = eval(operand);

    while ((result != false) && ((operand = getNextOperand(operand)) != null)) {
      result = result && eval(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalEqualityExpression(AstNode exprAst) {
    AstNode lhs = exprAst.getFirstChild();
    AstNode operator = lhs.getNextSibling();
    AstNode rhs = operator.getNextSibling();
    AstNodeType operatorType = operator.getType();

    boolean result;
    if (CppPunctuator.EQ == operatorType) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) == 0;
    } else if (CppPunctuator.NOT_EQ == operatorType) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) != 0;
    } else {
      throw new EvaluationException("Unknown equality operator '" + operatorType.toString() + "'");
    }

    while ((operator = rhs.getNextSibling()) != null) {
      operatorType = operator.getType();
      rhs = operator.getNextSibling();
      if (CppPunctuator.EQ == operatorType) {
        result = result == eval(rhs);
      } else if (CppPunctuator.NOT_EQ == operatorType) {
        result = result != eval(rhs);
      } else {
        throw new EvaluationException("Unknown equality operator '" + operatorType.toString() + "'");
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
    if (CppPunctuator.LT == operatorType) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) < 0;
    } else if (CppPunctuator.GT == operatorType) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) > 0;
    } else if (CppPunctuator.LT_EQ == operatorType) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) <= 0;
    } else if (CppPunctuator.GT_EQ == operatorType) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) >= 0;
    } else {
      throw new EvaluationException("Unknown relational operator '" + operatorType.toString() + "'");
    }

    BigInteger resultAsInt;
    while ((operator = rhs.getNextSibling()) != null) {
      operatorType = operator.getType();
      rhs = operator.getNextSibling();

      resultAsInt = result ? BigInteger.ONE : BigInteger.ZERO;
      if (CppPunctuator.LT == operatorType) {
        result = resultAsInt.compareTo(evalToInt(rhs)) < 0;
      } else if (CppPunctuator.GT == operatorType) {
        result = resultAsInt.compareTo(evalToInt(rhs)) > 0;
      } else if (CppPunctuator.LT_EQ == operatorType) {
        result = resultAsInt.compareTo(evalToInt(rhs)) <= 0;
      } else if (CppPunctuator.GT_EQ == operatorType) {
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

    if (CppPunctuator.PLUS == operatorType) {
      return evalToInt(operand);
    } else if (CppPunctuator.MINUS == operatorType) {
      return evalToInt(operand).negate();
    } else if (CppPunctuator.NOT == operatorType) {
      boolean result = !eval(operand);
      return result ? BigInteger.ONE : BigInteger.ZERO;
    } else if (CppPunctuator.BW_NOT == operatorType) {
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

      if (CppPunctuator.BW_LSHIFT == operatorType) {
        result = result.shiftLeft(evalToInt(rhs).intValue()).and(UINT64_MAX);
      } else if (CppPunctuator.BW_RSHIFT == operatorType) {
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

      if (CppPunctuator.PLUS == operatorType) {
        result = result.add(evalToInt(rhs));
      } else if (CppPunctuator.MINUS == operatorType) {
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

      if (CppPunctuator.MUL == operatorType) {
        result = result.multiply(evalToInt(rhs));
      } else if (CppPunctuator.DIV == operatorType) {
        result = result.divide(evalToInt(rhs));
      } else if (CppPunctuator.MODULO == operatorType) {
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
      return BigInteger.ZERO;
    }

    return evalToInt(value, exprAst);
  }

  public static BigInteger decode(String number) {

    // This fuction is only responsible for providing a string and a radix to BigInteger.
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
