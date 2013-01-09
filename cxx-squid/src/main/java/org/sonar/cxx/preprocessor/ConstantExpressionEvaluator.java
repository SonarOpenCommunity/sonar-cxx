/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.cxx.preprocessor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.cxx.CxxConfiguration;

public final class ConstantExpressionEvaluator {
  public static final Logger LOG = LoggerFactory.getLogger("ConstantExpressionEvaluator");

  private Parser<CppGrammar> parser;
  private CxxPreprocessor preprocessor;

  public ConstantExpressionEvaluator(CxxConfiguration conf, CxxPreprocessor preprocessor){
    parser = CppParser.createConstantExpressionParser(conf);

    this.preprocessor = preprocessor;
  }

  public boolean eval(String constExpr){
    return _eval(constExpr) != 0;
  }

  public int _eval(String constExpr){
    AstNode constExprAst = parser.parse(constExpr);
    return eval(constExprAst);
  }

  private int eval(AstNode exprAst) {
    String nodeType = exprAst.getName();
    int noChildren = exprAst.getNumberOfChildren();

    LOG.debug("Evaluating expression: {}", exprAst);

    if(noChildren == 0){
      // Evaluation of leafs
      //
      if("NUMBER".equals(nodeType)){
        return evalNumber(exprAst.getTokenValue());
      } else if("CHARACTER".equals(nodeType)){
        return evalCharacter(exprAst.getTokenValue());
      } else if("IDENTIFIER".equals(nodeType)){
        String value = preprocessor.valueOf(exprAst.getTokenValue());
        return value == null ? 0 : _eval(value);
      } else {
        throw new EvaluationException("Unknown expression type '" + nodeType + "'");
      }
    } else if (noChildren == 1){
      // Evaluation of booleans and 'pass-through's
      //
      if("bool".equals(nodeType)){
        return evalBool(exprAst.getTokenValue());
      }
      return eval(exprAst.getChild(0));
    } else {
      // More complex expressions with more than one child
      //
      if("unary_expression".equals(nodeType)){
        return evalUnaryExpression(exprAst);
      } else if("conditional_expression".equals(nodeType)){
        return evalConditionalExpression(exprAst);
      } else if("logical_or_expression".equals(nodeType)){
        return evalLogicalOrExpression(exprAst);
      } else if("logical_and_expression".equals(nodeType)){
        return evalLogicalAndExpression(exprAst);
      } else if("inclusive_or_expression".equals(nodeType)){
        return evalInclusiveOrExpression(exprAst);
      } else if("exclusive_or_expression".equals(nodeType)){
        return evalExclusiveOrExpression(exprAst);
      } else if("and_expression".equals(nodeType)){
        return evalAndExpression(exprAst);
      } else if("equality_expression".equals(nodeType)){
        return evalEqualityExpression(exprAst);
      } else if("relational_expression".equals(nodeType)){
        return evalRelationalExpression(exprAst);
      } else if("shift_expression".equals(nodeType)){
        return evalShiftExpression(exprAst);
      } else if("additive_expression".equals(nodeType)){
        return evalAdditiveExpression(exprAst);
      } else if("multiplicative_expression".equals(nodeType)){
        return evalMultiplicativeExpression(exprAst);
      } else if("primary_expression".equals(nodeType)){
        return evalPrimaryExpression(exprAst);
      } else if("defined_expression".equals(nodeType)){
        return evalDefinedExpression(exprAst);
      } else if("functionlike_macro".equals(nodeType)){
        return evalFunctionlikeMacro(exprAst);
      } else {
        throw new EvaluationException("Unknown expression type '" + nodeType + "'");
      }
    }
  }


  /////////////////// Primitives //////////////////////
  int evalBool(String boolValue){
    return boolValue.toLowerCase().equals("true") ? 1 : 0;
  }

  int evalNumber(String intValue){
    // the if expressions arent allowed to contain floats

    return Integer.decode(stripSuffix(intValue)).intValue();
  }

  int evalCharacter(String charValue){
    // TODO: replace this simplification by something more sane
    return charValue.equals("'\0'") ? 0 : 1;
  }


  //////////////// logical expressions ///////////////////////////
  int evalLogicalOrExpression(AstNode exprAst){
    boolean result = (eval(exprAst.getChild(0)) != 0) || (eval(exprAst.getChild(2)) != 0);
    return result ? 1 : 0;
  }

  int evalLogicalAndExpression(AstNode exprAst){
    boolean result = (eval(exprAst.getChild(0)) != 0) && (eval(exprAst.getChild(2)) != 0);
    return result ? 1 : 0;
  }

  int evalEqualityExpression(AstNode exprAst){
    String operator = exprAst.getChild(1).getTokenValue();
    AstNode lhs = exprAst.getChild(0);
    AstNode rhs = exprAst.getChild(2);
    boolean result;
    if(operator.equals("==")){
      result = eval(lhs) == eval(rhs);
    } else if(operator.equals("!=")){
      result = eval(lhs) != eval(rhs);
    } else {
      throw new EvaluationException("Unknown equality operator '" + operator + "'");
    }

    return result ? 1 : 0;
  }

