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
    assertEquals(mc.get("k"), "v");
  }

  @Test
  public void gettingLowPrioMapping() {
    mc.setHighPrio(false);
    mc.put("k", "v");
    assertEquals(mc.get("k"), "v");
  }

  @Test
  public void removeLowPrioMapping() {
    mc.setHighPrio(false);
    mc.put("k", "v");
    mc.removeLowPrio("k");
    assertEquals(mc.get("k"), null);
  }

  @Test
  public void gettingNotExistingMapping() {
    mc.setHighPrio(false);
    mc.put("k", "vlow");
    mc.setHighPrio(true);
    mc.put("k", "vhigh");
    assertEquals(mc.get("k"), "vhigh");
  }

  @Test
  public void gettingOverwrittenMapping() {
    assertEquals(mc.get("k"), null);
  }

  @Test
  public void clearingLowPrioDeletesLowPrioMappings() {
    mc.setHighPrio(false);
    mc.put("k", "v");
    mc.clearLowPrio();
    assertEquals(mc.get("k"), null);
  }

  @Test
  public void clearingLowPrioDoesntAffectHighPrioMappings() {
    mc.setHighPrio(true);
    mc.put("k", "v");
    mc.clearLowPrio();
    assertEquals(mc.get("k"), "v");
  }

  @Test
  public void disable() {
    mc.setHighPrio(true);
    mc.put("khigh", "vhigh");
    mc.setHighPrio(false);
    mc.put("klow", "vlow");

    mc.disable("khigh");
    mc.disable("klow");

    assertEquals(mc.get("khigh"), null);
    assertEquals(mc.get("klow"), null);
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

    assertEquals(mc.get("khigh"), "vhigh");
    assertEquals(mc.get("klow"), "vlow");
  }
}
