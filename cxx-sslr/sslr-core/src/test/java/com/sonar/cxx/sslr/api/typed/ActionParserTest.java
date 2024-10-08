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
package com.sonar.cxx.sslr.api.typed;

import com.sonar.cxx.sslr.api.AstNode;
import com.sonar.cxx.sslr.api.AstNodeType;
import com.sonar.cxx.sslr.api.GenericTokenType;
import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.api.Trivia.TriviaKind;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.sonar.cxx.sslr.grammar.GrammarRuleKey;
import org.sonar.cxx.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.cxx.sslr.internal.vm.PatternExpression;
import org.sonar.cxx.sslr.internal.vm.TriviaExpression;

class ActionParserTest {

  @Test
  void not_matching() throws Exception {
    var thrown = catchThrowableOfType(RecognitionException.class, () -> parse(MyGrammarKeys.NUMERIC, "x"));
    assertThat(thrown).isExactlyInstanceOf(RecognitionException.class);
  }

  @Test
  void basic() throws Exception {
    assertThat(parse(MyGrammarKeys.NUMERIC, "42", Numeric.class)).hasToString("42");
  }

  @Test
  void firstOf() throws Exception {
    assertThat(parse(MyGrammarKeys.OPERATOR, "+", Operator.class)).hasToString("+");
    assertThat(parse(MyGrammarKeys.OPERATOR, "-", Operator.class)).hasToString("-");
    assertNotParse(MyGrammarKeys.OPERATOR, "x");
  }

  @Test
  void optional() throws Exception {
    assertThat(parse(MyGrammarKeys.UNARY_EXP, "42", UnaryExp.class)).hasToString("42");
    assertThat(parse(MyGrammarKeys.UNARY_EXP, "+42", UnaryExp.class)).hasToString("+ 42");
  }

  @Test
  void oneOrMore() throws Exception {
    assertThat(parse(MyGrammarKeys.NUMERIC_LIST, "42 7", NumericList.class)).hasToString("[42, 7]");
    assertNotParse(MyGrammarKeys.NUMERIC_LIST, "");
  }

  @Test
  void zeroOrMore() throws Exception {
    assertThat(parse(MyGrammarKeys.POTENTIALLY_EMPTY_NUMERIC_LIST, "42 7", NumericList.class)).hasToString(
      "[42, 7]");
    assertThat(parse(MyGrammarKeys.POTENTIALLY_EMPTY_NUMERIC_LIST, "", NumericList.class)).hasToString("[]");
  }

  @Test
  void skipped_astnode() throws Exception {
    assertThat(parse(MyGrammarKeys.NUMERIC_WITH_EOF, "42", Numeric.class)).hasToString("42");
  }

  @Test
  void undefined_token_type() throws Exception {
    var numeric = parse(MyGrammarKeys.NUMERIC, "42", Numeric.class);
    var type = numeric.getFirstChild().getToken().getType();
    assertThat(type.hasToBeSkippedFromAst(null)).isFalse();
    assertThat(type.getValue()).isEqualTo("TOKEN");
    assertThat(type.toString()).isNotNull();
  }

  @Test
  void comment() throws Exception {
    var numeric = parse(MyGrammarKeys.NUMERIC, "/* myComment */42", Numeric.class);
    var trivia = numeric.getFirstChild().getToken().getTrivia().get(0);
    assertThat(trivia.isComment()).isTrue();
    assertThat(trivia.getToken().getOriginalValue()).isEqualTo("/* myComment */");
  }

  @Test
  void skipped_text() throws Exception {
    assertThat(parse(MyGrammarKeys.NUMERIC, "  42", Numeric.class)).hasToString("42");
  }

  @Test
  void unknown_trivia() throws Exception {
    var thrown = catchThrowableOfType(IllegalStateException.class, ()
      -> parse(MyGrammarKeys.NUMERIC, "#preprocessor 42", Numeric.class)
    );
    assertThat(thrown).hasMessage("Unexpected trivia kind: PREPROCESSOR");
  }

  @Test
  void rootRule() throws Exception {
    assertThat(parser(MyGrammarKeys.OPERATOR).rootRule()).isEqualTo(MyGrammarKeys.OPERATOR);
  }

  @Test
  void parse_file() throws Exception {
    var parser = parser(MyGrammarKeys.UNARY_EXP);
    var node = parser.parse(new File("src/test/resources/typed/42.txt"));
    assertThat(node).isInstanceOf(UnaryExp.class);
  }

  @Test
  void unknown_file() throws Exception {
    var parser = parser(MyGrammarKeys.NUMERIC);
    try {
      parser.parse(new File("unknown"));
      fail("expceted exception");
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(NoSuchFileException.class);
    }
  }

  @Test
  void more_than_one_call_to_the_same_action_method() throws Exception {
    assertThat(parse(MyGrammarKeys.NUMERIC, "42", Numeric.class)).hasToString("42");
    assertThat(parse(MyGrammarKeys.NUMERIC2, "42", Numeric.class)).hasToString("42");
  }

  @SuppressWarnings("unchecked")
  private <T extends AstNode> T parse(GrammarRuleKey ruleKey, String toParse, Class<T> expectedClass) {
    var astNode = parse(ruleKey, toParse);
    assertThat(astNode).isInstanceOf(expectedClass);
    return (T) astNode;
  }

  private AstNode parse(GrammarRuleKey ruleKey, String toParse) {
    return parser(ruleKey).parse(toParse);
  }

