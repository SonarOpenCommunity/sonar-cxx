/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.api.TokenType;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.util.ArrayList;
import java.util.List;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxTokenType;

/**
 * Replace text macros while possibly concatenating or quoting identifiers
 * (controlled by directives #define and #undef, and operators # and ##).
 */
class PPReplace {

  private final CxxPreprocessor pp;

  PPReplace(CxxPreprocessor pp) {
    this.pp = pp;
  }

  /**
   * Object-like macros.
   *
   * Object-like macros replace every occurrence of defined identifier with replacement-list. Version (1) of the #define
   * directive behaves exactly like that.
   */
  List<Token> replaceObjectLikeMacro(PPMacro macro, String macroExpression) {
    List<Token> tokens = pp.tokenizeMacro(macro, macroExpression);

    // make sure that all expanded Tokens are marked as generated it will prevent
    // them from being involved into NCLOC / complexity / highlighting
    return PPGeneratedToken.markAllAsGenerated(tokens);
  }

  /**
   * Function-like macros.
   *
   * Function-like macros replace each occurrence of defined identifier with replacement-list, additionally taking a
   * number of arguments, which then replace corresponding occurrences of any of the parameters in the replacement-list.
   */
  int replaceFunctionLikeMacro(PPMacro macro, List<Token> restTokens, List<Token> expansion) {
    List<Token> arguments = new ArrayList<>();
    int tokensConsumedMatchingArgs = extractArguments(restTokens, arguments);

    if (macro.checkArgumentsCount(arguments.size())) {
      if (arguments.size() > macro.parameterList.size()) {
        // group all arguments into the last one (__VA_ARGS__)
        List<Token> vaargs = arguments.subList(macro.parameterList.size() - 1, arguments.size());
        var firstToken = vaargs.get(0);
        arguments = arguments.subList(0, macro.parameterList.size() - 1);
        arguments.add(Token.builder()
          .setLine(firstToken.getLine())
          .setColumn(firstToken.getColumn())
          .setURI(firstToken.getURI())
          .setValueAndOriginalValue(TokenUtils.merge(vaargs, ","))
          .setType(CxxTokenType.STRING)
          .build());
      }
      List<Token> replTokens = replaceParams(macro, arguments);
      replTokens = PPConcatenation.concatenate(replTokens);
      expansion.addAll(replaceObjectLikeMacro(macro, TokenUtils.merge(replTokens)));
    }

    return tokensConsumedMatchingArgs;
  }

  /**
   * The syntax of a function-like macro invocation is similar to the syntax of a function call: each instance of the
   * macro name followed by a ( as the next preprocessing token introduces the sequence of tokens that is replaced by
   * the replacement-list. The sequence is terminated by the matching ) token, skipping intervening matched pairs of
   * left and right parentheses.
   */
  @SuppressWarnings({"java:S3776", "java:S1541"})
  private static int extractArguments(List<Token> tokens, List<Token> arguments) {
    // argument list must start with '('
    int size = tokens.size();
    if ((size < 1) || !"(".equals(tokens.get(0).getValue())) {
      return 0;
    }
    // split arguments: ( a, b, c ) => a b c
    var addArgument = false;
    var nestingLevel = -1;
    var fromIndex = 0;
    for (var i = 0; i < size; i++) {
      switch (tokens.get(i).getValue()) {
        case "(":
          nestingLevel++;
          break;
        case ",":
          if (nestingLevel == 0) {
            addArgument = true;
          }
          break;
        case ")":
          if (nestingLevel == 0) {
            addArgument = true;
          }
          nestingLevel--;
          break;
        default:
          break;
      }

      // add argument to list
      if (addArgument) {
        if ((i - fromIndex) > 1) {
          var matchedTokens = tokens.subList(fromIndex + 1, i);
          var firstToken = matchedTokens.get(0);

          arguments.add(Token.builder()
            .setLine(firstToken.getLine())
            .setColumn(firstToken.getColumn())
            .setURI(firstToken.getURI())
            .setValueAndOriginalValue(TokenUtils.merge(matchedTokens).trim()) // trim because of SONARPLUGINS-3060 issue
            .setType(CxxTokenType.STRING)
            .build());
        }
        // end of parameter list: closing ')'
        if (nestingLevel < 0) {
          return i + 1;
        }

        addArgument = false;
        fromIndex = i;
      }
    }

    return 0;
  }

