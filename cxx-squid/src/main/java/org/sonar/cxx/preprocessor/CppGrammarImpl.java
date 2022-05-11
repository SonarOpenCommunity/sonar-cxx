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

import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.api.Grammar;
import org.sonar.cxx.parser.CxxTokenType;
import static org.sonar.cxx.parser.CxxTokenType.WS;
import static org.sonar.cxx.preprocessor.CppKeyword.DEFINE;
import static org.sonar.cxx.preprocessor.CppKeyword.ELIF;
import static org.sonar.cxx.preprocessor.CppKeyword.ELSE;
import static org.sonar.cxx.preprocessor.CppKeyword.ENDIF;
import static org.sonar.cxx.preprocessor.CppKeyword.ERROR;
import static org.sonar.cxx.preprocessor.CppKeyword.IF;
import static org.sonar.cxx.preprocessor.CppKeyword.IFDEF;
import static org.sonar.cxx.preprocessor.CppKeyword.IFNDEF;
import static org.sonar.cxx.preprocessor.CppKeyword.INCLUDE;
import static org.sonar.cxx.preprocessor.CppKeyword.INCLUDE_NEXT;
import static org.sonar.cxx.preprocessor.CppKeyword.LINE;
import static org.sonar.cxx.preprocessor.CppKeyword.PRAGMA;
import static org.sonar.cxx.preprocessor.CppKeyword.UNDEF;
import static org.sonar.cxx.preprocessor.CppKeyword.WARNING;
import static org.sonar.cxx.preprocessor.CppPunctuator.HASH;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.grammar.LexerfulGrammarBuilder;

/**
 * The rules are a subset of those found in the C++ Standard, **A.12 Preprocessing directives [gram.cpp]**
 *
 * The grammar defines the rules which are necessary to parse single lines.
 * These rules end with 'new-line'. The further processing is done in 'CxxPreprocessor'.
 *
 * Deviating from the grammar in the standard, whitespaces between the tokens must also be processed here.
 */
@SuppressWarnings({"squid:S00115", "squid:S00103"})
public enum CppGrammarImpl implements GrammarRuleKey {
  preprocessorLine,
  defineLine,
  includeLine,
  includeBody,
  expandedIncludeBody,
  includeBodyQuoted,
  includeBodyBracketed,
  includeBodyFreeform,
  ifdefLine,
  replacementList,
  argumentList,
  parameterList,
  variadicparameter,
  ppToken,
  ifLine,
  elifLine,
  constantExpression,
  primaryExpression,
  unaryExpression,
  unaryOperator,
  multiplicativeExpression,
  additiveExpression,
  shiftExpression,
  stringPrefix,
  relationalExpression,
  equalityExpression,
  andExpression,
  exclusiveOrExpression,
  inclusiveOrExpression,
  logicalAndExpression,
  logicalOrExpression,
  conditionalExpression,
  expression,
  bool,
  literal,
  definedExpression,
  functionlikeMacro,
  hasIncludeExpression,
  //hasIncludeBodyFreeform,
  functionlikeMacroDefinition,
  objectlikeMacroDefinition,
  elseLine,
  endifLine,
  undefLine,
  lineLine,
  errorLine,
  pragmaLine,
  warningLine,
  miscLine,
  argument,
  somethingContainingParantheses,
  somethingWithoutParantheses,
  allButLeftParan,
  allButRightParan,
  allButParan,
  allButComma,
  ppImport,
  ppModule;

  public static Grammar create() {
    var b = LexerfulGrammarBuilder.create();

    toplevelDefinitionGrammar(b);
    defineLineGrammar(b);
    includeLineGrammar(b);
    moduleLineGrammar(b);
    ifLineGrammar(b);
    allTheOtherLinesGrammar(b);

    b.setRootRule(preprocessorLine);

    return b.buildWithMemoizationOfMatchesForAllRules();
  }