  int evalRelationalExpression(AstNode exprAst){
    String operator = exprAst.getChild(1).getTokenValue();
    AstNode lhs = exprAst.getChild(0);
    AstNode rhs = exprAst.getChild(2);
    boolean result;
    if(operator.equals("<")){
      result = eval(lhs) < eval(rhs);
    } else if(operator.equals(">")) {
      result = eval(lhs) > eval(rhs);
    } else if(operator.equals("<=")) {
      result = eval(lhs) <= eval(rhs);
    } else if(operator.equals(">=")) {
      result = eval(lhs) >= eval(rhs);
    } else {
      throw new EvaluationException("Unknown relational operator '" + operator + "'");
    }

    return result ? 1 : 0;
  }


  /////////////////// bitwise expressions ///////////////////////
  int evalAndExpression(AstNode exprAst){
    return eval(exprAst.getChild(0)) & eval(exprAst.getChild(2));
  }

  int evalInclusiveOrExpression(AstNode exprAst){
    return eval(exprAst.getChild(0)) | eval(exprAst.getChild(2));
  }

  int evalExclusiveOrExpression(AstNode exprAst){
    return eval(exprAst.getChild(0)) ^ eval(exprAst.getChild(2));
  }


  /////////////////// other ... ///////////////////
  int evalUnaryExpression(AstNode exprAst){
    // only 'unary-operator cast-expression' production is allowed in #if-context

    String operator = exprAst.getChild(0).getTokenValue();
    AstNode operand = exprAst.getChild(1);
    if(operator.equals("+")){
      return +eval(operand);
    } else if(operator.equals("-")){
      return -eval(operand);
    } else if(operator.equals("!")){
      boolean result = !(eval(operand) != 0);
      return result ? 1 : 0;
    } else if(operator.equals("~")){
      return ~eval(operand);
    }
    else{
      throw new EvaluationException("Unknown unary operator  '" + operator + "'");
    }
  }

  int evalShiftExpression(AstNode exprAst){
    String operator = exprAst.getChild(1).getTokenValue();
    AstNode lhs = exprAst.getChild(0);
    AstNode rhs = exprAst.getChild(2);
    if(operator.equals("<<")){
      return eval(lhs) << eval(rhs);
    } else if(operator.equals(">>")){
      return eval(lhs) >> eval(rhs);
    } else {
      throw new EvaluationException("Unknown shift operator '" + operator + "'");
    }
  }

  int evalAdditiveExpression(AstNode exprAst){
    String operator = exprAst.getChild(1).getTokenValue();
    AstNode lhs = exprAst.getChild(0);
    AstNode rhs = exprAst.getChild(2);
    if(operator.equals("+")){
      return eval(lhs) + eval(rhs);
    } else if(operator.equals("-")){
      return eval(lhs) - eval(rhs);
    } else {
      throw new EvaluationException("Unknown additive operator '" + operator + "'");
    }
  }

  int evalMultiplicativeExpression(AstNode exprAst){
    String operator = exprAst.getChild(1).getTokenValue();
    AstNode lhs = exprAst.getChild(0);
    AstNode rhs = exprAst.getChild(2);
    if(operator.equals("*")){
      return eval(lhs) * eval(rhs);
    } else if(operator.equals("/")){
      return eval(lhs) / eval(rhs);
    } else if(operator.equals("%")){
      return eval(lhs) % eval(rhs);
    } else {
      throw new EvaluationException("Unknown multiplicative operator '" + operator + "'");
    }
  }

  int evalConditionalExpression(AstNode exprAst){
    AstNode decisionOperand = exprAst.getChild(0);
    AstNode trueCaseOperand = exprAst.getChild(2);
    AstNode falseCaseOperand = exprAst.getChild(4);
    return eval(decisionOperand) != 0 ? eval(trueCaseOperand) : eval(falseCaseOperand);
  }

  int evalPrimaryExpression(AstNode exprAst){
    // case "( expression )"
    return eval(exprAst.getChild(1));
  }

  int evalDefinedExpression(AstNode exprAst){
    int posOfMacroName = exprAst.getNumberOfChildren() == 2 ? 1 : 2;
    String macroName = exprAst.getChild(posOfMacroName).getTokenValue();
    String value = preprocessor.valueOf(macroName);
    return value == null ? 0 : 1;
  }

  int evalFunctionlikeMacro(AstNode exprAst){
    // Probably we should use (made public) preprocessor.expandMacro functionality here...
    // Use valueOf for now.
    String macroName = exprAst.getChild(0).getTokenValue();
    String value = preprocessor.valueOf(macroName);
    return value == null ? 0: _eval(value);
  }

  String stripSuffix(String number){
    return number.replaceAll("[LlUu]", "");
  }
}
