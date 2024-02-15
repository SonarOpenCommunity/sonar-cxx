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
package org.sonar.plugins.cxx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.slf4j.event.Level;

class DroppedPropertiesSensorTest {

  @TempDir
  File tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void testNoMsg() throws Exception {
    var contextTester = SensorContextTester.create(tempDir);
    var mapSettings = new MapSettings().setProperty("sonar.cxx.xxx", "value");
    contextTester.setSettings(mapSettings);
    List<String> analysisWarnings = new ArrayList<>();
    var sensor = new DroppedPropertiesSensor(analysisWarnings::add);
    sensor.execute(contextTester);

    assertThat(logTester.logs(Level.WARN)).isEmpty();
    assertThat(analysisWarnings).isEmpty();
  }

  @Test
  void testNoLongerSupported() throws Exception {
    var contextTester = SensorContextTester.create(tempDir);
    var mapSettings = new MapSettings().setProperty("sonar.cxx.cppncss.reportPaths", "value");
    contextTester.setSettings(mapSettings);
    List<String> analysisWarnings = new ArrayList<>();
    var sensor = new DroppedPropertiesSensor(analysisWarnings::add);
    sensor.execute(contextTester);

    var msg = "CXX property 'sonar.cxx.cppncss.reportPaths' is no longer supported.";
    assertThat(logTester.logs(Level.WARN)).contains(msg);
    assertThat(analysisWarnings).containsExactly(msg);
  }

  @Test
  void testNoLongerSupportedWithInfo() throws Exception {
    var contextTester = SensorContextTester.create(tempDir);
    var mapSettings = new MapSettings().setProperty("sonar.cxx.suffixes.sources", "value");
    contextTester.setSettings(mapSettings);
    List<String> analysisWarnings = new ArrayList<>();
    var sensor = new DroppedPropertiesSensor(analysisWarnings::add);
    sensor.execute(contextTester);

    var msg = "CXX property 'sonar.cxx.suffixes.sources' is no longer supported."
            + " Use key 'sonar.cxx.file.suffixes' instead.";
    assertThat(logTester.logs(Level.WARN)).contains(msg);
    assertThat(analysisWarnings).containsExactly(msg);
  }

}