  private ActionParser<AstNode> parser(GrammarRuleKey ruleKey) {
    var b = LexerlessGrammarBuilder.create();
    b.rule(MyGrammarKeys.PLUS).is(b.regexp("\\+"));
    b.rule(MyGrammarKeys.MINUS).is(b.regexp("-"));
    b.rule(MyGrammarKeys.NUMERIC_TOKEN).is(
      b.optional(b.commentTrivia(b.regexp("/\\*[\\s\\S]*?\\*/"))),
      b.optional(new TriviaExpression(TriviaKind.PREPROCESSOR, new PatternExpression("\\#[a-z]+"))),
      b.optional(b.skippedTrivia(b.regexp("\\s+"))),
      b.regexp("[0-9]+"));
    b.rule(MyGrammarKeys.EOF).is(b.token(GenericTokenType.EOF, b.endOfInput())).skip();
    return new ActionParser<>(StandardCharsets.UTF_8, b, MyGrammar.class, new MyTreeFactory(), new AstNodeBuilder(),
      ruleKey);
  }

  private void assertNotParse(GrammarRuleKey ruleKey, String toParse) {
    try {
      parse(ruleKey, toParse);
      fail(ruleKey + " should not match '" + toParse + "'");
    } catch (RecognitionException e) {
      // OK
    }
  }

  private enum MyGrammarKeys implements GrammarRuleKey, AstNodeType {
    NUMERIC, NUMERIC2, NUMERIC_TOKEN,
    PLUS, MINUS, OPERATOR, UNARY_EXP,
    NUMERIC_LIST, POTENTIALLY_EMPTY_NUMERIC_LIST,
    EOF, NUMERIC_WITH_EOF
  }

  public static class MyGrammar {

    private final GrammarBuilder<AstNode> b;
    private final MyTreeFactory f;

    public MyGrammar(GrammarBuilder<AstNode> b, MyTreeFactory treeFactory) {
      this.b = b;
      this.f = treeFactory;
    }

    public Numeric NUMERIC() {
      return b.<Numeric>nonterminal(MyGrammarKeys.NUMERIC)
        .is(f.numeric(b.invokeRule(MyGrammarKeys.NUMERIC_TOKEN)));
    }

    // Includes a 2nd call to MyTreeFactory.numeric(...)
    public Numeric NUMERIC2() {
      return b.<Numeric>nonterminal(MyGrammarKeys.NUMERIC2)
        .is(f.numeric(b.invokeRule(MyGrammarKeys.NUMERIC_TOKEN)));
    }

    public Operator OPERATOR() {
      return b.<Operator>nonterminal(MyGrammarKeys.OPERATOR)
        .is(
          f.operator(
            b.firstOf(
              b.token(MyGrammarKeys.PLUS),
              b.token(MyGrammarKeys.MINUS))));
    }

    public UnaryExp UNARY_EXP() {
      return b.<UnaryExp>nonterminal(MyGrammarKeys.UNARY_EXP)
        .is(
          f.unaryExp(
            b.optional(b.token(MyGrammarKeys.PLUS)),
            NUMERIC()));
    }

    public AstNode NUMERIC_LIST() {
      return b.<AstNode>nonterminal(MyGrammarKeys.NUMERIC_LIST)
        .is(
          f.numericList(b.oneOrMore(NUMERIC())));
    }

    public AstNode POTENTIALLY_EMPTY_EXP_LIST() {
      return b.<AstNode>nonterminal(MyGrammarKeys.POTENTIALLY_EMPTY_NUMERIC_LIST)
        .is(
          f.numericList(b.zeroOrMore(NUMERIC())));
    }

    public AstNode NUMERIC_WITH_EOF() {
      return b.<Numeric>nonterminal(MyGrammarKeys.NUMERIC_WITH_EOF)
        .is(
          f.numeric(
            NUMERIC(),
            b.invokeRule(MyGrammarKeys.EOF)));
    }

  }

  public static class MyTree extends AstNode {

    public MyTree(AstNodeType type) {
      super(type, type.toString(), null);
    }

  }

  public static class Numeric extends MyTree {

    private final AstNode node;

    public Numeric(AstNode node) {
      super(MyGrammarKeys.NUMERIC);
      this.node = node;
      addChild(node);
    }

    @Override
    public String toString() {
      return node.getTokenValue();
    }

  }

  public static class Operator extends MyTree {

    private final String value;

    public Operator(AstNode node) {
      super(MyGrammarKeys.OPERATOR);
      this.value = node.getTokenValue();
    }

    @Override
    public String toString() {
      return value;
    }

  }

  public static class UnaryExp extends MyTree {

    private final Numeric operand;
    private final Optional<AstNode> plus;

    public UnaryExp(Optional<AstNode> plus, Numeric operand) {
      super(MyGrammarKeys.UNARY_EXP);
      this.plus = plus;
      this.operand = operand;
    }

    @Override
    public String toString() {
      return (plus.isPresent() ? "+ " : "") + operand;
    }

  }

  public static class NumericList extends MyTree {

    private final List<Numeric> list;

    public NumericList(List<Numeric> list) {
      super(MyGrammarKeys.NUMERIC_LIST);
      this.list = list;
    }

    @Override
    public String toString() {
      return list.toString();
    }

  }

  public static class MyTreeFactory {

    public UnaryExp unaryExp(Optional<AstNode> plus, Numeric numeric) {
      return new UnaryExp(plus, numeric);
    }

    public AstNode numericList(List<Numeric> list) {
      return new NumericList(list);
    }

    public AstNode numericList(Optional<List<Numeric>> list) {
      return new NumericList(list.isPresent() ? list.get() : Collections.emptyList());
    }

    public Operator operator(AstNode node) {
      return new Operator(node);
    }

    public Numeric numeric(AstNode node) {
      return new Numeric(node);
    }

    public Numeric numeric(Numeric numeric, AstNode eof) {
      return numeric;
    }

  }

}
