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
import java.util.HashMap;
import java.util.List;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.cxx.parser.CxxTokenType;

/**
 * Replace text macros while possibly concatenating or quoting identifiers
 * (controlled by directives #define and #undef, and operators # and ##).
 */
class PPReplace {

  private static final Logger LOG = Loggers.get(PPReplace.class);
  private CxxPreprocessor pp;

  PPReplace(CxxPreprocessor pp) {
    this.pp = pp;
  }

  /**
   * Object-like macros.
   *
   * Object-like macros replace every occurrence of defined identifier with replacement-list. Version (1) of the #define
   * directive behaves exactly like that.
   */
  List<Token> replaceObjectLikeMacro(String macroName, String macroExpression) {
    // C++ standard 16.3.4/2 Macro Replacement - Rescanning and further replacement
    List<Token> tokens = pp.tokenizeMacro(macroName, macroExpression);

    // make sure that all expanded Tokens are marked as generated it will prevent them from being involved into
    // NCLOC / complexity / highlighting
    // TODO: mark tokens already in macro expansion
    return PPGeneratedToken.markAllAsGenerated(tokens);
  }

  /**
   * Function-like macros.
   *
   * Function-like macros replace each occurrence of defined identifier with replacement-list, additionally taking a
   * number of arguments, which then replace corresponding occurrences of any of the parameters in the replacement-list.
   */
  int replaceFunctionLikeMacro(String macroName, List<Token> restTokens, List<Token> expansion) {
    List<Token> arguments = new ArrayList<>();
    int tokensConsumedMatchingArgs = matchArguments(restTokens, arguments);

    PPMacro macro = pp.getMacro(macroName);
    if (macro != null && macro.checkArgumentsCount(arguments.size())) {
      if (arguments.size() > macro.params.size()) {
        // group all arguments into the last one (__VA_ARGS__)
        List<Token> vaargs = arguments.subList(macro.params.size() - 1, arguments.size());
        var firstToken = vaargs.get(0);
        arguments = arguments.subList(0, macro.params.size() - 1);
        arguments.add(Token.builder()
          .setLine(firstToken.getLine())
          .setColumn(firstToken.getColumn())
          .setURI(firstToken.getURI())
          .setValueAndOriginalValue(TokenUtils.merge(vaargs, ","))
          .setType(CxxTokenType.STRING)
          .build());
      }
      List<Token> replTokens = replaceParams(macro.body, macro.params, arguments);
      replTokens = PPConcatenation.concatenate(replTokens);
      expansion.addAll(replaceObjectLikeMacro(macro.name, TokenUtils.merge(replTokens)));
    }

    return tokensConsumedMatchingArgs;
  }

