/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2024 SonarOpenCommunity
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
package org.sonar.cxx.preprocessor;

import java.io.IOException;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MacroContainerTest {

  private MacroContainer<String, String> mc;

  @BeforeEach
  void setUp() {
    mc = new MacroContainer<>();
  }

  @Test
  void getMapping() {
    mc.put("k", "v");
    assertThat(mc.get("k")).isEqualTo("v");
  }

  @Test
  void removeMapping() {
    mc.put("k", "v");
    mc.remove("k");
    assertThat(mc.get("k")).isNull();
  }

  @Test
  void noValueMapping() {
    assertThat(mc.get("k")).isNull();
  }

  @Test
  void clearMapping() {
    mc.put("k", "v");
    mc.clear();
    assertThat(mc.get("k")).isNull();
  }

  @Test
  void disable() {
    mc.put("k", "v");
    mc.pushDisable("k");
    assertThat(mc.get("k")).isNull();
  }

  @Test
  void enable() {
    mc.put("k", "v");
    mc.pushDisable("k");
    mc.popDisable();
    assertThat(mc.get("k")).isEqualTo("v");
  }

  @Test
  void persistentStorage(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
    Path fileName = tempDir.resolve("container.test");

    mc.put("key1", "value1");
    mc.put("key2", "value3");
    mc.pushDisable("key2");

    mc.writeToFile(fileName.toString());
    mc.readFromFile(fileName.toString());

    assertThat(mc.get("key1")).isEqualTo("value1");
    assertThat(mc.get("key2")).isNull();
  }

}
