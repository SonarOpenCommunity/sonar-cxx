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

import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.token.TokenUtils;
import java.util.ArrayList;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.parser.CxxKeyword;
import org.sonar.cxx.parser.CxxTokenType;

/**
 * Replace text macros while possibly concatenating or quoting identifiers
 * (controlled by directives #define and #undef, and operators # and ##).
 */
class PPReplace {

  private static final Logger LOG = Loggers.get(PPReplace.class);
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
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further argument
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
    int tokensConsumedMatchingArgs = matchArguments(restTokens, arguments);

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

  private static int matchArguments(List<Token> tokens, List<Token> arguments) {
    // argument list must start with '('
    int size = tokens.size();
    if ((size < 1) || !"(".equals(tokens.get(0).getValue())) {
      return 0;
    }
    // split arguments: ( a, b, c ) => a b c
    var endOfArgument = false;
    var nestingLevel = -1;
    var fromIndex = 0;
    for (var i = 0; i < size; i++) {
      switch (tokens.get(i).getValue()) {
        case "(":
          nestingLevel++;
          break;
        case ",":
          if (nestingLevel == 0) {
            endOfArgument = true;
          }
          break;
        case ")":
          if (nestingLevel == 0) {
            endOfArgument = true;
          }
          nestingLevel--;
          break;
        default:
          break;
      }

      // add argument to list
      if (endOfArgument) {
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

        endOfArgument = false;
        fromIndex = i;
      }
    }

    LOG.error("preprocessor 'matchArguments' error, missing ')': {}", tokens.toString());
    return 0;
  }

  private String expand(String replacementString) {
    return TokenUtils.merge(pp.tokenize(replacementString));
  }

  private List<Token> replaceParams(PPMacro macro, List<Token> argumentList) {
    // replace all parameterList by according argumentList "Stringify" the argument if the according parameter is
    // preceded by an #

    var result = new ArrayList<Token>(macro.replacementList.size());
    replaceParams(macro.replacementList, macro.getParameterNames(), argumentList, result);
    return result;
  }

  private void replaceParams(List<Token> replacementList, List<String> parameterList, List<Token> argumentList,
                             List<Token> result) {
    // replace all parameterList by according argumentList "Stringify" the argument if the according parameter is
    // preceded by an #

    var tokenPastingLeftOp = false;
    var tokenPastingRightOp = false;

    for (var i = 0; i < replacementList.size(); ++i) {
      var token = replacementList.get(i);
      var tokenValue = token.getValue();
      var tokenType = token.getType();
      String newValue = "";

      int parameterIndex = -1;
      if (GenericTokenType.IDENTIFIER.equals(tokenType) || (tokenType instanceof CxxKeyword)) {
        parameterIndex = parameterList.indexOf(tokenValue);
      }

      if (parameterIndex == -1) {
        if ("__VA_OPT__".equals(tokenValue)) {
          boolean keep = parameterList.size() == argumentList.size();
          i += replaceVaOpt(replacementList.subList(i, replacementList.size()), parameterList, argumentList, keep,
                            result);
        } else {
          if (tokenPastingRightOp && !PPPunctuator.HASHHASH.equals(tokenType)) {
            tokenPastingRightOp = false;
          }
          result.add(token);
        }
      } else if (parameterIndex == argumentList.size()) {
        // EXTENSION: GCC's special meaning of token paste operator:
        // If variable argument is left out then the comma before the paste operator will be deleted.
        if (i > 0
              && PPPunctuator.HASHHASH.equals(replacementList.get(i - 1).getType())
              && PPPunctuator.COMMA.equals(replacementList.get(i - 2).getType())) {
          result.subList(result.size() - 2, result.size()).clear(); // remove , ##
        } else if (i > 0 && ",".equals(replacementList.get(i - 1).getValue())) {
          // Got empty variadic args, remove comma
          result.remove(result.size() - 1);
        }
      } else if (parameterIndex < argumentList.size()) {
        // token pasting operator?
        int j = i + 1;
        if (j < replacementList.size() && PPPunctuator.HASHHASH.equals(replacementList.get(j).getType())) {
          tokenPastingLeftOp = true;
        }
        // in case of token pasting operator do not fully expand
        var argument = argumentList.get(parameterIndex);
        newValue = argument.getValue();
        if (tokenPastingLeftOp) {
          tokenPastingLeftOp = false;
          tokenPastingRightOp = true;
        } else if (tokenPastingRightOp) {
          tokenPastingLeftOp = false;
          tokenPastingRightOp = false;
        } else {
          if (i > 0 && PPPunctuator.HASH.equals(replacementList.get(i - 1).getType())) {
            // In function-like macros, a # operator before an identifier in the argument-list runs the identifier
            // through parameter argument and encloses the result in quotes, effectively creating a string literal.
            result.remove(result.size() - 1);
            newValue = PPStringification.stringify(newValue);
          } else {
            // otherwise the argumentList have to be fully expanded before expanding the replacementList of the macro
            newValue = expand(newValue);
          }
        }

        if (!newValue.isEmpty()) {
          result.add(PPGeneratedToken.build(argument, argument.getType(), newValue));
        }
      }

      if (newValue.isEmpty() && "__VA_ARGS__".equals(tokenValue)) {
        var n = result.size() - 1;
        if (n >= 0) {
          if (PPPunctuator.HASH.equals(result.get(n).getType())) {
            // handle empty #__VA_ARGS__ => "", e.g. puts(#__VA_ARGS__) => puts("")
            result.set(n, PPGeneratedToken.build(result.get(n), CxxTokenType.STRING, "\"\""));
          } else if (PPPunctuator.COMMA.equals(result.get(n).getType())) {
            // the Visual C++ implementation will suppress a trailing comma if no argumentList are passed to the ellipsis
            result.remove(n);
          }
        }
      }
    }
  }

  /**
   * Replacement-list may contain the token sequence __VA_OPT__ ( content ). __VA_OPT__ ( content ) macro may only
   * appear in the definition of a variadic macro. If the variable argument has any tokens, then a __VA_OPT__ invocation
   * expands to its argument; but if the variable argument does not have any tokens, the __VA_OPT__ expands to nothing.
   *
   * @param keep true means expand, false remove
   *
   * <code>
   * va-opt-argument:
   * __VA_OPT__ ( pp-tokensopt )
   * </code>
   */
  private int replaceVaOpt(List<Token> replacementList, List<String> parameterList, List<Token> argumentList,
                           boolean keep, List<Token> result) {
    var firstIndex = -1;
    var lastIndex = -1;
    var brackets = 0;

    for (int i = 0; i < replacementList.size(); i++) {
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

    if (firstIndex > 0 && lastIndex < replacementList.size()) {
      if (keep) {
        // __VA_OPT__ ( pp-tokensopt ), keep pp-tokensopt
        var ppTokens = replacementList.subList(firstIndex + 1, lastIndex);
        replaceParams(ppTokens, parameterList, argumentList, result);
        return 2 + ppTokens.size();
      } else {
        // remove __VA_OPT__ ( pp-tokensopt )
        return 1 + lastIndex - firstIndex;
      }
    }
    return 0;
  }

}
