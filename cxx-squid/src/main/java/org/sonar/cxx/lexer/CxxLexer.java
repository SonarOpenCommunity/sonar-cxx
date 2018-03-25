/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2018 SonarOpenCommunity
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
package org.sonar.cxx.lexer;

//@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.sslr.impl.channel.PunctuatorChannel;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.ANY_CHAR;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.and;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.o2n;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.opt;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;
import com.sonar.sslr.impl.channel.UnknownCharacterChannel;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.api.CxxKeyword;
import org.sonar.cxx.api.CxxPunctuator;
import org.sonar.cxx.api.CxxTokenType;
import org.sonar.cxx.channels.CharacterLiteralsChannel;
import org.sonar.cxx.channels.PreprocessorChannel;
import org.sonar.cxx.channels.StringLiteralsChannel;

public final class CxxLexer {

  private static final String HEX_PREFIX = "0[xX]";
  private static final String EXPONENT = "([Ee][+-]?+[0-9_]([']?+[0-9_]++)*+)";
  private static final String BINARY_EXPONENT = "([pP][+-]?+[0-9]([']?+[0-9]++)*+)"; // since C++17
  //private static final String INTEGER_SUFFIX = "(((U|u)(i64|LL|ll|L|l)?)|((i64|LL|ll|L|l)(u|U)?))";  
  //private static final String FLOAT_SUFFIX = "(f|l|F|L)";
  // ud-suffix: identifier (including INTEGER_SUFFIX, FLOAT_SUFFIX)
  private static final String UD_SUFFIX = "([_a-zA-Z]([_a-zA-Z0-9]*+))";
  private static final String HEXDIGIT_SEQUENCE = "([0-9a-fA-F]([']?+[0-9a-fA-F]++)*+)";

  private CxxLexer() {
  }

  public static Lexer create(Preprocessor... preprocessors) { //@todo deprecated Preprocessor
    return create(new CxxConfiguration(), preprocessors);
  }

  public static Lexer create(CxxConfiguration conf, Preprocessor... preprocessors) { //@todo deprecated Preprocessor

    //
    // changes here must be always aligned: CxxLexer.java <=> CppLexer.java
    //
    Lexer.Builder builder = Lexer.builder()
      .withCharset(conf.getCharset())
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(new BlackHoleChannel("\\s"))
      // C++ Standard, Section 2.8 "Comments"
      .withChannel(commentRegexp("//[^\\n\\r]*+"))
      .withChannel(commentRegexp("/\\*", ANY_CHAR + "*?", "\\*/"))
      // backslash at the end of the line: just throw away
      .withChannel(new BackslashChannel())
      // Preprocessor directives
      .withChannel(new PreprocessorChannel())
      // C++ Standard, Section 2.14.3 "Character literals"
      .withChannel(new CharacterLiteralsChannel())
      // C++ Standard, Section 2.14.5 "String literals"
      .withChannel(new StringLiteralsChannel())
      // C++ Standard, Section 2.14.4 "Floating literals"
      .withChannel(regexp(CxxTokenType.NUMBER, "[0-9]([']?+[0-9]++)*+\\.([0-9]([']?+[0-9]++)*+)*+"
        + opt(EXPONENT) + opt(UD_SUFFIX)))
      .withChannel(regexp(CxxTokenType.NUMBER, "\\.[0-9]([']?+[0-9]++)*+"
        + opt(EXPONENT) + opt(UD_SUFFIX)))
      .withChannel(regexp(CxxTokenType.NUMBER, "[0-9]([']?+[0-9]++)*+"
        + EXPONENT + opt(UD_SUFFIX)))
      .withChannel(regexp(CxxTokenType.NUMBER, HEX_PREFIX + HEXDIGIT_SEQUENCE
        + BINARY_EXPONENT + opt(UD_SUFFIX))) // since C++17
      .withChannel(regexp(CxxTokenType.NUMBER, HEX_PREFIX + HEXDIGIT_SEQUENCE + "."
        + BINARY_EXPONENT + opt(UD_SUFFIX))) // since C++17
      .withChannel(regexp(CxxTokenType.NUMBER, HEX_PREFIX + opt(HEXDIGIT_SEQUENCE) + "." + HEXDIGIT_SEQUENCE
        + BINARY_EXPONENT + opt(UD_SUFFIX))) // since C++17
      // C++ Standard, Section 2.14.2 "Integer literals"
      .withChannel(regexp(CxxTokenType.NUMBER, "[1-9]([']?+[0-9]++)*+" + opt(UD_SUFFIX))) // Decimal literals
      .withChannel(regexp(CxxTokenType.NUMBER, "0[bB][01]([']?+[01]++)*+" + opt(UD_SUFFIX))) // Binary Literals      
      .withChannel(regexp(CxxTokenType.NUMBER, "0([']?+[0-7]++)++" + opt(UD_SUFFIX))) // Octal Literals
      .withChannel(regexp(CxxTokenType.NUMBER, HEX_PREFIX + HEXDIGIT_SEQUENCE + opt(UD_SUFFIX))) // Hex Literals
      .withChannel(regexp(CxxTokenType.NUMBER, "0" + opt(UD_SUFFIX))) // Decimal zero

      // C++ Standard, Section 2.14.7 "Pointer literals"
      .withChannel(regexp(CxxTokenType.NUMBER, CxxKeyword.NULLPTR.getValue() + "\\b"))
      // C++ Standard, Section 2.12 "Keywords"
      // C++ Standard, Section 2.11 "Identifiers"
      .withChannel(new IdentifierAndKeywordChannel(and("[a-zA-Z_]", o2n("\\w")), true, CxxKeyword.values()))
      // C++ Standard, Section 2.13 "Operators and punctuators"
      .withChannel(new PunctuatorChannel(CxxPunctuator.values()))
      .withChannel(new UnknownCharacterChannel());

    for (Preprocessor preprocessor : preprocessors) { //@todo deprecated Preprocessor
      builder.withPreprocessor(preprocessor);
    }

    return builder.build();
  }
}