  /**
   * The root note is not 'preprocessing-file' but a collection of rules that
   * process lines (ending with 'new-line' in grammar).
   */
  private static void toplevelDefinitionGrammar(LexerfulGrammarBuilder b) {
    b.rule(preprocessorLine).is(
      b.firstOf(
        includeLine, // control-line, # include
        ppImport, // control-line, ppImport
        defineLine, // control-line, # define
        undefLine, // control-line, # undef
        lineLine, // control-line, # line
        errorLine, // control-line, # error
        pragmaLine, // control-line, # pragma
        warningLine, // control-line, # warning
        ifLine, // if-section, if-group, # if
        ifdefLine, // if-section, if-group, # ifdef/ifndef
        elifLine, // if-section, elif-group, #elif
        elseLine, // if-section, else-group, #else
        endifLine, // if-section, endif-line, #endif
        miscLine,
        ppModule // ... module ...

      )
    );
  }

  private static void defineLineGrammar(LexerfulGrammarBuilder b) {
    // control-line, # define
    b.rule(defineLine).is(
      b.firstOf(
        functionlikeMacroDefinition,
        objectlikeMacroDefinition
      )
    );

    b.rule(functionlikeMacroDefinition).is(
      b.firstOf(
        b.sequence(DEFINE, b.oneOrMore(WS), ppToken, "(", b.zeroOrMore(WS), b.optional(parameterList), b.zeroOrMore(WS),
                   ")", b.optional(b.sequence(b.zeroOrMore(WS), replacementList))),
        b.sequence(DEFINE, b.oneOrMore(WS), ppToken, "(", b.zeroOrMore(WS), variadicparameter, b.zeroOrMore(WS), ")", b
                   .optional(b.sequence(b.zeroOrMore(WS), replacementList))),
        b.sequence(DEFINE, b.oneOrMore(WS), ppToken, "(", b.zeroOrMore(WS), parameterList, b.zeroOrMore(WS), ",", b
                   .zeroOrMore(WS), variadicparameter, b.zeroOrMore(WS), ")", b.optional(b.sequence(b.zeroOrMore(WS),
                                                                                                    replacementList)))
      )
    );

    b.rule(variadicparameter).is(
      b.optional(IDENTIFIER), b.zeroOrMore(WS), "..."
    );

    b.rule(objectlikeMacroDefinition).is(
      DEFINE, b.oneOrMore(WS), ppToken, b.optional(b.sequence(b.oneOrMore(WS), replacementList))
    );

    b.rule(replacementList).is(
      b.oneOrMore(
        b.firstOf(
          "##",
          b.sequence(b.optional(stringPrefix), "#"),
          ppToken
        )
      )
    );

    b.rule(parameterList).is(
      IDENTIFIER, b.zeroOrMore(b.zeroOrMore(WS), ",", b.zeroOrMore(WS), IDENTIFIER,
                               b.nextNot(b.sequence(b.zeroOrMore(WS), "...")))
    );
    b.rule(argumentList).is(
      argument, b.zeroOrMore(b.zeroOrMore(WS), ",", b.zeroOrMore(WS), argument)
    );
    b.rule(stringPrefix).is(
      b.firstOf("L", "u8", "u", "U")
    );

    b.rule(argument).is(
      b.firstOf(
        b.oneOrMore(somethingContainingParantheses),
        somethingWithoutParantheses
      )
    );

    b.rule(somethingContainingParantheses).is(
      b.zeroOrMore(allButParan),
      "(",
      b.firstOf(
        somethingContainingParantheses,
        b.zeroOrMore(allButRightParan), ")"
      ),
      allButComma
    );

    b.rule(somethingWithoutParantheses).is(
      b.oneOrMore(b.nextNot(b.firstOf(",", ")", "(")), b.anyToken())
    );

    b.rule(allButLeftParan).is(
      b.nextNot("("), b.anyToken()
    );

    b.rule(allButRightParan).is(
      b.nextNot(")"), b.anyToken()
    );

    b.rule(allButParan).is(
      b.nextNot(b.firstOf("(", ")")), b.anyToken()
    );

    b.rule(allButComma).is(
      b.nextNot(","), b.anyToken()
    );

    b.rule(ppToken).is(b.anyToken());
  }

