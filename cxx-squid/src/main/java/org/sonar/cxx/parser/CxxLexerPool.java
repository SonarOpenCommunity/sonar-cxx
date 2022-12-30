/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2023 SonarOpenCommunity
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
package org.sonar.cxx.parser;

import com.sonar.cxx.sslr.api.Preprocessor;
import com.sonar.cxx.sslr.impl.Lexer;
import com.sonar.cxx.sslr.impl.channel.BlackHoleChannel;
import com.sonar.cxx.sslr.impl.channel.BomCharacterChannel;
import com.sonar.cxx.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.cxx.sslr.impl.channel.PunctuatorChannel;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.ANY_CHAR;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.and;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.g;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.o2n;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.opt;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.or;
import static com.sonar.cxx.sslr.impl.channel.RegexpChannelBuilder.regexp;
import com.sonar.cxx.sslr.impl.channel.UnknownCharacterChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import org.sonar.cxx.channels.BackslashChannel;
import org.sonar.cxx.channels.CharacterLiteralsChannel;
import org.sonar.cxx.channels.PreprocessorChannel;
import org.sonar.cxx.channels.RightAngleBracketsChannel;
import org.sonar.cxx.channels.StringLiteralsChannel;
import org.sonar.cxx.preprocessor.PPSpecialIdentifier;

public final class CxxLexerPool {

  private static final String HEX_PREFIX = "0[xX]";
  private static final String BIN_PREFIX = "0[bB]";
  private static final String EXPONENT = "[Ee][+-]?+[0-9_]([']?+[0-9_]++)*+";
  private static final String BINARY_EXPONENT = "[pP][+-]?+\\d([']?+\\d++)*+"; // since C++17
  //private static final String INTEGER_SUFFIX = "(((U|u)(i64|LL|ll|L|l)?)|((i64|LL|ll|L|l)(u|U)?))";
  //private static final String FLOAT_SUFFIX = "(f|l|F|L)";
  // ud-suffix: identifier (including INTEGER_SUFFIX, FLOAT_SUFFIX)
  private static final String UD_SUFFIX = "[_a-zA-Z]\\w*+";
  private static final String DECDIGIT_SEQUENCE = "\\d([']?+\\d++)*+";
  private static final String HEXDIGIT_SEQUENCE = "[0-9a-fA-F]([']?+[0-9a-fA-F]++)*+";
  private static final String BINDIGIT_SEQUENCE = "[01]([']?+[01]++)*+";
  private static final String POINT = "\\.";

  private Lexer.Builder builder;
  private final Set<Lexer> available = new HashSet<>();
  private final Set<Lexer> inUse = new HashSet<>();

  private CxxLexerPool() {
  }

  public static CxxLexerPool create(Preprocessor... preprocessors) {
    return create(Charset.defaultCharset(), preprocessors);
  }

  public static CxxLexerPool create(Charset charset, Preprocessor... preprocessors) {
    var lexer = new CxxLexerPool();

    //
    // changes here must be always aligned: CxxLexerPool.java <=> CppLexer.java
    //
    lexer.builder = Lexer.builder()
      .withCharset(charset)
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(new BlackHoleChannel("\\s++"))
      // C++ Standard, Section 2.8 "Comments"
      .withChannel(commentRegexp("//[^\\n\\r]*+"))
      .withChannel(commentRegexp("/\\*", ANY_CHAR + "*?", "\\*/"))
      // backslash at the end of the line: just throw away
      .withChannel(new BackslashChannel())
      // detects preprocessor directives:
      // This channel detects source code lines which should be handled by the preprocessor.
      // If a line is not marked CxxTokenType.PREPROCESSOR it is not handled by CppLexer and CppGrammar.
      .withChannel(new PreprocessorChannel(PPSpecialIdentifier.values()))
      // C++ Standard, Section 2.14.3 "Character literals"
      .withChannel(new CharacterLiteralsChannel())
      // C++ Standard, Section 2.14.5 "String literals"
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
      // C++ Standard, Section 2.14.7 "Pointer literals"
      .withChannel(regexp(CxxTokenType.NUMBER, CxxKeyword.NULLPTR.getValue() + "\\b"))
      // C++ Standard, Section 2.12 "Keywords"
      // C++ Standard, Section 2.11 "Identifiers"
      .withChannel(new IdentifierAndKeywordChannel(and("[a-zA-Z_]", o2n("\\w")), true, CxxKeyword.values()))
      // C++ Standard, Section 2.13 "Operators and punctuators"
      .withChannel(new RightAngleBracketsChannel())
      .withChannel(new PunctuatorChannel(CxxPunctuator.values()))
      .withChannel(new BomCharacterChannel())
      .withChannel(new UnknownCharacterChannel());

    for (var preprocessor : preprocessors) {
      lexer.builder.withPreprocessor(preprocessor);
    }

    return lexer;
  }

  public Lexer getLexer() {
    return builder.build();
  }

  public Lexer borrowLexer() {
    if (available.isEmpty()) {
      available.add(getLexer());
    }
    var instance = available.iterator().next();
    available.remove(instance);
    inUse.add(instance);
    return instance;
  }

  public void returnLexer(Lexer instance) {
    inUse.remove(instance);
    available.add(instance);
  }

}
