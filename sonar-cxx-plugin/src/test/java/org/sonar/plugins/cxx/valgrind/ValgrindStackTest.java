/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010 Neticoa SAS France
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cxx.valgrind;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

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

    ValgrindFrame frame = new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null, "main.cc", 1);
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
    assertEquals(new ValgrindStack().getLastOwnFrame(new File("/asdf"), null).size(), 0);
  }

  @Test
  public void getLastOwnFrame_returnsNullIfNoOwnFrameThere() {
    ValgrindFrame frame = new ValgrindFrame(null, null, null, null, null, 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(frame);

    assertEquals(new ValgrindStack().getLastOwnFrame(new File("/asdf"), null).size(), 0);
  }

  @Test
  public void getLastOwnFrame_returnsTheOwnFrame() {
    ValgrindFrame otherFrame = new ValgrindFrame(null, null, "function2", "someotherpath", "file2", 1);
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, "function1", "somepath", "file1", 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);
    
    Map<String, List<String>> lookupTable = new TreeMap<String, List<String>>();
    List<String> fileLocations = new ArrayList<String>();
    fileLocations.add("somepath/file1");
    lookupTable.put("function1", fileLocations);

    List<ValgrindFrame> frames = stack.getLastOwnFrame(new File("/asdf"), lookupTable);
    assertEquals(1, frames.size());
    assertEquals("somepath", frames.get(0).getDir());
    assertEquals("file1", frames.get(0).getFile());
    assertEquals("function1", frames.get(0).getFunction());
  }
  
  @Test
  public void getLastOwnFrame_returnsCorrectFrameWithDuplicatedFunctionsInFrame() {
    ValgrindFrame otherFrame = new ValgrindFrame(null, null, "function2", "/abc/fdg", "file.cpp", 1);
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, "function2", "/src/dir1", "file1.cpp", 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);
    
    Map<String, List<String>> lookupTable = new TreeMap<String, List<String>>();
    List<String> fileLocations = new ArrayList<String>();
    fileLocations.add("dir1/file1.cpp");
    lookupTable.put("function2", fileLocations);

    List<ValgrindFrame> frames = stack.getLastOwnFrame(new File("/asdf"), lookupTable);
    assertEquals(1, frames.size());
    assertEquals("dir1", frames.get(0).getDir());
    assertEquals("file1.cpp", frames.get(0).getFile());
    assertEquals("function2", frames.get(0).getFunction());   
  }
  
  @Test
  public void getLastOwnFrame_returnsCorrectFrameWithDuplicatedFunctionsInLookupTable() {
    ValgrindFrame otherFrame = new ValgrindFrame(null, null, "function2", "/abc/fdg", "file2", 1);
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, "function1", "/src/dir1", "file1", 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);
    
    Map<String, List<String>> lookupTable = new TreeMap<String, List<String>>();
    List<String> fileLocations = new ArrayList<String>();
    fileLocations.add("dir1/file1");
    fileLocations.add("dir2/file2");
    lookupTable.put("function1", fileLocations);

    List<ValgrindFrame> frames = stack.getLastOwnFrame(new File("/asdf"), lookupTable);
    assertEquals(frames.get(0).getDir(), "dir1");
    assertEquals(frames.get(0).getFile(), "file1"); 
  }  
  
  @Test
  public void getLastOwnFrame_returnsCorrectFrameWithAbsoluteWindowsPathsInFrames() {
    ValgrindFrame otherFrame = new ValgrindFrame(null, null, "function2", "e:\\abc\\fdg", "file2", 1);
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, "function1", "e:\\src\\dir1", "file1", 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);
    
    Map<String, List<String>> lookupTable = new TreeMap<String, List<String>>();
    List<String> fileLocations = new ArrayList<String>();
    fileLocations.add("dir1/file1");
    fileLocations.add("dir2/file2");
    lookupTable.put("function1", fileLocations);

    List<ValgrindFrame> frames = stack.getLastOwnFrame(new File("/asdf"), lookupTable);
    assertEquals(frames.get(0).getDir(), "dir1");
    assertEquals(frames.get(0).getFile(), "file1"); 
  }
  
  @Test
  public void getLastOwnFrame_returnsCorrectFrameWithRelativeWindowsPathsInFrames() {
    ValgrindFrame otherFrame = new ValgrindFrame(null, null, "function2", "abc\\fdg", "file2", 1);
    ValgrindFrame ownFrame = new ValgrindFrame(null, null, "function1", "src\\dir1", "file1", 1);
    ValgrindStack stack = new ValgrindStack();
    stack.addFrame(otherFrame);
    stack.addFrame(ownFrame);
    
    Map<String, List<String>> lookupTable = new TreeMap<String, List<String>>();
    List<String> fileLocations = new ArrayList<String>();
    fileLocations.add("dir1\\file1");
    fileLocations.add("dir2\\file2");
    lookupTable.put("function1", fileLocations);

    List<ValgrindFrame> frames = stack.getLastOwnFrame(new File("/asdf"), lookupTable);
    assertEquals(frames.get(0).getDir(), "dir1");
    assertEquals(frames.get(0).getFile(), "file1"); 
  }    
}