  private String expand(String expression) {
    return TokenUtils.merge(pp.tokenize(expression));
  }

  /**
   * Taking a number of arguments, which then replace corresponding occurrences of any of the parameters in the
   * replacement-list.
   */
  private List<Token> replaceParams(PPMacro macro, List<Token> arguments) {
    var result = new ArrayList<Token>(macro.replacementList.size());
    handleOperators(macro.replacementList, macro.getParameterNames(), arguments, result);
    return result;
  }

  /**
   * Handle # and ## operators.
   *
   * In function-like macros, a # operator before an identifier in the replacement-list runs the identifier through
   * parameter replacement and encloses the result in quotes, effectively creating a string literal.
   *
   * A ## operator between any two successive identifiers in the replacement-list runs parameter replacement on the two
   * identifiers (which are not macro-expanded first) and then concatenates the result.
   *
   */
  @SuppressWarnings({"java:S3776"})
  private void handleOperators(List<Token> replacementList, List<String> parameters, List<Token> arguments,
                               List<Token> result) {

    int tokensConsumed = 0;

    while (tokensConsumed < replacementList.size()) {

      var view = replacementList.subList(tokensConsumed, replacementList.size());
      var token = view.get(0);
      var i = 0;

      Token argument = token;
      String newValue = "";

      int parameterIndex = getParameterIndex(token, parameters);
      if (parameterIndex == -1) {
        //
        // not a token to be replaced by a macro argument
        //
        if (((i = handleVaOpt(view, parameters, arguments, result)) <= 0)
              && ((i = handleConcatenation(view, parameters, arguments, result)) <= 0)) {
          result.add(token);
        }
      } else if (parameterIndex < arguments.size()) {
        //
        // token to be replaced by a macro argument
        //
        argument = arguments.get(parameterIndex);

        if (((i = handleConcatenation(view, parameters, arguments, result)) <= 0)
              && (tokensConsumed < 1 || !handleStringification(
                  replacementList.subList(tokensConsumed - 1, replacementList.size()), argument, result))) {
          newValue = expand(argument.getValue());
        }
      }

      if (newValue.isEmpty()) {
        handleEmptyVaArgs(view, result);
      } else {
        result.add(PPGeneratedToken.build(argument, argument.getType(), newValue));
      }

      tokensConsumed += (i + 1);
    }
  }

  private static boolean isIdentifier(TokenType type) {
    return GenericTokenType.IDENTIFIER.equals(type) || (type instanceof CxxKeyword);
  }

  private static int getParameterIndex(Token token, List<String> parameters) {
    int parameterIndex = -1;
    var type = token.getType();
    if (isIdentifier(type)) {
      parameterIndex = parameters.indexOf(token.getValue());
    }
    return parameterIndex;
  }

  private static Token getReplacementToken(Token token, List<String> parameters, List<Token> arguments) {
    int parameterIndex = getParameterIndex(token, parameters);
    if (parameterIndex != -1 && parameterIndex < arguments.size()) {
      var argument = arguments.get(parameterIndex);
      return PPGeneratedToken.build(argument, argument.getType(), argument.getValue());
    }

    return token;
  }

  /**
   * A ## operator between any two successive identifiers in the replacement-list runs parameter replacement on the two
   * identifiers (which are not macro-expanded first) and then concatenates the result. This operation is called
   * "concatenation" or "token pasting". Only tokens that form a valid token together may be pasted: identifiers that
   * form a longer identifier, digits that form a number, or operators + and = that form a +=. A comment cannot be
   * created by pasting / and * because comments are removed from text before macro substitution is considered.
   *
   * Special cases:
   * (1) A ## ## B == A ## B
   * (2) A ## B ## C ...
   */
  private static int handleConcatenation(List<Token> replacementList, List<String> parameters, List<Token> arguments,
                                         List<Token> result) {

    int tokensConsumed = 0;

    while ((tokensConsumed + 1) < replacementList.size()
             && isIdentifier(replacementList.get(tokensConsumed).getType())
             && PPPunctuator.HASHHASH.equals(replacementList.get(tokensConsumed + 1).getType())) {
      if (tokensConsumed == 0) {
        result.add(getReplacementToken(replacementList.get(0), parameters, arguments)); // A
      }
      tokensConsumed++;
      result.add(replacementList.get(tokensConsumed)); // ##
      tokensConsumed++;

      while ((tokensConsumed + 1) < replacementList.size()
               && PPPunctuator.HASHHASH.equals(replacementList.get(tokensConsumed).getType())) {
        tokensConsumed++;  // handle special case A ## ## ... B
      }
      result.add(getReplacementToken(replacementList.get(tokensConsumed), parameters, arguments)); // B, C, ...
    }

    return tokensConsumed;
  }

