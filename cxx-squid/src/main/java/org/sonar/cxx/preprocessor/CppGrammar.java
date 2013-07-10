/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2011 Waleri Enns and CONTACT Software GmbH
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

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.impl.matcher.GrammarFunctions;
import org.sonar.cxx.api.CxxTokenType;

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.anyToken;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Predicate.not;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.one2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or;
import static org.sonar.cxx.api.CppKeyword.DEFINE;
import static org.sonar.cxx.api.CppKeyword.ELIF;
import static org.sonar.cxx.api.CppKeyword.ELSE;
import static org.sonar.cxx.api.CppKeyword.ENDIF;
import static org.sonar.cxx.api.CppKeyword.ERROR;
import static org.sonar.cxx.api.CppKeyword.IF;
import static org.sonar.cxx.api.CppKeyword.IFDEF;
import static org.sonar.cxx.api.CppKeyword.IFNDEF;
import static org.sonar.cxx.api.CppKeyword.INCLUDE;
import static org.sonar.cxx.api.CppKeyword.INCLUDE_NEXT;
import static org.sonar.cxx.api.CppKeyword.LINE;
import static org.sonar.cxx.api.CppKeyword.PRAGMA;
import static org.sonar.cxx.api.CppKeyword.UNDEF;
import static org.sonar.cxx.api.CppKeyword.WARNING;
import static org.sonar.cxx.api.CppPunctuator.HASH;
import static org.sonar.cxx.api.CxxTokenType.WS;

/**
 * The rules are a subset of those found in the C++ Standard, A.14 "Preprocessor directives"
 */
public class CppGrammar extends Grammar {
  public Rule preprocessorLine;
  public Rule defineLine;
  public Rule includeLine;
  public Rule includeBody;
  public Rule expandedIncludeBody;
  public Rule ifdefLine;
  public Rule replacementList;
  public Rule argumentList;
  public Rule parameterList;
  public Rule ppToken;
  public Rule ifLine;
  public Rule elifLine;
  public Rule constantExpression;
  public Rule primaryExpression;
  public Rule unaryExpression;
  public Rule unaryOperator;
  public Rule multiplicativeExpression;
  public Rule additiveExpression;
  public Rule shiftExpression;
  public Rule relationalExpression;
  public Rule equalityExpression;
  public Rule andExpression;
  public Rule exclusiveOrExpression;
  public Rule inclusiveOrExpression;
  public Rule logicalAndExpression;
  public Rule logicalOrExpression;
  public Rule conditionalExpression;
  public Rule expression;
  public Rule bool;
  public Rule literal;
  public Rule definedExpression;
  public Rule functionlikeMacro;
  public Rule functionlikeMacroDefinition;
  public Rule objectlikeMacroDefinition;
  public Rule elseLine;
  public Rule endifLine;
  public Rule undefLine;
  public Rule lineLine;
  public Rule errorLine;
  public Rule pragmaLine;
  public Rule warningLine;
  public Rule miscLine;
  public Rule argument;
  public Rule somethingContainingParantheses;
  public Rule somethingWithoutParantheses;
  public Rule allButLeftParan;
  public Rule allButRightParan;
  public Rule allButParan;
  public Rule allButComma;

  public CppGrammar() {
    toplevelDefinitionGrammar();
    defineLineGrammar();
    includeLineGrammar();
    ifLineGrammar();
    allTheOtherLinesGrammar();

    GrammarFunctions.enableMemoizationOfMatchesForAllRules(this);
  }

  private void toplevelDefinitionGrammar() {
    preprocessorLine.is(
        or(
            defineLine,
            includeLine,
            ifdefLine,
            ifLine,
            elifLine,
            elseLine,
            endifLine,
            undefLine,
            lineLine,
            errorLine,
            pragmaLine,
            warningLine,
            miscLine
        )
        );
  }

  private void defineLineGrammar() {
    defineLine.is(
        or(
            functionlikeMacroDefinition,
            objectlikeMacroDefinition
        )
        );

    functionlikeMacroDefinition.is(
        or(
            and(DEFINE, one2n(WS), ppToken, "(", o2n(WS), opt(parameterList), o2n(WS), ")", opt(and(WS, replacementList))),
            and(DEFINE, one2n(WS), ppToken, "(", o2n(WS), "...", o2n(WS), ")", opt(and(WS, replacementList))),
            and(DEFINE, one2n(WS), ppToken, "(", o2n(WS), parameterList, o2n(WS), ",", o2n(WS), "...", o2n(WS), ")", opt(and(WS, replacementList)))
        )
        );

    objectlikeMacroDefinition.is(
        or(
        and(DEFINE, one2n(WS), ppToken, opt(and(one2n(WS), replacementList)))
        )
        );

    replacementList.is(
        one2n(
        or(
            "##",
            "#",
            ppToken
        )
        )
        );

    parameterList.is(IDENTIFIER, o2n(o2n(WS), ",", o2n(WS), IDENTIFIER));
    argumentList.is(argument, o2n(o2n(WS), ",", o2n(WS), argument));

    argument.is(
        or(
            one2n(somethingContainingParantheses),
            somethingWithoutParantheses
        )
        );

    somethingContainingParantheses.is(
        o2n(allButParan),
        "(",
        or(
            somethingContainingParantheses,
            o2n(allButRightParan), ")"
        ),
        allButComma
        );

    somethingWithoutParantheses.is(one2n(not(or(",", ")", "(")), anyToken()));

    allButLeftParan.is(not("("), anyToken());
    allButRightParan.is(not(")"), anyToken());
    allButParan.is(not(or("(", ")")), anyToken());
    allButComma.is(not(","), anyToken());

    ppToken.is(anyToken());
  }

