/*
 * C++ Community Plugin (cxx plugin)
 * Copyright (C) 2010-2022 SonarOpenCommunity
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class MapChainTest {

  private final MapChain<String, String> mc;

  public MapChainTest() {
    mc = new MapChain<>();
  }

  @Test
  public void getMapping() {
    mc.put("k", "v");
    assertEquals("v", mc.get("k"));
  }

  @Test
  public void removeMapping() {
    mc.put("k", "v");
    mc.remove("k");
    assertNull(mc.get("k"));
  }

  @Test
  public void noValueMapping() {
    assertNull(mc.get("k"));
  }

  @Test
  public void clearMapping() {
    mc.put("k", "v");
    mc.clear();
    assertNull(mc.get("k"));
  }

  @Test
  public void disable() {
    mc.put("k", "v");
    mc.disable("k");
    assertNull(mc.get("k"));
  }

  @Test
  public void enable() {
    mc.put("k", "v");
    mc.disable("k");
    mc.enable("k");
    assertEquals("v", mc.get("k"));
  }

}
