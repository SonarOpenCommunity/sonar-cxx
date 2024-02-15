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

import static com.sonar.cxx.sslr.api.GenericTokenType.IDENTIFIER;
import com.sonar.cxx.sslr.api.Grammar;
import org.sonar.cxx.parser.CxxTokenType;
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
@SuppressWarnings({"squid:S00115", "squid:S00103", "java:S138"})
enum PPGrammarImpl implements GrammarRuleKey {
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
  variadicParameter,
  ppToken,
  ifLine,
  elifLine,
  elifdefLine,
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

  static Grammar create() {
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
        elifdefLine, // if-section, elif-group, #elifdef/elifndef
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

    b.rule(functionlikeMacroDefinition).is(b.firstOf(
      b.sequence(PPKeyword.DEFINE, IDENTIFIER, "(", b.optional(parameterList), ")",
                 b.optional(replacementList)),
      b.sequence(PPKeyword.DEFINE, IDENTIFIER, "(", variadicParameter, ")",
                 b.optional(replacementList)),
      b.sequence(PPKeyword.DEFINE, IDENTIFIER, "(", parameterList, ",", variadicParameter, ")",
                 b.optional(replacementList))
    )
    );

    b.rule(variadicParameter).is(
      b.optional(IDENTIFIER), "..."
    );

    b.rule(objectlikeMacroDefinition).is(
      PPKeyword.DEFINE, IDENTIFIER, b.optional(replacementList)
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
      IDENTIFIER, b.zeroOrMore(",", IDENTIFIER, b.nextNot("..."))
    );
    b.rule(argumentList).is(
      argument, b.zeroOrMore(",", argument)
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
        PPKeyword.INCLUDE,
        PPKeyword.INCLUDE_NEXT
      ),
      includeBody
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
    final String MODULE = PPSpecialIdentifier.MODULE.getValue();
    final String IMPORT = PPSpecialIdentifier.IMPORT.getValue();
    final String EXPORT = PPSpecialIdentifier.EXPORT.getValue();

    // control-line, ppImport
    b.rule(ppImport).is(
      b.firstOf(
        b.sequence(IMPORT, expandedIncludeBody),
        b.sequence(b.optional(EXPORT), IMPORT, b.oneOrMore(ppToken))
      )
    );

    // ... module ...
    b.rule(ppModule).is(
      b.firstOf(
        b.sequence(b.optional(EXPORT), MODULE, b.oneOrMore(ppToken)),
        b.sequence(EXPORT, b.oneOrMore(ppToken))
      )
    );
  }

  private static void allTheOtherLinesGrammar(LexerfulGrammarBuilder b) {
    // control-line, # undef
    b.rule(undefLine).is(
      PPKeyword.UNDEF, IDENTIFIER
    );

    // control-line, # line
    b.rule(lineLine).is(
      PPKeyword.LINE, b.oneOrMore(ppToken)
    );

    // control-line, # error
    b.rule(errorLine).is(
      PPKeyword.ERROR, b.zeroOrMore(ppToken)
    );

    // control-line, # pragma
    b.rule(pragmaLine).is(
      PPKeyword.PRAGMA, b.zeroOrMore(ppToken)
    );

    // control-line, # warning
    b.rule(warningLine).is(
      PPKeyword.WARNING, b.zeroOrMore(ppToken)
    );

    b.rule(miscLine).is(
      PPPunctuator.HASH, b.zeroOrMore(ppToken)
    );
  }

  private static void ifLineGrammar(LexerfulGrammarBuilder b) {
    // if-section, if-group, # if
    b.rule(ifLine).is(
      PPKeyword.IF, constantExpression
    );

    // if-section, if-group, # ifdef/ifndef
    b.rule(ifdefLine).is(
      b.firstOf(
        PPKeyword.IFDEF,
        PPKeyword.IFNDEF
      ), IDENTIFIER
    );

    // if-section, elif-group, #elif
    b.rule(elifLine).is(
      PPKeyword.ELIF, constantExpression
    );

    // if-section, elif-group, #elifdef/elifndef
    b.rule(elifdefLine).is(
      b.firstOf(
        PPKeyword.ELIFDEF,
        PPKeyword.ELIFNDEF
      ), IDENTIFIER
    );

    // if-section, else-group, #else
    b.rule(elseLine).is(
      PPKeyword.ELSE
    );

    // if-section, endif-line, #endif
    b.rule(endifLine).is(
      PPKeyword.ENDIF
    );

    b.rule(constantExpression).is(
      conditionalExpression
    );

    b.rule(conditionalExpression).is(
      b.firstOf(
        b.sequence(logicalOrExpression, "?", b.optional(expression), ":", conditionalExpression),
        logicalOrExpression
      )
    ).skipIfOneChild();

    b.rule(logicalOrExpression).is(
      logicalAndExpression, b.zeroOrMore("||", logicalAndExpression)
    ).skipIfOneChild();

    b.rule(logicalAndExpression).is(
      inclusiveOrExpression, b.zeroOrMore("&&", inclusiveOrExpression)
    ).skipIfOneChild();

    b.rule(inclusiveOrExpression).is(
      exclusiveOrExpression, b.zeroOrMore("|", exclusiveOrExpression)
    ).skipIfOneChild();

    b.rule(exclusiveOrExpression)
      .is(andExpression, b.zeroOrMore("^", andExpression)
      ).skipIfOneChild();

    b.rule(andExpression).is(
      equalityExpression, b.zeroOrMore("&", equalityExpression)
    ).skipIfOneChild();

    b.rule(equalityExpression).is(
      relationalExpression, b.zeroOrMore(b.firstOf("==", "!="), relationalExpression)
    ).skipIfOneChild();

    b.rule(relationalExpression).is(
      shiftExpression, b.zeroOrMore(b.firstOf("<", ">", "<=", ">="), shiftExpression)
    ).skipIfOneChild();

    b.rule(shiftExpression).is(
      additiveExpression, b.zeroOrMore(b.firstOf("<<", ">>"), additiveExpression)
    ).skipIfOneChild();

    b.rule(additiveExpression).is(
      multiplicativeExpression, b.zeroOrMore(b.firstOf("+", "-"), multiplicativeExpression)
    ).skipIfOneChild();

    b.rule(multiplicativeExpression).is(
      unaryExpression, b.zeroOrMore(b.firstOf("*", "/", "%"), unaryExpression)
    ).skipIfOneChild();

    b.rule(unaryExpression).is(
      b.firstOf(
        b.sequence(unaryOperator, multiplicativeExpression),
        primaryExpression
      )
    ).skipIfOneChild();

    b.rule(unaryOperator).is(
      b.firstOf("+", "-", "!", "~")
    );

    b.rule(primaryExpression).is(
      b.firstOf(
        literal,
        b.sequence("(", expression, ")"),
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
      conditionalExpression, b.zeroOrMore(",", conditionalExpression)
    );

    b.rule(definedExpression).is(
      "defined",
      b.firstOf(
        b.sequence("(", IDENTIFIER, ")"),
        IDENTIFIER
      )
    );

    b.rule(functionlikeMacro).is(
      IDENTIFIER, "(", b.optional(b.nextNot(")"), argumentList), ")"
    );

    b.rule(hasIncludeExpression).is(
      "__has_include", "(", includeBody, ")"
    );
  }

}
