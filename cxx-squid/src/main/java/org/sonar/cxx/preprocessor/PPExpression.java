/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.parser.CxxTokenType;

/**
 * Helper class to evaluate expressions of conditional expression preprocessor directives.
 *
 * <pre>{@code
 * if-group:
 *   # if constant-expression new-line groupopt
 *
 * constant-expression:
 *   conditional-expression
 *
 * multiplicative-expression:
 *   pm-expression
 *   multiplicative-expression * pm-expression
 *   multiplicative-expression / pm-expression
 *   multiplicative-expression % pm-expression
 *
 * additive-expression:
 *   multiplicative-expression
 *   additive-expression + multiplicative-expression
 *   additive-expression - multiplicative-expression
 *
 * shift-expression:
 *   additive-expression
 *   shift-expression << additive-expression
 *   shift-expression >> additive-expression
 *
 * compare-expression:
 *   shift-expression
 *   compare-expression <=> shift-expression
 *
 * relational-expression:
 *   compare-expression
 *   relational-expression < compare-expression
 *   relational-expression > compare-expression
 *   relational-expression <= compare-expression
 *   relational-expression >= compare-expression
 *
 * equality-expression:
 *   relational-expression
 *   equality-expression == relational-expression
 *   equality-expression != relational-expression
 *
 * and-expression:
 *   equality-expression
 *   and-expression & equality-expression
 *
 * exclusive-or-expression:
 *   and-expression
 *   exclusive-or-expression ^ and-expression
 *
 * inclusive-or-expression:
 *   exclusive-or-expression
 *   inclusive-or-expression | exclusive-or-expression
 *
 * logical-and-expression:
 *   inclusive-or-expression
 *   logical-and-expression && inclusive-or-expression
 *
 * logical-or-expression:
 *   logical-and-expression
 *   logical-or-expression || logical-and-expression
 *
 * conditional-expression:
 *   logical-or-expression
 *   logical-or-expression ? expression : assignment-expression
 * }</pre>
 */
final class PPExpression {

  private static final Logger LOG = Loggers.get(PPExpression.class);

