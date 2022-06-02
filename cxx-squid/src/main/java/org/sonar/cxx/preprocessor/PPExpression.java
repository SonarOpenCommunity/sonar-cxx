/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Grammar;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Parser;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.parser.CxxTokenType;

/**
 * Helper class to evaluate expressions of conditional preprocessor directives.
 */
final class PPExpression {

  private static final BigInteger UINT64_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);
  private static final Logger LOG = Loggers.get(PPExpression.class);
  private final Parser<Grammar> parser;
  private final CxxPreprocessor pp;
  private final Deque<String> macroEvaluationStack;

  PPExpression(CxxPreprocessor preprocessor) {
    parser = PPParser.create(PPGrammarImpl.constantExpression, preprocessor.getCharset());

    this.pp = preprocessor;
    macroEvaluationStack = new ArrayDeque<>();
  }

  boolean evaluate(String constantExpression) {
    return evalToBoolean(constantExpression, null);
  }

  boolean evaluate(AstNode constantExpression) {
    return evalToBoolean(constantExpression);
  }

  // ///////////////// Primitives //////////////////////
  private static BigInteger evalBool(String boolValue) {
    return "true".equalsIgnoreCase(boolValue) ? BigInteger.ONE : BigInteger.ZERO;
  }

  private static BigInteger evalNumber(String intValue) {
    // the if expressions aren't allowed to contain floats
    BigInteger number;
    try {
      number = PPNumber.decode(intValue);
    } catch (java.lang.NumberFormatException e) {
      LOG.warn("preprocessor cannot decode the number '{}' falling back to value '{}' instead",
               intValue, BigInteger.ONE);
      number = BigInteger.ONE;
    }

    return number;
  }

  private static BigInteger evalCharacter(String charValue) {
    // TODO: replace this simplification by something more sane
    return "'\0'".equals(charValue) ? BigInteger.ZERO : BigInteger.ONE;
  }

  @CheckForNull
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

  private BigInteger evalToInt(String constExpr, @Nullable AstNode exprAst) {
    AstNode constExprAst;
    try {
      constExprAst = parser.parse(constExpr);
    } catch (com.sonar.cxx.sslr.api.RecognitionException e) {
      if (exprAst != null) {
        LOG.warn("preprocessor error evaluating expression '{}' for token '{}', assuming 0",
                 constExpr, exprAst.getToken());
      } else {
        LOG.warn("preprocessor error evaluating expression '{}', assuming 0", constExpr);
      }
      return BigInteger.ZERO;
    }

    return evalToInt(constExprAst);
  }

  private BigInteger evalToInt(AstNode exprAst) {
    int noChildren = exprAst.getNumberOfChildren();
    if (noChildren == 0) {
      return evalLeaf(exprAst);
    } else if (noChildren == 1) {
      return evalOneChildAst(exprAst);
    }

    return evalComplexAst(exprAst);
  }

  private boolean evalToBoolean(AstNode exprAst) {
    return !BigInteger.ZERO.equals(evalToInt(exprAst));
  }

  private boolean evalToBoolean(String constExpr, @Nullable AstNode exprAst) {
    return !BigInteger.ZERO.equals(evalToInt(constExpr, exprAst));
  }

  private BigInteger evalLeaf(AstNode exprAst) {
    // Evaluation of leafs
    //
    var nodeType = exprAst.getType();

    if (nodeType.equals(CxxTokenType.NUMBER)) {
      return evalNumber(exprAst.getTokenValue());
    } else if (nodeType.equals(CxxTokenType.CHARACTER)) {
      return evalCharacter(exprAst.getTokenValue());
    } else if (nodeType.equals(GenericTokenType.IDENTIFIER)) {

      String id = exprAst.getTokenValue();
      if (macroEvaluationStack.contains(id)) {
        LOG.debug("preprocessor: self-referential macro '{}' detected;"
                    + " assume true; evaluation stack = ['{} <- {}']",
                  id, id, String.join(" <- ", macroEvaluationStack));
        return BigInteger.ONE;
      }
      String value = pp.valueOf(id);
      if (value == null) {
        return BigInteger.ZERO;
      }

      macroEvaluationStack.push(id);
      BigInteger expansion = evalToInt(value, exprAst);
      macroEvaluationStack.pop();
      return expansion;

    } else {
      throw new EvaluationException("Unknown expression type '" + nodeType + "'");
    }
  }

  private BigInteger evalOneChildAst(AstNode exprAst) {
    // Evaluation of booleans and 'pass-through's
    //
    var nodeType = exprAst.getType();
    if (nodeType.equals(PPGrammarImpl.bool)) {
      return evalBool(exprAst.getTokenValue());
    }
    return evalToInt(exprAst.getFirstChild());
  }

  private BigInteger evalComplexAst(AstNode exprAst) {

    // More complex expressions with more than one child
    //
    var type = exprAst.getType();
    if (type instanceof PPGrammarImpl) {
      switch ((PPGrammarImpl) type) {
        case unaryExpression:
          return evalUnaryExpression(exprAst);
        case conditionalExpression:
          return evalConditionalExpression(exprAst);
        case logicalOrExpression:
          return evalLogicalOrExpression(exprAst);
        case logicalAndExpression:
          return evalLogicalAndExpression(exprAst);
        case inclusiveOrExpression:
          return evalInclusiveOrExpression(exprAst);
        case exclusiveOrExpression:
          return evalExclusiveOrExpression(exprAst);
        case andExpression:
          return evalAndExpression(exprAst);
        case equalityExpression:
          return evalEqualityExpression(exprAst);
        case relationalExpression:
          return evalRelationalExpression(exprAst);
        case shiftExpression:
          return evalShiftExpression(exprAst);
        case additiveExpression:
          return evalAdditiveExpression(exprAst);
        case multiplicativeExpression:
          return evalMultiplicativeExpression(exprAst);
        case primaryExpression:
          return evalPrimaryExpression(exprAst);
        case definedExpression:
          return evalDefinedExpression(exprAst);
        case functionlikeMacro:
          return evalFunctionlikeMacro(exprAst);
        case hasIncludeExpression:
          return evalHasIncludeExpression(exprAst);
      }
    }

    LOG.error("preprocessor: unknown expression type '" + type + "' for token '" + exprAst.getToken()
                + "', assuming 0");
    return BigInteger.ZERO;
  }

  // ////////////// logical expressions ///////////////////////////
  private BigInteger evalLogicalOrExpression(AstNode exprAst) {
    var operand = exprAst.getFirstChild();
    boolean result = evalToBoolean(operand);

    while (!result && ((operand = getNextOperand(operand)) != null)) {
      result = evalToBoolean(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalLogicalAndExpression(AstNode exprAst) {
    var operand = exprAst.getFirstChild();
    boolean result = evalToBoolean(operand);

    while (result && ((operand = getNextOperand(operand)) != null)) {
      result = evalToBoolean(operand);
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalEqualityExpression(AstNode exprAst) {
    var lhs = exprAst.getFirstChild();
    var operator = lhs.getNextSibling();
    var rhs = operator.getNextSibling();
    var operatorType = operator.getType();

    boolean result;
    if (operatorType.equals(PPPunctuator.EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) == 0;
    } else if (operatorType.equals(PPPunctuator.NOT_EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) != 0;
    } else {
      throw new EvaluationException("Unknown equality operator '" + operatorType + "'");
    }

    while ((operator = rhs.getNextSibling()) != null) {
      operatorType = operator.getType();
      rhs = operator.getNextSibling();
      if (operatorType.equals(PPPunctuator.EQ)) {
        result = result == evalToBoolean(rhs);
      } else if (operatorType.equals(PPPunctuator.NOT_EQ)) {
        result = result != evalToBoolean(rhs);
      } else {
        throw new EvaluationException("Unknown equality operator '" + operatorType + "'");
      }
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalRelationalExpression(AstNode exprAst) {
    var lhs = exprAst.getFirstChild();
    var operator = lhs.getNextSibling();
    var rhs = operator.getNextSibling();
    var operatorType = operator.getType();

    boolean result;
    if (operatorType.equals(PPPunctuator.LT)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) < 0;
    } else if (operatorType.equals(PPPunctuator.GT)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) > 0;
    } else if (operatorType.equals(PPPunctuator.LT_EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) <= 0;
    } else if (operatorType.equals(PPPunctuator.GT_EQ)) {
      result = evalToInt(lhs).compareTo(evalToInt(rhs)) >= 0;
    } else {
      throw new EvaluationException("Unknown relational operator '" + operatorType + "'");
    }

    BigInteger resultAsInt;
    while ((operator = rhs.getNextSibling()) != null) {
      operatorType = operator.getType();
      rhs = operator.getNextSibling();

      resultAsInt = result ? BigInteger.ONE : BigInteger.ZERO;
      if (operatorType.equals(PPPunctuator.LT)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) < 0;
      } else if (operatorType.equals(PPPunctuator.GT)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) > 0;
      } else if (operatorType.equals(PPPunctuator.LT_EQ)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) <= 0;
      } else if (operatorType.equals(PPPunctuator.GT_EQ)) {
        result = resultAsInt.compareTo(evalToInt(rhs)) >= 0;
      } else {
        throw new EvaluationException("Unknown relational operator '" + operatorType + "'");
      }
    }

    return result ? BigInteger.ONE : BigInteger.ZERO;
  }

  // ///////////////// bitwise expressions ///////////////////////
  private BigInteger evalAndExpression(AstNode exprAst) {
    var operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.and(evalToInt(operand));
    }

    return result;
  }

  private BigInteger evalInclusiveOrExpression(AstNode exprAst) {
    var operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.or(evalToInt(operand));
    }

    return result;
  }

  private BigInteger evalExclusiveOrExpression(AstNode exprAst) {
    var operand = exprAst.getFirstChild();
    BigInteger result = evalToInt(operand);

    while ((operand = getNextOperand(operand)) != null) {
      result = result.xor(evalToInt(operand));
    }

    return result;
  }

  // ///////////////// other ... ///////////////////
  private BigInteger evalUnaryExpression(AstNode exprAst) {
    // only 'unary-operator cast-expression' production is allowed in #if-context

    var operator = exprAst.getFirstChild();
    var operand = operator.getNextSibling();
    var operatorType = operator.getFirstChild().getType();

    if (operatorType.equals(PPPunctuator.PLUS)) {
      return evalToInt(operand);
    } else if (operatorType.equals(PPPunctuator.MINUS)) {
      return evalToInt(operand).negate();
    } else if (operatorType.equals(PPPunctuator.NOT)) {
      boolean result = !evalToBoolean(operand);
      return result ? BigInteger.ONE : BigInteger.ZERO;
    } else if (operatorType.equals(PPPunctuator.BW_NOT)) {
      //todo: need more information (signed/unsigned, data type length) to invert bits in all cases correct
      return evalToInt(operand).not().and(UINT64_MAX);
    } else {
      throw new EvaluationException("Unknown unary operator  '" + operatorType + "'");
    }
  }

  private BigInteger evalShiftExpression(AstNode exprAst) {
    var rhs = exprAst.getFirstChild();
    AstNode operator;
    BigInteger result = evalToInt(rhs);

    while ((operator = rhs.getNextSibling()) != null) {
      var operatorType = operator.getType();
      rhs = operator.getNextSibling();

      if (operatorType.equals(PPPunctuator.BW_LSHIFT)) {
        result = result.shiftLeft(evalToInt(rhs).intValue()).and(UINT64_MAX);
      } else if (operatorType.equals(PPPunctuator.BW_RSHIFT)) {
        result = result.shiftRight(evalToInt(rhs).intValue());
      } else {
        throw new EvaluationException("Unknown shift operator '" + operatorType + "'");
      }
    }

    return result;
  }

  private BigInteger evalAdditiveExpression(AstNode exprAst) {
    var rhs = exprAst.getFirstChild();
    AstNode operator;
    BigInteger result = evalToInt(rhs);

    while ((operator = rhs.getNextSibling()) != null) {
      var operatorType = operator.getType();
      rhs = operator.getNextSibling();

      if (operatorType.equals(PPPunctuator.PLUS)) {
        result = result.add(evalToInt(rhs));
      } else if (operatorType.equals(PPPunctuator.MINUS)) {
        result = result.subtract(evalToInt(rhs));
      } else {
        throw new EvaluationException("Unknown additive operator '" + operatorType + "'");
      }
    }

    return result;
  }

  private BigInteger evalMultiplicativeExpression(AstNode exprAst) {
    var rhs = exprAst.getFirstChild();
    AstNode operator;
    BigInteger result = evalToInt(rhs);

    while ((operator = rhs.getNextSibling()) != null) {
      var operatorType = operator.getType();
      rhs = operator.getNextSibling();

      if (operatorType.equals(PPPunctuator.MUL)) {
        result = result.multiply(evalToInt(rhs));
      } else if (operatorType.equals(PPPunctuator.DIV)) {
        result = result.divide(evalToInt(rhs));
      } else if (operatorType.equals(PPPunctuator.MODULO)) {
        result = result.mod(evalToInt(rhs));
      } else {
        throw new EvaluationException("Unknown multiplicative operator '" + operatorType + "'");
      }
    }

    return result;
  }

  private BigInteger evalConditionalExpression(AstNode exprAst) {
    if (exprAst.getNumberOfChildren() == 5) {
      var decisionOperand = exprAst.getFirstChild();
      var operator = decisionOperand.getNextSibling();
      var trueCaseOperand = operator.getNextSibling();
      operator = trueCaseOperand.getNextSibling();
      var falseCaseOperand = operator.getNextSibling();
      return evalToBoolean(decisionOperand) ? evalToInt(trueCaseOperand) : evalToInt(falseCaseOperand);
    } else {
      var decisionOperand = exprAst.getFirstChild();
      var operator = decisionOperand.getNextSibling();
      operator = operator.getNextSibling();
      var falseCaseOperand = operator.getNextSibling();
      BigInteger decision = evalToInt(decisionOperand);
      return decision.compareTo(BigInteger.ZERO) != 0 ? decision : evalToInt(falseCaseOperand);
    }
  }

  private BigInteger evalPrimaryExpression(AstNode exprAst) {
    // case "( expression )"
    var caseNode = exprAst.getFirstChild();
    return evalToInt(caseNode.getNextSibling());
  }

  private BigInteger evalDefinedExpression(AstNode exprAst) {
    var child = exprAst.getFirstChild();

    if (exprAst.getNumberOfChildren() != 2) {
      child = child.getNextSibling();
    }

    String macroName = child.getNextSibling().getTokenValue();
    String value = pp.valueOf(macroName);
    return value == null ? BigInteger.ZERO : BigInteger.ONE;
  }

  private BigInteger evalFunctionlikeMacro(AstNode exprAst) {
    String macroName = exprAst.getFirstChild().getTokenValue();
    List<Token> tokens = exprAst.getTokens();
    List<Token> restTokens = tokens.subList(1, tokens.size());
    String value = pp.expandFunctionLikeMacro(macroName, restTokens);

    if (value == null || "".equals(value)) {
      LOG.error("preprocessor: undefined function-like macro '{}' assuming 0", macroName);
      return BigInteger.ZERO;
    }

    return evalToInt(value, exprAst);
  }

  private BigInteger evalHasIncludeExpression(AstNode exprAst) {
    return pp.expandHasIncludeExpression(exprAst) ? BigInteger.ONE : BigInteger.ZERO;
  }

}