  private static int matchArguments(List<Token> tokens, List<Token> arguments) {
    // argument list must start with '('
    int size = tokens.size();
    if ((size < 1) || !"(".equals(tokens.get(0).getValue())) {
      return 0;
    }
    // split arguments ','
    var endOfArgument = false;
    var nestingLevel = -1;
    var fromIndex = 0;
    for (var i = 0; i < size; i++) {
      var token = tokens.get(i);
      var tokenValue = token.getValue();
      switch (tokenValue) {
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

  private List<Token> replaceParams(List<Token> body, List<Token> parameters, List<Token> arguments) {
    // replace all parameters by according arguments "Stringify" the argument if the according parameter is
    // preceded by an #

    var newTokens = new ArrayList<Token>();
    if (!body.isEmpty()) {
      var tokenPastingLeftOp = false;
      var tokenPastingRightOp = false;

      // container to search parameter by name
      var paramterIndex = new HashMap<String, Integer>();
      for (var index = 0; index < parameters.size(); index++) {
        paramterIndex.put(parameters.get(index).getValue(), index);
      }

      for (var i = 0; i < body.size(); ++i) {
        var curr = body.get(i);
        int index = -1;
        if (curr.getType().equals(GenericTokenType.IDENTIFIER)) {
          index = paramterIndex.getOrDefault(curr.getValue(), -1);
        }
        if (index == -1) {
          if (curr.getValue().equals("__VA_OPT__")) {
            boolean keep = parameters.size() == arguments.size();
            replaceVaOpt(body.subList(i, body.size()), keep);
          } else {
            if (tokenPastingRightOp && !curr.getType().equals(CxxTokenType.WS) && !curr.getType().equals(
              PPPunctuator.HASHHASH)) {
              tokenPastingRightOp = false;
            }
            newTokens.add(curr);
          }
        } else if (index == arguments.size()) {
          // EXTENSION: GCC's special meaning of token paste operator:
          // If variable argument is left out then the comma before the paste operator will be deleted.
          int j = i;
          while (j > 0 && body.get(j - 1).getType().equals(CxxTokenType.WS)) {
            j--;
          }
          if (j > 0 && "##".equals(body.get(j - 1).getValue())) {
            int k = --j;
            while (j > 0 && body.get(j - 1).getType().equals(CxxTokenType.WS)) {
              j--;
            }
            if (j > 0 && ",".equals(body.get(j - 1).getValue())) {
              newTokens.remove(newTokens.size() - 1 + j - i); // remove the comma
              newTokens.remove(newTokens.size() - 1 + k - i); // remove the paste operator
            }
          } else if (j > 0 && ",".equals(body.get(j - 1).getValue())) {
            // Got empty variadic args, remove comma
            newTokens.remove(newTokens.size() - 1 + j - i);
          }
        } else if (index < arguments.size()) {
          // token pasting operator?
          int j = i + 1;
          while (j < body.size() && body.get(j).getType().equals(CxxTokenType.WS)) {
            j++;
          }
          if (j < body.size() && "##".equals(body.get(j).getValue())) {
            tokenPastingLeftOp = true;
          }
          // in case of token pasting operator do not fully expand
          var replacement = arguments.get(index);
          String newValue;
          if (tokenPastingLeftOp) {
            newValue = replacement.getValue();
            tokenPastingLeftOp = false;
            tokenPastingRightOp = true;
          } else if (tokenPastingRightOp) {
            newValue = replacement.getValue();
            tokenPastingLeftOp = false;
            tokenPastingRightOp = false;
          } else {
            if (i > 0 && "#".equals(body.get(i - 1).getValue())) {
              // In function-like macros, a # operator before an identifier in the replacement-list runs the identifier
              // through parameter replacement and encloses the result in quotes, effectively creating a string literal.
              newTokens.remove(newTokens.size() - 1);
              newValue = PPStringification.stringify(replacement.getValue());
            } else {
              // otherwise the arguments have to be fully expanded before expanding the body of the macro
              newValue = TokenUtils.merge(replaceObjectLikeMacro("", replacement.getValue()));
            }
          }

          if (newValue.isEmpty() && "__VA_ARGS__".equals(curr.getValue())) {
            // the Visual C++ implementation will suppress a trailing comma if no arguments are passed to the ellipsis
            for (var n = newTokens.size() - 1; n != 0; n = newTokens.size() - 1) {
              if (newTokens.get(n).getType().equals(CxxTokenType.WS)) {
                newTokens.remove(n);
              } else if (newTokens.get(n).getType().equals(PPPunctuator.COMMA)) {
                newTokens.remove(n);
                break;
              } else {
                break;
              }
            }
          } else {
            newTokens.add(PPGeneratedToken.build(replacement, replacement.getType(), newValue));
          }
        }
      }
    }

    // replace # with "" if sequence HASH BR occurs for body HASH __VA_ARGS__
    if (newTokens.size() > 3 && newTokens.get(newTokens.size() - 2).getType().equals(PPPunctuator.HASH)
          && newTokens.get(newTokens.size() - 1).getType().equals(PPPunctuator.BR_RIGHT)) {
      for (var n = newTokens.size() - 2; n != 0; n--) {
        if (newTokens.get(n).getType().equals(CxxTokenType.WS)) {
          newTokens.remove(n);
        } else if (newTokens.get(n).getType().equals(PPPunctuator.HASH)) {
          newTokens.remove(n);
          newTokens.add(n, PPGeneratedToken.build(newTokens.get(n), CxxTokenType.STRING, "\"\""));
          // TODO use set
          //newTokens.set(n, generatedToken(newTokens.get(n), CxxTokenType.STRING, "\"\""));
          break;
        } else {
          break;
        }
      }
    }
    return newTokens;
  }

  /**
   * Replacement-list may contain the token sequence __VA_OPT__ ( content ), which is replaced by
   * content if __VA_ARGS__is non-empty, and expands to nothing otherwise.
   *
   * <code>
   * va-opt-replacement:
   *   __VA_OPT__ ( pp-tokensopt )
   * </code>
   */
  private static void replaceVaOpt(List<Token> tokens, boolean keep) {
    var firstIndex = -1;
    var lastIndex = -1;
    var brackets = 0;

    for (var i = 0; i < tokens.size(); i++) {
      switch (tokens.get(i).getValue()) {
        case "(":
          brackets++;
          break;
        case ")":
          brackets--;
          break;
      }
      if (brackets > 0) {
        if (firstIndex == -1) {
          firstIndex = i;
        }
      } else {
        if (firstIndex != -1 && lastIndex == -1) {
          lastIndex = i;
          break;
        }
      }
    }

    if (firstIndex > 0 && lastIndex < tokens.size()) {
      if (keep) {
        // keep pp-tokensopt, remove ) and __VA_OPT__ (
        tokens.subList(lastIndex, lastIndex + 1).clear();
        tokens.subList(0, firstIndex).clear();
      } else {
        // remove from body:  __VA_OPT__ ( pp-tokensopt )
        tokens.subList(firstIndex - 1, lastIndex + 1).clear();
      }
    }
  }

}
