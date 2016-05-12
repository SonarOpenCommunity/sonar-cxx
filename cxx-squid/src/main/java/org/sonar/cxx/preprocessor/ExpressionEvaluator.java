/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011-2016 SonarOpenCommunity
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.CxxConfiguration;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import org.sonar.cxx.preprocessor.CxxPreprocessor.MismatchException;

public final class ExpressionEvaluator {

  private static final BigInteger UINT64_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);
  private static final String HEX_REGEX = "0[xX]([0-9A-Fa-f]+)(ui64)?";
  public static final Logger LOG = LoggerFactory.getLogger("Evaluator");

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
    String nodeType = exprAst.getName();
    if ("NUMBER".equals(nodeType)) {
      return evalNumber(exprAst.getTokenValue());
    } else if ("CHARACTER".equals(nodeType)) {
      return evalCharacter(exprAst.getTokenValue());
    } else if ("IDENTIFIER".equals(nodeType)) {
      String value = preprocessor.valueOf(exprAst.getTokenValue());
      return value == null ? BigInteger.ZERO : evalToInt(value, exprAst);
    } else {
      throw new EvaluationException("Unknown expression type '" + nodeType + "'");
    }
  }

  private BigInteger evalOneChildAst(AstNode exprAst) {
    // Evaluation of booleans and 'pass-through's
    //
    String nodeType = exprAst.getName();
    if ("bool".equals(nodeType)) {
      return evalBool(exprAst.getTokenValue());
    }
    return evalToInt(exprAst.getFirstChild());
  }

  private BigInteger evalComplexAst(AstNode exprAst) {
    // More complex expressions with more than one child
    //
    String nodeType = exprAst.getName();
    if ("unaryExpression".equals(nodeType)) {
      return evalUnaryExpression(exprAst);
    } else if ("conditionalExpression".equals(nodeType)) {
      return evalConditionalExpression(exprAst);
    } else if ("logicalOrExpression".equals(nodeType)) {
      return evalLogicalOrExpression(exprAst);
    } else if ("logicalAndExpression".equals(nodeType)) {
      return evalLogicalAndExpression(exprAst);
    } else if ("inclusiveOrExpression".equals(nodeType)) {
      return evalInclusiveOrExpression(exprAst);
    } else if ("exclusiveOrExpression".equals(nodeType)) {
      return evalExclusiveOrExpression(exprAst);
    } else if ("andExpression".equals(nodeType)) {
      return evalAndExpression(exprAst);
    } else if ("equalityExpression".equals(nodeType)) {
      return evalEqualityExpression(exprAst);
    } else if ("relationalExpression".equals(nodeType)) {
      return evalRelationalExpression(exprAst);
    } else if ("shiftExpression".equals(nodeType)) {
      return evalShiftExpression(exprAst);
    } else if ("additiveExpression".equals(nodeType)) {
      return evalAdditiveExpression(exprAst);
    } else if ("multiplicativeExpression".equals(nodeType)) {
      return evalMultiplicativeExpression(exprAst);
    } else if ("primaryExpression".equals(nodeType)) {
      return evalPrimaryExpression(exprAst);
    } else if ("definedExpression".equals(nodeType)) {
      return evalDefinedExpression(exprAst);
    } else if ("functionlikeMacro".equals(nodeType)) {
      return evalFunctionlikeMacro(exprAst);
    } else {
      LOG.error("'evalComplexAst' Unknown expression type '" + nodeType + "' for AstExt '" + exprAst.getToken() + "', assuming 0");
      return BigInteger.ZERO;
    }
  }

  // ///////////////// Primitives //////////////////////
  BigInteger evalBool(String boolValue) {
    return "true".equalsIgnoreCase(boolValue) ? BigInteger.ONE : BigInteger.ZERO;
  }

  BigInteger evalNumber(String intValue) {
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

  BigInteger evalCharacter(String charValue) {
    // TODO: replace this simplification by something more sane
    return "'\0'".equals(charValue) ? BigInteger.ZERO : BigInteger.ONE;
  }

  AstNode getNextOperand(AstNode node) {
    if( node != null) {
      node = node.getNextSibling();
      if( node != null) {
         node = node.getNextSibling();
      }
    }
    return node;
  }
  
  // ////////////// logical expressions ///////////////////////////
  BigInteger evalLogicalOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    boolean result = eval(operand);

    while ((result != true) && ((operand = getNextOperand(operand)) != null)) {
      result = result || eval(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  BigInteger evalLogicalAndExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    boolean result = eval(operand);

    while ((result != false) && ((operand = getNextOperand(operand)) != null)) {
      result = result && eval(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  BigInteger evalEqualityExpression(AstNode exprAst) {
    AstNode lhs = exprAst.getFirstChild();
    AstNode op = lhs.getNextSibling();
    AstNode rhs = op.getNextSibling();
    String operator = op.getTokenValue();

    boolean result;
    if ("==".equals(operator)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) == 0;
    } else if ("!=".equals(operator)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) != 0;
    } else {
      throw new EvaluationException("Unknown equality operator '" + operator + "'");
    }

    while ((op = rhs.getNextSibling()) != null) {
      operator = op.getTokenValue();
      rhs = op.getNextSibling();
      if ("==".equals(operator)) {
        result = result == eval(rhs);
      } else if ("!=".equals(operator)) {
        result = result != eval(rhs);
      } else {
        throw new EvaluationException("Unknown equality operator '" + operator + "'");
      }
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  BigInteger evalRelationalExpression(AstNode exprAst) {
    AstNode lhs = exprAst.getFirstChild();
    AstNode op = lhs.getNextSibling();
    AstNode rhs = op.getNextSibling();
    String operator = op.getTokenValue();

    boolean result;
    if ("<".equals(operator)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) < 0;
    } else if (">".equals(operator)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) > 0;
    } else if ("<=".equals(operator)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) <= 0;
    } else if (">=".equals(operator)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) >= 0;
    } else {
      throw new EvaluationException("Unknown relational operator '" + operator + "'");
    }

    BigInteger resultAsInt;
    while ((op = rhs.getNextSibling()) != null) {
      operator = op.getTokenValue();
      rhs = op.getNextSibling();

      resultAsInt = result ? BigInteger.ONE : BigInteger.ZERO;
      if ("<".equals(operator)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) < 0;
      } else if (">".equals(operator)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) > 0;
      } else if ("<=".equals(operator)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) <= 0;
      } else if (">=".equals(operator)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) >= 0;
      } else {
        throw new EvaluationException("Unknown relational operator '" + operator + "'");
      }
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  // ///////////////// bitwise expressions ///////////////////////
  BigInteger evalAndExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.and(evalToInt(operand));
    }

    return result;
  }

  BigInteger evalInclusiveOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.or(evalToInt(operand));
    }

    return result;
  }

  BigInteger evalExclusiveOrExpression(AstNode exprAst) {
    AstNode operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);
    
    while ((operand = getNextOperand(operand)) != null) {
      result = result.xor(evalToInt(operand));
    }

    return result;
  }

  // ///////////////// other ... ///////////////////
  BigInteger evalUnaryExpression(AstNode exprAst) {
    // only 'unary-operator cast-expression' production is allowed in #if-context

    AstNode op = exprAst.getFirstChild();
    AstNode operand = op.getNextSibling();
    String operator = op.getTokenValue();
    
    if ("+".equals(operator)) {
      return evalToInt(operand);
    } else if ("-".equals(operator)) {
      return evalToInt(operand).negate();
    } else if ("!".equals(operator)) {
      boolean result = !eval(operand);
      return result ? BigInteger.ONE : BigInteger.ZERO;
    } else if ("~".equals(operator)) {
      //todo: need more information (signed/unsigned, data type length) to invert bits in all cases correct
      return evalToInt(operand).not().and(UINT64_MAX);
    } else {
      throw new EvaluationException("Unknown unary operator  '" + operator + "'");
    }
  }

  BigInteger evalShiftExpression(AstNode exprAst) {
    AstNode rhs = exprAst.getFirstChild();
    AstNode op;
    BigInteger result = evalToInt(rhs);

    while ((op = rhs.getNextSibling()) != null) {
      String operator = op.getTokenValue();
      rhs = op.getNextSibling();

      if ("<<".equals(operator)) {
        result = result.shiftLeft(evalToInt(rhs).intValue()).and(UINT64_MAX);
      } else if (">>".equals(operator)) {
        result = result.shiftRight(evalToInt(rhs).intValue());
      } else {
        throw new EvaluationException("Unknown shift operator '" + operator + "'");
      }
    }

    return result;
  }

  BigInteger evalAdditiveExpression(AstNode exprAst) {
    AstNode rhs = exprAst.getFirstChild();
    AstNode op;
    BigInteger result = evalToInt(rhs);

    while ((op = rhs.getNextSibling()) != null) {
      String operator = op.getTokenValue();
      rhs = op.getNextSibling();

      if ("+".equals(operator)) {
        result = result.add(evalToInt(rhs));
      } else if ("-".equals(operator)) {
        result = result.subtract(evalToInt(rhs));
      } else {
        throw new EvaluationException("Unknown additive operator '" + operator + "'");
      }
    }

    return result;
  }

  BigInteger evalMultiplicativeExpression(AstNode exprAst) {
    AstNode rhs = exprAst.getFirstChild();
    AstNode op;
    BigInteger result = evalToInt(rhs);

    while ((op = rhs.getNextSibling()) != null) {
      String operator = op.getTokenValue();
      rhs = op.getNextSibling();

      if ("*".equals(operator)) {
        result = result.multiply(evalToInt(rhs));
      } else if ("/".equals(operator)) {
        result = result.divide(evalToInt(rhs));
      } else if ("%".equals(operator)) {
        result = result.mod(evalToInt(rhs));
      } else {
        throw new EvaluationException("Unknown multiplicative operator '" + operator + "'");
      }
    }

    return result;
  }

  BigInteger evalConditionalExpression(AstNode exprAst) {
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

  BigInteger evalPrimaryExpression(AstNode exprAst) {
    // case "( expression )"
    AstNode caseNode = exprAst.getFirstChild();
    return evalToInt(caseNode.getNextSibling());
  }

  BigInteger evalDefinedExpression(AstNode exprAst) {
    AstNode child = exprAst.getFirstChild();

    if (exprAst.getNumberOfChildren() != 2) {
      child = child.getNextSibling();
    }

    String macroName = child.getNextSibling().getTokenValue();
    String value = preprocessor.valueOf(macroName);
    LOG.trace("expanding '{}' to '{}'", macroName, value);

    return value == null ? BigInteger.ZERO : BigInteger.ONE;
  }

  BigInteger evalFunctionlikeMacro(AstNode exprAst) {
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

  String stripSuffix(String number) {
    return number.replaceAll("[LlUu]", "");
  }

  BigInteger decode(String number) {
    int radix = 10;
    if (number.length() > 2) {
      if (number.charAt(0) == '0') {
        radix = 8; // 0...
        try {
          Pattern p = Pattern.compile(HEX_REGEX);
          Matcher m = p.matcher(number);
          if (m.find()) {
            radix = 16;
            number = m.group(1);
          }
        } catch (java.lang.IllegalStateException e) {
          // ignore "No match found"
        }
      }
    }

    return new BigInteger(stripSuffix(number), radix);
  }
}
