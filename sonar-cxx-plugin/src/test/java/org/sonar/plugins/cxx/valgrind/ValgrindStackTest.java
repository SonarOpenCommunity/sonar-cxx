/*
 * Sonar Cxx Plugin, open source software quality management tool.
 * Copyright (C) 2010 - 2011, Neticoa SAS France - Tous droits reserves.
 * Author(s) : Franck Bonin, Neticoa SAS France.
 *
 * Sonar Cxx Plugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar Cxx Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar Cxx Plugin; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.cxx.valgrind;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class ValgrindStackTest {
  ValgrindStack stack;
  ValgrindStack equalStack;
  ValgrindStack otherStack;

  @Before
  public void setUp() {
    ValgrindFrame frame = new ValgrindFrame("", "", "lala", "", "lala", 111);
    ValgrindFrame equalFrame = new ValgrindFrame("", "", "lala", "", "lala", 111);
    ValgrindFrame otherFrame = new ValgrindFrame("", "", "haha", "", "haha", 111);

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
    assert(!stack.equals(null));
  }

  @Test
  public void stackDoesntEqualsMiscObject() {
    assert(!stack.equals("string"));
  }

  @Test
  public void stackEqualityIsReflexive() {
    assert(stack.equals(stack));
    assert(otherStack.equals(otherStack));
    assert(equalStack.equals(equalStack));
  }

  @Test
  public void stackEqualityWorksAsExpected() {
    assert(stack.equals(equalStack));
    assert(!stack.equals(otherStack));
  }

  @Test
  public void stackHashWorksAsExpected() {
    assert(stack.hashCode() == equalStack.hashCode());
    assert(stack.hashCode() != otherStack.hashCode());
  }

  @Test
  public void stringRepresentationShouldResembleValgrindsStandard() {
    Map<String, ValgrindStack> ioMap = new HashMap<String, ValgrindStack>();

    ValgrindFrame frame = new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null,  "main.cc", 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(frame);

    ioMap.put("", new ValgrindStack());
    ioMap.put(frame.toString()+"\n", stack);

    for(Map.Entry<String, ValgrindStack> entry: ioMap.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().toString());
    }
  }

  @Test
  public void getLastOwnFrame_returnsNullOnEmptyStack() {
    assertEquals(new ValgrindStack().getLastOwnFrame("somepath"), null);
  }

  @Test
  public void getLastOwnFrame_returnsNullIfNoOwnFrameThere() {
    ValgrindFrame frame = new ValgrindFrame(null, null, null, null, null, 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(frame);

    assertEquals(new ValgrindStack().getLastOwnFrame("somepath"), null);
  }

  @Test
  public void getLastOwnFrame_returnsTheOwnFrame() {
    File BASE_DIR = new File("our", "path");
    File OWN_PATH = new File(BASE_DIR, "subdir");

    ValgrindFrame otherFrame = new ValgrindFrame(null, null, null, "someotherpath",  null, 1);
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, null, OWN_PATH.getPath(),  null, 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);

    assertEquals(stack.getLastOwnFrame(BASE_DIR.getPath()), ownFrame);
  }
}