  private void includeLineGrammar() {
    includeLine.is(
        or(INCLUDE, INCLUDE_NEXT),
        o2n(WS),
        includeBody,
        o2n(WS)
        );
    includeBody.is(
      or(
        expandedIncludeBody,
        one2n(ppToken)
        )
      );
    expandedIncludeBody.is(
      or(
        and("<", one2n(not(">"), ppToken), ">"),
        CxxTokenType.STRING
        )
      );
  }

  private void allTheOtherLinesGrammar() {
    ifdefLine.is(or(IFDEF, IFNDEF), one2n(WS), IDENTIFIER, o2n(WS));
    elseLine.is(ELSE, o2n(WS));
    endifLine.is(ENDIF, o2n(WS));
    undefLine.is(UNDEF, one2n(WS), IDENTIFIER);
    lineLine.is(LINE, one2n(WS), one2n(ppToken));
    errorLine.is(ERROR, o2n(WS), o2n(ppToken));
    pragmaLine.is(PRAGMA, o2n(WS), o2n(ppToken));
    warningLine.is(WARNING, o2n(WS), o2n(ppToken));
    miscLine.is(HASH, o2n(ppToken));
  }

  private void ifLineGrammar() {
    ifLine.is(IF, o2n(WS), constantExpression, o2n(WS));
    elifLine.is(ELIF, o2n(WS), constantExpression, o2n(WS));

    constantExpression.is(conditionalExpression);

    conditionalExpression.is(
        or(
            and(logicalOrExpression, o2n(WS), "?", o2n(WS), expression, o2n(WS), ":", o2n(WS), conditionalExpression),
            logicalOrExpression
        )
        ).skipIfOneChild();

    logicalOrExpression.is(logicalAndExpression, o2n(o2n(WS), "||", o2n(WS), logicalAndExpression)).skipIfOneChild();

    logicalAndExpression.is(inclusiveOrExpression, o2n(o2n(WS), "&&", o2n(WS), inclusiveOrExpression)).skipIfOneChild();

    inclusiveOrExpression.is(exclusiveOrExpression, o2n(o2n(WS), "|", o2n(WS), exclusiveOrExpression)).skipIfOneChild();

    exclusiveOrExpression.is(andExpression, o2n(o2n(WS), "^", o2n(WS), andExpression)).skipIfOneChild();

    andExpression.is(equalityExpression, o2n(o2n(WS), "&", o2n(WS), equalityExpression)).skipIfOneChild();

    equalityExpression.is(relationalExpression, o2n(o2n(WS), or("==", "!="), o2n(WS), relationalExpression)).skipIfOneChild();

    relationalExpression.is(shiftExpression, o2n(o2n(WS), or("<", ">", "<=", ">="), o2n(WS), shiftExpression)).skipIfOneChild();

    shiftExpression.is(additiveExpression, o2n(o2n(WS), or("<<", ">>"), o2n(WS), additiveExpression)).skipIfOneChild();

    additiveExpression.is(multiplicativeExpression, o2n(o2n(WS), or("+", "-"), o2n(WS), multiplicativeExpression)).skipIfOneChild();

    multiplicativeExpression.is(unaryExpression, o2n(o2n(WS), or("*", "/", "%"), o2n(WS), unaryExpression)).skipIfOneChild();

    unaryExpression.is(
        or(
            and(unaryOperator, o2n(WS), multiplicativeExpression),
            primaryExpression
        )
        ).skipIfOneChild();

    unaryOperator.is(
        or("+", "-", "!", "~")
        );

    primaryExpression.is(
        or(
            literal,
            and("(", o2n(WS), expression, o2n(WS), ")"),
            definedExpression,
            functionlikeMacro,
            IDENTIFIER
        )
        ).skipIfOneChild();

    literal.is(
        or(
            CxxTokenType.CHARACTER,
            CxxTokenType.STRING,
            CxxTokenType.NUMBER,
            bool
        )
        );

    bool.is(
        or(
            "true",
            "false"
        )
        );

    expression.is(conditionalExpression, o2n(o2n(WS), ",", o2n(WS), conditionalExpression));

    definedExpression.is(
        "defined",
        or(
            and(o2n(WS), "(", o2n(WS), IDENTIFIER, o2n(WS), ")"),
            and(one2n(WS), IDENTIFIER)
        )
        );

    functionlikeMacro.is(IDENTIFIER, o2n(WS), "(", o2n(WS), opt(not(")"), argumentList), o2n(WS), ")");
  }

  @Override
  public Rule getRootRule() {
    return preprocessorLine;
  }
}
