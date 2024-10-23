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
package org.sonar.cxx.sslr.internal.vm; // cxx: in use

import com.sonar.cxx.sslr.api.RecognitionException;
import com.sonar.cxx.sslr.api.Token;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.internal.matchers.ImmutableInputBuffer;
import org.sonar.cxx.sslr.internal.matchers.InputBuffer;
import org.sonar.cxx.sslr.internal.matchers.Matcher;
import org.sonar.cxx.sslr.internal.matchers.ParseNode;
import org.sonar.cxx.sslr.internal.vm.lexerful.LexerfulParseErrorFormatter;
import org.sonar.cxx.sslr.parser.ParseError;
import org.sonar.cxx.sslr.parser.ParsingResult;

public class Machine implements CharSequence {

  private final char[] input;
  private final Token[] tokens;
  private final int inputLength;

  private MachineStack stack;
  private int index;
  private int address;
  private boolean matched = true;

  private final ParseNode[] memos;

  // Number of instructions in grammar for Java is about 2000.
  private final int[] calls;

  private final MachineHandler handler;

  private boolean ignoreErrors = false;

  private static final MachineHandler NOP_HANDLER = (Machine machine) -> {
    // nop
  };

  public Machine(String input, Instruction[] instructions, MachineHandler handler) {
    this(input.toCharArray(), null, instructions, handler);
  }

  private Machine(@Nullable char[] input, @Nullable Token[] tokens, Instruction[] instructions,
    MachineHandler handler) {
    this.input = input;
    this.tokens = tokens;
    if (input != null) {
      this.inputLength = input.length;
    } else if (tokens != null) {
      this.inputLength = tokens.length;
    } else {
      this.inputLength = 0;
    }

    this.handler = handler;
    this.memos = new ParseNode[inputLength + 1];
    this.stack = new MachineStack();
    stack = stack.getOrCreateChild();
    stack.setIndex(-1);
    calls = new int[instructions.length];
    Arrays.fill(calls, -1);
  }

  // @VisibleForTesting
  public Machine(String input, Instruction[] instructions) {
    this(input, instructions, NOP_HANDLER);
  }

  public static ParseNode parse(List<Token> tokens, CompiledGrammar grammar) {
    var inputTokens = tokens.toArray(Token[]::new);

    var errorLocatingHandler = new ErrorLocatingHandler();
    var machine = new Machine(null, inputTokens, grammar.getInstructions(), errorLocatingHandler);
    machine.execute(grammar.getMatcher(grammar.getRootRuleKey()),
      grammar.getRootRuleOffset(), grammar.getInstructions());

    if (machine.matched) {
      return machine.stack.subNodes().get(0);
    } else {
      if (tokens.isEmpty()) {
        // Godin: weird situation - I expect that list of tokens contains at least EOF,
        // but this is not the case in C Parser
        throw new RecognitionException(1, "No tokens");
      } else {
        int errorIndex = errorLocatingHandler.getErrorIndex();
        var errorMsg = new LexerfulParseErrorFormatter().format(tokens, errorIndex);
        int errorLine = errorIndex < tokens.size() ? tokens.get(errorIndex).getLine() : tokens.get(tokens.size() - 1)
          .getLine();
        throw new RecognitionException(errorLine, errorMsg);
      }
    }
  }

  public static ParsingResult parse(char[] input, CompiledGrammar grammar) {
    var instructions = grammar.getInstructions();

    var errorLocatingHandler = new ErrorLocatingHandler();
    var machine = new Machine(input, null, instructions, errorLocatingHandler);
    machine.execute(grammar.getMatcher(grammar.getRootRuleKey()), grammar.getRootRuleOffset(), instructions);

    if (machine.matched) {
      return new ParsingResult(
        new ImmutableInputBuffer(machine.input),
        machine.matched,
        // TODO what if there is no nodes, or more than one?
        machine.stack.subNodes().get(0),
        null);
    } else {
      InputBuffer inputBuffer = new ImmutableInputBuffer(machine.input);
      var parseError = new ParseError(inputBuffer, errorLocatingHandler.getErrorIndex());
      return new ParsingResult(inputBuffer, machine.matched, null, parseError);
    }
  }

