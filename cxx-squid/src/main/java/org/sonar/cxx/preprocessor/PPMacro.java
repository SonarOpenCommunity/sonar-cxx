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
import com.sonar.cxx.sslr.api.Token;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * The preprocessor supports text and function-like macro replacement.
 * <code>
 * #define identifier replacement-list(optional)                      (1)
 * #define identifier( parameters ) replacement-list(optional)        (2)
 * #define identifier( parameters, ... ) replacement-list(optional)   (3) since C++11
 * #define identifier( ... ) replacement-list(optional)               (4) since C++11
 * </code>
 */
final class PPMacro {

  public final String identifier;
  public final List<Token> parameterList;
  public final boolean isVariadic; // (3, 4) => parameters, ...
  public final List<Token> replacementList;

  private PPMacro(String identifier,
                  @Nullable List<Token> parameterList,
                  @Nullable List<Token> replacementList,
                  boolean isVariadic) {
    this.identifier = identifier;
    this.parameterList = parameterList != null ? new ArrayList<>(parameterList) : null;
    this.replacementList = replacementList != null ? new ArrayList<>(replacementList) : null;
    this.isVariadic = isVariadic;
  }

  static PPMacro create(AstNode defineLineAst) {
    var root = defineLineAst.getFirstChild();

    var identifier = getIdentifier(root);
    List<Token> parameterList;
    List<Token> replacementList;
    boolean isVariadic;
    if (PPGrammarImpl.objectlikeMacroDefinition.equals(root.getType())) {
      parameterList = null;
      isVariadic = false;
      replacementList = getReplacementList(root);
    } else {
      // a macro is identified as 'functionlike' if and only if the parentheses aren't separated from the identifier
      // by whitespaces. Otherwise, the parentheses belong to the replacement string (objectlikeMacro).
      int whitespaces = 0;
      var children = root.getChildren();
      if (children.size() > 2) {
        whitespaces = children.get(2).getToken().getColumn()
                        - children.get(1).getToken().getColumn()
                        - identifier.length();
      }

      if (whitespaces > 0) {
        // special case: objectlikeMacroDefinition #define identifier (...
        parameterList = null;
        isVariadic = false;
        var tokens = root.getTokens();
        // starting after #define identifier ... without EOF
        replacementList = tokens.subList(2, tokens.size() - 1);
      } else {
        // functionlikeMacroDefinition
        var variadicParameter = root.getFirstDescendant(PPGrammarImpl.variadicParameter);
        isVariadic = variadicParameter != null;
        if (isVariadic) {
          parameterList = getParameterList(root, variadicParameter);
        } else {
          parameterList = getParameterList(root);
        }
        replacementList = getReplacementList(root);
      }
    }

    return new PPMacro(identifier, parameterList, replacementList, isVariadic);
  }

  /**
   * Create a macro from a string (#define ...)
   */
  static PPMacro create(String source) {
    if (!source.startsWith("#define")) {
      throw new RuntimeException("String for macro creation must start with '#define ...'");
    }
    return PPMacro.create(PPParser.lineParser(source).getFirstChild(PPGrammarImpl.defineLine));
  }

  /**
   * For version (2), the number of arguments must be the same as the number of parameters in macro definition. For
   * versions (3,4), the number of arguments must not be less than the number of parameters (not counting
   * ...). Otherwise the program is ill-formed.
   */
  boolean checkArgumentsCount(int count) {
    if (isFunctionLikeMacro()) {
      return isVariadic ? count >= parameterList.size() - 1 : count == parameterList.size();
    }
    return false;
  }

  boolean isFunctionLikeMacro() {
    return parameterList != null;
  }

  int getParameterIndex(String parameterName) {
    if (isFunctionLikeMacro()) {
      for (int i = 0; i < parameterList.size(); i++) {
        if (parameterList.get(i).getValue().equals(parameterName)) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public String toString() {
    StringBuilder ab = new StringBuilder(64);
    ab.append("{");
    ab.append(identifier);
    if (isFunctionLikeMacro()) {
      ab.append("(");
      ab.append(parameterList.stream().map(Token::getValue).collect(Collectors.joining(", ")));
      if (isVariadic) {
        ab.append("...");
      }
      ab.append(")");
    }
    if (replacementList != null) {
      ab.append(":");
      ab.append(replacementList.stream().map(Token::getValue).collect(Collectors.joining()));
    }
    ab.append("}");
    return ab.toString();
  }

  private static String getIdentifier(AstNode root) {
    var token = root.getFirstDescendant(GenericTokenType.IDENTIFIER);
    return token.getTokenValue();
  }

  private static List<Token> getParameterList(AstNode root) {
    var token = root.getFirstDescendant(PPGrammarImpl.parameterList);
    return token != null ? getChildrenIdentifierTokens(token) : Collections.emptyList();
  }

  private static List<Token> getParameterList(AstNode root, AstNode variadicParameter) {
    var token = root.getFirstDescendant(PPGrammarImpl.parameterList);
    List<Token> parameterList = token != null ? getChildrenIdentifierTokens(token) : new ArrayList<>();
    var optionalIdentifier = variadicParameter.getFirstChild(GenericTokenType.IDENTIFIER);
    if (optionalIdentifier == null) { // ... only
      parameterList.add(
        PPGeneratedToken.build(variadicParameter.getToken(), GenericTokenType.IDENTIFIER, "__VA_ARGS__")
      );
    } else { // identifier ...
      parameterList.add(optionalIdentifier.getToken());
    }
    return parameterList;
  }

  private static List<Token> getReplacementList(AstNode root) {
    var token = root.getFirstDescendant(PPGrammarImpl.replacementList);
    return token != null ? token.getTokens().subList(0, token.getTokens().size() - 1) : Collections.emptyList();
  }

  private static List<Token> getChildrenIdentifierTokens(AstNode identListAst) {
    return identListAst.getChildren(GenericTokenType.IDENTIFIER)
      .stream()
      .map(AstNode::getToken)
      .collect(Collectors.toList());
  }

}