  private static final BigInteger UINT64_MAX = new BigInteger("FFFFFFFFFFFFFFFF", 16);

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
    return booleanToBigInteger("true".equalsIgnoreCase(boolValue));
  }

  private static BigInteger evalNumber(String intValue) {
    // the if expressions aren't allowed to contain floats
    BigInteger number;
    try {
      number = PPNumber.decodeString(intValue);
    } catch (java.lang.NumberFormatException e) {
      LOG.warn("preprocessor cannot decode the number '{}' falling back to value '{}' instead",
               intValue, BigInteger.ONE);
      number = BigInteger.ONE;
    }

    return number;
  }

  private static BigInteger evalCharacter(String charValue) {
    charValue = charValue.substring(1, charValue.length() - 1); // remove '
    return PPNumber.decodeCharacter(charValue);
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
    switch (exprAst.getNumberOfChildren()) {
      case 0:
        return evalLeaf(exprAst);
      case 1:
        return evalOneChildAst(exprAst);
      default:
        return evalComplexAst(exprAst);
    }
  }

  private boolean evalToBoolean(AstNode exprAst) {
    return !BigInteger.ZERO.equals(evalToInt(exprAst));
  }

  private boolean evalToBoolean(String constExpr, @Nullable AstNode exprAst) {
    return !BigInteger.ZERO.equals(evalToInt(constExpr, exprAst));
  }

  private static BigInteger booleanToBigInteger(boolean value) {
    return value ? BigInteger.ONE : BigInteger.ZERO;
  }

  private BigInteger evalLeaf(AstNode exprAst) {
    // Evaluation of leafs
    //
    BigInteger result = BigInteger.ZERO;
    var type = exprAst.getType();

    if (CxxTokenType.NUMBER.equals(type)) {
      result = evalNumber(exprAst.getTokenValue());
    } else if (CxxTokenType.CHARACTER.equals(type)) {
      result = evalCharacter(exprAst.getTokenValue());
    } else if (GenericTokenType.IDENTIFIER.equals(type)) {

      String id = exprAst.getTokenValue();
      if (!macroEvaluationStack.contains(id)) {
        PPMacro macro = pp.getMacro(id);
        if (macro != null) {
          if (macro.replacementList.size() == 1 && macro.replacementList.get(0).getValue().equals(macro.identifier)) {
            // special case, self-referencing macro, e.g. __has_include=__has_include
            result = BigInteger.ONE;
          } else {
            macroEvaluationStack.push(id);
            result = evalToInt(TokenUtils.merge(macro.replacementList), exprAst);
            macroEvaluationStack.pop();
          }
        }
      } else {
        LOG.debug("preprocessor: self-referential macro '{}' detected;"
                    + " assume true; evaluation stack = ['{} <- {}']",
                  id, id, String.join(" <- ", macroEvaluationStack));
        result = BigInteger.ONE;
      }
    } else {
      throw new EvaluationException("Unknown expression type '" + type + "'");
    }

    return result;
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

  @SuppressWarnings({"java:S131", "java:S1541", "java:S1142"})
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

    return booleanToBigInteger(result);
  }

  private BigInteger evalLogicalAndExpression(AstNode exprAst) {
    var operand = exprAst.getFirstChild();
    boolean result = evalToBoolean(operand);

    while (result && ((operand = getNextOperand(operand)) != null)) {
      result = evalToBoolean(operand);
    }

    return booleanToBigInteger(result);
  }

  private static BigInteger evalEqualityExpression(BigInteger lhs, PPPunctuator type, BigInteger rhs) {
    boolean result;
    switch (type) {
      case EQ:
        result = lhs.compareTo(rhs) == 0;
        break;
      case NOT_EQ:
        result = lhs.compareTo(rhs) != 0;
        break;
      default:
        throw new EvaluationException("Unknown equality operator '" + type + "'");
    }

    return booleanToBigInteger(result);
  }

  private BigInteger evalEqualityExpression(AstNode exprAst) {
    var lhs = exprAst.getFirstChild();
    var next = lhs.getNextSibling();
    var rhs = next.getNextSibling();

    BigInteger result = evalEqualityExpression(evalToInt(lhs),
                                               (PPPunctuator) next.getType(),
                                               evalToInt(rhs));

    while ((next = rhs.getNextSibling()) != null) {
      rhs = next.getNextSibling();
      result = evalEqualityExpression(result,
                                      (PPPunctuator) next.getType(),
                                      booleanToBigInteger(evalToBoolean(rhs)));
    }

    return result;
  }

  private static BigInteger evalRelationalExpression(BigInteger lhs, PPPunctuator type, BigInteger rhs) {
    boolean result;
    switch (type) {
      case LT:
        result = lhs.compareTo(rhs) < 0;
        break;
      case GT:
        result = lhs.compareTo(rhs) > 0;
        break;
      case LT_EQ:
        result = lhs.compareTo(rhs) <= 0;
        break;
      case GT_EQ:
        result = lhs.compareTo(rhs) >= 0;
        break;
      default:
        throw new EvaluationException("Unknown relational operator '" + type + "'");
    }
    return booleanToBigInteger(result);
  }

  private BigInteger evalRelationalExpression(AstNode exprAst) {
    var lhs = exprAst.getFirstChild();
    var next = lhs.getNextSibling();
    var rhs = next.getNextSibling();

    BigInteger result = evalRelationalExpression(evalToInt(lhs),
                                                 (PPPunctuator) next.getType(),
                                                 evalToInt(rhs));

    while ((next = rhs.getNextSibling()) != null) {
      rhs = next.getNextSibling();
      result = evalRelationalExpression(result,
                                        (PPPunctuator) next.getType(),
                                        evalToInt(rhs));
    }

    return result;
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
  @SuppressWarnings({"java:S1142"})
  private BigInteger evalUnaryExpression(AstNode exprAst) {
    // only 'unary-next cast-expression' production is allowed in #if-context

    var operator = exprAst.getFirstChild();
    var operand = operator.getNextSibling();
    var type = operator.getFirstChild().getType();

    switch ((PPPunctuator) type) {
      case PLUS:
        return evalToInt(operand);
      case MINUS:
        return evalToInt(operand).negate();
      case NOT:
        return evalToBoolean(operand) ? BigInteger.ZERO : BigInteger.ONE;
      case BW_NOT:
        // need more information (signed/unsigned, data type length) to invert bits in all cases correct
        return evalToInt(operand).not().and(UINT64_MAX);
      default:
        throw new EvaluationException("Unknown unary operator  '" + type + "'");
    }
  }

  private BigInteger evalShiftExpression(AstNode exprAst) {
    var rhs = exprAst.getFirstChild();
    BigInteger result = evalToInt(rhs);
    AstNode operator;

    while ((operator = rhs.getNextSibling()) != null) {
      var type = operator.getType();
      rhs = operator.getNextSibling();

      switch ((PPPunctuator) type) {
        case BW_LSHIFT:
          result = result.shiftLeft(evalToInt(rhs).intValue()).and(UINT64_MAX);
          break;
        case BW_RSHIFT:
          result = result.shiftRight(evalToInt(rhs).intValue());
          break;
        default:
          throw new EvaluationException("Unknown shift operator '" + type + "'");
      }
    }

    return result;
  }

  private BigInteger evalAdditiveExpression(AstNode exprAst) {
    var rhs = exprAst.getFirstChild();
    BigInteger result = evalToInt(rhs);
    AstNode operator;

    while ((operator = rhs.getNextSibling()) != null) {
      var type = operator.getType();
      rhs = operator.getNextSibling();

      switch ((PPPunctuator) type) {
        case PLUS:
          result = result.add(evalToInt(rhs));
          break;
        case MINUS:
          result = result.subtract(evalToInt(rhs));
          break;
        default:
          throw new EvaluationException("Unknown additive operator '" + type + "'");
      }
    }

    return result;
  }

  private BigInteger evalMultiplicativeExpression(AstNode exprAst) {
    var rhs = exprAst.getFirstChild();
    BigInteger result = evalToInt(rhs);
    AstNode operator;

    while ((operator = rhs.getNextSibling()) != null) {
      var type = operator.getType();
      rhs = operator.getNextSibling();

      switch ((PPPunctuator) type) {
        case MUL:
          result = result.multiply(evalToInt(rhs));
          break;
        case DIV:
          result = result.divide(evalToInt(rhs));
          break;
        case MODULO:
          result = result.mod(evalToInt(rhs));
          break;
        default:
          throw new EvaluationException("Unknown multiplicative operator '" + type + "'");
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
    PPMacro macro = pp.getMacro(macroName);
    if (macro != null) {
      return BigInteger.ONE;
    }

    return BigInteger.ZERO;
  }

  private BigInteger evalFunctionlikeMacro(AstNode exprAst) {
    String macroName = exprAst.getFirstChild().getTokenValue();
    List<Token> tokens = exprAst.getTokens();
    List<Token> restTokens = tokens.subList(1, tokens.size());
    String value = "";

    PPMacro macro = pp.getMacro(macroName);
    if (macro != null) {
      var expansion = new ArrayList<Token>();
      pp.replace().replaceFunctionLikeMacro(macro, restTokens, expansion); // todo, remove replace()
      value = TokenUtils.merge(expansion);
    }

    if ("".equals(value)) {
      LOG.error("preprocessor: undefined function-like macro '{}' assuming 0", macroName);
      return BigInteger.ZERO;
    }

    return evalToInt(value, exprAst);
  }

  private BigInteger evalHasIncludeExpression(AstNode exprAst) {
    return pp.include().searchFile(exprAst) != null ? BigInteger.ONE : BigInteger.ZERO; // todo remove include()
  }

}