  private void execute(Matcher matcher, int offset, Instruction[] instructions) {
    // Place first rule on top of stack
    push(-1);
    stack.setMatcher(matcher);
    jump(offset);

    execute(instructions);
  }

  // @VisibleForTesting
  public static boolean execute(String input, Instruction[] instructions) {
    var machine = new Machine(input, instructions);
    while (machine.address != -1 && machine.address < instructions.length) {
      instructions[machine.address].execute(machine);
    }
    return machine.matched;
  }

  // @VisibleForTesting
  public static boolean execute(Instruction[] instructions, Token... input) {
    var machine = new Machine(null, input, instructions, NOP_HANDLER);
    while (machine.address != -1 && machine.address < instructions.length) {
      instructions[machine.address].execute(machine);
    }
    return machine.matched;
  }

  private void execute(Instruction[] instructions) {
    while (address != -1) {
      instructions[address].execute(this);
    }
  }

  public int getAddress() {
    return address;
  }

  public void setAddress(int address) {
    this.address = address;
  }

  public void jump(int offset) {
    address += offset;
  }

  private void push(int address) {
    stack = stack.getOrCreateChild();
    stack.subNodes().clear();
    stack.setAddress(address);
    stack.setIndex(index);
    stack.setIgnoreErrors(ignoreErrors);
  }

  public void popReturn() {
    calls[stack.calledAddress()] = stack.leftRecursion();
    stack = stack.parent();
  }

  public void pushReturn(int returnOffset, Matcher matcher, int callOffset) {
    var memo = memos[index];
    if (memo != null && memo.getMatcher() == matcher) {
      stack.subNodes().add(memo);
      index = memo.getEndIndex();
      address += returnOffset;
    } else {
      push(address + returnOffset);
      stack.setMatcher(matcher);
      address += callOffset;

      if (calls[address] == index) {
        // TODO better message, e.g. dump stack
        throw new GrammarException("Left recursion has been detected, involved rule: " + matcher.toString());
      }
      stack.setCalledAddress(address);
      stack.setLeftRecursion(calls[address]);
      calls[address] = index;
    }
  }

  public void pushBacktrack(int offset) {
    push(address + offset);
    stack.setMatcher(null);
  }

  public void pop() {
    stack = stack.parent();
  }

  public MachineStack peek() {
    return stack;
  }

  public void setIgnoreErrors(boolean ignoreErrors) {
    this.ignoreErrors = ignoreErrors;
  }

  public void backtrack() {
    // pop any return addresses from the top of the stack
    while (stack.isReturn()) {

      // TODO we must have this inside of loop, otherwise report won't be generated in case of
      // input "foo" and rule "nextNot(foo)"
      ignoreErrors = stack.isIgnoreErrors();
      if (!ignoreErrors) {
        handler.onBacktrack(this);
      }

      popReturn();
    }

    if (stack.isEmpty()) {
      // input does not match
      address = -1;
      matched = false;
    } else {
      // restore state
      index = stack.index();
      address = stack.address();
      ignoreErrors = stack.isIgnoreErrors();
      stack = stack.parent();
    }
  }

  public void createNode() {
    var node = new ParseNode(stack.index(), index, stack.subNodes(), stack.matcher());
    stack.parent().subNodes().add(node);
    if (stack.matcher() instanceof MemoParsingExpression && ((MemoParsingExpression) stack.matcher()).shouldMemoize()) {
      memos[stack.index()] = node;
    }
  }

  public void createLeafNode(Matcher matcher, int offset) {
    var node = new ParseNode(index, index + offset, matcher);
    stack.subNodes().add(node);
    index += offset;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void advanceIndex(int offset) {
    index += offset;
  }

  @Override
  public int length() {
    return inputLength - index;
  }

  @Override
  public char charAt(int offset) {
    return input[index + offset];
  }

  /**
   * Not supported.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public CharSequence subSequence(int start, int end) {
    throw new UnsupportedOperationException();
  }

  public Token tokenAt(int offset) {
    return tokens[index + offset];
  }

}