  /**
   * In function-like macros, a # operator before an identifier in the argument-list runs the identifier through
   * parameter argument and encloses the result in quotes, effectively creating a string literal.
   */
  private static boolean handleStringification(List<Token> replacementList, Token argument, List<Token> result) {
    if (PPPunctuator.HASH.equals(replacementList.get(0).getType())) {
      result.set(result.size() - 1,
                 PPGeneratedToken.build(argument, argument.getType(),
                                        PPStringification.stringify(argument.getValue()))
      );
      return true;
    }
    return false;
  }

  /**
   * Special handling for empty __VA_ARGS__.
   *
   * (1) Handle stringification of empty __VA_ARGS__: #__VA_ARGS__ => "", e.g. puts(#__VA_ARGS__) => puts("").
   * (2) Some compilers offer an extension that allows ## to appear after a comma and before __VA_ARGS__, in which case
   * the ## does nothing when the variable arguments are present, but removes the comma when the variable arguments are
   * not present: this makes it possible to define macros such as fprintf (stderr, format, ##__VA_ARGS__).
   */
  private static void handleEmptyVaArgs(List<Token> replacementList, List<Token> result) {
    if (!"__VA_ARGS__".equals(replacementList.get(0).getValue())) {
      return;
    }

    if (!result.isEmpty()) {
      var lastIndex = result.size() - 1;
      var type = result.get(lastIndex).getType();
      if (type instanceof PPPunctuator) {
        switch ((PPPunctuator) type) {
          case HASH: // (1)
            result.set(lastIndex, PPGeneratedToken.build(result.get(lastIndex), CxxTokenType.STRING, "\"\""));
            break;
          case HASHHASH: // (2)
            lastIndex -= 1;
            if (lastIndex > 0) {
              result.subList(lastIndex, result.size()).clear();
            }
            break;
          case COMMA: // (2)
            result.remove(lastIndex);
            break;
          default:
            break;
        }
      }
    }
  }

  /**
   * Replacement-list may contain the token sequence __VA_OPT__ ( content ). __VA_OPT__ ( content ) macro may only
   * appear in the definition of a variadic macro. If the variable argument has any tokens, then a __VA_OPT__ invocation
   * expands to its argument; but if the variable argument does not have any tokens, the __VA_OPT__ expands to nothing.
   *
   * <code>
   * va-opt-argument:
   * __VA_OPT__ ( pp-tokensopt )
   * </code>
   */
  @SuppressWarnings({"java:S3776", "java:S1142"})
  private int handleVaOpt(List<Token> replacementList, List<String> parameters, List<Token> arguments,
                          List<Token> result) {
    var firstIndex = -1;
    var lastIndex = -1;
    var brackets = 0;

    if (!"__VA_OPT__".equals(replacementList.get(0).getValue())) {
      return 0;
    }

    for (int i = 1; i < replacementList.size(); i++) {
      var value = replacementList.get(i).getValue();
      if ("(".equals(value)) {
        brackets++;
        if (firstIndex == -1) {
          firstIndex = i;
        }
      } else if (")".equals(value)) {
        brackets--;
        if (brackets == 0) {
          lastIndex = i;
          break;
        }
      }
    }

    int consumedTokens = 0;

    if (firstIndex != -1 && lastIndex != -1) {
      if (parameters.size() == arguments.size()) {
        // __VA_OPT__ ( pp-tokensopt ), keep pp-tokensopt
        var ppTokens = replacementList.subList(firstIndex + 1, lastIndex);
        handleOperators(ppTokens, parameters, arguments, result);
        consumedTokens = 2 + ppTokens.size();
      } else {
        // remove __VA_OPT__ ( pp-tokensopt )
        consumedTokens = 1 + lastIndex - firstIndex;
      }
    }

    return consumedTokens;
  }

}
