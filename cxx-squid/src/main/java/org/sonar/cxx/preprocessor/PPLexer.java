/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2025 SonarOpenCommunity
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

import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.channel.BlackHoleChannel;
import com.sonar.cxx.sslr.impl.channel.BomCharacterChannel;
import com.sonar.cxx.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.cxx.sslr.impl.channel.PunctuatorChannel;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.*;
import com.sonar.cxx.sslr.impl.channel.UnknownCharacterChannel;
import java.nio.charset.Charset;
import org.sonar.cxx.channels.CharacterLiteralsChannel;
import org.sonar.cxx.channels.KeywordChannel;
import org.sonar.cxx.channels.StringLiteralsChannel;
import org.sonar.cxx.parser.CxxTokenType;

final class PPLexer {

  private static final String HEX_PREFIX = "0[xX]";
  private static final String BIN_PREFIX = "0[bB]";
  private static final String EXPONENT = "[eE][+-]?+[0-9_]([']?+[0-9_]++)*+";
  private static final String BINARY_EXPONENT = "[pP][+-]?+\\d([']?+\\d++)*+"; // since C++17
  // ud-suffix: identifier (including INTEGER_SUFFIX, FLOAT_SUFFIX)
  private static final String UD_SUFFIX = "[_a-zA-Z]\\w*+";
  private static final String DECDIGIT_SEQUENCE = "\\d([']?+\\d++)*+";
  private static final String HEXDIGIT_SEQUENCE = "[0-9a-fA-F]([']?+[0-9a-fA-F]++)*+";
  private static final String BINDIGIT_SEQUENCE = "[01]([']?+[01]++)*+";
  private static final String POINT = "\\.";

  private PPLexer() {
  }

  static Lexer create() {
    return create(Charset.defaultCharset());
  }

  static Lexer create(Charset charset) {

    //
    // changes here must be always aligned: CxxLexer.java <=> PPLexer.java
    //
    var builder = Lexer.builder()
      .withCharset(charset)
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(new BlackHoleChannel("\\s++"))
      .withChannel(commentRegexp("//[^\\n\\r]*+"))
      .withChannel(commentRegexp("/\\*", ANY_CHAR + "*?", "\\*/"))
      .withChannel(new CharacterLiteralsChannel())
      .withChannel(new StringLiteralsChannel())
      // C++ Standard, Section 2.14.2 "Integer literals"
      // C++ Standard, Section 2.14.4 "Floating literals"
      .withChannel(
        regexp(CxxTokenType.NUMBER,
               and(
                 or(
                   g(POINT, DECDIGIT_SEQUENCE, opt(g(EXPONENT))),
                   g(HEX_PREFIX, opt(g(HEXDIGIT_SEQUENCE)), opt(POINT), opt(g(HEXDIGIT_SEQUENCE)), opt(
                     g(BINARY_EXPONENT))),
                   g(BIN_PREFIX, BINDIGIT_SEQUENCE),
                   g(DECDIGIT_SEQUENCE, opt(POINT), opt(g(DECDIGIT_SEQUENCE)), opt(g(EXPONENT)))
                 ),
                 opt(g(UD_SUFFIX))
               )
        )
      )
      .withChannel(new KeywordChannel(and("#", o2n("\\s"), "[a-z]", o2n("\\w")), PPKeyword.values()))
      .withChannel(new IdentifierAndKeywordChannel(and("[a-zA-Z_]", o2n("\\w")), true))
      .withChannel(new PunctuatorChannel(PPPunctuator.values()))
      .withChannel(new BomCharacterChannel())
      .withChannel(new UnknownCharacterChannel());

    return builder.build();
  }

}
