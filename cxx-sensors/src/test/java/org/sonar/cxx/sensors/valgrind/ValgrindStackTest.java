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
package org.sonar.cxx.sensors.valgrind;

import java.io.File;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValgrindStackTest {

  private final ValgrindStack stack = new ValgrindStack();
  private final ValgrindStack equalStack = new ValgrindStack();
  private final ValgrindStack otherStack = new ValgrindStack();

  @BeforeEach
  public void setUp() {
    var frame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    var equalFrame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    var otherFrame = new ValgrindFrame("", "", "haha", "", "haha", "111");

    stack.addFrame(frame);
    stack.addFrame(otherFrame);

    equalStack.addFrame(equalFrame);
    equalStack.addFrame(otherFrame);

    otherStack.addFrame(otherFrame);
    otherStack.addFrame(frame);
  }

  @Test
  void stackDoesntEqualsNull() {
    assertThat(stack).isNotNull();
  }

  @Test
  void stackDoesntEqualsMiscObject() {
    assertThat(stack).isNotEqualTo("string");
  }

  @Test
  void stackEqualityIsReflexive() {
    assertThat(stack).isEqualTo(stack);
    assertThat(otherStack).isEqualTo(otherStack);
    assertThat(equalStack).isEqualTo(equalStack);
  }

  @Test
  void stackEqualityWorksAsExpected() {
    assertThat(stack)
      .isEqualTo(equalStack)
      .isNotEqualTo(otherStack);
  }

  @Test
  void stackHashWorksAsExpected() {
    assertThat(stack).hasSameHashCodeAs(equalStack);
    assertThat(stack.hashCode()).isNotEqualTo(otherStack.hashCode());
  }

  @Test
  void stringRepresentationShouldResembleValgrindsStandard() {
    var frame0 = new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null, "main.cc", "1");
    var stack0 = new ValgrindStack();
    stack0.addFrame(frame0);

    var frame1 = new ValgrindFrame("0xBADC0FFE", "libc.so", "abort()", null, "main.cc", "2");
    var stack1 = new ValgrindStack();
    stack1.addFrame(frame1);
    stack1.addFrame(frame0);

    var softly = new SoftAssertions();
    softly.assertThat(new ValgrindStack().toString()).isEmpty();
    softly.assertThat(stack0).hasToString(frame0.toString());
    softly.assertThat(stack1).hasToString(frame1.toString() + "\n" + frame0.toString());
    softly.assertAll();
  }

  @Test
  void getLastOwnFrameReturnsNullOnEmptyStack() {
    assertThat(new ValgrindStack().getLastOwnFrame("somepath")).isNull();
  }

  @Test
  void getLastOwnFrameReturnsNullIfNoOwnFrameThere() {
    var frame = new ValgrindFrame(null, null, null, null, null, "1");
    var valgrindStack = new ValgrindStack();
    valgrindStack.addFrame(frame);

    assertThat(new ValgrindStack().getLastOwnFrame("somepath")).isNull();
  }

  @Test
  void getLastOwnFrameReturnsTheOwnFrame1() {
    final var baseDir = new File("our", "path");
    final var ownPath = new File(baseDir, "subdir");

    var otherFrame = new ValgrindFrame(null, null, null, "someotherpath", null, "1");
    var ownFrame = new ValgrindFrame(null, null, null, ownPath.getPath(), null, "1");
    var valgrindStack = new ValgrindStack();
    valgrindStack.addFrame(otherFrame);
    valgrindStack.addFrame(ownFrame);

    assertThat(ownFrame).isEqualTo(valgrindStack.getLastOwnFrame(baseDir.getPath()));
  }

  @Test
  void getLastOwnFrameReturnsTheOwnFrame2() {
    final var baseDir = new File("our/path/.");
    final var ownPath = new File("our/../our/./path/subdir");

    var otherFrame = new ValgrindFrame(null, null, null, "someotherpath", null, "1");
    var ownFrame = new ValgrindFrame(null, null, null, ownPath.getPath(), null, "1");
    var valgrindStack = new ValgrindStack();
    valgrindStack.addFrame(otherFrame);
    valgrindStack.addFrame(ownFrame);

    assertThat(ownFrame).isEqualTo(valgrindStack.getLastOwnFrame(baseDir.getPath()));
  }

}