  private static void includeLineGrammar(LexerfulGrammarBuilder b) {
    // control-line, # include
    b.rule(includeLine).is(
      b.firstOf(
        INCLUDE,
        INCLUDE_NEXT
      ),
      b.zeroOrMore(WS),
      includeBody,
      b.zeroOrMore(WS)
    );
    b.rule(includeBody).is(
      b.firstOf(
        includeBodyQuoted,
        includeBodyBracketed,
        includeBodyFreeform
      )
    );
    b.rule(expandedIncludeBody).is(
      b.firstOf(
        includeBodyQuoted,
        includeBodyBracketed
      )
    );

    b.rule(includeBodyQuoted).is(
      CxxTokenType.STRING
    );
    b.rule(includeBodyBracketed).is(
      "<", b.oneOrMore(b.nextNot(">"), ppToken), ">"
    );
    b.rule(includeBodyFreeform).is(
      b.oneOrMore(ppToken)
    );
  }

  private static void moduleLineGrammar(LexerfulGrammarBuilder b) {

    // control-line, ppImport
    b.rule(ppImport).is(
      b.firstOf(
        b.sequence("import", b.zeroOrMore(WS), expandedIncludeBody, b.zeroOrMore(WS)),
        b.sequence(b.optional("export"), b.zeroOrMore(WS), "import", b.zeroOrMore(WS), b.oneOrMore(ppToken))
      )
    );

    // ... module ...
    b.rule(ppModule).is(
      b.firstOf(
        b.sequence(b.optional("export"), b.zeroOrMore(WS), "module", b.oneOrMore(ppToken)),
        b.sequence("export", b.oneOrMore(ppToken))
      )
    );
  }

  private static void allTheOtherLinesGrammar(LexerfulGrammarBuilder b) {
    // if-section, if-group, # ifdef/ifndef
    b.rule(ifdefLine).is(
      b.firstOf(
        IFDEF,
        IFNDEF
      ), b.oneOrMore(WS), IDENTIFIER, b.zeroOrMore(WS)
    );

    // if-section, else-group, #else
    b.rule(elseLine).is(
      ELSE, b.zeroOrMore(WS)
    );

    // if-section, endif-line, #endif
    b.rule(endifLine).is(
      ENDIF, b.zeroOrMore(WS)
    );

    // control-line, # undef
    b.rule(undefLine).is(
      UNDEF, b.oneOrMore(WS), IDENTIFIER
    );

    // control-line, # line
    b.rule(lineLine).is(
      LINE, b.oneOrMore(WS), b.oneOrMore(ppToken)
    );

    // control-line, # error
    b.rule(errorLine).is(
      ERROR, b.zeroOrMore(WS), b.zeroOrMore(ppToken)
    );

    // control-line, # pragma
    b.rule(pragmaLine).is(
      PRAGMA, b.zeroOrMore(WS), b.zeroOrMore(ppToken)
    );

    // control-line, # warning
    b.rule(warningLine).is(
      WARNING, b.zeroOrMore(WS), b.zeroOrMore(ppToken)
    );

    b.rule(miscLine).is(
      HASH, b.zeroOrMore(ppToken)
    );
  }

  private static void ifLineGrammar(LexerfulGrammarBuilder b) {
    // if-section, if-group, # if
    b.rule(ifLine).is(
      IF, b.zeroOrMore(WS), constantExpression, b.zeroOrMore(WS)
    );

    // if-section, elif-group, #elif
    b.rule(elifLine).is(
      ELIF, b.zeroOrMore(WS), constantExpression, b.zeroOrMore(WS)
    );

    b.rule(constantExpression).is(
      conditionalExpression
    );

    b.rule(conditionalExpression).is(
      b.firstOf(
        b.sequence(logicalOrExpression, b.zeroOrMore(WS), "?", b.zeroOrMore(WS), b.optional(expression), b
                   .zeroOrMore(WS), ":", b.zeroOrMore(WS), conditionalExpression),
        logicalOrExpression
      )
    ).skipIfOneChild();

    b.rule(logicalOrExpression).is(
      logicalAndExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                         "||", b.zeroOrMore(WS), logicalAndExpression)
    ).skipIfOneChild();

