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

import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.sslr.impl.channel.PunctuatorChannel;
import com.sonar.sslr.impl.channel.UnknownCharacterChannel;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CppKeyword;
import org.sonar.cxx.api.CppPunctuator;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.channels.CharacterLiteralsChannel;
import org.sonar.cxx.channels.StringLiteralsChannel;

import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.ANY_CHAR;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.and;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.o2n;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.opt;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;

public final class CppLexer {

  private static final String INTEGER_SUFFIX = "(((U|u)(LL|ll|L|l)?)|((LL|ll|L|l)(u|U)?))";
  private static final String EXP = "([Ee][+-]?+[0-9_]++)";
  private static final String FLOAT_SUFFIX = "(f|l|F|L)";

  private CppLexer() {
  }

  public static Lexer create() {
    return create(new CxxConfiguration());
  }

  public static Lexer create(CxxConfiguration conf) {
    Lexer.Builder builder = Lexer.builder()
      .withCharset(conf.getCharset())
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(regexp(CxxTokenType.WS, "\\s+"))
      .withChannel(commentRegexp("//[^\\n\\r]*+"))
      .withChannel(commentRegexp("/\\*", ANY_CHAR + "*?", "\\*/"))
      .withChannel(new CharacterLiteralsChannel())
      .withChannel(new StringLiteralsChannel())

      // C++ Standard, Section 2.14.4 "Floating literals"
      .withChannel(regexp(CxxTokenType.NUMBER, "[0-9]++\\.[0-9]*+" + opt(EXP) + opt(FLOAT_SUFFIX)))
      .withChannel(regexp(CxxTokenType.NUMBER, "\\.[0-9]++" + opt(EXP) + opt(FLOAT_SUFFIX)))
      .withChannel(regexp(CxxTokenType.NUMBER, "[0-9]++" + EXP + opt(FLOAT_SUFFIX)))

      // C++ Standard, Section 2.14.2 "Integer literals"
      .withChannel(regexp(CxxTokenType.NUMBER, "[1-9][0-9]*+" + opt(INTEGER_SUFFIX))) // Decimal literals
      .withChannel(regexp(CxxTokenType.NUMBER, "0[0-7]++" + opt(INTEGER_SUFFIX))) // Octal Literals
      .withChannel(regexp(CxxTokenType.NUMBER, "0[xX][0-9a-fA-F]++" + opt(INTEGER_SUFFIX))) // Hex Literals
      .withChannel(regexp(CxxTokenType.NUMBER, "0" + opt(INTEGER_SUFFIX))) // Decimal zero

      .withChannel(new KeywordChannel(and("#", o2n("\\s"), "[a-z]", o2n("\\w")), CppKeyword.values()))
      .withChannel(new IdentifierAndKeywordChannel(and("[a-zA-Z_]", o2n("\\w")), true))
      .withChannel(new PunctuatorChannel(CppPunctuator.values()))
      .withChannel(new UnknownCharacterChannel());

    return builder.build();
  }
}
