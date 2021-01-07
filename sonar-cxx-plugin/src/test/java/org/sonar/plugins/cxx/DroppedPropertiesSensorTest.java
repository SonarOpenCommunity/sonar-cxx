/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2021 SonarOpenCommunity
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

import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

public class DroppedPropertiesSensorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @Rule
  public LogTester logTester = new LogTester();

  @Test
  public void testNoMsg() throws Exception {
    SensorContextTester contextTester = SensorContextTester.create(tmp.newFolder());
    MapSettings mapSettings = new MapSettings().setProperty("sonar.cxx.xxx", "value");
    contextTester.setSettings(mapSettings);
    List<String> analysisWarnings = new ArrayList<>();
    DroppedPropertiesSensor sensor = new DroppedPropertiesSensor(analysisWarnings::add);
    sensor.execute(contextTester);

    assertThat(logTester.logs(LoggerLevel.WARN)).isEmpty();
    assertThat(analysisWarnings).isEmpty();
  }

  @Test
  public void testNoLongerSupported() throws Exception {
    SensorContextTester contextTester = SensorContextTester.create(tmp.newFolder());
    MapSettings mapSettings = new MapSettings().setProperty("sonar.cxx.cppncss.reportPaths", "value");
    contextTester.setSettings(mapSettings);
    List<String> analysisWarnings = new ArrayList<>();
    DroppedPropertiesSensor sensor = new DroppedPropertiesSensor(analysisWarnings::add);
    sensor.execute(contextTester);

    String msg = "CXX property 'sonar.cxx.cppncss.reportPaths' is no longer supported.";
    assertThat(logTester.logs(LoggerLevel.WARN)).contains(msg);
    assertThat(analysisWarnings).containsExactly(msg);
  }

  @Test
  public void testNoLongerSupportedWithInfo() throws Exception {
    SensorContextTester contextTester = SensorContextTester.create(tmp.newFolder());
    MapSettings mapSettings = new MapSettings().setProperty("sonar.cxx.suffixes.sources", "value");
    contextTester.setSettings(mapSettings);
    List<String> analysisWarnings = new ArrayList<>();
    DroppedPropertiesSensor sensor = new DroppedPropertiesSensor(analysisWarnings::add);
    sensor.execute(contextTester);

    String msg = "CXX property 'sonar.cxx.suffixes.sources' is no longer supported."
                   + " Use key 'sonar.cxx.file.suffixes' instead.";
    assertThat(logTester.logs(LoggerLevel.WARN)).contains(msg);
    assertThat(analysisWarnings).containsExactly(msg);
  }

}
