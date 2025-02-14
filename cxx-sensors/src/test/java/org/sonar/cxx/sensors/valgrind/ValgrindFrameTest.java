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

import java.util.HashMap;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValgrindFrameTest {

  private ValgrindFrame frame;
  private ValgrindFrame equalFrame;
  private ValgrindFrame otherFrame;

  @BeforeEach
  public void setUp() {
    frame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    equalFrame = new ValgrindFrame("", "", "lala", "", "lala", "111");
    otherFrame = new ValgrindFrame("", "", "haha", "", "haha", "111");
  }

  @Test
  void frameDoesntEqualsNull() {
    assertThat(frame).isNotNull();
  }

  @Test
  void frameDoesntEqualsMiscObject() {
    assertThat(frame).isNotEqualTo("string");
  }

  @Test
  void frameEqualityIsReflexive() {
    assertThat(frame).isEqualTo(frame);
    assertThat(otherFrame).isEqualTo(otherFrame);
    assertThat(equalFrame).isEqualTo(equalFrame);
  }

  @Test
  void frameEqualityWorksAsExpected() {
    assertThat(frame)
      .isEqualTo(equalFrame)
      .isNotEqualTo(otherFrame);
  }

  @Test
  void frameHashWorksAsExpected() {
    assertThat(frame).hasSameHashCodeAs(equalFrame);
    assertThat(frame.hashCode()).isNotEqualTo(otherFrame.hashCode());
  }

  @Test
  void stringRepresentationShouldResembleValgrindsStandard() {
    var ioMap = new HashMap<String, ValgrindFrame>();

    ioMap.put("0xDEADBEAF: main() (main.cc:1)",
      new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", "src", "main.cc", "1"));
    ioMap.put("0xDEADBEAF: main() (main.cc:1)",
      new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null, "main.cc", "1"));
    ioMap.put("0xDEADBEAF: main() (main.cc)",
      new ValgrindFrame("0xDEADBEAF", "libX.so", "main()", null, "main.cc", ""));
    ioMap.put("0xDEADBEAF: ??? (main.cc:1)",
      new ValgrindFrame("0xDEADBEAF", "libX.so", null, "src", "main.cc", "1"));
    ioMap.put("0xDEADBEAF: ??? (in libX.so)",
      new ValgrindFrame("0xDEADBEAF", "libX.so", null, "src", null, "1"));
    ioMap.put("0xDEADBEAF: ???",
      new ValgrindFrame("0xDEADBEAF", null, null, null, null, ""));
    ioMap.put("???: ???",
      new ValgrindFrame(null, null, null, null, null, ""));

    for (var entry : ioMap.entrySet()) {
      assertThat(entry.getValue()).hasToString(entry.getKey());
    }
  }

}
