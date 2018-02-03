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
package org.sonar.cxx.preprocessor;

//@todo: deprecated, see http://javadocs.sonarsource.org/4.5.2/apidocs/deprecated-list.html
import com.sonar.sslr.api.Preprocessor;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.ANY_CHAR;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import org.sonar.cxx.CxxConfiguration;
import org.sonar.cxx.channels.PreprocessorChannel;

public final class IncludeLexer {

  private IncludeLexer() {
  }

  public static Lexer create(Preprocessor... preprocessors) { //@todo deprecated Preprocessor
    return create(new CxxConfiguration(), preprocessors);
  }

  public static Lexer create(CxxConfiguration conf, Preprocessor... preprocessors) { //@todo deprecated Preprocessor
    Lexer.Builder builder = Lexer.builder()
      .withCharset(conf.getCharset())
      .withFailIfNoChannelToConsumeOneCharacter(true)
      .withChannel(new BlackHoleChannel("\\s"))
      .withChannel(new PreprocessorChannel())
      .withChannel(commentRegexp("/\\*", ANY_CHAR + "*?", "\\*/"))
      .withChannel(new BlackHoleChannel(".*"));

    for (Preprocessor preprocessor : preprocessors) { //@todo deprecated Preprocessor
      builder.withPreprocessor(preprocessor);
    }

    return builder.build();
  }
}
