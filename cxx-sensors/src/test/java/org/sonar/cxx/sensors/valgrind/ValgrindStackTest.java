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
package org.sonar.cxx.sensors.valgrind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;

public class ValgrindStackTest {

  private final ValgrindStack stack = new ValgrindStack();
  private final ValgrindStack equalStack = new ValgrindStack();
  private final ValgrindStack otherStack = new ValgrindStack();

  @Before
  public void setUp() {
    ValgrindFrame frame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    ValgrindFrame equalFrame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    ValgrindFrame otherFrame = new ValgrindFrame("", "", "haha", "", "haha", "111");

    stack.addFrame(frame);
    stack.addFrame(otherFrame);

    equalStack.addFrame(equalFrame);
    equalStack.addFrame(otherFrame);

    otherStack.addFrame(otherFrame);
    otherStack.addFrame(frame);
  }

  @Test
  public void stackDoesntEqualsNull() {
    assertThat(stack).isNotNull();
  }

  @Test
  public void stackDoesntEqualsMiscObject() {
    assertThat(stack).isNotEqualTo("string");
  }

  @Test
  public void stackEqualityIsReflexive() {
    assertThat(stack).isEqualTo(stack);
    assertThat(otherStack).isEqualTo(otherStack);
    assertThat(equalStack).isEqualTo(equalStack);
  }

  @Test
  public void stackEqualityWorksAsExpected() {
    assertThat(stack).isEqualTo(equalStack);
    assertThat(stack).isNotEqualTo(otherStack);
  }

  @Test
  public void stackHashWorksAsExpected() {
    assertThat(stack.hashCode() == equalStack.hashCode()).isTrue();
    assertThat(stack.hashCode() != otherStack.hashCode()).isTrue();
  }

  @Test
  public void stringRepresentationShouldResembleValgrindsStandard() {
    final ValgrindFrame frame0 = new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null, "main.cc", "1");
    final ValgrindStack stack0 = new ValgrindStack();
    stack0.addFrame(frame0);

    final ValgrindFrame frame1 = new ValgrindFrame("0xBADC0FFE", "libc.so", "abort()", null, "main.cc", "2");
    final ValgrindStack stack1 = new ValgrindStack();
    stack1.addFrame(frame1);
    stack1.addFrame(frame0);

    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(new ValgrindStack().toString()).isEqualTo("");
    softly.assertThat(stack0.toString()).isEqualTo(frame0.toString());
    softly.assertThat(stack1.toString()).isEqualTo(frame1.toString() + "\n" + frame0.toString());
    softly.assertAll();
  }

  @Test
  public void getLastOwnFrame_returnsNullOnEmptyStack() {
    assertEquals(null, new ValgrindStack().getLastOwnFrame("somepath"));
  }

  @Test
  public void getLastOwnFrame_returnsNullIfNoOwnFrameThere() {
    ValgrindFrame frame = new ValgrindFrame(null, null, null, null, null, "1");
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(frame);

    assertEquals(null, new ValgrindStack().getLastOwnFrame("somepath"));
  }

  @Test
  public void getLastOwnFrame_returnsTheOwnFrame1() {
    File BASE_DIR = new File("our", "path");
    File OWN_PATH = new File(BASE_DIR, "subdir");

    ValgrindFrame otherFrame = new ValgrindFrame(null, null, null, "someotherpath", null, "1");
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, null, OWN_PATH.getPath(), null, "1");
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);

    assertEquals(stack.getLastOwnFrame(BASE_DIR.getPath()), ownFrame);
  }

  @Test
  public void getLastOwnFrame_returnsTheOwnFrame2() {
    File BASE_DIR = new File("our/path/.");
    File OWN_PATH = new File("our/../our/./path/subdir");

    ValgrindFrame otherFrame = new ValgrindFrame(null, null, null, "someotherpath", null, "1");
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, null, OWN_PATH.getPath(), null, "1");
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);

    assertEquals(stack.getLastOwnFrame(BASE_DIR.getPath()), ownFrame);
  }

}
