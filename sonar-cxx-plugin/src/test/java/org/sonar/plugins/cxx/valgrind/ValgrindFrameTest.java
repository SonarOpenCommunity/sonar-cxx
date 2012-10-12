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

import java.util.Map;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class ValgrindFrameTest {
  ValgrindFrame frame;
  ValgrindFrame equalFrame;
  ValgrindFrame otherFrame;

  @Before
  public void setUp() {
    frame = new ValgrindFrame("", "", "lala", "", "lala", 111);
    equalFrame = new ValgrindFrame("", "", "lala", "", "lala", 111);
    otherFrame = new ValgrindFrame("", "", "haha", "", "haha", 111);
  }

  @Test
  public void frameDoesntEqualsNull() {
    assert(!frame.equals(null));
  }

  @Test
  public void frameDoesntEqualsMiscObject() {
    assert(!frame.equals("string"));
  }

  @Test
  public void frameEqualityIsReflexive() {
    assert(frame.equals(frame));
    assert(otherFrame.equals(otherFrame));
    assert(equalFrame.equals(equalFrame));
  }

  @Test
  public void frameEqualityWorksAsExpected() {
    assert(frame.equals(equalFrame));
    assert(!frame.equals(otherFrame));
  }

  @Test
  public void frameHashWorksAsExpected() {
    assert(frame.hashCode() == equalFrame.hashCode());
    assert(frame.hashCode() != otherFrame.hashCode());
  }

  @Test
  public void stringRepresentationShouldResembleValgrindsStandard() {
    Map<String, ValgrindFrame> ioMap = new HashMap<String, ValgrindFrame>();

    ioMap.put("0xDEADBEAF: main() (main.cc:1)",
              new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", "src", "main.cc", 1));
    ioMap.put("0xDEADBEAF: main() (main.cc:1)",
              new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null,  "main.cc", 1));
    ioMap.put("0xDEADBEAF: main() (main.cc)",
              new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null,  "main.cc", -1));
    ioMap.put("0xDEADBEAF: ??? (main.cc:1)",
              new ValgrindFrame("0xDEADBEAF", "libX.so", null,     "src", "main.cc", 1));
    ioMap.put("0xDEADBEAF: ??? (in libX.so)",
              new ValgrindFrame("0xDEADBEAF", "libX.so", null,     "src", null,      1));
    ioMap.put("0xDEADBEAF: ???",
              new ValgrindFrame("0xDEADBEAF", null,      null,     null,  null,      -1));
    ioMap.put("???: ???",
              new ValgrindFrame(null,         null,      null,     null,  null,      -1));

    for(Map.Entry<String, ValgrindFrame> entry: ioMap.entrySet()) {
      assertEquals(entry.getKey(), entry.getValue().toString());
    }
  }
}
