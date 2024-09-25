/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2024 SonarOpenCommunity
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
/**
 * fork of SonarSource Language Recognizer: https://github.com/SonarSource/sslr
 * Copyright (C) 2010-2021 SonarSource SA / mailto:info AT sonarsource DOT com / license: LGPL v3
 */
package org.sonar.cxx.sslr.parser; // cxx: in use

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.api.Token;
import com.sonar.cxx.sslr.impl.Parser;
import com.sonar.cxx.sslr.impl.matcher.RuleDefinition;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.sonar.cxx.sslr.internal.matchers.AstCreator;
import org.sonar.cxx.sslr.internal.matchers.LocatedText;

/**
 * Adapts {@link ParseRunner} to be used as {@link Parser}.
 *
 * <p>
 * This class is not intended to be subclassed by clients.</p>
 *
 * @since 1.16
 */
public class ParserAdapter<G extends LexerlessGrammar> extends Parser<G> {

  private final Charset charset;
  private final ParseRunner parseRunner;

  public ParserAdapter(@Nonnull Charset charset, @Nonnull G grammar) {
    super(Objects.requireNonNull(grammar, "grammar"));
    this.charset = Objects.requireNonNull(charset, "charset");
    this.parseRunner = new ParseRunner(grammar.getRootRule());
  }

  /**
   * @return constructed AST
   * @throws RecognitionException if unable to parse
   */
  @Override
  public AstNode parse(String source) {
    // LocatedText is used in order to be able to retrieve TextLocation
    var text = new LocatedText(null, source.toCharArray());
    return parse(text);
  }

  /**
   * @return constructed AST
   * @throws RecognitionException if unable to parse
   */
  @Override
  public AstNode parse(File file) {
    var text = new LocatedText(file, fileToCharArray(file, charset));
    return parse(text);
  }

  private static char[] fileToCharArray(File file, Charset charset) {
    try {
      return new String(Files.readAllBytes(Path.of(file.getPath())), charset).toCharArray();
    } catch (IOException e) {
      throw new RecognitionException(0, e.getMessage(), e);
    }
  }

  private AstNode parse(LocatedText input) {
    var chars = input.toChars();
    var result = parseRunner.parse(chars);
    if (result.isMatched()) {
      return AstCreator.create(result, input);
    } else {
      var parseError = result.getParseError();
      var inputBuffer = parseError.getInputBuffer();
      int line = inputBuffer.getPosition(parseError.getErrorIndex()).getLine();
      var message = new ParseErrorFormatter().format(parseError);
      throw new RecognitionException(line, message);
    }
  }

  @Override
  public AstNode parse(List<Token> tokens) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RuleDefinition getRootRule() {
    throw new UnsupportedOperationException();
  }

}
