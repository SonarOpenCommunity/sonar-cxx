/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2016 SonarOpenCommunity
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
package org.sonar.plugins.cxx.valgrind;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ValgrindStackTest {

  ValgrindStack stack;
  ValgrindStack equalStack;
  ValgrindStack otherStack;

  @Before
  public void setUp() {
    ValgrindFrame frame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    ValgrindFrame equalFrame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    ValgrindFrame otherFrame = new ValgrindFrame("", "", "haha", "", "haha", "111");

    stack = new ValgrindStack();
    stack.addFrame(frame);
    stack.addFrame(otherFrame);

    equalStack = new ValgrindStack();
    equalStack.addFrame(equalFrame);
    equalStack.addFrame(otherFrame);

    otherStack = new ValgrindStack();
    otherStack.addFrame(otherFrame);
    otherStack.addFrame(frame);
  }

  @Test
  public void stackDoesntEqualsNull() {
    assert (!stack.equals(null));
  }

  @Test
  public void stackDoesntEqualsMiscObject() {
    assert (!stack.equals("string"));
  }

  @Test
  public void stackEqualityIsReflexive() {
    assert (stack.equals(stack));
    assert (otherStack.equals(otherStack));
    assert (equalStack.equals(equalStack));
  }

  @Test
  public void stackEqualityWorksAsExpected() {
    assert (stack.equals(equalStack));
    assert (!stack.equals(otherStack));
  }

  @Test
  public void stackHashWorksAsExpected() {
    assert (stack.hashCode() == equalStack.hashCode());
    assert (stack.hashCode() != otherStack.hashCode());
  }

  @Test
  public void stringRepresentationShouldResembleValgrindsStandard() {
    Map<String, ValgrindStack> ioMap = new HashMap<String, ValgrindStack>();

    ValgrindFrame frame = new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null, "main.cc", "1");
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(frame);

    ioMap.put("", new ValgrindStack());
    ioMap.put(frame.toString() + "\n", stack);

    for (Map.Entry<String, ValgrindStack> entry : ioMap.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().toString());
    }
  }

  @Test
  public void getLastOwnFrame_returnsNullOnEmptyStack() {
    assertEquals(new ValgrindStack().getLastOwnFrame("somepath"), null);
  }

  @Test
  public void getLastOwnFrame_returnsNullIfNoOwnFrameThere() {
    ValgrindFrame frame = new ValgrindFrame(null, null, null, null, null, "1");
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(frame);

    assertEquals(new ValgrindStack().getLastOwnFrame("somepath"), null);
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
