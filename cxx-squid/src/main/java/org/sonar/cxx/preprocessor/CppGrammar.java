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

import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.api.Rule;
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
import static org.sonar.cxx.api.CppKeyword.INCLUDE;
import static org.sonar.cxx.api.CppKeyword.IFDEF;
import static org.sonar.cxx.api.CppKeyword.IFNDEF;
import static org.sonar.cxx.api.CppKeyword.ELSE;
import static org.sonar.cxx.api.CppKeyword.ENDIF;

/**
 * The rules are a subset of those found in the C++ Standard, A.14 "Preprocessor directives"
 */
public class CppGrammar extends Grammar {
  public Rule preprocessor_line;
  public Rule define_line;
  public Rule include_line;
  public Rule ifdef_line;
  public Rule ifndef_line;
  public Rule replacement_list;
  public Rule identifier_list;
  public Rule pp_token;

  public CppGrammar() {
    preprocessor_line.is(
      or(
        include_line,
        define_line,
        ifdef_line,
        ifndef_line
        )
      );

    define_line.is(
      or(
        and(DEFINE, pp_token, "(", opt(identifier_list), ")", replacement_list),
        and(DEFINE, pp_token, "(", "...", ")", replacement_list),
        and(DEFINE, pp_token, "(", identifier_list, ",", "...", ")", replacement_list),
        and(DEFINE, pp_token, replacement_list)
        )
        );

    include_line.is(INCLUDE,
                    or(
                      and("<", one2n(not(">"), pp_token), ">"),
                      CxxTokenType.STRING
                      )
      );
    
    ifdef_line.is(IFDEF, IDENTIFIER);
    
    ifndef_line.is(IFNDEF, IDENTIFIER);
    
    replacement_list.is(
        o2n(
        or(
            "##",
            "#",
            pp_token
          )
        )
        );

    identifier_list.is(IDENTIFIER, o2n(",", IDENTIFIER));
    pp_token.is(anyToken());
  }

  @Override
  public Rule getRootRule() {
    return preprocessor_line;
  }
}
