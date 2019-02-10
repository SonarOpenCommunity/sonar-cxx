/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2019 SonarOpenCommunity
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
import org.junit.Test;

public class MapChainTest {

  MapChain<String, String> mc;

  public MapChainTest() {
    mc = new MapChain<>();
  }

  @Test
  public void gettingHighPrioMapping() {
    mc.setHighPrio(true);
    mc.put("k", "v");
    assertEquals("v", mc.get("k"));
  }

  @Test
  public void gettingLowPrioMapping() {
    mc.setHighPrio(false);
    mc.put("k", "v");
    assertEquals("v", mc.get("k"));
  }

  @Test
  public void removeLowPrioMapping() {
    mc.setHighPrio(false);
    mc.put("k", "v");
    mc.removeLowPrio("k");
    assertEquals(null, mc.get("k"));
  }

  @Test
  public void gettingNotExistingMapping() {
    mc.setHighPrio(false);
    mc.put("k", "vlow");
    mc.setHighPrio(true);
    mc.put("k", "vhigh");
    assertEquals("vhigh", mc.get("k"));
  }

  @Test
  public void gettingOverwrittenMapping() {
    assertEquals(null, mc.get("k"));
  }

  @Test
  public void clearingLowPrioDeletesLowPrioMappings() {
    mc.setHighPrio(false);
    mc.put("k", "v");
    mc.clearLowPrio();
    assertEquals(null, mc.get("k"));
  }

  @Test
  public void clearingLowPrioDoesntAffectHighPrioMappings() {
    mc.setHighPrio(true);
    mc.put("k", "v");
    mc.clearLowPrio();
    assertEquals("v", mc.get("k"));
  }

  @Test
  public void disable() {
    mc.setHighPrio(true);
    mc.put("khigh", "vhigh");
    mc.setHighPrio(false);
    mc.put("klow", "vlow");

    mc.disable("khigh");
    mc.disable("klow");

    assertEquals(null, mc.get("khigh"));
    assertEquals(null, mc.get("klow"));
  }

  @Test
  public void enable() {
    mc.setHighPrio(true);
    mc.put("khigh", "vhigh");
    mc.setHighPrio(false);
    mc.put("klow", "vlow");
    mc.disable("khigh");
    mc.disable("klow");

    mc.enable("khigh");
    mc.enable("klow");

    assertEquals("vhigh", mc.get("khigh"));
    assertEquals("vlow", mc.get("klow"));
  }

}
