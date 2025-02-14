/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2022-2025 SonarOpenCommunity
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
package com.sonar.cxx.sslr.api.typed;

import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.impl.typed.GrammarBuilderInterceptor;
import com.sonar.cxx.sslr.impl.typed.Interceptor;
import com.sonar.cxx.sslr.impl.typed.MethodInterceptor;
import com.sonar.cxx.sslr.impl.typed.ReflectionUtils;
import com.sonar.cxx.sslr.impl.typed.SyntaxTreeCreator;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.cxx.sslr.parser.ParseErrorFormatter;
import org.sonar.cxx.sslr.parser.ParseRunner;

/**
 * Parser using a lexer rules and a grammar.
 *
 * @param <N> type of Abstract Syntax Tree nodes
 *
 * @since 1.21
 */
public class ActionParser<N> {

  private final Charset charset;

  private final SyntaxTreeCreator<N> syntaxTreeCreator;
  private final GrammarRuleKey rootRule;
  private final ParseRunner parseRunner;

  /**
   * Create a new parser.
   *
   * @param charset charset to use for parsing
   * @param b rules for lexer
   * @param grammarClass grammar definition
   * @param treeFactory tree factory to use
   * @param nodeBuilder node builder to use
   * @param rootRule define root rule in grammar
   */
  public ActionParser(Charset charset, LexerlessGrammarBuilder b, Class grammarClass, Object treeFactory,
    NodeBuilder nodeBuilder, GrammarRuleKey rootRule) {
    this.charset = charset;

    var grammarBuilderInterceptor = new GrammarBuilderInterceptor(b);
    var treeFactoryInterceptor = Interceptor.create(
      treeFactory.getClass(),
      new Class[]{},
      new Object[]{},
      new ActionMethodInterceptor(grammarBuilderInterceptor)
    );
    var grammar = Interceptor.create(
      grammarClass,
      new Class[]{GrammarBuilder.class, treeFactory.getClass()},
      new Object[]{grammarBuilderInterceptor, treeFactoryInterceptor},
      grammarBuilderInterceptor
    );

    for (var method : grammarClass.getMethods()) {
      if (method.getDeclaringClass().equals(Object.class)) {
        continue;
      }

      ReflectionUtils.invokeMethod(method, grammar);
    }

    this.syntaxTreeCreator = new SyntaxTreeCreator<>(treeFactory, grammarBuilderInterceptor, nodeBuilder);

    b.setRootRule(rootRule);
    this.rootRule = rootRule;
    this.parseRunner = new ParseRunner(b.build().getRootRule());
  }

  /**
   * Parse transferred file.
   *
   * @param file file to parse
   * @return resulting Abstract Syntax Tree
   */
  public N parse(File file) {
    try {
      var chars = new String(Files.readAllBytes(Path.of(file.getPath())), charset).toCharArray();
      return parse(new Input(chars, file.toURI()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse transferred string.
   *
   * @param source string to parse
   * @return resulting Abstract Syntax Tree
   */
  public N parse(String source) {
    return parse(new Input(source.toCharArray()));
  }

  private N parse(Input input) {
    var result = parseRunner.parse(input.input());

    if (!result.isMatched()) {
      var parseError = result.getParseError();
      var inputBuffer = parseError.getInputBuffer();
      var line = inputBuffer.getPosition(parseError.getErrorIndex()).getLine();
      var message = new ParseErrorFormatter().format(parseError);
      throw new RecognitionException(line, message);
    }

    return syntaxTreeCreator.create(result.getParseTreeRoot(), input);
  }

  /**
   * Get root rule from the assigned grammar.
   *
   * @return root rule from the grammar
   */
  public GrammarRuleKey rootRule() {
    return rootRule;
  }

  private static class ActionMethodInterceptor implements MethodInterceptor {

    private final GrammarBuilderInterceptor grammarBuilderInterceptor;

    public ActionMethodInterceptor(GrammarBuilderInterceptor grammarBuilderInterceptor) {
      this.grammarBuilderInterceptor = grammarBuilderInterceptor;
    }

    @Override
    public boolean intercept(Method method) {
      grammarBuilderInterceptor.addAction(method, method.getParameterCount());
      return true;
    }

  }

}
