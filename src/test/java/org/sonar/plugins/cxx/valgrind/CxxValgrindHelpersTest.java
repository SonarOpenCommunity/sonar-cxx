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

import org.junit.Before;
import org.junit.Test;

public class CxxValgrindHelpersTest {
  CxxValgrindSensor.ValgrindFrame frame;
  CxxValgrindSensor.ValgrindFrame equalFrame;
  CxxValgrindSensor.ValgrindFrame otherFrame;
  
  @Before
  public void setUp() {
    frame = new CxxValgrindSensor.ValgrindFrame();
    frame.fn = frame.file = "lala";
    frame.line = 111;
    
    equalFrame = new CxxValgrindSensor.ValgrindFrame();
    equalFrame.fn = equalFrame.file = "lala";
    equalFrame.line = 111;
    
    otherFrame = new CxxValgrindSensor.ValgrindFrame();
    otherFrame.fn = otherFrame.file = "haha";
    otherFrame.line = 222;
  }
  
  @Test
  public void frameEqualityShouldWorkAsExpected() {
    //reflexivity
    assert(frame.equals(frame));
    assert(otherFrame.equals(otherFrame));
    assert(equalFrame.equals(equalFrame));

    // equality
    assert(frame.equals(equalFrame));
    assert(!frame.equals(otherFrame));
    assert(frame.hashCode() == equalFrame.hashCode());
    assert(frame.hashCode() != otherFrame.hashCode());
  }
  
  @Test
  public void stackEqualityWorksAsExpected() {
    CxxValgrindSensor.ValgrindStack stack = new CxxValgrindSensor.ValgrindStack();
    stack.frames.add(frame);
    stack.frames.add(otherFrame);

    CxxValgrindSensor.ValgrindStack equalStack = new CxxValgrindSensor.ValgrindStack();
    equalStack.frames.add(equalFrame);
    equalStack.frames.add(otherFrame);
    
    CxxValgrindSensor.ValgrindStack otherStack = new CxxValgrindSensor.ValgrindStack();
    otherStack.frames.add(otherFrame);
    
    CxxValgrindSensor.ValgrindStack otherStack2 = new CxxValgrindSensor.ValgrindStack();
    otherStack2.frames.add(otherFrame);
    otherStack2.frames.add(frame);
    
    //reflexivity
    assert(stack.equals(stack));
    assert(equalStack.equals(equalStack));
    assert(otherStack.equals(otherStack));
    assert(otherStack2.equals(otherStack2));
    
    // equality
    assert(stack.equals(equalStack));
    assert(!stack.equals(otherStack));
    assert(!stack.equals(otherStack2));
    assert(stack.hashCode() == equalStack.hashCode());
    assert(stack.hashCode() != otherStack.hashCode());
    assert(stack.hashCode() != otherStack2.hashCode());
  }
}