    b.rule(logicalAndExpression).is(
      inclusiveOrExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                          "&&", b.zeroOrMore(WS), inclusiveOrExpression)
    ).skipIfOneChild();

    b.rule(inclusiveOrExpression).is(
      exclusiveOrExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                          "|", b.zeroOrMore(WS), exclusiveOrExpression)
    ).skipIfOneChild();

    b.rule(exclusiveOrExpression)
      .is(andExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                      "^", b.zeroOrMore(WS), andExpression)
      ).skipIfOneChild();

    b.rule(andExpression).is(
      equalityExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                       "&", b.zeroOrMore(WS), equalityExpression)
    ).skipIfOneChild();

    b.rule(equalityExpression).is(
      relationalExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                         b.firstOf("==", "!="), b.zeroOrMore(WS), relationalExpression)
    ).skipIfOneChild();

    b.rule(relationalExpression).is(
      shiftExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                    b.firstOf("<", ">", "<=", ">="), b.zeroOrMore(WS), shiftExpression)
    ).skipIfOneChild();

    b.rule(shiftExpression).is(
      additiveExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                       b.firstOf("<<", ">>"), b.zeroOrMore(WS), additiveExpression)
    ).skipIfOneChild();

    b.rule(additiveExpression).is(
      multiplicativeExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                             b.firstOf("+", "-"), b.zeroOrMore(WS), multiplicativeExpression)
    ).skipIfOneChild();

    b.rule(multiplicativeExpression).is(
      unaryExpression, b.zeroOrMore(b.zeroOrMore(WS),
                                    b.firstOf("*", "/", "%"), b.zeroOrMore(WS), unaryExpression)
    ).skipIfOneChild();

    b.rule(unaryExpression).is(
      b.firstOf(
        b.sequence(unaryOperator, b.zeroOrMore(WS), multiplicativeExpression),
        primaryExpression
      )
    ).skipIfOneChild();

    b.rule(unaryOperator).is(
      b.firstOf("+", "-", "!", "~")
    );

    b.rule(primaryExpression).is(
      b.firstOf(
        literal,
        b.sequence("(", b.zeroOrMore(WS), expression, b.zeroOrMore(WS), ")"),
        hasIncludeExpression,
        definedExpression,
        functionlikeMacro,
        IDENTIFIER
      )
    ).skipIfOneChild();

    b.rule(literal).is(
      b.firstOf(
        CxxTokenType.CHARACTER,
        CxxTokenType.STRING,
        CxxTokenType.NUMBER,
        bool
      )
    );

    b.rule(bool).is(
      b.firstOf(
        "true",
        "false"
      )
    );

    b.rule(expression).is(
      conditionalExpression, b.zeroOrMore(b.zeroOrMore(WS), ",", b.zeroOrMore(WS),
                                          conditionalExpression)
    );

    b.rule(definedExpression).is(
      "defined",
      b.firstOf(
        b.sequence(b.zeroOrMore(WS), "(", b.zeroOrMore(WS), IDENTIFIER, b.zeroOrMore(WS), ")"),
        b.sequence(b.oneOrMore(WS), IDENTIFIER)
      )
    );

    b.rule(functionlikeMacro).is(
      IDENTIFIER, b.zeroOrMore(WS), "(", b.zeroOrMore(WS), b.optional(b.nextNot(")"), argumentList),
      b.zeroOrMore(WS), ")"
    );

    b.rule(hasIncludeExpression).is(
      b.firstOf(
        b.sequence(
          "__has_include", b.zeroOrMore(WS), "(", b.zeroOrMore(WS), includeBodyBracketed, b.zeroOrMore(WS), ")"
        ),
        b.sequence(
          "__has_include", b.zeroOrMore(WS), "(", b.zeroOrMore(WS), includeBodyQuoted, b.zeroOrMore(WS), ")"
        )
      //todo: b.sequence("__has_include", "(", hasIncludeBodyFreeform, )")
      )
    );

    //todo: b.rule(hasIncludeBodyFreeform).is(b.oneOrMore(b.nextNot(")"), ppToken));
  }

}
