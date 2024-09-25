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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.sonar.cxx.sslr.grammar.GrammarException;
import org.sonar.cxx.sslr.internal.matchers.Matcher;

public abstract class Instruction {

  private static final Instruction RET = new RetInstruction();
  private static final Instruction BACKTRACK = new BacktrackInstruction();
  private static final Instruction END = new EndInstruction();
  private static final Instruction FAIL_TWICE = new FailTwiceInstruction();
  private static final Instruction IGNORE_ERRORS = new IgnoreErrorsInstruction();

  public static void addAll(List<Instruction> list, Instruction[] array) {
    list.addAll(Arrays.asList(array));
  }

  public static Instruction jump(int offset) {
    return new JumpInstruction(offset);
  }

  public static Instruction call(int offset, Matcher matcher) {
    return new CallInstruction(offset, matcher);
  }

  public static Instruction ret() {
    return RET;
  }

  public static Instruction backtrack() {
    return BACKTRACK;
  }

  public static Instruction end() {
    return END;
  }

  public static Instruction choice(int offset) {
    return new ChoiceInstruction(offset);
  }

  public static Instruction predicateChoice(int offset) {
    return new PredicateChoiceInstruction(offset);
  }

  public static Instruction commit(int offset) {
    return new CommitInstruction(offset);
  }

  public static Instruction commitVerify(int offset) {
    return new CommitVerifyInstruction(offset);
  }

  public static Instruction failTwice() {
    return FAIL_TWICE;
  }

  public static Instruction backCommit(int offset) {
    return new BackCommitInstruction(offset);
  }

  public static Instruction ignoreErrors() {
    return IGNORE_ERRORS;
  }

  /**
   * Executes this instruction.
   */
  public abstract void execute(Machine machine);

  public static final class JumpInstruction extends Instruction {

    private final int offset;

    public JumpInstruction(int offset) {
      this.offset = offset;
    }

    @Override
    public void execute(Machine machine) {
      machine.jump(offset);
    }

    @Override
    public String toString() {
      return "Jump " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return (getClass() == obj.getClass()) && (this.offset == ((JumpInstruction) obj).offset);
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

  public static final class CallInstruction extends Instruction {

    private final int offset;
    private final Matcher matcher;

    public CallInstruction(int offset, Matcher matcher) {
      this.offset = offset;
      this.matcher = matcher;
    }

    @Override
    public void execute(Machine machine) {
      machine.pushReturn(1, matcher, offset);
    }

    @Override
    public String toString() {
      return "Call " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() == obj.getClass()) {
        var other = (CallInstruction) obj;
        return this.offset == other.offset
                 && Objects.equals(this.matcher, other.matcher);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

  public static final class ChoiceInstruction extends Instruction {

    private final int offset;

    public ChoiceInstruction(int offset) {
      this.offset = offset;
    }

    @Override
    public void execute(Machine machine) {
      machine.pushBacktrack(offset);
      machine.jump(1);
    }

    @Override
    public String toString() {
      return "Choice " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return (getClass() == obj.getClass()) && (this.offset == ((ChoiceInstruction) obj).offset);
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

  public static final class IgnoreErrorsInstruction extends Instruction {

    @Override
    public void execute(Machine machine) {
      machine.setIgnoreErrors(true);
      machine.jump(1);
    }

    @Override
    public String toString() {
      return "IgnoreErrors";
    }
  }

  /**
   * Instruction dedicated for predicates.
   * Behaves exactly as {@link ChoiceInstruction}, but disables error reports.
   */
  public static final class PredicateChoiceInstruction extends Instruction {

    private final int offset;

    public PredicateChoiceInstruction(int offset) {
      this.offset = offset;
    }

    @Override
    public void execute(Machine machine) {
      machine.pushBacktrack(offset);
      machine.setIgnoreErrors(true);
      machine.jump(1);
    }

    @Override
    public String toString() {
      return "PredicateChoice " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return (getClass() == obj.getClass()) && (this.offset == ((PredicateChoiceInstruction) obj).offset);
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

  public static final class CommitInstruction extends Instruction {

    private final int offset;

    public CommitInstruction(int offset) {
      this.offset = offset;
    }

    @Override
    public void execute(Machine machine) {
      // add all nodes to parent
      machine.peek().parent().subNodes().addAll(machine.peek().subNodes());

      machine.pop();
      machine.jump(offset);
    }

    @Override
    public String toString() {
      return "Commit " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return (getClass() == obj.getClass()) && (this.offset == ((CommitInstruction) obj).offset);
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

  public static final class CommitVerifyInstruction extends Instruction {

    private final int offset;

    public CommitVerifyInstruction(int offset) {
      this.offset = offset;
    }

    @Override
    public void execute(Machine machine) {
      if (machine.getIndex() == machine.peek().index()) {
        // TODO better message, e.g. dump stack
        throw new GrammarException("The inner part of ZeroOrMore and OneOrMore must not allow empty matches");
      }
      // add all nodes to parent
      machine.peek().parent().subNodes().addAll(machine.peek().subNodes());

      machine.pop();
      machine.jump(offset);
    }

    @Override
    public String toString() {
      return "CommitVerify " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return (getClass() == obj.getClass()) && (this.offset == ((CommitVerifyInstruction) obj).offset);
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

  public static final class RetInstruction extends Instruction {

    @Override
    public void execute(Machine machine) {
      machine.createNode();
      var stack = machine.peek();
      machine.setIgnoreErrors(stack.isIgnoreErrors());
      machine.setAddress(stack.address());
      machine.popReturn();
    }

    @Override
    public String toString() {
      return "Ret";
    }
  }

  public static final class BacktrackInstruction extends Instruction {

    @Override
    public void execute(Machine machine) {
      machine.backtrack();
    }

    @Override
    public String toString() {
      return "Backtrack";
    }
  }

  public static final class EndInstruction extends Instruction {

    @Override
    public void execute(Machine machine) {
      machine.setAddress(-1);
    }

    @Override
    public String toString() {
      return "End";
    }
  }

  public static final class FailTwiceInstruction extends Instruction {

    @Override
    public void execute(Machine machine) {
      // restore state of machine to correctly report error during backtrack
      // note that there is no need restore value of "IgnoreErrors", because this will be done during backtrack
      machine.setIndex(machine.peek().index());

      // remove pending alternative pushed by Choice instruction
      machine.pop();
      machine.backtrack();
    }

    @Override
    public String toString() {
      return "FailTwice";
    }
  }

  public static final class BackCommitInstruction extends Instruction {

    private final int offset;

    public BackCommitInstruction(int offset) {
      this.offset = offset;
    }

    @Override
    public void execute(Machine machine) {
      var stack = machine.peek();
      machine.setIndex(stack.index());
      machine.setIgnoreErrors(stack.isIgnoreErrors());
      machine.pop();
      machine.jump(offset);
    }

    @Override
    public String toString() {
      return "BackCommit " + offset;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      return (getClass() == obj.getClass()) && (this.offset == ((BackCommitInstruction) obj).offset);
    }

    @Override
    public int hashCode() {
      return offset;
    }
  }

}
