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

import static com.sonar.sslr.api.GenericTokenType.IDENTIFIER;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Advanced.anyToken;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.and;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.o2n;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.opt;
import static com.sonar.sslr.impl.matcher.GrammarFunctions.Standard.or;

/**
 * The rules are a subset of those found in the C++ Standard, A.14 "Preprocessor directives" 
 */
public class CppGrammar extends Grammar {
  public Rule define_line;
  public Rule replacement_list;
  public Rule identifier_list;
  public Rule pp_token;
  
  public CppGrammar() {
    define_line.is(
        or(
            and(pp_token, "(", opt(identifier_list), ")", replacement_list),
            and(pp_token, "(", "...", ")", replacement_list),
            and(pp_token, "(", identifier_list, ",", "...", ")", replacement_list),
            and(pp_token, replacement_list)
        )
        );

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
    return define_line;
  }
}
